package com.labnex.app.helpers.attachments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import com.labnex.app.R;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 * @author mmarif
 */
public class AttachmentManager {

	private final Context context;
	private final List<AttachmentsModel> attachments = new ArrayList<>();
	private AttachmentListener listener;
	private ActivityResultLauncher<Intent> filePickerLauncher;
	private int maxFileCount = -1;
	private long maxFileSizeBytes = -1;

	public interface AttachmentListener {
		void onAttachmentsChanged(int count);

		void onAttachmentAdded(Uri uri);

		void onAttachmentRemoved(int position);

		void onAttachmentRejected(String reason);
	}

	public AttachmentManager(Context context) {
		this.context = context;
	}

	public void setMaxFileCount(int maxFileCount) {
		this.maxFileCount = maxFileCount;
	}

	public void setMaxFileSize(long maxFileSizeBytes) {
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public void setListener(AttachmentListener listener) {
		this.listener = listener;
	}

	public void registerFilePicker(ActivityResultLauncher<Intent> launcher) {
		this.filePickerLauncher = launcher;
	}

	public void openFilePicker() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		filePickerLauncher.launch(intent);
	}

	public void handleFilePickerResult(Uri uri) {
		if (uri != null) {
			try {
				context.getContentResolver()
						.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
			} catch (Exception ignored) {
			}
			addAttachment(uri);
		}
	}

	private void addAttachment(Uri uri) {
		String fileName = AttachmentUtils.queryName(context, uri);
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();

		if (maxFileCount > 0 && attachments.size() >= maxFileCount) {
			if (listener != null) {
				listener.onAttachmentRejected(
						context.getString(R.string.attachment_limit_count, maxFileCount));
			}
			return;
		}

		long fileSize = AttachmentUtils.getFileSize(context, uri);

		if (maxFileSizeBytes > 0 && fileSize > maxFileSizeBytes) {
			if (listener != null) {
				String sizeStr = AttachmentUtils.formatFileSize(maxFileSizeBytes);
				listener.onAttachmentRejected(
						context.getString(R.string.attachment_limit_size, sizeStr));
			}
			return;
		}

		AttachmentsModel attachment = new AttachmentsModel(fileName, uri);
		attachment.setFileSize(fileSize);

		attachments.add(attachment);

		if (listener != null) {
			listener.onAttachmentsChanged(attachments.size());
			listener.onAttachmentAdded(uri);
		}
	}

	public void removeAttachment(int position) {
		if (position >= 0 && position < attachments.size()) {
			attachments.remove(position);

			if (listener != null) {
				listener.onAttachmentsChanged(attachments.size());
				listener.onAttachmentRemoved(position);
			}
		}
	}

	public List<Uri> getPendingUris() {
		List<Uri> uris = new ArrayList<>();
		for (AttachmentsModel a : attachments) {
			uris.add(a.getUri());
		}
		return uris;
	}

	public int getAttachmentCount() {
		return attachments.size();
	}

	public void clear() {
		attachments.clear();
	}
}

package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.databinding.ListFilesBinding;
import com.labnex.app.databinding.ListFilesBreadcrumbBinding;
import com.labnex.app.helpers.FileIcon;
import com.labnex.app.models.repository.Tree;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int VIEW_TYPE_BREADCRUMB = 0;
	private static final int VIEW_TYPE_FILE = 1;

	private final Context context;
	private List<Tree> fileList;
	private final FilesAdapterListener listener;
	private String currentPath = "";

	public interface FilesAdapterListener {
		void onClickFile(Tree tree);

		void onBreadcrumbClick(String path);

		void onFileMenuClick(Tree tree);
	}

	public FilesAdapter(Context context, List<Tree> fileList, FilesAdapterListener listener) {
		this.context = context;
		this.fileList = new ArrayList<>(fileList);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Tree> newList) {
		this.fileList = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setCurrentPath(String path) {
		this.currentPath = path != null ? path : "";
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 && !currentPath.isEmpty()) {
			return VIEW_TYPE_BREADCRUMB;
		}
		return VIEW_TYPE_FILE;
	}

	@Override
	public int getItemCount() {
		int count = fileList.size();
		if (!currentPath.isEmpty()) {
			count++;
		}
		return count;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_BREADCRUMB) {
			ListFilesBreadcrumbBinding binding =
					ListFilesBreadcrumbBinding.inflate(LayoutInflater.from(context), parent, false);
			return new BreadcrumbViewHolder(binding);
		} else {
			ListFilesBinding binding =
					ListFilesBinding.inflate(LayoutInflater.from(context), parent, false);
			return new FilesViewHolder(binding);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof BreadcrumbViewHolder) {
			((BreadcrumbViewHolder) holder).bind(currentPath);
			((BreadcrumbViewHolder) holder)
					.binding
					.getRoot()
					.updateAppearance(position, getItemCount());
		} else if (holder instanceof FilesViewHolder) {
			int filePosition = currentPath.isEmpty() ? position : position - 1;
			((FilesViewHolder) holder).bind(fileList.get(filePosition));
			((FilesViewHolder) holder).binding.getRoot().updateAppearance(position, getItemCount());
		}
	}

	public class BreadcrumbViewHolder extends RecyclerView.ViewHolder {

		ListFilesBreadcrumbBinding binding;

		BreadcrumbViewHolder(ListFilesBreadcrumbBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(String path) {
			binding.breadcrumbContainer.removeAllViews();

			addBreadcrumbItem("/", "", true);

			if (!path.isEmpty()) {
				String[] segments = path.split("/");
				StringBuilder accumulatedPath = new StringBuilder();

				for (int i = 0; i < segments.length; i++) {
					if (i > 0) accumulatedPath.append("/");
					accumulatedPath.append(segments[i]);

					String displayName = segments[i];
					String clickPath = accumulatedPath.toString();

					addBreadcrumbItem(displayName, clickPath, i < segments.length - 1);
				}
			}

			binding.breadcrumbContainer.post(
					() -> {
						HorizontalScrollView scrollView =
								(HorizontalScrollView) binding.breadcrumbContainer.getParent();
						scrollView.fullScroll(View.FOCUS_RIGHT);
					});
		}

		private void addBreadcrumbItem(String label, String path, boolean clickable) {
			TextView tv = new TextView(context);
			tv.setText(label);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			tv.setPadding(
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									8,
									context.getResources().getDisplayMetrics()),
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									4,
									context.getResources().getDisplayMetrics()),
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									8,
									context.getResources().getDisplayMetrics()),
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									4,
									context.getResources().getDisplayMetrics()));

			if (clickable) {
				tv.setTextColor(resolveColor(com.google.android.material.R.attr.colorOnSurface));
				tv.setOnClickListener(
						v -> {
							if (listener != null) {
								listener.onBreadcrumbClick(path);
							}
						});
			} else {
				tv.setTextColor(resolveColor(com.google.android.material.R.attr.colorOnSurface));
				tv.setTypeface(null, Typeface.BOLD);
			}

			if (binding.breadcrumbContainer.getChildCount() > 0) {
				TextView separator = new TextView(context);
				separator.setText(" › ");
				separator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				separator.setTextColor(
						resolveColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
				binding.breadcrumbContainer.addView(separator);
			}

			binding.breadcrumbContainer.addView(tv);
		}

		@ColorInt
		private int resolveColor(int attr) {
			TypedValue typedValue = new TypedValue();
			context.getTheme().resolveAttribute(attr, typedValue, true);
			return typedValue.data;
		}
	}

	public class FilesViewHolder extends RecyclerView.ViewHolder {

		ListFilesBinding binding;
		private Tree currentTree;

		FilesViewHolder(ListFilesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						if (listener != null && currentTree != null) {
							listener.onClickFile(currentTree);
						}
					});
		}

		void bind(Tree tree) {
			this.currentTree = tree;

			if ("tree".equalsIgnoreCase(tree.getType())) {
				binding.fileIcon.setImageResource(R.drawable.ic_file_directory);
				binding.fileIcon.setContentDescription(context.getString(R.string.folder));
				binding.fileMenu.setVisibility(View.GONE);
			} else if ("blob".equalsIgnoreCase(tree.getType())) {
				int iconRes = FileIcon.getIconResource(tree.getName(), tree.getType());
				binding.fileIcon.setImageResource(iconRes);
				binding.fileIcon.setContentDescription(context.getString(R.string.file));
				binding.fileMenu.setVisibility(View.VISIBLE);

				binding.fileMenu.setOnClickListener(
						v -> {
							if (listener != null && currentTree != null) {
								listener.onFileMenuClick(currentTree);
							}
						});
			}

			binding.fileName.setText(tree.getName());
		}
	}
}

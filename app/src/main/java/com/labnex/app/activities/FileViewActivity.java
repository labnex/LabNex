package com.labnex.app.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityFileViewBinding;
import com.labnex.app.helpers.Constants;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.repository.FileContents;
import com.labnex.app.models.repository.Tree;
import com.labnex.app.notifications.Notifications;
import com.vdurmont.emoji.EmojiParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class FileViewActivity extends BaseActivity {

	private boolean renderMd = false;
	private ActivityFileViewBinding binding;
	private Tree tree;
	private String ref;
	private int projectId;
	public ProjectsContext projectsContext;
	private boolean processable = false;

	ActivityResultLauncher<Intent> activityResultLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {

							assert result.getData() != null;

							try {

								OutputStream outputStream =
										getContentResolver()
												.openOutputStream(
														Objects.requireNonNull(
																result.getData().getData()));

								NotificationCompat.Builder builder =
										new NotificationCompat.Builder(ctx, ctx.getPackageName())
												.setContentTitle(
														getString(
																R.string
																		.file_viewer_notification_title_started))
												.setContentText(
														getString(
																R.string
																		.file_viewer_notification_description_started,
																tree.getName()))
												.setSmallIcon(R.drawable.labnex_logo_monochrome)
												.setPriority(NotificationCompat.PRIORITY_LOW)
												.setChannelId(
														Constants.downloadNotificationChannelId)
												.setProgress(100, 0, false)
												.setOngoing(true);

								int notificationId = Notifications.uniqueNotificationId(ctx);

								NotificationManager notificationManager =
										(NotificationManager)
												getSystemService(Context.NOTIFICATION_SERVICE);
								notificationManager.notify(notificationId, builder.build());

								Thread thread =
										new Thread(
												() -> {
													try {

														Call<FileContents> call =
																RetrofitClient.getApiInterface(ctx)
																		.getFileContents(
																				projectId,
																				tree.getPath(),
																				ref);

														Response<FileContents> response =
																call.execute();

														assert response.body() != null;

														builder.setOngoing(false)
																.setContentTitle(
																		getString(
																				R.string
																						.file_viewer_notification_title_finished))
																.setContentText(
																		getString(
																				R.string
																						.file_viewer_notification_description_finished,
																				tree.getName()));

														byte[] decodedStr =
																Base64.getDecoder()
																		.decode(
																				response.body()
																						.getContent()
																						.getBytes(
																								StandardCharsets
																										.UTF_8));
														ByteArrayInputStream byteStream =
																new ByteArrayInputStream(
																		decodedStr);
														Utils.copyProgress(
																byteStream,
																outputStream,
																response.body().getSize(),
																progress -> {
																	builder.setProgress(
																			100, progress, false);
																	notificationManager.notify(
																			notificationId,
																			builder.build());
																});

													} catch (IOException ignored) {

														builder.setOngoing(false)
																.setContentTitle(
																		getString(
																				R.string
																						.file_viewer_notification_title_failed))
																.setContentText(
																		getString(
																				R.string
																						.file_viewer_notification_description_failed,
																				tree.getName()));

													} finally {

														builder.setProgress(0, 0, false)
																.setOngoing(false);

														notificationManager.notify(
																notificationId, builder.build());
													}
												});

								thread.start();

							} catch (IOException ignored) {
							}
						}
					});

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityFileViewBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		projectsContext = ProjectsContext.fromIntent(getIntent());

		projectId = projectsContext.getProjectId();
		tree = (Tree) getIntent().getSerializableExtra("tree");
		ref = getIntent().getStringExtra("ref");

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		if (!FilenameUtils.getExtension(tree.getName().toLowerCase()).equalsIgnoreCase("md")) {
			binding.bottomAppBar.getMenu().removeItem(R.id.render_md);
		}

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.download) {
						requestFileDownload();
					}
					if (menuItem.getItemId() == R.id.render_md) {

						if (!renderMd) {
							if (binding.markdown.getAdapter() == null) {
								Markdown.render(
										ctx,
										EmojiParser.parseToUnicode(binding.contents.getContent()),
										binding.markdown,
										projectsContext);
							}

							binding.contents.setVisibility(View.GONE);
							binding.markdownFrame.setVisibility(View.VISIBLE);

							renderMd = true;
						} else {
							binding.markdownFrame.setVisibility(View.GONE);
							binding.contents.setVisibility(View.VISIBLE);

							renderMd = false;
						}
					}
					return false;
				});

		getFileContents();
	}

	private void getFileContents() {

		Thread thread =
				new Thread(
						() -> {
							Call<FileContents> call =
									RetrofitClient.getApiInterface(ctx)
											.getFileContents(projectId, tree.getPath(), ref);

							try {

								Response<FileContents> response = call.execute();

								if (response.code() == 200) {

									FileContents responseBody = response.body();

									if (responseBody != null) {

										runOnUiThread(
												() -> binding.progressBar.setVisibility(View.GONE));
										String fileExtension =
												FilenameUtils.getExtension(
														responseBody.getFileName());

										switch (Utils.getFileType(fileExtension)) {
											case IMAGE:
												if (Arrays.asList(
																"bmp", "gif", "jpg", "jpeg", "png",
																"webp", "heic", "heif")
														.contains(fileExtension.toLowerCase())) {

													byte[] decodedImg =
															Base64.getDecoder()
																	.decode(
																			responseBody
																					.getContent()
																					.getBytes(
																							StandardCharsets
																									.UTF_8));

													Bitmap image =
															BitmapFactory.decodeByteArray(
																	decodedImg,
																	0,
																	decodedImg.length);
													processable = image != null;
													if (processable) {
														runOnUiThread(
																() -> {
																	binding.contents.setVisibility(
																			View.GONE);
																	binding.markdownFrame
																			.setVisibility(
																					View.GONE);

																	binding.photoView.setVisibility(
																			View.VISIBLE);
																	binding.photoView
																			.setImageBitmap(image);
																});
													}
												}
												break;

											case UNKNOWN:
											case TEXT:
												if (responseBody.getSize()
														> Constants.maximumFileViewerSize) {
													break;
												}

												processable = true;
												String text =
														Utils.decodeBase64(
																responseBody.getContent());

												runOnUiThread(
														() -> {
															binding.photoView.setVisibility(
																	View.GONE);

															binding.contents.setContent(
																	text, fileExtension);

															if (renderMd) {
																Markdown.render(
																		ctx,
																		EmojiParser.parseToUnicode(
																				text),
																		binding.markdown,
																		projectsContext);

																binding.contents.setVisibility(
																		View.GONE);
																binding.markdownFrame.setVisibility(
																		View.VISIBLE);
															} else {
																binding.markdownFrame.setVisibility(
																		View.GONE);
																binding.contents.setVisibility(
																		View.VISIBLE);
															}
														});
												break;
										}

										if (!processable) {
											runOnUiThread(
													() -> {
														binding.photoView.setVisibility(View.GONE);
														binding.contents.setVisibility(View.GONE);

														binding.markdownFrame.setVisibility(
																View.VISIBLE);
														binding.markdown.setVisibility(View.GONE);
														binding.markdownTv.setVisibility(
																View.VISIBLE);
														binding.markdownTv.setText(
																getString(
																		R.string
																				.exclude_files_in_fileviewer));
														binding.markdownTv.setGravity(
																Gravity.CENTER);
														binding.markdownTv.setTypeface(
																null, Typeface.BOLD);
													});
										}
									} else {

										runOnUiThread(
												() -> {
													binding.markdownTv.setText("");
													binding.progressBar.setVisibility(View.GONE);
												});
									}
								} else {

									if (response.code() == 401) {

										Snackbar.info(
												FileViewActivity.this,
												binding.bottomAppBar,
												getString(R.string.not_authorized));
									} else if (response.code() == 403) {

										Snackbar.info(
												FileViewActivity.this,
												binding.bottomAppBar,
												getString(R.string.access_forbidden_403));
									} else {

										Snackbar.info(
												FileViewActivity.this,
												binding.bottomAppBar,
												getString(R.string.generic_error));
									}
								}
							} catch (IOException ignored) {
							}
						});

		thread.start();
	}

	private void requestFileDownload() {

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, tree.getName());
		intent.setType("*/*");

		activityResultLauncher.launch(intent);
	}
}

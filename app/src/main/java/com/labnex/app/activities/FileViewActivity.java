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
import android.view.LayoutInflater;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.databinding.ActivityFileViewBinding;
import com.labnex.app.databinding.BottomSheetFileActionsBinding;
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
public class FileViewActivity extends BaseActivity implements CreateFileActivity.UpdateInterface {

	private boolean renderMd = false;
	private ActivityFileViewBinding binding;
	private Tree tree;
	private String ref;
	private int projectId;
	public ProjectsContext projectsContext;
	private boolean processable = false;
	private String fileContent;
	private BottomSheetFileActionsBinding sheetBinding;

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

		sheetBinding =
				BottomSheetFileActionsBinding.inflate(LayoutInflater.from(this), null, false);

		if (projectsContext == null) {
			projectId = getIntent().getIntExtra("projectId", -1);
			String projectName = getIntent().getStringExtra("projectName");
			String path = getIntent().getStringExtra("path");
			if (projectId == -1 || projectName == null || path == null) {
				ProjectsApi projectsApi = BaseApi.getInstance(ctx, ProjectsApi.class);
				assert projectsApi != null;
				com.labnex.app.database.models.Projects dbProject =
						projectsApi.fetchByProjectId(projectId);
				if (dbProject != null) {
					projectName = dbProject.getProjectName();
					path = dbProject.getProjectPath();
				} else {
					finish();
					return;
				}
			}
			projectsContext = new ProjectsContext(projectName, path, projectId, ctx);
		}
		projectId = projectsContext.getProjectId();

		CreateFileActivity.setUpdateListener(FileViewActivity.this);

		tree = (Tree) getIntent().getSerializableExtra("tree");
		ref = getIntent().getStringExtra("ref");

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		if (!FilenameUtils.getExtension(tree.getName().toLowerCase()).equalsIgnoreCase("md")) {
			if (binding.bottomAppBar.getMenu().findItem(R.id.render_md) != null) {
				binding.bottomAppBar.getMenu().removeItem(R.id.render_md);
			}
		}

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.menu) {
						showFileActionsBottomSheet();
						return true;
					} else if (menuItem.getItemId() == R.id.render_md) {
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
						return true;
					}
					return false;
				});

		getFileContents();
	}

	@Override
	public void createFileDataListener(String str, String branch) {

		if (str.equalsIgnoreCase("updated")) {
			Snackbar.info(
					FileViewActivity.this,
					binding.bottomAppBar,
					getString(R.string.file_update, branch));
			getFileContents();
		} else if (str.equalsIgnoreCase("deleted")) {
			Snackbar.info(
					FileViewActivity.this,
					binding.bottomAppBar,
					getString(R.string.delete_file_via_branch, branch));
		}
	}

	private void showFileActionsBottomSheet() {

		/*BottomSheetFileActionsBinding sheetBinding =
		BottomSheetFileActionsBinding.inflate(LayoutInflater.from(this), null, false);*/
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		sheetBinding.editItem.setOnClickListener(
				v -> {
					Intent intent = new Intent(this, CreateFileActivity.class);
					intent.putExtra("mode", "edit");
					intent.putExtra("projectId", projectId);
					intent.putExtra("filename", tree.getName());
					intent.putExtra("branch", ref);
					intent.putExtra("fileContent", fileContent);
					intent.putExtra("projectsContext", projectsContext);
					startActivity(intent);
					bottomSheetDialog.dismiss();
				});

		sheetBinding.deleteItem.setOnClickListener(
				v -> {
					Intent intent = new Intent(this, CreateFileActivity.class);
					intent.putExtra("mode", "delete");
					intent.putExtra("projectId", projectId);
					intent.putExtra("filename", tree.getName());
					intent.putExtra("branch", ref);
					intent.putExtra("projectsContext", projectsContext);
					startActivity(intent);
					bottomSheetDialog.dismiss();
				});

		sheetBinding.downloadItem.setOnClickListener(
				v -> {
					requestFileDownload();
					bottomSheetDialog.dismiss();
				});

		if (!processable) {
			sheetBinding.editItemCard.setVisibility(View.GONE);
		}

		bottomSheetDialog.show();
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
													sheetBinding.editItem.setVisibility(View.GONE);
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
												fileContent =
														Utils.decodeBase64(
																responseBody.getContent());
												runOnUiThread(
														() -> {
															binding.photoView.setVisibility(
																	View.GONE);
															binding.contents.setContent(
																	fileContent, fileExtension);
															if (renderMd) {
																Markdown.render(
																		ctx,
																		EmojiParser.parseToUnicode(
																				fileContent),
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
									runOnUiThread(
											() -> {
												binding.progressBar.setVisibility(View.GONE);
												String errorMessage;
												if (response.code() == 401) {
													errorMessage =
															getString(R.string.not_authorized);
												} else if (response.code() == 403) {
													errorMessage =
															getString(
																	R.string.access_forbidden_403);
												} else {
													errorMessage =
															getString(R.string.generic_error);
												}
												Snackbar.info(
														FileViewActivity.this,
														binding.bottomAppBar,
														errorMessage);
											});
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

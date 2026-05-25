package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.MergeRequestsActivity;
import com.labnex.app.databinding.BottomsheetCreateMrBinding;
import com.labnex.app.databinding.ItemPickerCardBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.merge_requests.CrudeMergeRequest;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.milestone.Milestones;
import com.labnex.app.models.projects.ForkedFromProject;
import com.labnex.app.models.templates.Templates;
import com.labnex.app.viewmodels.MergeRequestsViewModel;
import com.labnex.app.viewmodels.TemplatesViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class CreateMergeRequestBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateMrBinding binding;
	private MergeRequestsViewModel viewModel;
	private TemplatesViewModel templatesViewModel;
	private String type;
	private long projectId;
	private boolean isEditMode = false;
	private long mrIid;
	private boolean canModify;
	private boolean isFromCreateFile;
	private String preSourceBranch;
	private String preTitle;

	private ItemPickerCardBinding sourceCard, targetCard, labelsCard, milestoneCard, upstreamCard;
	private List<String> selectedLabels = new ArrayList<>();
	private Milestones selectedMilestone;
	private ForkedFromProject upstreamProject;
	private boolean createUpstreamMr = false;

	public static CreateMergeRequestBottomSheet newInstance(
			String type,
			long projectId,
			boolean canModify,
			boolean isFromCreateFile,
			@Nullable String sourceBranch,
			@Nullable String mrTitle,
			@Nullable MergeRequests mr,
			@Nullable ForkedFromProject upstreamProject) {
		CreateMergeRequestBottomSheet sheet = new CreateMergeRequestBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("projectId", projectId);
		args.putBoolean("canModify", canModify);
		args.putBoolean("isFromCreateFile", isFromCreateFile);
		if (sourceBranch != null) args.putString("sourceBranch", sourceBranch);
		if (mrTitle != null) args.putString("mrTitle", mrTitle);
		if (mr != null) args.putSerializable("mr", mr);
		if (upstreamProject != null) args.putSerializable("upstreamProject", upstreamProject);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getString("type", "project");
			projectId = getArguments().getLong("projectId", 0);
			canModify = getArguments().getBoolean("canModify", false);
			isFromCreateFile = getArguments().getBoolean("isFromCreateFile", false);
			preSourceBranch = getArguments().getString("sourceBranch");
			preTitle = getArguments().getString("mrTitle");
			upstreamProject = (ForkedFromProject) getArguments().getSerializable("upstreamProject");
			MergeRequests mr = (MergeRequests) getArguments().getSerializable("mr");
			if (mr != null) {
				isEditMode = true;
				mrIid = mr.getIid();
			}
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateMrBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(MergeRequestsViewModel.class);
		templatesViewModel = new ViewModelProvider(this).get(TemplatesViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());

		bindCards();
		setupCardLabels();
		setupPreFilledData();
		setupEditMode();
		setupCardListeners();
		setupTemplates();
		setupEditorExpand();

		binding.btnSubmit.setOnClickListener(v -> submitMr());
		observeViewModel();
		return binding.getRoot();
	}

	private void bindCards() {
		sourceCard = ItemPickerCardBinding.bind(binding.cardSourceBranch.getRoot());
		targetCard = ItemPickerCardBinding.bind(binding.cardTargetBranch.getRoot());
		labelsCard = ItemPickerCardBinding.bind(binding.cardLabels.getRoot());
		milestoneCard = ItemPickerCardBinding.bind(binding.cardMilestone.getRoot());
		upstreamCard = ItemPickerCardBinding.bind(binding.cardUpstream.getRoot());
	}

	private void setupCardLabels() {
		sourceCard.cardTitle.setText(R.string.source_branch);
		sourceCard.cardIcon.setImageResource(R.drawable.ic_branch);

		targetCard.cardTitle.setText(R.string.target_branch);
		targetCard.cardIcon.setImageResource(R.drawable.ic_branch);

		labelsCard.cardTitle.setText(R.string.labels);
		labelsCard.cardIcon.setImageResource(R.drawable.ic_labels);

		milestoneCard.cardTitle.setText(R.string.milestone);
		milestoneCard.cardIcon.setImageResource(R.drawable.ic_milestones);

		binding.cardLabels.getRoot().setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.cardMilestone.getRoot().setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.switchSquash.setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.switchRemoveSource.setVisibility(canModify ? View.VISIBLE : View.GONE);

		if (upstreamProject != null) {
			binding.cardUpstream.getRoot().setVisibility(View.VISIBLE);
			upstreamCard.cardTitle.setText(R.string.create_upstream_mr);
			upstreamCard.cardIcon.setImageResource(R.drawable.ic_forks);

			createUpstreamMr = true;
			upstreamCard.cardSubtitle.setText(upstreamProject.getNameWithNamespace());

			binding.cardLabels.getRoot().setVisibility(View.GONE);
			binding.cardMilestone.getRoot().setVisibility(View.GONE);

			binding.cardUpstream
					.getRoot()
					.setOnClickListener(
							v -> {
								createUpstreamMr = !createUpstreamMr;
								upstreamCard.cardSubtitle.setText(
										createUpstreamMr
												? upstreamProject.getNameWithNamespace()
												: getString(R.string.tap_to_enable));

								targetCard.cardSubtitle.setText(R.string.tap_to_select_branch);
								targetCard.cardClear.setVisibility(View.GONE);

								binding.cardLabels
										.getRoot()
										.setVisibility(
												createUpstreamMr
														? View.GONE
														: (canModify ? View.VISIBLE : View.GONE));
								binding.cardMilestone
										.getRoot()
										.setVisibility(
												createUpstreamMr
														? View.GONE
														: (canModify ? View.VISIBLE : View.GONE));
							});
		}
	}

	private void setupPreFilledData() {
		if (preSourceBranch != null) {
			sourceCard.cardSubtitle.setText(preSourceBranch);
			sourceCard.cardClear.setVisibility(View.VISIBLE);
		} else {
			sourceCard.cardSubtitle.setText(R.string.tap_to_select_branch);
		}

		targetCard.cardSubtitle.setText(R.string.tap_to_select_branch);

		labelsCard.cardSubtitle.setText(R.string.tap_to_select_labels);
		milestoneCard.cardSubtitle.setText(R.string.tap_to_select_milestone);

		if (preTitle != null) {
			binding.titleInput.setText(preTitle);
		}

		binding.descriptionInput.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});
	}

	private void setupEditMode() {
		if (!isEditMode || getArguments() == null) return;

		MergeRequests mr = (MergeRequests) getArguments().getSerializable("mr");
		if (mr == null) return;

		binding.sheetTitle.setText(R.string.edit_mr);
		binding.btnSubmit.setText(R.string.update);
		binding.titleInput.setText(mr.getTitle());
		binding.descriptionInput.setText(mr.getDescription() != null ? mr.getDescription() : "");
		sourceCard.cardSubtitle.setText(mr.getSourceBranch());
		targetCard.cardSubtitle.setText(mr.getTargetBranch());

		if (mr.getLabels() != null && !mr.getLabels().isEmpty()) {
			selectedLabels = new ArrayList<>(mr.getLabels());
			labelsCard.cardSubtitle.setText(String.join(", ", selectedLabels));
			labelsCard.cardClear.setVisibility(View.VISIBLE);
		}

		if (mr.getMilestone() != null) {
			selectedMilestone = new Milestones();
			selectedMilestone.setId(mr.getMilestone().getId());
			selectedMilestone.setTitle(mr.getMilestone().getTitle());
			milestoneCard.cardSubtitle.setText(mr.getMilestone().getTitle());
			milestoneCard.cardClear.setVisibility(View.VISIBLE);
		}
	}

	private void setupCardListeners() {
		binding.cardSourceBranch
				.getRoot()
				.setOnClickListener(
						v ->
								BranchesBottomSheet.newPickerInstance(
												projectId,
												branch -> {
													sourceCard.cardSubtitle.setText(branch);
													sourceCard.cardClear.setVisibility(
															View.VISIBLE);
													checkBranchesSame(
															branch,
															targetCard
																	.cardSubtitle
																	.getText()
																	.toString());
												})
										.show(getParentFragmentManager(), "sourceBranchPicker"));

		sourceCard.cardClear.setOnClickListener(
				v -> {
					sourceCard.cardSubtitle.setText(R.string.tap_to_select_branch);
					sourceCard.cardClear.setVisibility(View.GONE);
				});

		binding.cardTargetBranch
				.getRoot()
				.setOnClickListener(
						v -> {
							long branchProjectId =
									createUpstreamMr ? upstreamProject.getId() : projectId;
							BranchesBottomSheet.newPickerInstance(
											branchProjectId,
											branch -> {
												targetCard.cardSubtitle.setText(branch);
												targetCard.cardClear.setVisibility(View.VISIBLE);
												checkBranchesSame(
														sourceCard
																.cardSubtitle
																.getText()
																.toString(),
														branch);
											})
									.show(getParentFragmentManager(), "targetBranchPicker");
						});

		targetCard.cardClear.setOnClickListener(
				v -> {
					targetCard.cardSubtitle.setText(R.string.tap_to_select_branch);
					targetCard.cardClear.setVisibility(View.GONE);
				});

		binding.cardLabels
				.getRoot()
				.setOnClickListener(
						v -> {
							LabelsPickerBottomSheet picker =
									LabelsPickerBottomSheet.newInstance(
											type, projectId, selectedLabels);
							picker.setOnLabelsSelectedListener(
									labels -> {
										selectedLabels = labels;
										labelsCard.cardSubtitle.setText(
												labels.isEmpty()
														? getString(R.string.tap_to_select_labels)
														: String.join(", ", labels));
										labelsCard.cardClear.setVisibility(
												labels.isEmpty() ? View.GONE : View.VISIBLE);
									});
							picker.show(getParentFragmentManager(), "labelsPicker");
						});

		labelsCard.cardClear.setOnClickListener(
				v -> {
					selectedLabels.clear();
					labelsCard.cardSubtitle.setText(R.string.tap_to_select_labels);
					labelsCard.cardClear.setVisibility(View.GONE);
				});

		binding.cardMilestone
				.getRoot()
				.setOnClickListener(
						v -> {
							MilestonesPickerBottomSheet picker =
									MilestonesPickerBottomSheet.newInstance(
											type, projectId, selectedMilestone);
							picker.setOnMilestoneSelectedListener(
									milestone -> {
										selectedMilestone = milestone;
										milestoneCard.cardSubtitle.setText(milestone.getTitle());
										milestoneCard.cardClear.setVisibility(View.VISIBLE);
									});
							picker.show(getParentFragmentManager(), "milestonePicker");
						});

		milestoneCard.cardClear.setOnClickListener(
				v -> {
					selectedMilestone = null;
					milestoneCard.cardSubtitle.setText(R.string.tap_to_select_milestone);
					milestoneCard.cardClear.setVisibility(View.GONE);
				});
	}

	private void checkBranchesSame(String source, String target) {
		if (!source.equals(getString(R.string.tap_to_select_branch))
				&& !target.equals(getString(R.string.tap_to_select_branch))
				&& source.equals(target)) {
			Toasty.show(requireContext(), getString(R.string.mr_branches_are_same));
		}
	}

	private void setupTemplates() {
		templatesViewModel.loadTemplates(requireContext(), projectId, "merge_requests");
		templatesViewModel
				.getTemplateList()
				.observe(
						getViewLifecycleOwner(),
						templates -> {
							if (templates != null && !templates.isEmpty()) {
								List<String> names = new ArrayList<>();
								names.add(getString(R.string.select_template));
								for (Templates t : templates) names.add(t.getName());
								ArrayAdapter<String> adapter =
										new ArrayAdapter<>(
												requireContext(),
												R.layout.item_dropdown_entry_simple,
												names);
								binding.templateInput.setAdapter(adapter);
								binding.templateInput.setOnItemClickListener(
										(parent, view, position, id) -> {
											if (position > 0) {
												templatesViewModel.loadTemplateContent(
														requireContext(),
														projectId,
														"merge_requests",
														templates.get(position - 1).getName());
											} else {
												binding.descriptionInput.setText("");
											}
										});
							} else {
								binding.templateLayout.setVisibility(View.GONE);
							}
						});

		templatesViewModel
				.getTemplateContent()
				.observe(
						getViewLifecycleOwner(),
						content -> {
							if (content != null) binding.descriptionInput.setText(content);
						});
	}

	private void setupEditorExpand() {
		binding.btnExpand.setOnClickListener(
				v -> {
					String content =
							binding.descriptionInput.getText() != null
									? binding.descriptionInput.getText().toString()
									: "";
					EditorBottomSheet editor =
							EditorBottomSheet.newInstance(
									content, null, EditorBottomSheet.EditorMode.MARKDOWN, null);
					editor.setEditorListener(
							newContent -> binding.descriptionInput.setText(newContent));
					editor.show(getChildFragmentManager(), "fullscreenEditor");
				});
	}

	private void submitMr() {
		String title =
				binding.titleInput.getText() != null
						? binding.titleInput.getText().toString().trim()
						: "";
		if (title.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.title_required));
			return;
		}

		String source = sourceCard.cardSubtitle.getText().toString();
		String target = targetCard.cardSubtitle.getText().toString();
		if (source.equals(getString(R.string.tap_to_select_branch))
				|| target.equals(getString(R.string.tap_to_select_branch))) {
			Toasty.show(requireContext(), getString(R.string.source_target_branch_empty_error));
			return;
		}
		if (source.equals(target)) {
			Toasty.show(requireContext(), getString(R.string.mr_branches_are_same));
			return;
		}

		CrudeMergeRequest mr = new CrudeMergeRequest();
		mr.setTitle(title);
		mr.setDescription(
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "");
		mr.setSourceBranch(source);
		mr.setTargetBranch(target);
		mr.setSquash(binding.switchSquash.isChecked());
		mr.setShouldRemoveSourceBranch(binding.switchRemoveSource.isChecked());
		if (!selectedLabels.isEmpty()) mr.setLabels(selectedLabels);
		if (selectedMilestone != null) mr.setMilestoneId((long) selectedMilestone.getId());

		if (!createUpstreamMr) {
			if (!selectedLabels.isEmpty()) mr.setLabels(selectedLabels);
			if (selectedMilestone != null) mr.setMilestoneId((long) selectedMilestone.getId());
		}

		if (createUpstreamMr && upstreamProject != null) {
			mr.setTargetProjectId((long) upstreamProject.getId());
		}

		if (isEditMode) {
			viewModel.updateMergeRequest(requireContext(), projectId, mrIid, mr);
		} else {
			viewModel.createMergeRequest(requireContext(), projectId, mr);
		}
	}

	private void observeViewModel() {
		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.btnSubmit.setText(null);
								binding.btnSubmit.setEnabled(false);
								binding.loadingIndicator.setVisibility(View.VISIBLE);
							} else {
								binding.loadingIndicator.setVisibility(View.GONE);
								binding.btnSubmit.setText(
										isEditMode ? R.string.update : R.string.create);
								binding.btnSubmit.setEnabled(true);
							}
						});

		viewModel
				.getCreateSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success) && !isEditMode) {
								Toasty.show(requireContext(), R.string.mr_created);
								viewModel.clearCreateSuccess();
								AppUIStateManager.refreshData();
								triggerGlobalRefresh();
								navigateAfterCreate();
								dismiss();
							}
						});

		viewModel
				.getEditSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success) && isEditMode) {
								Toasty.show(requireContext(), R.string.mr_updated);
								viewModel.clearEditSuccess();
								AppUIStateManager.refreshData();
								triggerGlobalRefresh();
								dismiss();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(
											requireContext(), getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(
											requireContext(),
											getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(requireContext(), getString(R.string.not_found));
									break;
								case "generic_error":
									Toasty.show(
											requireContext(), getString(R.string.generic_error));
									break;
								default:
									Toasty.show(requireContext(), errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}

	private void triggerGlobalRefresh() {
		if (getActivity() instanceof BaseActivity) {
			((BaseActivity) getActivity()).triggerGlobalRefresh();
		}
	}

	private void navigateAfterCreate() {
		if (isFromCreateFile && getActivity() != null) {
			long targetProject = createUpstreamMr ? upstreamProject.getId() : projectId;
			startActivity(
					new Intent(getActivity(), MergeRequestsActivity.class)
							.putExtra("source", "mr")
							.putExtra("id", (int) targetProject));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

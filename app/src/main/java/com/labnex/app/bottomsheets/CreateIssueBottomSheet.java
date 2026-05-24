package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.databinding.BottomsheetCreateIssueBinding;
import com.labnex.app.databinding.ItemPickerCardBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.issues.CrudeIssue;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.issues.Milestone;
import com.labnex.app.models.milestone.Milestones;
import com.labnex.app.models.templates.Templates;
import com.labnex.app.viewmodels.IssuesViewModel;
import com.labnex.app.viewmodels.TemplatesViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class CreateIssueBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateIssueBinding binding;
	private IssuesViewModel viewModel;
	private TemplatesViewModel templatesViewModel;
	private String type;
	private long projectId;
	private boolean isEditMode = false;
	private long issueIid;
	private boolean canModify;

	private ItemPickerCardBinding dueDateCard, labelsCard, milestoneCard;
	private List<String> selectedLabels = new ArrayList<>();
	private Milestones selectedMilestone;
	private boolean dueDateSet = false;

	public static CreateIssueBottomSheet newInstance(
			String type, long projectId, boolean canModify, @Nullable Issues issue) {
		CreateIssueBottomSheet sheet = new CreateIssueBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("projectId", projectId);
		args.putBoolean("canModify", canModify);
		if (issue != null) args.putSerializable("issue", issue);
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
			Issues issue = (Issues) getArguments().getSerializable("issue");
			if (issue != null) {
				isEditMode = true;
				issueIid = issue.getIid();
			}
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateIssueBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(IssuesViewModel.class);
		templatesViewModel = new ViewModelProvider(this).get(TemplatesViewModel.class);

		templatesViewModel.loadTemplates(requireContext(), projectId, "issues");
		templateObserver();

		binding.descriptionInput.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == android.view.MotionEvent.ACTION_UP
							|| event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		binding.btnClose.setOnClickListener(v -> dismiss());

		dueDateCard = ItemPickerCardBinding.bind(binding.cardDueDate.getRoot());
		labelsCard = ItemPickerCardBinding.bind(binding.cardLabels.getRoot());
		milestoneCard = ItemPickerCardBinding.bind(binding.cardMilestone.getRoot());

		dueDateCard.cardTitle.setText(R.string.due_date);
		dueDateCard.cardSubtitle.setText(R.string.tap_to_select_date);
		dueDateCard.cardIcon.setImageResource(R.drawable.ic_calendar);

		labelsCard.cardTitle.setText(R.string.labels);
		labelsCard.cardSubtitle.setText(R.string.tap_to_select_labels);
		labelsCard.cardIcon.setImageResource(R.drawable.ic_labels);

		milestoneCard.cardTitle.setText(R.string.milestone);
		milestoneCard.cardSubtitle.setText(R.string.tap_to_select_milestone);
		milestoneCard.cardIcon.setImageResource(R.drawable.ic_milestones);

		binding.switchConfidential.setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.cardDueDate.getRoot().setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.cardLabels.getRoot().setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.cardMilestone.getRoot().setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.weightSlider.setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.weightLabel.setVisibility(canModify ? View.VISIBLE : View.GONE);

		if (isEditMode && getArguments() != null) {
			Issues issue = (Issues) getArguments().getSerializable("issue");
			if (issue != null) {
				binding.sheetTitle.setText(R.string.edit_issue);
				binding.btnSubmit.setText(R.string.update);
				binding.titleInput.setText(issue.getTitle());
				binding.descriptionInput.setText(
						issue.getDescription() != null ? issue.getDescription() : "");
				binding.switchConfidential.setChecked(issue.isConfidential());
				if (issue.getDueDate() != null) {
					dueDateCard.cardSubtitle.setText(issue.getDueDate());
					dueDateSet = true;
					dueDateCard.cardClear.setVisibility(View.VISIBLE);
				}
				if (issue.getLabels() != null && !issue.getLabels().isEmpty()) {
					selectedLabels = new ArrayList<>(issue.getLabels());
					labelsCard.cardSubtitle.setText(String.join(", ", selectedLabels));
					labelsCard.cardClear.setVisibility(View.VISIBLE);
				}
				if (issue.getMilestone() != null) {
					Milestone m = issue.getMilestone();
					selectedMilestone = new Milestones();
					selectedMilestone.setId(m.getId());
					selectedMilestone.setTitle(m.getTitle());
					milestoneCard.cardSubtitle.setText(m.getTitle());
					milestoneCard.cardClear.setVisibility(View.VISIBLE);
				}
				binding.weightSlider.setValue(issue.getWeight());
			}
		}

		binding.cardDueDate
				.getRoot()
				.setOnClickListener(
						v ->
								showDatePicker(
										date -> {
											dueDateCard.cardSubtitle.setText(date);
											dueDateCard.cardClear.setVisibility(View.VISIBLE);
											dueDateSet = true;
										}));
		dueDateCard.cardClear.setOnClickListener(
				v -> {
					dueDateCard.cardSubtitle.setText(R.string.tap_to_select_date);
					dueDateCard.cardClear.setVisibility(View.GONE);
					dueDateSet = false;
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
										if (labels.isEmpty()) {
											labelsCard.cardSubtitle.setText(
													R.string.tap_to_select_labels);
											labelsCard.cardClear.setVisibility(View.GONE);
										} else {
											String text = String.join(", ", labels);
											labelsCard.cardSubtitle.setText(text);
											labelsCard.cardClear.setVisibility(View.VISIBLE);
										}
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

		binding.btnSubmit.setOnClickListener(v -> submitIssue());
		observeViewModel();

		return binding.getRoot();
	}

	private void templateObserver() {
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
														"issues",
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
							if (content != null) {
								binding.descriptionInput.setText(content);
							}
						});

		templatesViewModel.getIsLoadingContent().observe(getViewLifecycleOwner(), loading -> {});

		templatesViewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg != null) {
								Toasty.show(requireContext(), errorMsg);
								templatesViewModel.clearError();
							}
						});
	}

	private void showDatePicker(DateSelectedListener listener) {
		MaterialDatePicker<Long> picker =
				MaterialDatePicker.Builder.datePicker()
						.setTitleText(getString(R.string.select_date))
						.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
						.build();
		picker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(selection);
					listener.onDateSelected(
							String.format(
									Locale.US,
									"%d-%02d-%02d",
									cal.get(Calendar.YEAR),
									cal.get(Calendar.MONTH) + 1,
									cal.get(Calendar.DAY_OF_MONTH)));
				});
		picker.show(getParentFragmentManager(), "datePicker");
	}

	interface DateSelectedListener {
		void onDateSelected(String date);
	}

	private void submitIssue() {
		String title =
				binding.titleInput.getText() != null
						? binding.titleInput.getText().toString().trim()
						: "";
		if (title.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.title_required));
			return;
		}

		CrudeIssue issue = new CrudeIssue();
		issue.setTitle(title);
		issue.setDescription(
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "");
		issue.setConfidential(binding.switchConfidential.isChecked());
		if (dueDateSet) issue.setDueDate(dueDateCard.cardSubtitle.getText().toString());
		if (!selectedLabels.isEmpty()) issue.setLabels(selectedLabels);
		if (selectedMilestone != null) issue.setMilestoneId((long) selectedMilestone.getId());
		issue.setWeight((int) binding.weightSlider.getValue());

		if (isEditMode) {
			viewModel.updateIssue(requireContext(), type, projectId, issueIid, issue);
		} else {
			viewModel.createIssue(requireContext(), type, projectId, issue);
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
								Toasty.show(requireContext(), R.string.issue_created);
								AppUIStateManager.refreshData();
								if (getActivity() instanceof BaseActivity) {
									((BaseActivity) getActivity()).triggerGlobalRefresh();
								}
								viewModel.clearCreateSuccess();
								dismiss();
							}
						});

		viewModel
				.getEditSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success) && isEditMode) {
								Toasty.show(requireContext(), R.string.issue_updated);
								viewModel.clearEditSuccess();
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

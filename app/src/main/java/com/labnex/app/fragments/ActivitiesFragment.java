package com.labnex.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.MainActivity;
import com.labnex.app.adapters.ActivitiesAdapter;
import com.labnex.app.databinding.FragmentActivitiesBinding;
import com.labnex.app.viewmodels.ActivitiesViewModel;

/**
 * @author mmarif
 */
public class ActivitiesFragment extends Fragment {

	private Context ctx;
	private FragmentActivitiesBinding binding;
	private ActivitiesViewModel activitiesViewModel;
	private ActivitiesAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private String source;
	private BottomNavigationView bottomNavigationView;

	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ctx = requireContext();
		binding = FragmentActivitiesBinding.inflate(inflater, container, false);

		activitiesViewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();
		bottomNavigationView = ((MainActivity) requireContext()).findViewById(R.id.nav_view);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											binding.pullToRefresh.setRefreshing(false);
											fetchDataAsync();
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		fetchDataAsync();

		return binding.getRoot();
	}

	private void fetchDataAsync() {

		activitiesViewModel
				.getEvents(
						ctx,
						source,
						resultLimit,
						page,
						binding,
						requireActivity(),
						bottomNavigationView)
				.observe(
						requireActivity(),
						mainList -> {
							adapter = new ActivitiesAdapter(ctx, mainList);
							adapter.setLoadMoreListener(
									new ActivitiesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											activitiesViewModel.loadMore(
													ctx,
													source,
													resultLimit,
													page,
													adapter,
													binding,
													requireActivity(),
													bottomNavigationView);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								binding.recyclerView.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

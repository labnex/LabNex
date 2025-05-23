package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.adapters.SnippetsAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivitySnippetsBinding;
import com.labnex.app.viewmodels.SnippetsViewModel;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class SnippetsActivity extends BaseActivity {

	private ActivitySnippetsBinding binding;
	private SnippetsViewModel snippetsViewModel;
	private SnippetsAdapter adapter;
	private int page = 1;
	private int resultLimit;
	public ProjectsContext projectsContext;
	private boolean isLoading = false;

	private final ActivityResultLauncher<Intent> createSnippetLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == RESULT_OK) {
							page = 1;
							adapter.clearAdapter();
							binding.progressBar.setVisibility(View.VISIBLE);
							binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							fetchSnippets();
						}
					});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySnippetsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		projectsContext = ProjectsContext.fromIntent(getIntent());
		resultLimit = getAccount().getMaxPageLimit();

		snippetsViewModel = new ViewModelProvider(this).get(SnippetsViewModel.class);

		binding.recyclerView.setHasFixedSize(true);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		UserAccount userAccount = getAccount().getAccount();
		adapter =
				new SnippetsAdapter(
						this, this, new ArrayList<>(), userAccount, binding.bottomAppBar);
		binding.recyclerView.setAdapter(adapter);

		binding.progressBar.setVisibility(View.VISIBLE);
		binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);

		binding.bottomAppBar.setNavigationOnClickListener(v -> finish());

		binding.newSnippet.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, SnippetDetailActivity.class);
					intent.putExtra("MODE", "CREATE");
					createSnippetLauncher.launch(intent);
				});

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											adapter.clearAdapter();
											binding.pullToRefresh.setRefreshing(false);
											isLoading = false;
											fetchSnippets();
											binding.progressBar.setVisibility(View.VISIBLE);
											binding.nothingFoundFrame
													.getRoot()
													.setVisibility(View.GONE);
										},
										250));

		binding.recyclerView.addOnScrollListener(
				new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
						super.onScrolled(recyclerView, dx, dy);
						if (dy > 0) { // Scrolling down
							int totalItems = adapter.getItemCount();
							int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
							if (!isLoading
									&& adapter.isMoreDataAvailable()
									&& totalItems > 0
									&& lastVisibleItem >= totalItems - 5) {
								isLoading = true;
								page += 1;
								snippetsViewModel.loadMore(
										SnippetsActivity.this,
										resultLimit,
										page,
										adapter,
										SnippetsActivity.this,
										binding);
								binding.progressBar.setVisibility(View.VISIBLE);
							}
						}
					}
				});

		fetchSnippets();
	}

	private void fetchSnippets() {
		snippetsViewModel
				.getSnippets(this, resultLimit, page, this, binding)
				.observe(
						this,
						snippets -> {
							if (snippets != null) {
								adapter.updateList(snippets);
								binding.nothingFoundFrame
										.getRoot()
										.setVisibility(
												snippets.isEmpty() ? View.VISIBLE : View.GONE);
							}
							isLoading = false;
						});
	}
}

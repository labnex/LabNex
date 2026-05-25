package com.labnex.app.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.IssueDetailActivity;
import com.labnex.app.activities.MergeRequestDetailActivity;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.bottomsheets.CommitDiffsBottomSheet;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.core.MainGrammarLocator;
import com.labnex.app.helpers.codeeditor.markwon.MarkwonHighlighter;
import com.labnex.app.helpers.codeeditor.theme.Theme;
import com.labnex.app.helpers.markdown.AlertPlugin;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.core.spans.LinkSpan;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TableAwareMovementMethod;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler;
import io.noties.markwon.inlineparser.InlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParser;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.SimpleEntry;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.InlineParserFactory;
import org.commonmark.parser.Parser;
import org.commonmark.parser.PostProcessor;
import stormpot.Allocator;
import stormpot.BlazePool;
import stormpot.Config;
import stormpot.Pool;
import stormpot.Poolable;
import stormpot.Slot;
import stormpot.Timeout;

/**
 * @author opyale
 * @author mmarif
 */
public class Markdown {

	private static final int MAX_OBJECT_POOL_SIZE = 45;
	private static final int MAX_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

	private static final Timeout OBJECT_POOL_CLAIM_TIMEOUT = new Timeout(240, TimeUnit.SECONDS);

	private static final ExecutorService executorService =
			new ThreadPoolExecutor(
					MAX_THREAD_POOL_SIZE,
					MAX_THREAD_POOL_SIZE,
					0,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue<>());

	private static final Pool<Renderer> rendererPool;
	private static final Pool<RecyclerViewRenderer> rvRendererPool;

	static {
		Config<Renderer> config = new Config<>();

		config.setBackgroundExpirationEnabled(true);
		config.setPreciseLeakDetectionEnabled(true);
		config.setSize(MAX_OBJECT_POOL_SIZE);
		config.setAllocator(
				new Allocator<Renderer>() {

					@Override
					public Renderer allocate(Slot slot) {
						return new Renderer(slot);
					}

					@Override
					public void deallocate(Renderer poolable) {}
				});

		rendererPool = new BlazePool<>(config);

		Config<RecyclerViewRenderer> configRv = new Config<>();

		configRv.setBackgroundExpirationEnabled(true);
		configRv.setPreciseLeakDetectionEnabled(true);
		configRv.setSize(MAX_OBJECT_POOL_SIZE);
		configRv.setAllocator(
				new Allocator<RecyclerViewRenderer>() {

					@Override
					public RecyclerViewRenderer allocate(Slot slot) {

						return new RecyclerViewRenderer(slot);
					}

					@Override
					public void deallocate(RecyclerViewRenderer poolable) {}
				});

		rvRendererPool = new BlazePool<>(configRv);
	}

	public static Spanned renderToSpanned(Context context, String markdown) {
		Markwon markwon =
				Markwon.builder(context)
						.usePlugin(CorePlugin.create())
						.usePlugin(HtmlPlugin.create())
						.usePlugin(LinkifyPlugin.create(true))
						.usePlugin(SoftBreakAddsNewLinePlugin.create())
						.usePlugin(StrikethroughPlugin.create())
						.usePlugin(TaskListPlugin.create(context))
						.usePlugin(
								MarkwonHighlighter.create(
										context,
										Theme.getDefaultTheme(context),
										MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE))
						.usePlugin(
								new AbstractMarkwonPlugin() {
									private final Typeface tf = Typeface.DEFAULT;

									@Override
									public void beforeSetText(
											@NonNull TextView textView, @NonNull Spanned markdown) {

										textView.setTextIsSelectable(true);
										textView.setMovementMethod(
												LinkMovementMethod.getInstance());
										super.beforeSetText(textView, markdown);
									}

									@Override
									public void configureTheme(
											@NonNull MarkwonTheme.Builder builder) {

										builder.codeBlockMargin(
												(int)
														(context.getResources()
																		.getDisplayMetrics()
																		.density
																* 10));

										builder.headingTypeface(Typeface.create(tf, Typeface.BOLD));
									}
								})
						.build();

		return markwon.toMarkdown(markdown);
	}

	public static void render(Context context, String markdown, TextView textView) {

		try {
			Renderer renderer = rendererPool.claim(OBJECT_POOL_CLAIM_TIMEOUT);

			if (renderer != null) {
				renderer.setParameters(context, markdown, textView);
				executorService.execute(renderer);
			}
		} catch (InterruptedException ignored) {
		}
	}

	public static void render(
			Context context, String markdown, RecyclerView recyclerView, ProjectsContext projects) {

		try {
			RecyclerViewRenderer renderer = rvRendererPool.claim(OBJECT_POOL_CLAIM_TIMEOUT);

			if (renderer != null) {
				renderer.setParameters(context, markdown, recyclerView, projects);
				executorService.execute(renderer);
			}
		} catch (InterruptedException ignored) {
		}
	}

	// Render textview
	private static class Renderer implements Runnable, Poolable {

		private final Slot slot;

		private Markwon markwon;

		private Context context;
		private String markdown;
		private TextView textView;

		public Renderer(Slot slot) {

			this.slot = slot;
		}

		private void setup() {

			Markwon.Builder builder =
					Markwon.builder(context)
							.usePlugin(AlertPlugin.create(context))
							.usePlugin(CorePlugin.create())
							.usePlugin(HtmlPlugin.create())
							.usePlugin(LinkifyPlugin.create(true))
							.usePlugin(SoftBreakAddsNewLinePlugin.create())
							.usePlugin(TablePlugin.create(context))
							.usePlugin(
									MovementMethodPlugin.create(TableAwareMovementMethod.create()))
							.usePlugin(TaskListPlugin.create(context))
							.usePlugin(StrikethroughPlugin.create())
							.usePlugin(GlideImagesPlugin.create(context))
							.usePlugin(ImagesPlugin.create())
							.usePlugin(
									new AbstractMarkwonPlugin() {
										@Override
										public void configure(@NonNull Registry registry) {
											registry.require(
													ImagesPlugin.class,
													imagesPlugin ->
															imagesPlugin.addSchemeHandler(
																	OkHttpNetworkSchemeHandler
																			.create(
																					new OkHttpClient())));
										}
									})
							.usePlugin(
									MarkwonHighlighter.create(
											context,
											Theme.getDefaultTheme(context),
											MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE))
							.usePlugin(
									new AbstractMarkwonPlugin() {

										private final Typeface tf = Typeface.DEFAULT;

										@Override
										public void beforeSetText(
												@NonNull TextView textView,
												@NonNull Spanned markdown) {

											textView.setTextIsSelectable(true);
											textView.setMovementMethod(
													LinkMovementMethod.getInstance());
											super.beforeSetText(textView, markdown);
										}

										@Override
										public void configureTheme(
												@NonNull MarkwonTheme.Builder builder) {

											builder.codeBlockMargin(
													(int)
															(context.getResources()
																			.getDisplayMetrics()
																			.density
																	* 10));

											builder.headingTypeface(
													Typeface.create(tf, Typeface.BOLD));
										}
									});

			markwon = builder.build();
		}

		public void setParameters(Context context, String markdown, TextView textView) {
			this.context = context;
			this.markdown = markdown;
			this.textView = textView;
		}

		@Override
		public void run() {
			Objects.requireNonNull(context);
			Objects.requireNonNull(markdown);
			Objects.requireNonNull(textView);

			if (markwon == null) {
				setup();
			}

			Spanned processedMarkdown = markwon.toMarkdown(markdown);

			TextView localReference = textView;
			localReference.post(() -> localReference.setText(processedMarkdown));

			release();
		}

		@Override
		public void release() {
			context = null;
			markdown = null;
			textView = null;

			slot.release(this);
		}
	}

	// Render recyclerview
	private static class RecyclerViewRenderer implements Runnable, Poolable {

		private final Slot slot;

		private Markwon markwon;

		private Context context;
		private String markdown;
		private RecyclerView recyclerView;
		private MarkwonAdapter adapter;
		private ProjectsContext projects;

		private LinkPostProcessor linkPostProcessor;

		public RecyclerViewRenderer(Slot slot) {

			this.slot = slot;
		}

		private void setup() {

			Objects.requireNonNull(context);
			Objects.requireNonNull(projects);

			if (linkPostProcessor == null) {
				linkPostProcessor = new LinkPostProcessor(context.getString(R.string.notes));
				linkPostProcessor.projects = projects;
			}

			final InlineParserFactory inlineParserFactory =
					MarkwonInlineParser.factoryBuilder()
							.addInlineProcessor(new IssueInlineProcessor())
							.addInlineProcessor(new MrInlineProcessor())
							.addInlineProcessor(new UserInlineProcessor())
							.build();

			Markwon.Builder builder =
					Markwon.builder(context)
							.usePlugin(AlertPlugin.create(context))
							.usePlugin(CorePlugin.create())
							.usePlugin(HtmlPlugin.create())
							.usePlugin(LinkifyPlugin.create(true))
							.usePlugin(SoftBreakAddsNewLinePlugin.create())
							.usePlugin(TableEntryPlugin.create(context))
							.usePlugin(
									MovementMethodPlugin.create(TableAwareMovementMethod.create()))
							.usePlugin(TaskListPlugin.create(context))
							.usePlugin(StrikethroughPlugin.create())
							.usePlugin(GlideImagesPlugin.create(context))
							.usePlugin(ImagesPlugin.create())
							.usePlugin(
									new AbstractMarkwonPlugin() {
										@Override
										public void configure(@NonNull Registry registry) {
											registry.require(
													ImagesPlugin.class,
													imagesPlugin ->
															imagesPlugin.addSchemeHandler(
																	OkHttpNetworkSchemeHandler
																			.create(
																					new OkHttpClient())));
										}
									})
							.usePlugin(
									MarkwonHighlighter.create(
											context,
											Theme.getDefaultTheme(context),
											MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE))
							.usePlugin(
									new AbstractMarkwonPlugin() {

										private final Context context =
												RecyclerViewRenderer.this.context;
										private final Typeface tf = Typeface.DEFAULT;

										@Override
										public void beforeSetText(
												@NonNull TextView textView,
												@NonNull Spanned markdown) {

											textView.setTypeface(tf);
											textView.setTextIsSelectable(true);
											textView.setMovementMethod(
													LinkMovementMethod.getInstance());

											if (markdown instanceof Spannable spannable) {
												String content = spannable.toString();

												Pattern htmlShaPattern =
														Pattern.compile(
																"(?<=\\s|^)([a-f0-9]{8})(?=\\s|$)");
												Matcher matcher = htmlShaPattern.matcher(content);

												while (matcher.find()) {
													int start = matcher.start();
													int end = matcher.end();

													LinkSpan[] existingSpans =
															spannable.getSpans(
																	start, end, LinkSpan.class);

													if (existingSpans == null
															|| existingSpans.length == 0) {
														String extractedSha = matcher.group(1);

														LinkSpan shaLinkSpan =
																new LinkSpan(
																		markwon.configuration()
																				.theme(),
																		"labnexcommit://"
																				+ extractedSha,
																		markwon.configuration()
																				.linkResolver());

														spannable.setSpan(
																shaLinkSpan,
																start,
																end,
																Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
													}
												}
											}
											super.beforeSetText(textView, markdown);
										}

										@Override
										public void configureParser(
												@NonNull Parser.Builder builder) {

											builder.inlineParserFactory(inlineParserFactory);
											builder.postProcessor(linkPostProcessor);
										}

										@Override
										public void configureTheme(
												@NonNull MarkwonTheme.Builder builder) {

											builder.codeBlockMargin(
													(int)
															(context.getResources()
																			.getDisplayMetrics()
																			.density
																	* 10));

											builder.headingTypeface(
													Typeface.create(tf, Typeface.BOLD));
										}

										@Override
										public void configureConfiguration(
												@NonNull MarkwonConfiguration.Builder builder) {

											builder.linkResolver(
													(view, link) -> {
														ProjectsContext repoLocal =
																linkPostProcessor.projects;
														String baseInstanceUrl =
																((BaseActivity) context)
																		.getAccount()
																		.getAccount()
																		.getInstanceUrl();

														if (baseInstanceUrl.contains("api/v4/")) {
															baseInstanceUrl =
																	baseInstanceUrl.substring(
																			0,
																			baseInstanceUrl
																					.lastIndexOf(
																							"api/v4/"));
														}

														if (link.startsWith("/")
																&& link.contains("/diffs")) {
															android.net.Uri uri =
																	android.net.Uri.parse(
																			baseInstanceUrl
																					+ link
																							.substring(
																									1));
															String targetSha =
																	uri.getQueryParameter(
																			"start_sha");

															if (targetSha != null
																	&& !targetSha
																			.trim()
																			.isEmpty()) {
																Bundle args = new Bundle();
																args.putLong(
																		"projectId",
																		repoLocal.getProjectId());
																args.putString("sha", targetSha);

																CommitDiffsBottomSheet sheet =
																		new CommitDiffsBottomSheet();
																sheet.setArguments(args);
																sheet.show(
																		((FragmentActivity)
																						view
																								.getContext())
																				.getSupportFragmentManager(),
																		"commitDiffsSheet");
																return;
															}
														}

														if (link.contains("/commit/")
																|| link.contains("/diffs")) {
															android.net.Uri uri =
																	link.startsWith("/")
																			? android.net.Uri.parse(
																					baseInstanceUrl
																							+ link
																									.substring(
																											1))
																			: android.net.Uri.parse(
																					link);
															String targetSha =
																	uri.getQueryParameter(
																			"start_sha");

															if (targetSha == null) {
																targetSha =
																		uri.getLastPathSegment();
															}

															if (targetSha != null
																	&& !targetSha
																			.trim()
																			.isEmpty()) {
																Bundle args = new Bundle();
																args.putLong(
																		"projectId",
																		repoLocal.getProjectId());
																args.putString("sha", targetSha);

																CommitDiffsBottomSheet sheet =
																		new CommitDiffsBottomSheet();
																sheet.setArguments(args);
																sheet.show(
																		((FragmentActivity)
																						view
																								.getContext())
																				.getSupportFragmentManager(),
																		"commitDiffsSheet");
																return;
															}
														}

														if (link.startsWith("labnexuser://")) {
															Intent i =
																	new Intent(
																			view.getContext(),
																			ProfileActivity.class);
															i.putExtra(
																	"username", link.substring(13));
															view.getContext().startActivity(i);

														} else if (link.startsWith(
																"labnexissue://")) {
															link = link.substring(14);
															int issueIid;
															if (link.contains("/")) {
																issueIid =
																		Integer.parseInt(
																				link.split("#")[1]);
															} else {
																issueIid =
																		Integer.parseInt(
																				link.substring(1));
															}
															Intent i =
																	new IssueContext(
																					repoLocal,
																					issueIid, null)
																			.getIntent(
																					view
																							.getContext(),
																					IssueDetailActivity
																							.class);
															view.getContext().startActivity(i);
														} else if (link.startsWith("labnexmr://")) {
															link = link.substring(10);
															int mrIid;
															if (link.contains("/")) {
																mrIid =
																		Integer.parseInt(
																				link.split("!")[1]);
															} else {
																mrIid =
																		Integer.parseInt(
																				link.substring(1));
															}
															Intent i =
																	new MergeRequestContext(
																					repoLocal,
																					mrIid)
																			.getIntent(
																					view
																							.getContext(),
																					MergeRequestDetailActivity
																							.class);
															view.getContext().startActivity(i);
														} else if (link.startsWith(
																"labnexcommit://")) {
															link = link.substring(15);
															String sha =
																	link.contains("/")
																			? link.substring(
																					link
																									.lastIndexOf(
																											'/')
																							+ 1)
																			: link;

															Bundle args = new Bundle();
															args.putLong(
																	"projectId",
																	repoLocal.getProjectId());
															args.putString("sha", sha);

															CommitDiffsBottomSheet sheet =
																	new CommitDiffsBottomSheet();
															sheet.setArguments(args);
															sheet.show(
																	((FragmentActivity)
																					view
																							.getContext())
																			.getSupportFragmentManager(),
																	"commitDiffsSheet");
														} else {
															if (link.startsWith("/")) {
																link =
																		baseInstanceUrl
																				+ link.substring(1);
															}
															Utils.openUrlInBrowser(
																	view.getContext(), link);
														}
													});
											super.configureConfiguration(builder);
										}
									});

			markwon = builder.build();
		}

		private void setupAdapter() {

			adapter =
					MarkwonAdapter.builderTextViewIsRoot(R.layout.custom_markdown_adapter)
							.include(
									TableBlock.class,
									TableEntry.create(
											builder2 ->
													builder2.tableLayout(
																	R.layout.custom_markdown_table,
																	R.id.table_layout)
															.textLayoutIsRoot(
																	R.layout
																			.custom_markdown_adapter)))
							.include(
									FencedCodeBlock.class,
									SimpleEntry.create(
											R.layout.custom_markdown_code_block,
											R.id.textCodeBlock))
							.include(
									AlertPlugin.AlertBlock.class,
									SimpleEntry.create(
											R.layout.custom_markdown_alert_block,
											R.id.textAlertBlock))
							.build();
		}

		public void setParameters(
				Context context,
				String markdown,
				RecyclerView recyclerView,
				ProjectsContext projects) {

			this.context = context;
			this.markdown = markdown;
			this.recyclerView = recyclerView;
			this.projects = projects;
			if (linkPostProcessor != null) {
				linkPostProcessor.projects = projects;
			}
		}

		@SuppressLint("NotifyDataSetChanged")
		@Override
		public void run() {

			Objects.requireNonNull(context);
			Objects.requireNonNull(markdown);
			Objects.requireNonNull(recyclerView);
			Objects.requireNonNull(projects);

			if (markwon == null) {
				setup();
			}

			setupAdapter();

			RecyclerView localReference = recyclerView;
			String localMd = markdown;
			MarkwonAdapter localAdapter = adapter;
			localReference.post(
					() -> {
						localReference.setLayoutManager(
								new LinearLayoutManager(context) {

									@Override
									public boolean canScrollVertically() {

										return false; // disable RecyclerView scrolling, handled by
										// separate ScrollViews
									}
								});
						localReference.setAdapter(localAdapter);

						localAdapter.setMarkdown(markwon, localMd);
						localAdapter.notifyDataSetChanged();
					});

			release();
		}

		@Override
		public void release() {

			context = null;
			markdown = null;
			recyclerView = null;
			adapter = null;
			projects = null;

			slot.release(this);
		}

		private static class IssueInlineProcessor extends InlineProcessor {

			private static final Pattern RE = Pattern.compile("(?<!\\w)#\\d+");

			@Override
			public char specialCharacter() {

				return '#';
			}

			@Override
			protected Node parse() {

				final String id = match(RE);
				if (id != null) {
					Link link = new Link("labnexissue://" + id, null);
					link.appendChild(text(id));
					return link;
				}
				return null;
			}
		}

		private static class UserInlineProcessor extends InlineProcessor {

			private static final Pattern RE = Pattern.compile("(?<!\\w)@\\w+");

			@Override
			public char specialCharacter() {

				return '@';
			}

			@Override
			protected Node parse() {

				final String user = match(RE);
				if (user != null) {
					final Link link =
							new Link("labnexuser://" + user.substring(1 /* remove @ */), null);
					link.appendChild(text(user));
					return link;
				}
				return null;
			}
		}

		private static class MrInlineProcessor extends InlineProcessor {
			private static final Pattern RE = Pattern.compile("!\\d+");

			@Override
			public char specialCharacter() {
				return '!';
			}

			@Override
			protected Node parse() {
				final String id = match(RE);
				if (id != null) {
					Link link = new Link("labnexmr://" + id, null);
					link.appendChild(text(id));
					return link;
				}
				return null;
			}
		}

		private class LinkPostProcessor implements PostProcessor {

			private final String commentText;
			private final Context context;
			private String instanceUrl;
			private ProjectsContext projects;

			public LinkPostProcessor(String commentText) {

				this.commentText = commentText;
				this.context = RecyclerViewRenderer.this.context;
				init();
			}

			private Node insertNode(Node node, Node insertAfterNode) {

				insertAfterNode.insertAfter(node);
				return node;
			}

			private void init() {

				String instanceUrl =
						((BaseActivity) context).getAccount().getAccount().getInstanceUrl();
				instanceUrl =
						instanceUrl
								.substring(0, instanceUrl.lastIndexOf("api/v4/"))
								.replaceAll("\\.", ".");
				this.instanceUrl = instanceUrl;
			}

			@Override
			public Node process(Node node) {

				init();
				AutolinkVisitor autolinkVisitor = new AutolinkVisitor();
				node.accept(autolinkVisitor);
				return node;
			}

			private void link(Text textNode) {
				String literal = textNode.getLiteral();
				Node lastNode = textNode;
				boolean foundAny = false;

				final Pattern patternIssue =
						Pattern.compile(
								instanceUrl
										+ "([^/]+/[^/]+)/(?:issues|pulls)/(\\d+)(?:(?:/#|#)(issue-\\d+|issuecomment-\\d+)|)",
								Pattern.MULTILINE);
				final Matcher matcherIssue = patternIssue.matcher(literal);

				final Pattern patternCommit =
						Pattern.compile(
								instanceUrl + "([^/]+/[^/]+)/commit/([a-z0-9_]+)(?!`|\\)|\\S+)",
								Pattern.MULTILINE);
				final Matcher matcherCommit = patternCommit.matcher(literal);

				final Pattern patternSha = Pattern.compile("(?<=\\s|^)([a-f0-9]{8,40})(?=\\s|$)");
				final Matcher matcherSha = patternSha.matcher(literal);

				boolean hasSha = matcherSha.find();
				boolean hasCommit = matcherCommit.find();
				boolean hasIssue = matcherIssue.find();

				int currentIndex = 0;
				int literalLength = literal.length();

				while (currentIndex < literalLength) {
					int nearestMatchStart = literalLength;
					int nearestMatchEnd = -1;
					int matchType = -1;

					if (hasSha
							&& matcherSha.start() >= currentIndex
							&& matcherSha.start() < nearestMatchStart) {
						nearestMatchStart = matcherSha.start();
						nearestMatchEnd = matcherSha.end();
						matchType = 1;
					}

					if (hasCommit
							&& matcherCommit.start() >= currentIndex
							&& matcherCommit.start() < nearestMatchStart) {
						nearestMatchStart = matcherCommit.start();
						nearestMatchEnd = matcherCommit.end();
						matchType = 2;
					}

					if (hasIssue
							&& matcherIssue.start() >= currentIndex
							&& matcherIssue.start() < nearestMatchStart) {
						nearestMatchStart = matcherIssue.start();
						nearestMatchEnd = matcherIssue.end();
						matchType = 3;
					}

					if (matchType == -1) {
						break;
					}

					foundAny = true;

					if (nearestMatchStart > currentIndex) {
						lastNode =
								insertNode(
										new Text(
												literal.substring(currentIndex, nearestMatchStart)),
										lastNode);
					}

					if (matchType == 1) {
						String fullSha = matcherSha.group(1);
						String shortSha =
								(fullSha != null && fullSha.length() > 8)
										? fullSha.substring(0, 8)
										: fullSha;

						Link linkNode = new Link("labnexcommit://" + fullSha, null);
						linkNode.appendChild(new Text(shortSha));
						lastNode = insertNode(linkNode, lastNode);
						hasSha = matcherSha.find();

					} else if (matchType == 2) {
						String shortSha = matcherCommit.group(2);
						if (shortSha != null) {
							if (shortSha.length() > 10) shortSha = shortSha.substring(0, 10);
							String projectPath = matcherCommit.group(1);
							String displayLabel =
									Objects.equals(projectPath, projects.getProjectName())
											? shortSha
											: projectPath + "/" + shortSha;

							Link linkNode = new Link("labnexcommit://" + displayLabel, null);
							linkNode.appendChild(new Text(displayLabel));
							lastNode = insertNode(linkNode, lastNode);
						}

						hasCommit = matcherCommit.find();

					} else {
						String issueNum = matcherIssue.group(2);
						String projectPath = matcherIssue.group(1);
						String displayLabel =
								Objects.equals(projectPath, projects.getProjectName())
										? "#" + issueNum
										: projectPath + "#" + issueNum;

						Link linkNode = new Link("labnexissue://" + displayLabel, null);
						linkNode.appendChild(new Text(displayLabel));
						lastNode = insertNode(linkNode, lastNode);

						String anchor = matcherIssue.group(3);
						if (anchor != null && anchor.startsWith("issuecomment-")) {
							lastNode = insertNode(new Text(" "), lastNode);
							Link linkCommentNode = new Link(matcherIssue.group(), null);
							linkCommentNode.appendChild(new Text("(" + commentText + ")"));
							lastNode = insertNode(linkCommentNode, lastNode);
						}

						hasIssue = matcherIssue.find();
					}

					currentIndex = nearestMatchEnd;

					if (hasSha && matcherSha.start() < currentIndex) hasSha = matcherSha.find();
					if (hasCommit && matcherCommit.start() < currentIndex)
						hasCommit = matcherCommit.find();
					if (hasIssue && matcherIssue.start() < currentIndex)
						hasIssue = matcherIssue.find();
				}

				if (foundAny && currentIndex < literalLength) {
					insertNode(new Text(literal.substring(currentIndex)), lastNode);
				}

				if (foundAny) {
					textNode.unlink();
				}
			}

			private void linkifyImage(Image node) {

				final Matcher patternAttachments =
						Pattern.compile("(/uploads/\\S+)", Pattern.MULTILINE)
								.matcher(node.getDestination());
				if (patternAttachments.matches()) {
					node.setDestination(
							instanceUrl
									+ "-/project/"
									+ projects.getProjectId()
									+ patternAttachments.group(1));
				}
			}

			private class AutolinkVisitor extends AbstractVisitor {

				int inLink = 0;

				@Override
				public void visit(Link link) {

					inLink++;
					super.visit(link);
					inLink--;
				}

				@Override
				public void visit(Image image) {

					super.visit(image);
					linkifyImage(image);
				}

				@Override
				public void visit(Text text) {

					if (inLink == 0) {
						link(text);
					}
				}
			}
		}
	}
}

package io.cloudpipelines.projectcrawler;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry class that wraps around all available implementations
 * of repository managers.
 *
 * You can extend the available list by using the {@link ServiceLoader}
 * and putting the {@link RepositoryManagementBuilder} implementation in the
 * {@code /META-INF/services/io.cloudpipelines.projectcrawler.RepositoryManagementBuilder} file
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public final class ProjectCrawler implements RepositoryManagement {

	private final Options options;
	private static final ServiceLoader<RepositoryManagementBuilder> LOADED = ServiceLoader
			.load(RepositoryManagementBuilder.class);
	private static final List<RepositoryManagementBuilder> DEFAULT_BUILDERS
			= Arrays.asList(
					new GithubRepositoryManagementBuilder(),
					new GitlabRepositoryManagementBuilder()
	);

	public ProjectCrawler(Options options) {
		this.options = options;
	}

	@Override public List<Repository> repositories(String org) {
		return firstMatching().repositories(org);
	}

	@Override public String fileContent(String org, String repo, String branch,
			String filePath) {
		return firstMatching().fileContent(org, repo, branch, filePath);
	}

	private RepositoryManagement firstMatching() {
		RepositoryManagement management = firstMatching(LOADED);
		if (management != null) {
			return management;
		}
		management = firstMatching(DEFAULT_BUILDERS);
		if (management == null) {
			throw new IllegalStateException("Nothing is matching the root url [" + this.options.rootUrl + "]");
		}
		return management;
	}
	private RepositoryManagement firstMatching(Iterable<RepositoryManagementBuilder> builders) {
		for (RepositoryManagementBuilder builder : builders) {
			RepositoryManagement management = builder.build(this.options);
			if (management != null) {
				return management;
			}
		}
		return null;
	}
}

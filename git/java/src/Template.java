import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;

public class Template {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Git git = Git.open(new File(args[0]));
		RevCommit headCommit = git.log().setMaxCount(1).call().iterator()
				.next();
		System.out.println("repo:" + git.getRepository()
				+ ", head-commit:" + headCommit);
	}
}

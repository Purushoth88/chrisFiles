import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

public class CheckForCleanState_FindCommitForPath {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		
		Git git = Git.open(new File(args[0]));
		if (!git.status().call().isClean())
			throw new IllegalStateException("Repo state is not clean.");
		// find last commit
		for (RevCommit commit : git.log().addPath(args[1]).setMaxCount(1).call())
			System.out.println("A commit touching path "+args[1]+" in repo "+git+": "+commit.getId());
	}
}

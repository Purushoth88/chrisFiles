import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class MergeWithSpecificMergeMessage {
	public static void main(String args[]) throws IOException, GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CreateRepoWithEmptyCommits_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);

		Git git = Git.init().setDirectory(tmpDir).call();
		RevCommit initialCommit = git.commit().setMessage("initial empty commit").call();
		RevCommit commitOnMaster = git.commit().setMessage("next empty commit").call();
		git.checkout().setCreateBranch(true).setName("side").setStartPoint(initialCommit).call();
		git.commit().setMessage("next empty commit on side").call();

		git.merge().include(commitOnMaster).setCommit(false).call();
		git.commit().setMessage("my special merge commit message").call();
		
		for (RevCommit c : git.log().all().call())
			System.out.println(c + ": " + c.getShortMessage());
	}
}

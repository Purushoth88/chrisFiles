import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class DetermineWetherOneCommitIsMergedIntoAnother {
	public static void main(String args[]) throws IOException, GitAPIException {
		// create a temporary directory for our test repo
		File baseDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTests_" + System.currentTimeMillis());
		if (!baseDir.mkdir())
			throw new IOException("Couldn't create temporary directory "
					+ baseDir.getPath());

		Git git = Git.init().setDirectory(baseDir).call();
		RevCommit firstCommit = git.commit().setMessage("initial empty commit")
				.call();
		git.commit().setMessage("next empty commit").call();
		ObjectId master = git.getRepository().resolve("refs/heads/master");

		// Alternative 1: with git log
		boolean merged = false;
		for (RevCommit c : git.log().add(master).call())
			if (c.getId().equals(firstCommit.getId())) {
				merged = true;
				break;
			}
		System.out
				.println("in repo " + git.getRepository().getWorkTree()
						+ " commit " + firstCommit.getId() + " is "
						+ (merged ? "merged" : "not merged")
						+ " int refs/heads/master");

		// Alternative 2: with RevWalk
		System.out.println("in repo " + git.getRepository().getWorkTree()
				+ " commit " + firstCommit.getId() + " is "
				+ (isMergedInto(git.getRepository(), firstCommit.getName(), "master") ? "merged" : "not merged")
				+ " in refs/heads/master (" + master.getName() + ")");
		System.out.println("in repo " + git.getRepository().getWorkTree()
				+ " refs/heads/master (" + master.getName() + ")"
				+ (isMergedInto(git.getRepository(), "master", firstCommit.getName()) ? "merged" : "not merged")
				+ " commit " + firstCommit.getId() + " is ");
	}
	
	static boolean isMergedInto(Repository repo, String a, String b) throws MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, IOException {
		RevWalk rw = new RevWalk(repo);
		try {
			return rw.isMergedInto(rw.parseCommit(repo.resolve(a+"^{commit}")), rw.parseCommit(repo.resolve(b+"^{commit}")));
		} finally {
			rw.release();
		}
	}
}

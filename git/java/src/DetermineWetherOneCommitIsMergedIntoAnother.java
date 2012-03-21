import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

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

		boolean merged = false;
		for (RevCommit c : git.log()
				.add(git.getRepository().resolve("refs/heads/master")).call())
			if (c.getId().equals(firstCommit.getId())) {
				merged = true;
				break;
			}

		System.out
				.println("in repo " + git.getRepository().getWorkTree()
						+ " commit " + firstCommit.getId() + " is "
						+ (merged ? "merged" : "not merged")
						+ " int refs/heads/master");
	}

//	public static void main2(String args[]) throws IOException, GitAPIException {
//		// create a temporary directory for our test repo
//		File baseDir = new File(System.getProperty("java.io.tmpdir"),
//				"JGitTests_" + System.currentTimeMillis());
//		if (!baseDir.mkdir())
//			throw new IOException("Couldn't create temporary directory "
//					+ baseDir.getPath());
//		// baseDir.deleteOnExit();
//
//		Git git = Git.init().setDirectory(baseDir).call();
//		RevWalk rw = new RevWalk(git.getRepository());
//		try {
//			RevCommit firstCommit = rw.lookupCommit(git.commit()
//					.setMessage("initial empty commit").call());
//			git.commit()
//					.setMessage("next empty commit").call();
//
//			Repository repository = git.getRepository();
//			RevCommit masterCommit = rw.lookupCommit(repository
//					.resolve("refs/heads/master"));
//			boolean isMerged = rw.isMergedInto(firstCommit, masterCommit);
//
//			System.out.println("in repo " + git.getRepository().getWorkTree()
//					+ " commit " + firstCommit.getId() + " is "
//					+ (isMerged ? "merged" : "not merged")
//					+ " int refs/heads/master (" + masterCommit.getId() + ")");
//		} finally {
//			rw.release();
//		}
//	}
}

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

public class TestRevwalkSort {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		try (Git git = Git.open(new File(args[0]))) {
			System.out.println("Opened repo " + git.getRepository());
			RevWalk rw = new RevWalk(git.getRepository());
			Iterable<RevCommit> commits = git.log().call();
			RevCommit headCommit = commits.iterator().next();
			rw.markStart(headCommit);
			rw.sort(RevSort.TOPO);
			rw.setRetainBody(true);
			for(;;) {
				RevCommit next = rw.next();
				System.out.println("found commit:" + next.getShortMessage()+ " ("+next.getName()+")");
			}
		}
	}
}

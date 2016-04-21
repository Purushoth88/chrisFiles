
import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

public class RevWalkTests {
	public static void main(String args[]) throws IOException, GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_DetermineWetherOneCommitIsMergedIntoAnother_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: " + tmpDir);

		Git git = Git.init().setDirectory(tmpDir).call();

		RevCommit c1 = git.commit().setMessage("1").call();
		RevCommit c2 = git.commit().setMessage("2").call();
		RevCommit c3 = git.commit().setMessage("3").call();
		RevCommit c4 = git.commit().setMessage("4").call();

		Ref side = git.branchCreate().setName("side").setStartPoint(c2).call();
		git.checkout().setName(side.getName()).call();

		RevCommit ca = git.commit().setMessage("a").call();
		RevCommit cb = git.commit().setMessage("b").call();

		try (RevWalk rw = new RevWalk(git.getRepository())) {
			rw.markStart(rw.lookupCommit(c4));
			rw.setRevFilter(new InBetweenRevFilter(c2, c4));
			for (RevCommit curr; (curr = rw.next()) != null;)
				System.out.println("Inspecting entry: " + curr.getShortMessage());
		}
	}
}

class InBetweenRevFilter extends RevFilter {
	private RevCommit end;
	private RevCommit begin;

	public InBetweenRevFilter(RevCommit begin, RevCommit end) {
		this.begin = begin;
		this.end = end;
	}

	@Override
	public RevFilter clone() {
		return this;
	}

	@Override
	public boolean include(RevWalk walker, RevCommit c)
			throws StopWalkException, MissingObjectException, IncorrectObjectTypeException, IOException {
		return (walker.isMergedInto(begin, c) && walker.isMergedInto(c, end));
	}
}

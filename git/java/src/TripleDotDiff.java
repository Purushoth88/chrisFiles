import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class TripleDotDiff {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		try (Git git = Git.open(new File("/home/chris/git/dondalfi"));
				ObjectReader reader = git.getRepository().newObjectReader();
				RevWalk rw = new RevWalk(git.getRepository())) {
			Repository repo = git.getRepository();
			RevCommit commitA = rw.parseCommit(repo
					.resolve("refs/remotes/origin/Multiplyer"));
			RevCommit commitB = rw
					.parseCommit(repo.resolve("refs/remotes/origin/Adder"));
			rw.markStart(commitA);
			rw.markStart(commitB);
			rw.setRevFilter(RevFilter.MERGE_BASE);
			RevCommit base = rw.parseCommit(rw.next());

			CanonicalTreeParser aParser = new CanonicalTreeParser();
			aParser.reset(reader, base.getTree());
			CanonicalTreeParser bParser = new CanonicalTreeParser();
			bParser.reset(reader, commitB.getTree());
			for (DiffEntry diff : git.diff().setOldTree(aParser)
					.setNewTree(bParser).call()) {
				System.out.println(diff.toString());
			}
		}
	}
}

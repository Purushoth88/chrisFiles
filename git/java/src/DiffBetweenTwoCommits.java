import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class DiffBetweenTwoCommits {

	public static void main(String args[]) throws IOException, GitAPIException {
		if (args.length < 2 || args.length > 3) {
			System.err.println(
					"DiffBetweenTwoCommits: invalid number of argurments\nUsage: java DiffBetweenTwoCommits <pathToRepo> <refToCommitA> [<refToCommitB>]");
			System.exit(1);
		}

		String pathToRepo = args[0];
		String referenceToCommitA = args[1];
		String referenceToCommitB = (args.length == 2) ? "HEAD" : args[2];

		Git git = Git.open(new File(pathToRepo));
		Repository db = git.getRepository();
		try (ObjectReader or = db.newObjectReader(); RevWalk rw = new RevWalk(db)) {
			final ObjectId srcTreeId = db.resolve(referenceToCommitA + "^{tree}");
			final ObjectId tgtTreeId = db.resolve(referenceToCommitB + "^{tree}");
			final CanonicalTreeParser srcTreeParser = new CanonicalTreeParser();
			srcTreeParser.reset(or, rw.parseTree(srcTreeId));
			final CanonicalTreeParser tgtTreeParser = new CanonicalTreeParser();
			tgtTreeParser.reset(or, rw.parseTree(tgtTreeId));

			List<DiffEntry> diffs = git.diff().setOldTree(srcTreeParser).setNewTree(tgtTreeParser).call();
			for (DiffEntry diff : diffs)
				System.out.printf("changeType: %s, old path(mode): %s(%s), new path(mode): %s(%s)\n",
						diff.getChangeType(), diff.getOldPath(), diff.getOldMode(), diff.getNewPath(),
						diff.getNewMode());
		}
	}
}

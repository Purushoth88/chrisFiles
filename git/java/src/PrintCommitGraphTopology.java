import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

public class PrintCommitGraphTopology {
	static List<RevCommit> commits = new LinkedList<RevCommit>();

	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Repository db = Git.open(new File(args[0])).getRepository();
		RevWalk rw = new RevWalk(db.newObjectReader());
		rw.sort(RevSort.COMMIT_TIME_DESC);
		rw.sort(RevSort.REVERSE, true);

		Set<RevCommit> referencedCommits = new HashSet<RevCommit>();
		for (AnyObjectId id : db.getAllRefsByPeeledObjectId().keySet()) {
			RevObject ro = rw.parseAny(id);
			if (ro instanceof RevCommit)
				referencedCommits.add((RevCommit) ro);
		}

		rw.markStart(referencedCommits);
		StringBuilder desc = new StringBuilder();
		RevCommit last = null;
		for (RevCommit c = rw.next(); c != null; c = rw.next()) {
			commits.add(0, c);
			int np = c.getParentCount();
			if (np > 1 || (np == 1 && !c.getParent(0).equals(last)))
				for (int i = 0; i < np; i++) {
					int idx = commits.indexOf(c.getParent(i));
					if (idx >= Character.MAX_RADIX)
						desc.append('[');
					desc.append(Integer.toString(idx, Character.MAX_RADIX));
					if (idx >= Character.MAX_RADIX)
						desc.append(']');
				}
			desc.append('*');
			if (referencedCommits.contains(c))
				desc.append("!");
			last = c;
		}
		System.out.println("processed " + commits.size() + " commits in repo "
				+ db.getDirectory() + ".");
		System.out.println(desc.toString());
	}
}

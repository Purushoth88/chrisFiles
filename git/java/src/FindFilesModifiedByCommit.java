import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class FindFilesModifiedByCommit {
	public static void main(String args[]) throws IOException, GitAPIException, JGitInternalException {
		Git r = Git.open(new File(args[0]));
		try (RevWalk rw = new RevWalk(r.getRepository())) {
			RevCommit commitToCheck = (RevCommit) rw.parseAny(r.getRepository().resolve(args[1]));
			System.out.println("Inspecting commit " + args[1] + " with id:" + commitToCheck.getId() + " in repo "+r.getRepository().getDirectory());
			try (TreeWalk tw = new TreeWalk(r.getRepository())) {
				tw.setRecursive(true);
				tw.addTree(commitToCheck.getTree());
				// tw.setFilter(TreeFilter.ANY_DIFF);
				for (RevCommit parent : commitToCheck.getParents()) {
					rw.parseCommit(parent);
					System.out.println("Adding parent commit " + parent.getId());
					tw.addTree(parent.getTree());
				}
				while (tw.next()) {
					int similarParents = 0;
					for (int i = 1; i < tw.getTreeCount(); i++)
						if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
							similarParents++;
					if (similarParents == 0)
						System.out.println("modified path:" + tw.getPathString());
				}
			}
		}
	}
}

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class FindFilesAddedByCommit {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		int i = 0;
		Git r = Git.open(new File(args[0]));
		
		System.out.println("Hello");
		RevWalk rw = new RevWalk(r.getRepository());
		TreeWalk tw = new TreeWalk(r.getRepository());
		RevCommit commitToCheck = (RevCommit) rw.parseAny(r.getRepository().resolve(args[1]));
		tw.addTree(commitToCheck.getTree());
		for (RevCommit parent : commitToCheck.getParents()) {
			rw.parseCommit(parent);
			tw.addTree(parent.getTree());
		}
		tw.setRecursive(true);
		while (tw.next()) 
			if (pathWasAddedByTree0(tw))
				System.out.println("Commit has added path: "+tw.getPathString());
	}

	public static boolean pathWasAddedByTree0(TreeWalk tw) throws IOException {
		if (tw.getFileMode(0)!=FileMode.MISSING)
			for (int i=1; i<tw.getTreeCount(); i++)
				if (tw.getFileMode(i)!=FileMode.MISSING)
					return false;
		return true;
	}
}

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class ListContentOfCommit {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Git r = Git.open(new File(args[0]));
		RevWalk rw = new RevWalk(r.getRepository());
		TreeWalk tw = new TreeWalk(r.getRepository());
		RevCommit commitToCheck = rw.parseCommit(r.getRepository().resolve(
				args[1]));
		tw.addTree(commitToCheck.getTree());
		tw.setRecursive(true);
		while (tw.next()) {
			System.out.println("path: " + tw.getPathString() + ", mode: "
					+ tw.getFileMode(0) + ", oid:" + tw.getObjectId(0));
		}
	}
}

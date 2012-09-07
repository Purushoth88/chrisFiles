import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

public class FindAllFilesModifiedInCertainCommit {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		int i = 0;
		Git r = Git.open(new File(args[0]));
		for (RevCommit c : r.log().addPath(args[1]).call()) {
			if (i++ > Integer.parseInt(args[2]))
				break;
			for (DiffEntry e : r.diff().setNewTree(c.getTree().getId()).setOldTree(null)
					.call())
				System.out.println(e);
		}
	}
}

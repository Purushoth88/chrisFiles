import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;

public class CheckStatusOfSingleFile {
	/**
	 * @param args
	 *            args[0] is the path to working tree directory. args[1] is the
	 *            path (relative to root of repo) of the file to be checked
	 */
	public static void main(String args[]) throws IOException, GitAPIException {
		Git git = Git.open(new File(args[0]));
		IndexDiff diff = new IndexDiff(git.getRepository(), "HEAD",
				new FileTreeIterator(git.getRepository()));
		diff.setFilter(PathFilterGroup.createFromStrings(args[1]));
		diff.diff();
		if (!diff.getAdded().isEmpty())
			System.out.println("file was added");
		if (!diff.getChanged().isEmpty())
			System.out.println("file was changed");
		// ...
	}
}

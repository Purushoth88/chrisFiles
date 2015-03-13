import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

public class Bug457152 {
	public static void main(String[] args) throws IOException {
		final Repository repository = FileRepositoryBuilder.create(new File(
				"/home/chris/git/support/git-svn-tilda/.git"));
		TreeWalk treeWalk = new TreeWalk(repository);
		final DirCache dirCache = repository.readDirCache();
		treeWalk.addTree(new DirCacheIterator(dirCache));
		while (treeWalk.next())
			System.out.println("Treewalk on DirCache is on: "+treeWalk.getPathString());
	}
}

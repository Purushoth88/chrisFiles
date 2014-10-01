import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;

public class GitDiffReplacement {
	public static void main(String args[]) throws IOException, GitAPIException {
		Git git = Git.open(new File(args[0]));
		Repository repo = git.getRepository();

//		DirCacheIterator index = new DirCacheIterator(repo.readDirCache());
//		FileTreeIterator workingTree = new FileTreeIterator(repo);
//		DiffFormatter fmt = new DiffFormatter(System.out);
//		fmt.setRepository(repo);
//		fmt.format(index, workingTree);
//		fmt.flush();
//		fmt.release();
		
		DirCacheIterator dci = new DirCacheIterator(repo.readDirCache());
		FileTreeIterator fti = new FileTreeIterator(repo);
		DiffFormatter df = new DiffFormatter(System.out);
		df.setRepository(repo);
		List<DiffEntry> entries = df.scan(dci, fti);
		df.format(entries);
	}
}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

public class CommitIgnored {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_" + CommitIgnored.class.getName() + "_"
						+ System.currentTimeMillis());
		tmpDir.mkdirs();
		try {
			Git git = Git.init().setDirectory(tmpDir).setBare(false).call();
			write(new File(tmpDir, ".gitignore"), "bar");
			File subDir = new File(tmpDir, "subdir");
			subDir.mkdir();
			write(new File(subDir, "bar"), "Hello World");
			write(new File(subDir, "foo"), "Hello World");

			git.add().addFilepattern("subdir").call();
			git.commit().setOnly("subdir").setMessage("First commit").setCommitter("Foo Bar",
			 "foobar@localhost").call();
//			git.commit().setMessage("First commit")
//					.setCommitter("Foo Bar", "foobar@localhost").call();

			RevCommit headCommit = git.log().setMaxCount(1).call().iterator()
					.next();
			System.out.println("repo:" + git.getRepository() + ", head-commit:"
					+ headCommit);

			Repository r = git.getRepository();
			try (TreeWalk tw = new TreeWalk(r)) {
				tw.addTree(headCommit.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					System.out.println("path: " + tw.getPathString()
							+ ", mode: " + tw.getFileMode(0) + ", oid:"
							+ tw.getObjectId(0));
				}

			}
		} finally {
			// rm(tmpDir);
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}

	static void write(File f, String d) throws NoWorkTreeException, IOException {
		try (FileWriter fw = new FileWriter(f)) {
			fw.write(d);
		}
	}
}

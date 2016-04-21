import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;

public class TestFilter {
	public static void main(String args[])
			throws IOException, GitAPIException, JGitInternalException, InterruptedException {
		Path tmp = Files.createTempDirectory("JGitTest_" + TestFilter.class.getName());
		Path bobFile = tmp.resolve("bob.txt");
		try (Git git = Git.init().setDirectory(tmp.toFile()).call()) {
			Repository repo = git.getRepository();
			System.out.println("Repo created in " + tmp);
			StoredConfig config = repo.getConfig();
			config.setString("filter", "abCap", "clean", "sed s/a/AB/g && echo $(date -R): clean %f >>/tmp/a");
			config.setString("filter", "abCap", "smudge", "sed s/A/ab/g && echo $(date -R): smduge %f >>/tmp/a");
			config.save();

			Files.write(tmp.resolve(".gitattributes"), "*.txt filter=abCap".getBytes());
			git.add().addFilepattern(".gitattributes").call();
			RevCommit firstCommit = git.commit().setMessage("add .gitignore").call();

			Files.write(bobFile, "abcABC".getBytes());
			Thread.sleep(2100);
			git.add().addFilepattern("bob.txt").call();
			System.out.println("index: " + index(git, "bob.txt") + ", workTree: "
					+ ((Files.exists(bobFile)) ? new String(Files.readAllBytes(bobFile)) : "<none>"));

			RevCommit secondCommit = git.commit().setMessage("add bob.txt").call();
			System.out.println("secondCommit: " + secondCommit.getName());
			git.checkout().setName(firstCommit.getName()).call();
			System.out.println("index: " + index(git, "bob.txt") + ", workTree: "
					+ ((Files.exists(bobFile)) ? new String(Files.readAllBytes(bobFile)) : "<none>"));
			git.checkout().setName(secondCommit.getName()).call();
			System.out.println("index: " + index(git, "bob.txt") + ", workTree: "
					+ ((Files.exists(bobFile)) ? new String(Files.readAllBytes(bobFile)) : "<none>"));
		}
	}

	public static String index(Git git, String path) throws NoWorkTreeException, CorruptObjectException, IOException {
		Repository repo = git.getRepository();
		DirCacheEntry dce = repo.readDirCache().getEntry(path);
		return (dce == null) ? "<null>" : new String(repo.open(dce.getObjectId(), Constants.OBJ_BLOB).getCachedBytes());
	}
}

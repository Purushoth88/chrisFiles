import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class CheckoutWithDirtyFiles {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Path tmp = Files.createTempDirectory("JGitTest_"
				+ CheckoutWithDirtyFiles.class.getName());
		try (Git git = Git.init().setDirectory(tmp.toFile()).call()) {
			// Prepare a repo with two branches
			System.out.println("Repo created in " + tmp);
			Files.write(tmp.resolve("a"), "a".getBytes());
			git.add().addFilepattern("a").call();
			git.commit().setMessage("add a").call();
			git.branchCreate().setName("refs/heads/side").call();

			git.checkout().setName("refs/heads/side").call();
			Files.write(tmp.resolve("a"), "a-side".getBytes());
			git.commit().setAll(true).setMessage("modified a on side").call();
			Files.write(tmp.resolve("a"), "a-dirty".getBytes());

			// if you comment the following block the checkout will fail, if its
			// in the checkout succeeds.
			DirCache dc = git.getRepository().lockDirCache();
			DirCacheEntry entry = dc.getEntry(0);
			entry.setAssumeValid(true);
			dc.write();
			dc.commit();

			Ref call = git.checkout().setName("refs/heads/master").call();

			for (RevCommit c : git.log().call())
				System.out.println("Commit:" + c.getShortMessage() + " ("
						+ c.getName() + ")");
		} finally {
			Files.walkFileTree(tmp, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult postVisitDirectory(Path dir,
						IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}
}

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

public class Template_cloneRepoIntoTmp {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Path tmp = Files.createTempDirectory("JGitTest_"
				+ Template_cloneRepoIntoTmp.class.getName());
		try (Git git = Git.cloneRepository().setDirectory(tmp.toFile())
				.setURI(args[0]).setBare(false).call()) {
			System.out.println("Repo " + args[0] + " cloned to " + tmp);
			for (RevCommit c : git.log().setMaxCount(10).call())
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

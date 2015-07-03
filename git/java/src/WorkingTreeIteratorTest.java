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
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

public class WorkingTreeIteratorTest {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Path tmp = Files.createTempDirectory("JGitTest_"
				+ WorkingTreeIteratorTest.class.getName());
		Files.createDirectories(tmp.resolve("src/subm"));
		try (Git subGit = Git.init().setDirectory(tmp.resolve("src/subm").toFile()).call(); Git rootGit = Git.init().setDirectory(tmp.toFile()).call()) {
			Files.write(tmp.resolve("src/subm/c.txt"), "c".getBytes());
			subGit.add().addFilepattern(".").call();
			subGit.commit().setMessage("added c.txt to submodule").call();
			System.out.println("Submodulerepo created and filled in " + subGit.getRepository().getWorkTree());

			Files.write(tmp.resolve(".gitattributes"), "*.txt text".getBytes());
			Files.write(tmp.resolve("a.txt"), "a".getBytes());
			Files.write(tmp.resolve("src/b.txt"), "a".getBytes());
			rootGit.add().addFilepattern(".").call();
			rootGit.commit().setMessage("added a.txt and .gitattributes to root git").call();
			System.out.println("Submodulerepo created and filled in " + subGit.getRepository().getWorkTree());
			for (RevCommit c : rootGit.log().call())
				System.out.println("Commit:" + c.getShortMessage()+ " ("+c.getName()+")");
			
			Path submGit = tmp.resolve("src/subm/.git");
			Files.move(submGit, submGit.resolveSibling(".git2"));
			
			TreeWalk tw = new TreeWalk(rootGit.getRepository());
			tw.addTree(new FileTreeIterator(rootGit.getRepository()));
			tw.setRecursive(false);
			while(tw.next()) {
				System.out.println("path: "+tw.getPathString()+", mode:"+tw.getFileMode(0));
				if (tw.isSubtree()) {
					tw.enterSubtree();
				}
			}
		} finally {
			Files.walkFileTree(tmp, new SimpleFileVisitor<Path>() {
			   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			       Files.delete(file);
			       return FileVisitResult.CONTINUE;
			   }
			   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			       Files.delete(dir);
			       return FileVisitResult.CONTINUE;
			   }
			});			
		}
	}
}

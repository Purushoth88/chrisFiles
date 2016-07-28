import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.revwalk.RevCommit;

public class AddPerformance {
	public static void main(String args[]) throws Exception {
		int NR_OR_FILES = 100000;
		Path root;
		long content = 0;
		boolean explicitAdd=true;

		root = Files.createTempDirectory("JGitTest_" + AddPerformance.class.getName());
		try (Git git = Git.init().setDirectory(root.toFile()).call()) {
			for (int f = 0; f < NR_OR_FILES; f++) {
				Path path = root.resolve(path(f));
				Files.createDirectories(path.getParent());
				Files.write(path, Long.toString(content++).getBytes());
			}
			git.add().addFilepattern(".").call();
			git.commit().setMessage("initial").call();
			System.out.println("Created a repo with " + (1000 * NR_OR_FILES) + " files in " + root);
			int NR_OF_FILESTOMODIFY = 100;
			int NR_OF_ROUNDS = 10;
			Path root1 = git.getRepository().getWorkTree().toPath();
			long content1 = (long) (Math.random()*10000);
			
			for (int round = 0; round < NR_OF_ROUNDS; round++) {
				AddCommand add = git.add();
				for (int fileToModify = 0; fileToModify < NR_OF_FILESTOMODIFY; fileToModify++) {
					Files.write(root1.resolve(path(fileToModify)), Long.toString(content1++).getBytes());
				}
				long start = System.currentTimeMillis();
				if (explicitAdd)
					for (int fileToModify = 0; fileToModify < NR_OF_FILESTOMODIFY; fileToModify++)
						add.addFilepattern(path(fileToModify));
				else
					add.addFilepattern(".");
				add.call();
				git.commit().setMessage("round " + round).call();
				System.out
						.println("Time for adding (explicitAdd=" + explicitAdd + ") and committing " + NR_OF_FILESTOMODIFY
								+ " modified files in round " + round + ":" + (System.currentTimeMillis() - start) + "ms");
			}

		} finally {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
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

	static String path(int i) throws Exception {
		return (i / 1000) + "/" + (i % 1000);
	}
}

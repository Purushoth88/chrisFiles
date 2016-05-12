import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class DeleteWorktree {
	public static void main(final String[] args) throws IOException {
		final Repository repo = new RepositoryBuilder().setWorkTree(new File(args[0])).setMustExist(true).build();
		Files.walkFileTree(repo.getWorkTree().toPath(), new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return dir.equals(repo.getDirectory().toPath()) ? FileVisitResult.SKIP_SUBTREE
						: FileVisitResult.CONTINUE;
			}
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (!dir.equals(repo.getWorkTree().toPath()))
					Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}

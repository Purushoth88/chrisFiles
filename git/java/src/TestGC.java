import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.lib.ObjectId;

public class TestGC {
	public static void main(String args[]) throws GitAPIException,
			MissingObjectException, IOException, InterruptedException {
		File f = new File("gitGCTest.git");
		Git git;
		if (f.isDirectory())
			git = Git.open(f);
		else {
			git = Git.init().setDirectory(f).call();

			git.commit().setMessage("testCommit1").call();
			git.commit().setMessage("testCommit2").call();
			git.commit().setMessage("testCommit3").call();
			git.commit().setMessage("testCommit4").call();
			git.commit().setMessage("testCommit5").call();

			git.branchCreate().setName("branch1").call();
			git.branchCreate().setName("branch2").call();
			git.branchCreate().setName("branch3").call();
			git.branchCreate().setName("branch4").call();
			git.branchCreate().setName("branch5").call();
		}

		System.out.println("start working on repo "
				+ git.getRepository().getWorkTree().getAbsolutePath() + " at "
				+ new Date());

		new Thread() {
			@Override
			public void run() {
				try {
					Git otherGit = Git.open(new File("gitGCTest.git"));
					int i = 0;
					while (true) {
						GC gc = new GC((FileRepository) otherGit.getRepository());
						gc.repack();
						i++;
						if (i % 5000 == 0)
							System.out.println("Repacked " + i + " times");
					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}.start();

		long i = 0;
		ObjectId headCommitId = git.getRepository().getRef("branch1")
				.getObjectId();
		while (true) {
			if (git.getRepository().open(headCommitId) == null) {
				System.out.println("ERROR!!! at run " + i);
				Thread.sleep(1000);
				git = Git.open(new File("gitGCTest.git"));
				if (git.getRepository().open(headCommitId) == null)
					System.out.println("ERROR!!! even after sleeping and reopening.");
			}
			i++;
			if (i % 5000000 == 0)
				System.out.println("Looked up " + i + " times");
		}
	}
}

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.revwalk.RevCommit;

public class TryGC {
	public static void main(String args[]) throws IOException, GitAPIException {
		// create a temporary directory for our test repo
		File baseDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTests_" + System.currentTimeMillis());
		if (!baseDir.mkdir())
			throw new IOException("Couldn't create temporary directory "
					+ baseDir.getPath());
		System.out.println("Created a repo in " + baseDir);

		Git git = Git.init().setDirectory(baseDir).call();
		git.commit().setMessage("initial empty commit").call();
		git.commit().setMessage("next empty commit").call();
		for (RevCommit c : git.log().all().call())
			System.out.println(c + ": " + c.getShortMessage());
		GC gc = new GC((FileRepository) git.getRepository());
		GC.RepoStatistics stats= gc.getStatistics();
	}
}

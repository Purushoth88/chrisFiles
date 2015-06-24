import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;

public class CloseResourcesAfterRevWalk {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Path tmp = Files.createTempDirectory("JGitTest_"
				+ CloseResourcesAfterRevWalk.class.getName());
		try (Git git = Git.init().setDirectory(tmp.toFile()).call()) {
			System.out.println("Repo created in " + tmp);
			FileUtils.createNewFile(new File(git.getRepository().getWorkTree(), "foo"));
			git.add().addFilepattern("foo").call();
			git.commit().setMessage("add foo").call();
			for (RevCommit c : git.log().call())
				System.out.println("Found commit:" + c.getShortMessage()+ " ("+c.getName()+")");
			rm(tmp.toFile());
		} finally {
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}
}

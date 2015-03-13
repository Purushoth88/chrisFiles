import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

public class Template {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Path tmp = Files.createTempDirectory("JGitTest_"
				+ Template.class.getName());
		try (Git git = Git.cloneRepository().setDirectory(tmp.toFile())
				.setURI(args[0]).setBare(false).call()) {
			System.out.println("Repo " + args[0] + " clone to " + tmp);
			for (RevCommit c : git.log().setMaxCount(10).call())
				System.out.println("Commit:" + c.getShortMessage()+ " ("+c.getName()+")");
		} finally {
			rm(tmp.toFile());
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}
}

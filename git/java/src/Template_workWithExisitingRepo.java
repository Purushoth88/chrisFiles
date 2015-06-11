import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

public class Template_workWithExisitingRepo {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		try (Git git = Git.open(new File(args[1]))) {
			System.out.println("opened repo at "+git.getRepository().getDirectory());
			for (RevCommit c : git.log().setMaxCount(10).call())
				System.out.println("Commit:" + c.getShortMessage()+ " ("+c.getName()+")");
		}
	}
}

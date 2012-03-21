import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;

public class CloneValidation {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		System.out.println("Start cloning repo");
		Git r = Git.cloneRepository().setDirectory(new File("C:/tmp/abc")).setURI("c:/git/NGP/LDI/validation.git").setProgressMonitor(new TextProgressMonitor()).call();
		System.out.println("cloned the repo");
	}
}

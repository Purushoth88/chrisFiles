import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

public class JGitTestSshCloneWithOwnSessionFactory {
	public static void main(String args[]) throws InvalidRemoteException,
			TransportException, GitAPIException {
		Git clonedRepo = Git
				.cloneRepository()
				.setDirectory(new File("C:/git/tmp/egitTraining"))
				.setURI("ssh://d032780@git.wdf.sap.corp:29418/sandbox/git/egit-training.git").call();
		for (RevCommit c : clonedRepo.log().setMaxCount(10).call())
			System.out.println("Commit: " + c.getShortMessage());
	}
}

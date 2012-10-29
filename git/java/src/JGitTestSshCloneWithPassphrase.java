import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

public class JGitTestSshCloneWithPassphrase {
	public static void main(String args[]) throws InvalidRemoteException,
			TransportException, GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_JGitTestSshCloneWithPassphrase_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);

		Git clonedRepo = Git
				.cloneRepository()
				.setDirectory(tmpDir)
				.setURI("ssh://d032780@git.wdf.sap.corp:29418/sandbox/git/egit-training.git")
				.setCredentialsProvider(
						new PassPhraseCredentialsProvider("omaoma")).call();
		for (RevCommit c : clonedRepo.log().setMaxCount(10).call())
			System.out.println("Commit: " + c.getShortMessage());
	}
}

class PassPhraseCredentialsProvider extends CredentialsProvider {
	private String passphrase;

	public PassPhraseCredentialsProvider(String passphrase) {
		this.passphrase = passphrase;
	}

	@Override
	public boolean isInteractive() {
		return false;
	}

	public boolean supports(CredentialItem item) {
		return (item instanceof CredentialItem.StringType && item
				.getPromptText().startsWith("Passphrase for "));
	}

	@Override
	public boolean supports(CredentialItem... items) {
		for (CredentialItem item : items)
			if (!supports(item))
				return false;
		return true;
	}

	@Override
	public boolean get(URIish uri, CredentialItem... items)
			throws UnsupportedCredentialItem {
		for (CredentialItem item : items)
			if (supports(item))
				((CredentialItem.StringType) item).setValue(passphrase);
		return true;
	}
}

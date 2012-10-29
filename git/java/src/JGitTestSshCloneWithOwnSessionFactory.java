import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschSession;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JGitTestSshCloneWithOwnSessionFactory {
	public static void main(String args[]) throws InvalidRemoteException,
			GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_JGitTestSshCloneWithOwnSessionFactory_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);
		
		SshSessionFactory.setInstance(new SshSessionFactory() {
			File knownHostsFile = new File("/tmp/.ssh2/known_hosts");
			File privKeyFile = new File("/tmp/.ssh2/id_rsa");

			@Override
			public RemoteSession getSession(URIish uri,
					CredentialsProvider credentialsProvider, FS fs, int tms)
					throws TransportException {
				String user = uri.getUser();
				final String pass = uri.getPass();
				String host = uri.getHost();
				int port = uri.getPort();

				try {
					JSch jsch = new JSch();
					jsch.setKnownHosts(new FileInputStream(knownHostsFile));
					jsch.addIdentity(privKeyFile.getAbsolutePath());
					Session session = jsch.getSession(user, host, port);
					if (pass != null)
						session.setPassword(pass);
					int retries = 0;
					while (!session.isConnected() && retries < 3) {
						retries++;
						session.connect(tms);
					}
					return new JschSession(session, uri);
				} catch (Exception je) {
					throw new TransportException(
							"Exeception occured from Jsch: ", je);
				}
			}
		});

		Git clonedRepo = Git
				.cloneRepository()
				.setDirectory(tmpDir)
				.setURI("ssh://d032780@git.wdf.sap.corp:29418/sandbox/git/egit-training.git")
				.call();
		for (RevCommit c : clonedRepo.log().setMaxCount(10).call())
			System.out.println("Commit: " + c.getShortMessage());
	}
}

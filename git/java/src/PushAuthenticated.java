import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.awtui.AwtAuthenticator;
import org.eclipse.jgit.awtui.AwtCredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

public class PushAuthenticated {
	/**
	 * 
	 * @param args
	 *   0: repository url
	 *   1: user name
	 *   2: password
	 *   3: branchName
	 *   4: filename
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static void main(String args[]) throws IOException, GitAPIException {
		//UsernamePasswordCredentialsProvider cp = new UsernamePasswordCredentialsProvider(args[1], args[2]);
		//ConsoleCredentialsProvider.install();
		AwtAuthenticator.install();
		AwtCredentialsProvider.install();

		String millis = Long.toString(System.currentTimeMillis());
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_" + PushAuthenticated.class.getName() + "_" + millis);
		tmpDir.mkdirs();
		System.out.println("GIT_DIR: " + tmpDir);

		// Git git = Git.cloneRepository().setURI(args[0]).setCredentialsProvider(cp).setDirectory(tmpDir).call();
		Git git = Git.cloneRepository().setURI(args[0]).setDirectory(tmpDir).call();
		git.checkout().setCreateBranch(true).setName(args[3]).setStartPoint("refs/remotes/origin/" + args[3]).call();
		File f = new File(git.getRepository().getWorkTree(), args[4]);
		try (FileWriter fw = new FileWriter(f)) {
			fw.write(millis);
			fw.flush();
			git.add().addFilepattern(args[4]).call();
			git.commit().setMessage("add/modify "+args[4]+" to contain " + millis).call();
			Iterable<PushResult> res = git.push().setRemote("origin").setRefSpecs(new RefSpec("HEAD:refs/heads/"+args[3])).call();
			printPushResult(res);
		}
	}

	private static void printPushResult(Iterable<PushResult> pr) {
		for (PushResult p : pr) {
			System.out.println("Pushresult: " + p.getMessages());
			for (RemoteRefUpdate rru : p.getRemoteUpdates()) {
				System.out.println("  RemoteRefUpdate: ");
				System.out.println("    srcRef: " + rru.getSrcRef());
				System.out.println("    message: " + rru.getMessage());
				System.out.println("    status: " + rru.getStatus());
			}
		}
	}
}

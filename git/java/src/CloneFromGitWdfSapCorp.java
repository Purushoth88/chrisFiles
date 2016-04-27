import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class CloneFromGitWdfSapCorp {
	public static void main(String args[]) throws IOException, GitAPIException, JGitInternalException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneFromGitWdfSapCorp_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		try (Git git = Git.init().setDirectory(tmpDir).setBare(false).call()) {
			git.getRepository().getConfig().setBoolean("http", null, "sslVerify", false);
			UsernamePasswordCredentialsProvider cp = new UsernamePasswordCredentialsProvider(args[1], args[2]);
			git.fetch().setRemote(args[0]).setRefSpecs(new RefSpec("refs/heads/*:refs/remotes/origin/*"))
					.setCredentialsProvider(cp).call();
			git.checkout().setName("remotes/origin/master").call();
			System.out.println("Cloned repo " + args[0] + " to folder " + tmpDir + ". HEAD:"
					+ git.getRepository().resolve("HEAD"));
		}
	}
}

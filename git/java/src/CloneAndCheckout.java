import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class CloneAndCheckout {
	public static void main(String args[]) throws IOException, GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneAndCheckoutExample_"
						+ System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("GIT_DIR: " + tmpDir);

		Git git = Git.cloneRepository().setURI("https://github.com/chalstrick/dondalfi.git").setDirectory(tmpDir).setNoCheckout(true).call();
		git.checkout().setName("refs/heads/Adder").setCreateBranch(true).setStartPoint("refs/remotes/origin/Adder").call();
		System.out.println("clean status: "+git.status().call().isClean());
	}
}

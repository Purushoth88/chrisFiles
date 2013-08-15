import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class CloneRepoAndCheckState {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneTestCrlf_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("RepoUrl: "+args[0]+", user: "+args[1]+", pwd: "+args[2].replaceAll(".", "*")+", Working dir: " + tmpDir);
		try {
			Git r = Git
					.cloneRepository()
					.setDirectory(tmpDir)
					.setURI(args[0])
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(args[1],
									args[2])).call();
			for (Ref f : r.branchList().setListMode(ListMode.ALL).call())
				System.out.println("found branch " + f.getName());
			System.out.println("status: " + r.status().call().isClean());
		} finally {
			rm(tmpDir);
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}
}

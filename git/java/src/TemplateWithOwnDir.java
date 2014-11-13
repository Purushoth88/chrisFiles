import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;

public class TemplateWithOwnDir {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_" + TemplateWithOwnDir.class.getName() + "_"
						+ System.currentTimeMillis());
		tmpDir.mkdirs();
		try {
			Git git = Git.cloneRepository().setDirectory(tmpDir)
					.setURI(args[0]).setBare(true).call();
			RevCommit headCommit = git.log().setMaxCount(1).call().iterator().next();
			System.out.println("repo:" + git.getRepository()
					+ ", head-commit:" + headCommit+ ", uri:"+args[0]);
			System.out.println("git fetch: "+git.fetch().call().getMessages());
		} finally {
			// rm(tmpDir);
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}
}

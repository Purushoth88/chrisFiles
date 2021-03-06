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

public class CloneBranchExample {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneBranchExample_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);
		try {
			Git r = Git.cloneRepository().setDirectory(tmpDir)
					.setURI("http://github.com/chalstrick/dondalfi.git")
					.call();
			r.checkout().setName("origin/test").call();
			for (Ref f : r.branchList().setListMode(ListMode.ALL).call()) {
				r.checkout().setName(f.getName()).call();
				System.out.println("checked out branch " + f.getName()
						+ ". HEAD: " + r.getRepository().getRef("HEAD"));
			}
			// try to checkout branches by specifying abbreviated names
			r.checkout().setName("master").call();
			r.checkout().setName("origin/test").call();
			try {
				r.checkout().setName("test").call();
			} catch (RefNotFoundException e) {
				System.err.println("couldn't checkout 'test'. Got exception: "
						+ e.toString() + ". HEAD: "
						+ r.getRepository().getRef("HEAD"));
			}
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

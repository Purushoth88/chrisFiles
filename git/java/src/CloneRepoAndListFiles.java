import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.EmptyProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;

public class CloneRepoAndListFiles {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		String cloneUrl = args[0];
		String branch = args[1];
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneTestCrlf_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		try {
			Git r = Git.cloneRepository().setDirectory(tmpDir).setURI(cloneUrl)
					.setBare(true).setProgressMonitor(new EmptyProgressMonitor() {
						@Override
						public void beginTask(String title, int totalWork) {
							System.out.println("Start task " + title);
						}
					}).call();
			for (Ref ref : r.branchList().setListMode(ListMode.ALL).call())
				System.out.println("Found ref: " + ref.getName()
						+ ", peeled OID: " + ref.getPeeledObjectId());
			RevWalk rw = new RevWalk(r.getRepository());
			TreeWalk tw = new TreeWalk(r.getRepository());
			tw.addTree(rw.parseCommit(r.getRepository().resolve(branch))
					.getTree());
			tw.setRecursive(true);
			while (tw.next())
				System.out.println("path: " + tw.getPathString() + ", mode: "
						+ tw.getFileMode(0) + ", oid:" + tw.getObjectId(0));
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

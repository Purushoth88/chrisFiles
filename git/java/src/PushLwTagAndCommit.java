import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class PushLwTagAndCommit {
	public static void main(String args[]) throws IOException, GitAPIException {
		long millis = System.currentTimeMillis();
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_PushLwTagAndCommit_"
						+ millis);
		tmpDir.mkdirs();
		System.out.println("GIT_DIR: " + tmpDir);
		
		Git git = Git.init().setDirectory(tmpDir).setBare(false).call();
		git.getRepository().getConfig().setBoolean("http", null, "sslVerify", false);
		UsernamePasswordCredentialsProvider cp = new UsernamePasswordCredentialsProvider(args[1], args[2]);
		git.fetch().setRemote(args[0]).setRefSpecs(new RefSpec("refs/heads/*:refs/remotes/origin/*")).setCredentialsProvider(cp).call();
		git.checkout().setName("refs/heads/master").setCreateBranch(true).setStartPoint("refs/remotes/origin/master").call();

		String fname = "a"+millis;
		FileWriter fw = new FileWriter(new File(git.getRepository().getWorkTree(), fname));
		fw.write("Hello world: "+millis);
		fw.close();
		git.add().addFilepattern(fname).call();
		PersonIdent me = new PersonIdent("Christian Halstrick"	, "christian.halstrick@sap.com");
		RevCommit commit = git.commit().setMessage("test").setAuthor(me).setCommitter(me).call();
		RefUpdate ur = git.getRepository().updateRef("refs/tags/t_"+fname);
		ur.setNewObjectId(commit.getId());
		Result update = ur.update();
		
		Iterable<PushResult> pr = git.push().setPushTags().setRemote(args[0]).setCredentialsProvider(cp).call();
		printPushResult(pr);
		pr = git.push().setRefSpecs(new RefSpec("HEAD:refs/heads/master")).setRemote(args[0]).setCredentialsProvider(cp).call();
		printPushResult(pr);
		pr = git.push().setPushTags().setCredentialsProvider(cp).setRemote(args[0]).call();
		printPushResult(pr);
	}

	private static void printPushResult(Iterable<PushResult> pr) {
		for (PushResult p : pr) {
			System.out.println("Pushresult: "+p.getMessages());
			for (RemoteRefUpdate rru: p.getRemoteUpdates()) {
				System.out.println("  rru.getSrcRef: "+rru.getSrcRef());
				System.out.println("  rru.getMessage: "+rru.getMessage());
				System.out.println("  rru: "+rru.getStatus());
			}
		}
	}

}

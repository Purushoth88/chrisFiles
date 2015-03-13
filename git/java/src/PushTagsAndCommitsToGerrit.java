import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class PushTagsAndCommitsToGerrit {
	public final static String USER_FULLNAME="Christian Halstrick";
	public final static String USER_EMAIL="christian.halstrick@sap.com";
	public final static String USER_ID="d032780";
	public final static String USER_PWD="geheim";
	
	public final static String REPO_URL="https://git.wdf.sap.corp/supportportal/example";
	
	public static void main(String args[]) throws IOException, GitAPIException {
		PersonIdent me = new PersonIdent(USER_FULLNAME, USER_EMAIL);
		UsernamePasswordCredentialsProvider cp = new UsernamePasswordCredentialsProvider(
				USER_ID, USER_PWD);
		
		String millis = Long.toString(System.currentTimeMillis());
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_PushTagsAndCommitsToGerrit_" + millis);
		tmpDir.mkdirs();
		System.out.println("GIT_DIR: " + tmpDir);

		Git git = Git.cloneRepository().setURI(REPO_URL)
				.setCredentialsProvider(cp).setDirectory(tmpDir).call();
		git.checkout().setCreateBranch(true).setName("pushTest").setStartPoint("refs/remotes/origin/pushTest").call();
		File f = new File(git.getRepository().getWorkTree(), "foo");
		FileWriter fw = new FileWriter(f);
		
		fw.write(millis);
		git.add().addFilepattern(".").call();
		git.commit().setMessage("add/modify foo to contain "+millis).setAuthor(me).setCommitter(me).call();
		Iterable<PushResult> res = git.push().setCredentialsProvider(cp)
				.setRemote("origin").setPushAll().call();
		printPushResult(res);

		fw.write(millis+"b");
		git.add().addFilepattern(".").call();
		git.commit().setMessage("add/modify foo to contain "+millis+"b").setAuthor(me).setCommitter(me).call();
		git.tag().setAnnotated(true).setMessage("a tag message").setName("tag-"+millis+"-a").setTagger(me).call();
		git.tag().setAnnotated(false).setName("tag-"+millis+"-l").call();
		res = git.push().setCredentialsProvider(cp).setRemote("origin")
				.setPushAll().setPushTags().call();
		printPushResult(res);
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

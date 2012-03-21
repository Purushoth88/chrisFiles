import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.swing.event.ListSelectionEvent;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;

public class Test {
	private static String uri;

	public static void main(String[] args) throws IOException,
			JGitInternalException, GitAPIException, NoWorkTreeException,
			InterruptedException, URISyntaxException {

		useNotes();

		// cloneRepoAndListBranches("http://github.com/chalstrick/mergeExample.git");

		// File tmpDir = null;
		// try {
		// tmpDir =
		// new File(System.getProperty("java.io.tmpdir"), "tmp"
		// + System.currentTimeMillis());
		// testbpt(tmpDir, "file://C:/git/tmp/releasetools/.git",
		// "9f3613bd33f4dddf3f1d27aa6cf5dd89b842d303");
		// testbpt(tmpDir, "file://C:/git/tmp/releasetools/.git",
		// "f6f9f04919eee990f8d00d2136e2c60aaabeb127");
		// testbpt(tmpDir, "git://git.wdf.sap.corp/NGP/LDI/releasetools.git",
		// "39a003120926f7b3cad6a2cb8a2315d52c6f63d8");

		// cloneJGit(tmpDir);

		// commitTagLogWriteTagger(tmpDir);
		// pushTagRapidly(tmpDir);
		// cloneEgitAndSwitchBranches(tmpDir, "C:/git/egit");
		// } finally {
		// if (tmpDir != null && tmpDir.exists()) deleteDir(tmpDir);
		// }
	}

	public static void useNotes() throws NoWorkTreeException, IOException,
			NoFilepatternException, GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());

		try {
			Git repo = Git.init().setDirectory(new File(tmpDir, "repo1"))
					.call();
			new FileWriter(
					new File(repo.getRepository().getWorkTree(), "f.txt"))
					.write("hello world");
			repo.add().addFilepattern("f.txt").call();
			RevCommit commit = repo.commit().setMessage("tests").call();
			System.out.println("created commit with id: "+commit.getId());
			repo.notesAdd().setObjectId(commit).setMessage("content of Note").call();
			for (Note n : repo.notesList().call()) {
				System.out.println("Found note on object "+n.getName()+", data: "+n.getData());
			}
		} finally {
			if (tmpDir != null && tmpDir.exists())
				deleteDir(tmpDir);
		}

	}

	public static void bug339610() throws NoWorkTreeException, IOException,
			NoFilepatternException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());

		Git repo = Git.init().setDirectory(new File(tmpDir, "repo1")).call();
		System.out.println(repo.getRepository().getWorkTree());
		new FileWriter(new File(repo.getRepository().getWorkTree(), "f.txt"))
				.write("hello world");
		repo.add().addFilepattern("f.txt").call();
		// repo.reset().addPath("f.txt").setRef("HEAD").call();
		repo.rm().addFilepattern("f.txt").call();

	}

	public static void pullTest() throws GitAPIException, NoWorkTreeException,
			IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());

		Git repo = Git.init().setDirectory(new File(tmpDir, "1")).call();
		repo.fetch()
				.setRemote("git://github.com/chalstrick/testRepo.git")
				.setRefSpecs(
						new RefSpec("refs/heads/Add_Pi:refs/remotes/origin/pi"))
				.call();
		Ref newBranch = repo.branchCreate().setName("refs/heads/feature")
				.setStartPoint("refs/remotes/origin/pi").call();

		repo.push()
				.setRemote(
						"https://chalstrick@github.com/chalstrick/testRepo.git")
				.setRefSpecs(new RefSpec("refs/heads/master:refs/heads/xyz"))
				.call();

	}

	public static void cloneRepoAndListBranches(String uri) {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());

		Git repo = Git.cloneRepository().setURI(uri)
				.setDirectory(new File(tmpDir, "repo1")).setBare(true).call();
		for (Ref b : repo.branchList().setListMode(ListMode.ALL).call())
			System.out.println("(bare): cloned branch " + b.getName());
		repo = Git.cloneRepository().setURI(uri)
				.setDirectory(new File(tmpDir, "repo1c")).setBare(false).call();
		for (Ref b : repo.branchList().setListMode(ListMode.ALL).call())
			System.out.println("(nonbare): cloned branch " + b.getName());
		repo = Git.cloneRepository().setURI(uri)
				.setDirectory(new File(tmpDir, "repo1b"))
				.setCloneAllBranches(false)
				.setBranchesToClone(Arrays.asList("refs/heads/master"))
				.setBare(false).call();
		for (Ref b : repo.branchList().setListMode(ListMode.ALL).call())
			System.out.println("(masterNonbare): cloned branch " + b.getName());
		repo = Git.cloneRepository().setURI(uri)
				.setDirectory(new File(tmpDir, "repo2"))
				.setCloneAllBranches(true).setBare(true).call();
		for (Ref b : repo.branchList().setListMode(ListMode.ALL).call())
			System.out.println("(allBare): cloned branch " + b.getName());
		repo = Git.cloneRepository().setURI(uri)
				.setDirectory(new File(tmpDir, "repo3"))
				.setBranchesToClone(Arrays.asList("refs/heads/master"))
				.setBare(true).call();
		for (Ref b : repo.branchList().setListMode(ListMode.ALL).call())
			System.out.println("(masterBare): cloned branch " + b.getName());

		deleteDir(tmpDir);
	}

	public static void pushTagRapidly(File tmpDir) throws GitAPIException,
			NoWorkTreeException, IOException, InterruptedException {
		File f = new File(tmpDir, "git1");
		Git central = Git.init().setDirectory(new File(tmpDir, "repo1")).call();
		new FileWriter(new File(central.getRepository().getWorkTree(), "f.txt"))
				.write("hello world");
		central.add().addFilepattern("f.txt").call();
		RevCommit commit = central.commit().setMessage("Initial commit").call();

		Git local = Git
				.cloneRepository()
				.setURI(central.getRepository().getWorkTree().toURI()
						.toString()).setDirectory(new File(tmpDir, "local"))
				.call();
		Git local2 = Git
				.cloneRepository()
				.setURI(central.getRepository().getWorkTree().toURI()
						.toString()).setDirectory(new File(tmpDir, "local2"))
				.call();

		for (int i = 0; i < 5; ++i) {
			RevTag tag = local.tag().setMessage("m").setName("t1")
					.setObjectId(commit).setForceUpdate(true).call();
			System.out.println("tag on local with id: " + tag.getId());
			System.out.println("Attempt #" + i + " to push on local");
			Iterable<PushResult> pushResults = local.push().setRemote("origin")
					.setRefSpecs(new RefSpec("refs/tags/t1:refs/tags/t1"))
					.setForce(true).call();
			for (PushResult r : pushResults)
				for (RemoteRefUpdate rru : r.getRemoteUpdates())
					System.out.println("RemoteRefUpdate :"
							+ rru.getRemoteName() + "/" + rru.getStatus());
			Thread.sleep(1000);
		}

		RevTag tag2 = local2.tag().setMessage("m").setName("t1")
				.setObjectId(commit).call();
		System.out.println("tag2 on local2 with id: " + tag2.getId());
		for (int i = 0; i < 5; ++i) {
			System.out.println("Attempt #" + i + " to push on local2");
			Iterable<PushResult> pushResults = local2.push()
					.setRemote("origin")
					.setRefSpecs(new RefSpec("refs/tags/t1:refs/tags/t1"))
					.setForce(true).call();
			for (PushResult r : pushResults)
				for (RemoteRefUpdate rru : r.getRemoteUpdates())
					System.out.println("RemoteRefUpdate :"
							+ rru.getRemoteName() + "/" + rru.getStatus());
		}
	}

	public static void commitTagLogWriteTagger(File tmpDir)
			throws NoWorkTreeException, IOException, GitAPIException {
		Git git = Git.init().setDirectory(new File(tmpDir, "repo1")).call();
		Repository repo = git.getRepository();
		File f = new File(repo.getWorkTree(), "f.txt");
		new FileWriter(f).write("hello world");
		git.add().addFilepattern("f.txt").call();
		git.commit().setMessage("Initial commit").call();
		git.tag().setMessage("my tag").setName("tag100").call();

		for (RevCommit c : git.log().call())
			System.out.println("Found commit: " + c.getShortMessage());

		Ref ref = repo.getRef("refs/tags/tag100");
		RevTag tag = RevTag.parse(repo.open(ref.getObjectId()).getBytes());
		System.out.println("tag refs/tags/tag100 was created by:"
				+ tag.getTaggerIdent());
	}

	public static void cloneEgitAndSwitchBranches(File tmpDir, String egitURI)
			throws IOException, JGitInternalException, GitAPIException {
		Git egitClone = Git.cloneRepository()
				.setURI(new File(egitURI).toURI().toASCIIString())
				.setDirectory(new File(tmpDir, "egitClone")).call();
		Ref v11 = egitClone.checkout().setName("refs/tags/v0.11.1").call();
		Ref v10 = egitClone.checkout().setName("refs/tags/v0.10.1").call();
		Status status = egitClone.status().call();
		for (String changed : status.getChanged())
			System.out.println("changed file: " + changed);
	}

	private static void testbpt(File baseTmpDir, String uriToClone,
			String commitToCheckout) throws JGitInternalException,
			InvalidRemoteException, URISyntaxException, IOException,
			GitAPIException {
		File tmp = new File(baseTmpDir, "git1");

		// System.out.println("Will clone " + uriToClone + " into " +
		// tmp.getPath());
		// Git git =
		// Git.cloneRepository().setURI(uriToClone).setDirectory(tmp).call();

		System.out.println("Will fetch " + uriToClone + " into "
				+ tmp.getPath());
		Git git = Git.init().setDirectory(tmp).call();
		remoteAdd(git, "origin", uriToClone);
		FetchResult result = git
				.fetch()
				.setRemote("origin")
				.setRefSpecs(
						new RefSpec("refs/heads/*:refs/remotes/origin/heads/*"),
						new RefSpec("refs/tags/*:refs/remotes/origin/tags/*"))
				.call();

		System.out.println("Will lookup commit " + commitToCheckout);
		RevWalk revWalk = new RevWalk(git.getRepository());
		RevCommit parseCommit = revWalk.parseCommit(ObjectId
				.fromString(commitToCheckout));
		// RevCommit parseCommit =
		// revWalk.parseCommit(ObjectId.fromString("f6f9f04919eee990f8d00d2136e2c60aaabeb127"));
		System.out.println("Will checkout commit " + parseCommit);
		Ref n = git.checkout().setName(commitToCheckout).call();
		System.out.println("Success!");
	}

	private static void remoteAdd(Git git, String name, String uri)
			throws URISyntaxException, IOException {
		StoredConfig config = git.getRepository().getConfig();
		RemoteConfig rc = new RemoteConfig(config, name);
		rc.addURI(new URIish(uri));
		rc.update(config);
		config.save();
	}

	public static void deleteDir(File dir) {
		File[] children = dir.listFiles();
		if (children != null)
			for (File f : dir.listFiles())
				if (f.isDirectory())
					deleteDir(f);
				else
					f.delete();
		dir.delete();
	}

}

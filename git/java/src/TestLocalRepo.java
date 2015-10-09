import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class TestLocalRepo {
	private static String defaultRepo = "C:/git/chris";

	public static void main(String args[])
			throws GitAPIException, JGitInternalException, IOException, NoWorkTreeException, InterruptedException {
		String workTreePath = args.length > 0 ? args[0] : defaultRepo;
		// Git g = Git.open(new File(workTreePath));
		TestLocalRepo r = new TestLocalRepo();
		r.testBug(new File(workTreePath));
	}

	public void test(Git repo)
			throws GitAPIException, JGitInternalException, NoWorkTreeException, CorruptObjectException, IOException {
		for (RevCommit c : repo.log().call())
			System.out.println("found commit: " + c.getName() + ": " + c.getShortMessage());
		DirCache dc = repo.getRepository().readDirCache();
		System.out.println("found " + dc.getEntryCount() + " entries in the dircache");
	}

	public void testBug(File gitDir) throws GitAPIException, JGitInternalException, NoWorkTreeException,
			CorruptObjectException, IOException, InterruptedException {
		final Repository initialRepository = FileRepositoryBuilder.create(gitDir);
		initialRepository.create(false);

		final DirCache dirCache = DirCache.newInCore();
		try (ObjectInserter objectInserter = initialRepository.newObjectInserter()) {
			final ObjectId treeId;
			treeId = dirCache.writeTree(objectInserter);

			CommitBuilder commitBuilder;

			commitBuilder = new CommitBuilder();
			commitBuilder.setAuthor(new PersonIdent(initialRepository));
			commitBuilder.setCommitter(new PersonIdent(initialRepository));
			commitBuilder.setMessage("Initial commit");
			commitBuilder.setTreeId(treeId);

			final ObjectId commitId;
			commitId = objectInserter.insert(commitBuilder);

			RefUpdate refUpdate;

			refUpdate = initialRepository.updateRef("refs/heads/master");
			refUpdate.setNewObjectId(commitId);
			refUpdate.forceUpdate();

			refUpdate = initialRepository.updateRef("HEAD");
			refUpdate.link("refs/heads/master");

			Repository repository1 = FileRepositoryBuilder.create(gitDir);
			final ObjectId headId1 = repository1.resolve("HEAD"); // without
																	// this
																	// "resolve"
																	// the
																	// test is
																	// passed

			Repository repository2 = FileRepositoryBuilder.create(gitDir);
			final ObjectId headId2 = repository2.resolve("HEAD"); // without
																	// this"resolve"
																	// the test
																	// is
																	// passed

			assert(headId1.equals(headId2));
			assert(commitId.equals(headId1));

			commitBuilder = new CommitBuilder();
			commitBuilder.setAuthor(new PersonIdent(initialRepository));
			commitBuilder.setCommitter(new PersonIdent(initialRepository));
			commitBuilder.setMessage("Another commit");
			commitBuilder.setParentId(commitId);
			commitBuilder.setTreeId(treeId);

			final ObjectId anotherCommitId;
			anotherCommitId = objectInserter.insert(commitBuilder);

			refUpdate = repository1.updateRef("HEAD");
			refUpdate.setNewObjectId(anotherCommitId);
			refUpdate.forceUpdate();

			// we prepared two repository objects for the same repository
			// directory
			// now let's resolve HEAD in both of them

			Thread.sleep(4000); // without this "sleep" the test is passed

			final ObjectId anotherCommitId2 = repository2.resolve("HEAD");
			final ObjectId anotherCommitId1 = repository1.resolve("HEAD");

			assert(anotherCommitId1.equals(anotherCommitId2));
			assert(anotherCommitId1.equals(anotherCommitId));
		}
	}

}

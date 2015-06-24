import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GCReproducer {
	public static void main(String args[]) throws IOException,
			InterruptedException, ParseException {
		Path tmp = Files.createTempDirectory("ChrisFiles_"
				+ GCReproducer.class.getName());
		// Bare repository
		Repository repo = FileRepositoryBuilder.create(tmp.toFile());
		repo.create(true);

		// Some dangling blob, coming from a very old commit not longer
		// referenced
		ObjectInserter inserter = repo.newObjectInserter();
		byte[] raw = "Hello, world!".getBytes();
		ObjectId blob = inserter.insert(Constants.OBJ_BLOB, raw, 0, raw.length);
		inserter.close();

		// Wait
		Thread.sleep(1000);

		// (Process 1) Insert blob -> actually it is already in the database
		inserter = repo.newObjectInserter();
		raw = "Hello, world!".getBytes();
		blob = inserter.insert(Constants.OBJ_BLOB, raw, 0, raw.length);

		// (Process 1) Insert tree pointing to blob
		TreeFormatter formatter = new TreeFormatter();
		formatter.append("test.txt", FileMode.REGULAR_FILE, blob);
		ObjectId tree = inserter.insert(formatter);

		// (Process 2) GC
		GC gc = new GC((FileRepository) repo);
		gc.setExpireAgeMillis(200);
		gc.gc();

		// (Process 1) Insert commit pointing to tree (and to blob)
		PersonIdent person = new PersonIdent("Tester", "test@example.com");
		CommitBuilder c = new CommitBuilder();
		c.setTreeId(tree);
		c.setAuthor(person);
		c.setCommitter(person);
		c.setMessage("Hurray");
		ObjectId commit = inserter.insert(c);
		inserter.close();

		// (Process 1) Update reference
		RefUpdate ru = repo.updateRef(Constants.R_HEADS + Constants.MASTER);
		ru.setNewObjectId(commit);

		assert Result.NEW.equals(ru.update());

		// (Process 3) Browse the commit
		Ref ref = repo.getRef(Constants.R_HEADS + Constants.MASTER);
		ObjectId head = ref.getObjectId();

		RevWalk revWalk = new RevWalk(repo);
		revWalk.setRetainBody(false);
		ObjectId readCommit = revWalk.parseCommit(head).getId();

		RevWalk walk = new RevWalk(repo);
		ObjectId readTree = walk.parseCommit(readCommit).getTree().getId();
		TreeWalk t = new TreeWalk(repo);
		t.setRecursive(false);
		t.addTree(readTree);
		Map<String, ObjectId> content = new HashMap<String, ObjectId>();
		while (t.next()) {
			content.put(t.getNameString(), t.getObjectId(0));
		}
		ObjectId readBlob = content.get("test.txt");
		raw = repo.open(readBlob).getBytes();
		assert "Hello, world!".equals(new String(raw));
	}

}

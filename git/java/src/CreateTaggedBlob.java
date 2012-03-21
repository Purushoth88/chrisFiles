import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;

public class CreateTaggedBlob {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		/* create a new git repo in a temporary folder */ 
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());
		tmpDir.mkdirs();
		Git r = Git.init().setDirectory(tmpDir).call();
		
		/* create a blob not related to any tree or commit */
		ObjectInserter inserter = r.getRepository().newObjectInserter();
		ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, new byte[] { 65, 66, 67 });
		inserter.flush();
		
		/* tag the blob with a lightweight tag */
		RefUpdate updateRef = r.getRepository().updateRef(Constants.R_TAGS+ "specialTag007");
		updateRef.setNewObjectId(blobId);
		updateRef.setExpectedOldObjectId(ObjectId.zeroId());
		if (updateRef.update().equals(RefUpdate.Result.NEW))
			System.out.println("Succesfully tagged a blob ");
		else
			System.err.println("Failed to tagged a blob.");
	}
}

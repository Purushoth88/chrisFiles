import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Ref;

public class AddTwo {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_AddTwo_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);

		Git r = Git.init().setDirectory(tmpDir).call();
		new FileWriter(new File(tmpDir, "a")).write("content of a");
		new FileWriter(new File(tmpDir, "b")).write("content of b");
		r.add().addFilepattern(".").call();
		r.commit().setMessage("first").call();
	}
}

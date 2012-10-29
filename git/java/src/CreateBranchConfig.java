import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;


public class CreateBranchConfig {
	public static void main(String[] args) throws GitAPIException, IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CreateBranchConfig_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);
		Git git=Git.init().setDirectory(tmpDir).call();
		File f = new File(tmpDir, "a");
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(65);
		fos.close();
		git.add().addFilepattern("a").call();
		git.commit().setMessage("init");
		
		git.branchCreate().setName("config").call();
	}
	

}

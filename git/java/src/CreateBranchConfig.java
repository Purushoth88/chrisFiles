import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;


public class CreateBranchConfig {
	public static void main(String[] args) throws GitAPIException, IOException {
		File root = new File("C:/Temp/gitTest"+System.currentTimeMillis());
		root.mkdir();
		Git git=Git.init().setDirectory(root).call();
		File f = new File(root, "a");
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(65);
		fos.close();
		git.add().addFilepattern("a").call();
		git.commit().setMessage("init");
		
		git.branchCreate().setName("config").call();
	}
	

}

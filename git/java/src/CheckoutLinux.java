import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

public class CheckoutLinux {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Git r = Git.open(new File("/home/user/git/linux"));
		StopWatch sw = StopWatch.createAndStart();
		System.out.println("Current commit on HEAD: "+ r.log().call().iterator().next().getShortMessage());
		sw.stop("printed latest log message");
		
		FileOutputStream fos = new FileOutputStream(new File(r.getRepository().getWorkTree(), "a"));
		fos.write(65);
		fos.close();
		
		sw = StopWatch.createAndStart();
		r.add().addFilepattern("a").call();
		sw.stop("added file a");
	}
}

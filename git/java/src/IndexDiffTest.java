import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

public class IndexDiffTest {
	public static void main(String args[]) throws IOException, GitAPIException {
		Status status = Git.open(new File(".")).status().call();
		for(String s: status.getModified())
			System.out.println("modified: "+s);
		for(String s: status.getChanged())
			System.out.println("changed: "+s);
		for(String s: status.getUntracked())
			System.out.println("untracked: "+s);
	}
}

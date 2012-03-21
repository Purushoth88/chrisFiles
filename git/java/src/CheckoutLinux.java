import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Ref;

public class CheckoutLinux {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Git r = Git.open(new File("C:/git/git"));
		r.checkout().setName("master").call();
		System.out.println("Current commit on HEAD: "+ r.log().call().iterator().next().getShortMessage());
	}
}

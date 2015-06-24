import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class TestPush {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Git git = Git.open(new File("/tmp/g1/test"));
		try {
			Iterable<PushResult> res = git
					.push()
					.add("HEAD:refs/heads/master")
					.setRemote("origin")
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(
									"developer",
									"JIKqTIqFZoZCrDKdfl02DrTfuiD5eUCtlMFH0oKNVw"))
					.call();
			for (PushResult r : res) {
				System.out.println(r);
			}
		} catch (org.eclipse.jgit.api.errors.TransportException e) {
			System.out.println("Got an exception: " + e);
			System.out.println("Cause was: " + e.getCause());
			e.printStackTrace();
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}
}

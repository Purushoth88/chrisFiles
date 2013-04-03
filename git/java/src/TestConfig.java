import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;

public class TestConfig {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		Git r = Git.open(new File("c:/git/linux"));
		StopWatch sw = StopWatch.createAndStart();
		System.out.println("Current commit on HEAD: "+ r.log().call().iterator().next().getShortMessage());
		sw.stop("printed latest log message");
		
		System.out.println("My email: "+r.getRepository().getConfig().getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL));
	}
}

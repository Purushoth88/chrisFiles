import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FileUtils;

public class Test {

	public static void main(String args[]) throws JGitInternalException,
			InvalidRemoteException, IOException {
		File directory = new File("/tmp/a");
		FileUtils.delete(directory, FileUtils.RECURSIVE);
		Git git = Git
				.cloneRepository()
				.setURI("ssh://d032780@git.wdf.sap.corp:29418/NGJP/Services/metering")
				.setDirectory(directory).call();
		git.fetch()
				.setRefSpecs(
						new RefSpec(
								"refs/changes/01/51001/3:refs/changes/01/51001/3"))
				.call();
		for (String n : git.getRepository().getAllRefs().keySet()) 
			System.out.println(n);
		for (Ref f : git.getRepository().getRefDatabase().getAdditionalRefs())
			System.out.println(f);
	}
}

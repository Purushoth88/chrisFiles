import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.sun.javafx.collections.MappingChange.Map;
import com.sun.xml.internal.stream.StaxXMLInputSource;

public class CloneSubmoduleRepo {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneTestCrlf_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("RepoUrl: "+args[0]+", user: "+args[1]+", pwd: "+args[2].replaceAll(".", "*")+", Working dir: " + tmpDir);
		try {
			Git r = Git
					.cloneRepository()
					.setDirectory(tmpDir)
					.setURI(args[0])
					.setCloneSubmodules(true)
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(args[1],
									args[2])).call();
			
			for (Ref f : r.branchList().setListMode(ListMode.ALL).call())
				System.out.println("found branch " + f.getName());
			SubmoduleWalk smw = SubmoduleWalk.forIndex(r.getRepository());
			while (smw.next()) {
				System.out.println("Submodule.getPath:"+smw.getPath());
				System.out.println("Submodule.getDirectory:"+smw.getDirectory());
			}
			
			System.out.println("status: " + r.status().call().isClean());
			java.util.Map<String, SubmoduleStatus> status = r.submoduleStatus().call();
			for (String s: status.keySet())
				System.out.println("Found submodule "+s+" with status "+status.get(s));
		} finally {
			rm(tmpDir);
		}
	}

	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}
}

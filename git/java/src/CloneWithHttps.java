import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportHttp;

public class CloneWithHttps {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		System.setProperty("https.proxyHost", "proxy");
		System.setProperty("https.proxyPort", "8080");
		
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_CloneWithHttps_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		try {
			CloneCommand cloneCommand = Git.cloneRepository().setDirectory(tmpDir)
					.setURI("https://github.com/ifedorenko/p2-browser.git");
			cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
				@Override
				public void configure(Transport transport) {
					if (transport instanceof TransportHttp) {
						TransportHttp myTransport = (TransportHttp)transport;
						myTransport.applyConfig(null);
					}
				}
			});
			
			cloneCommand.getRepository().getConfig().setBoolean("http", null, "sslVerify", false);
			
			r.checkout().setName("origin/master").call();
			for (Ref f : r.branchList().setListMode(ListMode.ALL).call()) {
				r.checkout().setName(f.getName()).call();
				System.out.println("checked out branch " + f.getName()
						+ ". HEAD: " + r.getRepository().getRef("HEAD"));
			}
			r.getRepository().close();
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

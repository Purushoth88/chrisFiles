import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class ConcurrentFetch {
	public static void main(final String args[])
			throws IOException, GitAPIException, InterruptedException, BrokenBarrierException {
		Thread[] threads = new Thread[50];
		final CyclicBarrier barrier = new CyclicBarrier(threads.length);
		StoredConfig config = Git.open(new File(args[0])).getRepository().getConfig();
		config.setBoolean("http", null, "sslVerify", false);
		config.save();
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					RefSpec refSpec = new RefSpec(args[1]);
					String threadName = Thread.currentThread().getName();
					try (Git g = Git.open(new File(args[0]))) {
						System.out.println(threadName + ": Opened a repository at " + g.getRepository().getDirectory());
						barrier.await();
						System.out.println(threadName + ": Will fetch the following refspec:" + refSpec);
						FetchResult res = g.fetch().setRefSpecs(refSpec).setCredentialsProvider(
								new UsernamePasswordCredentialsProvider("d032780", "Bspld03km2")).call();
						for (TrackingRefUpdate u : res.getTrackingRefUpdates())
							System.out.println(
									threadName + ": Fetch resulted in :" + u.getLocalName() + ", " + u.getRemoteName());
						System.out.println(threadName + ": Fetch was ok. Updated " + res.getTrackingRefUpdates().size()
								+ " branches");
					} catch (Exception e) {
						ByteArrayOutputStream bao = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(bao));
						System.out.println(threadName + ": Caught an exception" + e + "\n" + bao.toString());
					}
				}
			}, "thread" + i);
			threads[i].start();
		}
	}
}

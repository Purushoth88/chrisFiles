import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class PushVerbose {
	// args[0]: the folder containing the repo to push from
	// args[1]: the user name for authentication
	// args[2]: the password for authentication
	// args[3]: the name of the remote to push to. E.g.: origin
	// args[4,5,6,...]: the refspec to push. E.g. "HEAD:/refs/for/master"
	public static void main(String args[]) throws IOException, GitAPIException {
		UsernamePasswordCredentialsProvider cp = new UsernamePasswordCredentialsProvider(args[1], args[2]);
		try (Git git = Git.open(new File(args[0]))) {
			List<RefSpec> rs = new LinkedList<RefSpec>();
			for (int i = 4; i < args.length; i++)
				rs.add(new RefSpec(args[i]));
			Iterable<PushResult> pushResults = git.push().setCredentialsProvider(cp).setRemote(args[3]).setRefSpecs(rs)
					.call();

			for (PushResult res : pushResults) {
				System.out.println(
						"Inspecting pushResult: peerUserAgent: " + res.getPeerUserAgent() + ", URI: " + res.getURI());
				for (Ref ref : res.getAdvertisedRefs())
					System.out.println("  AdvertizedRef:" + ref);
				for (RemoteRefUpdate ru : res.getRemoteUpdates()) {
					TrackingRefUpdate tru = ru.getTrackingRefUpdate();
					System.out.println("  RemoteUpdate: remoteName:" + ru.getRemoteName() + ", SrcRef: "
							+ ru.getSrcRef() + ", Message: " + ru.getMessage() + ", Status: " + ru.getStatus()
							+ ", ExpectedOldObjectId: " + ru.getExpectedOldObjectId() + ", TrackingRefUpdate:" + tru);
					if (tru != null) {
						System.out.println("    TrackingRefUpdate: LocalName:" + tru.getLocalName() + ", RemoteName: "
								+ tru.getRemoteName() + ", NewObjectId: " + tru.getNewObjectId() + ", Result: "
								+ tru.getResult() + ", OldObjectId(: " + tru.getOldObjectId());
					}
				}
				System.out.println("  Messages: " + res.getMessages());
				System.out.println("  TrackingRefUpdates:");
				for (TrackingRefUpdate tru : res.getTrackingRefUpdates()) {
					System.out.println("    TrackingRefUpdate: remoteName:" + tru.getRemoteName() + ", LocalName: "
							+ tru.getLocalName() + ", NewObjectId: " + tru.getNewObjectId() + ", OldObjectId: "
							+ tru.getOldObjectId() + ", Result: " + tru.getResult());

				}
			}
		}
	}
}
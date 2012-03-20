import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

public class ShowTypeOfObject {
	/**
	 * @param args
	 *            args[0] is the path to working tree directory. args[1] is the
	 *            name of a existing ref.
	 */
	public static void main(String args[]) throws IOException, GitAPIException {
		Repository repo = Git.open(new File(args[0])).getRepository();
		RevWalk rw = new RevWalk(repo);
		try {
			for (int i = 1; i < args.length; i++) {
				System.out.println("Processing root ref: " + args[i]);
				Ref ref = repo.getRef(args[i]);
				RevObject obj = null;
				for (;;) {
					obj = rw.parseAny(ref.getObjectId());
					rw.parseHeaders(obj);
					System.out.println(d(repo, ref, obj));
					if (!ref.isSymbolic())
						break;
					System.out.println("Derefering the symbolic ref "
							+ ref.getName() + "->" + ref.getTarget().getName());
					ref = ref.getTarget();
				}

			}
		} finally {
			rw.dispose();
		}
	}

	private static String d(Repository repo, Ref ref, RevObject obj)
			throws IOException {
		return "Ref: name:" + ref.getName() + ", Id/Type:"
				+ d(repo, ref.getObjectId()) + "/"
				+ Constants.typeString(obj.getType()) + ", Leaf:"
				+ ref.getLeaf().getName() + ", Target:"
				+ ref.getTarget().getName() + ", PeeledObjectID:"
				+ d(repo, ref.getPeeledObjectId()) + ", isPeeld:"
				+ ref.isPeeled() + ", isSymbolic:" + ref.isSymbolic();
	}

	private static String d(Repository repo, ObjectId objectId)
			throws IOException {
		return (objectId == null) ? "null" : repo.newObjectReader()
				.abbreviate(objectId).name();
	}
}

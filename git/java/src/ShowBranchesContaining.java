import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;

public class ShowBranchesContaining {
	public static void main(String[] args) throws IOException {
		Repository repo = new FileRepository(args[0]);
		RevWalk walk = new RevWalk(repo);
		RevCommit commit = walk.parseCommit(repo.resolve(args[1] + "^0"));
		for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet())
			if (e.getKey().startsWith(Constants.R_HEADS))
				if (walk.isMergedInto(commit,
						walk.parseCommit(e.getValue().getObjectId())))
					System.out.println("Ref " + e.getValue().getName()
							+ " contains commit " + commit);
	}
}

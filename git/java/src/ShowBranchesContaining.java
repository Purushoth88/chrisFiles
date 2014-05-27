import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class ShowBranchesContaining {
	public static void main(String[] args) throws IOException {
		Git git=Git.open(new File(args[0]));
		Repository repo=git.getRepository();
		RevWalk walk = new RevWalk(git.getRepository());
		RevCommit commit = walk.parseCommit(repo.resolve(args[1] + "^0"));
		for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet())
			if (e.getKey().startsWith(Constants.R_HEADS))
				if (walk.isMergedInto(commit,
						walk.parseCommit(e.getValue().getObjectId())))
					System.out.println("Ref " + e.getValue().getName()
							+ " contains commit " + commit);
	}
}

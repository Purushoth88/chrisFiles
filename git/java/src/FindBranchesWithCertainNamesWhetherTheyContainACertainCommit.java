import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class FindBranchesWithCertainNamesWhetherTheyContainACertainCommit {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		
		Git r = Git.open(new File(args[0]));
		
		System.out.println("Inspecting repo on "+r.getRepository().getDirectory());
		ObjectId commit = r.getRepository().resolve(args[1]);
		System.out.println("Inspecting commit "+commit);

		RevWalk rw = new RevWalk(r.getRepository());
		// list all branches
		for (Ref ref: r.branchList().setListMode(ListMode.ALL).call()) {
			if (ref.getName().contains("stable")) {
				System.out.println("Found ref "+ref.getName());
				rw.markStart(rw.parseCommit(ref.getObjectId()));
			}
		}
		RevCommit c;
		while ((c = rw.next()) != null) 
			if (c.getId().equals(commit)) {
				System.out.println("Commit "+commit.getName()+" is reachable from at least one of the branches containing stable");
				return;
			}
		System.out.println("Commit "+commit.getName()+" is NOT reachable from any branch containing stable");
	}
}

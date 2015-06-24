import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.revwalk.RevCommit;

public class TestEnc01 {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		try (Git git = Git.open(new File(args[0]))) {
			System.out.println("opened repo at "+git.getRepository().getDirectory());
			for (RevCommit c : git.log().setMaxCount(10).call())
				System.out.println("Commit:" + c.getShortMessage()+ " ("+c.getName()+")");
			for (Map.Entry<String, Ref> entry : git.getRepository().getRefDatabase().getRefs(RefDatabase.ALL).entrySet()) {
				System.out.println("Found ref: "+entry.getKey());
			}
			for (Map.Entry<String, Ref> entry : git.getRepository().getRefDatabase().getRefs(Constants.R_TAGS).entrySet()) {
				System.out.println("Found tag: "+entry.getKey());
			}
		}
	}
}

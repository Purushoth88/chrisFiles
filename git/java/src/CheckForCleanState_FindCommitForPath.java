import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;

public class CheckForCleanState_FindCommitForPath {
	public static void main(String args[]) throws IOException, GitAPIException,
			JGitInternalException {
		
		FS fs = FS.DETECTED;
				RepositoryCache.FileKey key;
		key = RepositoryCache.FileKey.lenient(new File(args[0]), fs);
		Repository repo = new RepositoryBuilder().setFS(fs).setGitDir(key.getFile()).setMustExist(true).build();
		Git git=new Git(repo);
		if (!git.status().call().isClean())
			throw new IllegalStateException("Repo state is not clean.");
		// find last commit
		for (RevCommit commit : git.log().addPath(args[1]).setMaxCount(1).call())
			System.out.println("A commit touching path "+args[1]+" in repo "+git.getRepository()+": "+commit.getId());
	}
}

package org.eclipse.jgit.revwalk;
import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class RevWalkTests {
	public static void main(String args[]) throws IOException, GitAPIException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_DetermineWetherOneCommitIsMergedIntoAnother_"+System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: "+tmpDir);

		Git git = Git.init().setDirectory(tmpDir).call();

		RevCommit a = git.commit().setMessage("a").call();
		RevCommit b = git.commit().setMessage("b").call();
		RevCommit c = git.commit().setMessage("c").call();
		RevCommit d = git.commit().setMessage("d").call();
		RevCommit e = git.commit().setMessage("e").call();
		
		Ref side = git.branchCreate().setName("side").setStartPoint(c).call();
		git.checkout().setName(side.getName()).call();

		RevCommit f = git.commit().setMessage("f").call();
		RevCommit g = git.commit().setMessage("g").call();
		
		RevWalk rw;

		rw = new RevWalk(git.getRepository());		
		System.out.println("===\nInitialised the RevWalk");
		System.out.println(describe(rw));

		rw.markUninteresting(rw.lookupCommit(f));
		System.out.println("===\nmarked f as uninteresting");
		System.out.println(describe(rw));

		rw.markStart(rw.lookupCommit(e));
		rw.markStart(rw.lookupCommit(g));
		System.out.println("===\nMarked e,g commits as starting points");
		System.out.println(describe(rw));

		System.out.println("===\nStarting the walk by calling next()");
		for (RevCommit curr; (curr=rw.next())!=null ;)
			System.out.println("Inspecting entry: "+describe(curr));

		System.out.println("===\nWalk has finished");
		System.out.println(describe(rw));

		
	}	
	
	public static String describe(RevWalk rw) {
		StringBuffer b=new StringBuffer();
		b.append("RevWalk ("+rw.toString()+")\n");
		b.append("  Objects");
		for (RevObject ro: rw.objects) {
			b.append("\n    ");
			b.append(describe(ro));
		}
		return b.toString();
	}
	
	public static String describe(RevObject c) {
		StringBuffer b=new StringBuffer();
		b.append("RevObject: ");
		if (c instanceof RevCommit && ((c.flags & RevWalk.PARSED)!=0)) {
			b.append("\"");
			b.append(((RevCommit)c).getShortMessage());
			b.append("\" (");
			b.append(c.getName());
			b.append(")");
		} else
			b.append(c);
				
		b.append(", Flags: ");
		b.append(hasFlag("PARSED", RevWalk.PARSED, c.flags));
		b.append(hasFlag("REWRITE", RevWalk.REWRITE, c.flags));
		b.append(hasFlag("SEEN", RevWalk.SEEN, c.flags));
		b.append(hasFlag("TOPO_DELAY", RevWalk.TOPO_DELAY, c.flags));
		b.append(hasFlag("UNINTERESTING", RevWalk.UNINTERESTING, c.flags));	
		return(b.toString());
	}
	
	public static String hasFlag(String desc, int whichFlag, int flags) {
		return desc+":"+ (((flags & whichFlag) == 0) ? "0," : "1,");
	}
}

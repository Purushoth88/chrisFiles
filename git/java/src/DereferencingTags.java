import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

public class DereferencingTags {
	public static void main(String args[]) throws GitAPIException, IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTest_DereferencingTags_" + System.currentTimeMillis());
		tmpDir.mkdirs();
		System.out.println("Working dir: " + tmpDir);
		Git git = Git.init().setDirectory(tmpDir).call();
		FileWriter fw = new FileWriter(new File(tmpDir, "a"));
		fw.write("content of a");
		fw.close();
		git.add().addFilepattern(".").call();
		RevCommit commit = git.commit().setMessage("first").call();
		System.out.println("Commit had id: "+commit.getId());
	
		// create a lightweight/annotated tags lw1/aw1 pointing to a commit 
		Ref lw1 = git.tag().setAnnotated(false).setName("lw1").setObjectId(commit).call();
		Ref at1 = git.tag().setAnnotated(true).setName("at1").setObjectId(commit).setMessage("an annotated tag pointing to a commit").call();
		System.out.println("Annotated tag at1 had id: "+at1.getObjectId());
		RevTag at1_rt = RevTag.parse(git.getRepository().open(at1.getObjectId()).getBytes());
		
		// create a lightweight/annotated tags lw2/aw2 pointing to a annotated tag 
		Ref lw2 = git.tag().setAnnotated(false).setName("lw2").setObjectId(at1_rt).call();
		Ref at2 = git.tag().setAnnotated(true).setName("at2").setObjectId(at1_rt).setMessage("an annotated tag pointing to another annotated tag").call();
		Repository r = git.getRepository();
		System.out.println("Annotated tag at2 had id: "+at2.getObjectId());
		
		// print info
		describeRef(lw1, r);
		describeRef(at1, r);
		describeRef(lw2, r);
		describeRef(at2, r);
	}

	private static void describeRef(Ref ref, Repository r) {
		if (!ref.isPeeled())
			ref = r.peel(ref);
		System.out.println("Describe ref with name "+ref.getName());
		System.out.println("  ObjectId: "+ref.getObjectId());
		System.out.println("  Leaf: "+ref.getLeaf());
		System.out.println("  peeledObjectId: "+ref.getPeeledObjectId());
		System.out.println("  target.getName(): "+ref.getTarget().getName());
	}
}

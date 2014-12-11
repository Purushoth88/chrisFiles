import java.io.File;
import java.io.IOException;

public class FileTests {
	public static void main(String args[]) throws IOException,
			InterruptedException {
		String fnames[] = new String[] { ".git.", ".git~", ".git ", ".git~1" };

		File parentDir = new File(System.getProperty("java.io.tmpdir"),
				"fileTests." + System.currentTimeMillis());
		if (!parentDir.mkdir())
			System.exit(-1);
		System.out
				.println("java.version:" + System.getProperty("java.version"));
		System.out.println("os.name:" + System.getProperty("os.name"));

		for (String fname : fnames) {
			File f = new File(parentDir, fname);
			boolean rc = f.createNewFile();
			System.out.println("File(" + f.getAbsolutePath()
					+ ").createNewFile() returned:" + rc);
		}
		Process dirProcess = Runtime.getRuntime().exec("cmd /c dir /X "+parentDir.getAbsolutePath());
		dirProcess.waitFor();
	}
}

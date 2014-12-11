import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class FileTests {
	public static void main(String args[]) throws IOException,
			InterruptedException {
		String dirnames[] = new String[] { ".git", "GIT~1", ".a" };
		String fnames[] = new String[] { "a", "a.git", "abcdefghijklmo", "abcdefghij.klmodfssfg", ".git/a", "a.b", "GIT~1", ".git.", ".git~", ".git ", ".git~1", ".git"};
		File parentDir = new File(System.getProperty("java.io.tmpdir"),
				"fileTests." + System.currentTimeMillis());
		if (!parentDir.mkdir())
			System.exit(-1);
		System.out
				.println("java.version:" + System.getProperty("java.version"));
		System.out.println("os.name:" + System.getProperty("os.name"));
		for (String dname : dirnames) {
			File f = new File(parentDir, dname);
			boolean rc = f.mkdirs();
			System.out.println("Directory(\"" + f.getAbsolutePath()
					+ "\").mkdirs() returned:" + rc);
		}
		for (String fname : fnames) {
			File f = new File(parentDir, fname);
			boolean rc = f.createNewFile();
			System.out.println("File(\"" + f.getAbsolutePath()
					+ "\").createNewFile() returned:" + rc);
		}
		final Process dirProcess = Runtime.getRuntime().exec(
				"cmd /c dir /X " + parentDir.getAbsolutePath()+ " .git GIT~1");
		dirProcess.getOutputStream();
		copyTo(dirProcess.getInputStream(), System.out);
		copyTo(dirProcess.getErrorStream(), System.err);
		dirProcess.waitFor();
	}
	static void copyTo(final InputStream in, final OutputStream out) {
		new Thread() {public void run() {int b;try {b = in.read();while (b != -1) {out.write(b);b = in.read();}} catch (IOException e) {e.printStackTrace();}}}.start();
	}
}
import java.io.File;
import java.io.IOException;

public class TestFileSystem {
	private static final int NR_OF_FILES = 1000;

	public static void main(String[] args) throws IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());
		tmpDir.mkdirs();

		try {
			long[] timestamps = new long[NR_OF_FILES + 1];

			for (int i = 0; i < NR_OF_FILES; i++) {
				timestamps[i] = System.currentTimeMillis();
				File f = new File(tmpDir, String.valueOf(i));
				f.createNewFile();
			}

			for (int i = 0; i < NR_OF_FILES; i++) {
				File f = new File(tmpDir, String.valueOf(i));
				System.out.println("File #" + i
						+ ": System timer when creating file: " + timestamps[i]
						+ ".\t lastModified of created file:"
						+ f.lastModified());
			}
		} finally {
			delete(tmpDir);
		}
	}

	static void delete(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				delete(c);
		f.delete();
	}
}

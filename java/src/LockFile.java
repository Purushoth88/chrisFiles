import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class LockFile {
	public enum OPS {
		WRITE, CAT, CREATE, UNLOCKX, UNLOCK, LOCKX, LOCK, DELETE, CLOSEOUTPUTSTREAM, COS, CLOSEINPUTSTREAM, CIS, OPENINPUTSTREAM, OIS, OPENOUTPUTSTREAM, OOS, QUIT
	};

	public static void main(String args[]) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel channel;
		FileLock lock = null;
		FileLock lockx = null;
		StringBuilder commands = new StringBuilder();
		for (OPS o : OPS.values())
			commands.append(o.toString().toLowerCase() + " ");
		StringBuilder usage = new StringBuilder("usage: LockFile <FileName>");
		String lastLine;
		BufferedReader sysInReader = new BufferedReader(new InputStreamReader(
				System.in));
		File f = (args.length == 1) ? new File(args[0]) : null;
		if (f == null || !f.exists()) {
			System.err.println(usage);
			System.exit(-1);
		}
		try {
			OPS op = null;
			do {
				System.out.println("Possible commands: " + commands);
				System.out.print("Enter command:");
				lastLine = sysInReader.readLine();
				op = getEnumFromString(OPS.class, lastLine);
				if (op == null) {
					System.out.println("Unknown command: <" + lastLine + ">");
					continue;
				}
				switch (op) {
				case CAT:
					System.out.println("Content of file " + f.getPath() + ":");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(f)));
					lastLine = br.readLine();
					while (lastLine != null) {
						System.out.println(lastLine);
						lastLine = br.readLine();
					}
					System.out.println("EOF");
					br.close();
					break;
				case CIS:
				case CLOSEINPUTSTREAM:
					if (fis == null) {
						System.out.println("fatal: no inputstream created");
						break;
					}
					fis.close();
					fis = null;
					System.out.println("closed FileInputStream");
					break;
				case COS:
				case CLOSEOUTPUTSTREAM:
					if (fos == null) {
						System.out.println("fatal: no outputstream created");
						break;
					}

					fos.close();
					fos = null;
					System.out.println("closed FileOutputStream");
					break;
				case CREATE:
					boolean createRc = f.createNewFile();
					System.out.println("Creating new file " + f.getPath()
							+ " lead to rc:" + createRc);
					break;
				case OIS:
				case OPENINPUTSTREAM:
					if (fis != null) {
						System.out.println("fatal: inputstream already created");
						break;
					}
					fis = new FileInputStream(f);
					System.out.println("created FileInputStream");
					break;
				case OOS:
				case OPENOUTPUTSTREAM:
					if (fos != null) {
						System.out.println("fatal: outputstream already created");
						break;
					}
					fos = new FileOutputStream(f);
					System.out.println("created FileOutputStream");
					break;
				case DELETE:
					boolean deleteRc = f.delete();
					System.out.println("Deleting file " + f.getPath()
							+ " lead to rc:" + deleteRc);
					break;
				case LOCK:
					System.out
							.println("About to lock a file through new RandomAccessFile(f, \"r\").getChannel().lock(0, 1, true)");
					channel = new RandomAccessFile(f, "r").getChannel();
					lock = channel.lock(0, 1, true);
					System.out
							.println("Locked file through new RandomAccessFile(f, \"r\").getChannel().lock(0, 1, true)");
					break;
				case LOCKX:
					System.out
							.println("About to lock a file through new RandomAccessFile(f, \"rw\").getChannel().lock()");
					channel = new RandomAccessFile(f, "rw").getChannel();
					lockx = channel.lock();
					System.out
							.println("Locked file through new RandomAccessFile(f, \"rw\").getChannel().lock()");
					break;
				case UNLOCK:
					if (lock == null) {
						System.out.println("fatal: file not locked");
						break;
					}
					lock.release();
					lock = null;
					System.out.println("unlocked file which was shared locked");
					break;
				case UNLOCKX:
					if (lockx == null) {
						System.out.println("fatal: file not locked");
						break;
					}
					lockx.release();
					lockx = null;
					System.out.println("unlocked file which was shared locked");
					break;
				case WRITE:
					if (fos == null) {
						System.out
								.println("fatal: not fileoutputstream has been created");
						break;
					}
					PrintWriter pw = new PrintWriter(fos, true);
					pw.println("Writing content to stream from process "
							+ ManagementFactory.getRuntimeMXBean().getName());
					break;
				case QUIT:
					break;
				default:

					break;
				}
			} while (op != OPS.QUIT);
		} finally {
			if (lock != null) {
				lock.release();
				System.out.println("Implicitly released shared lock");
			}
			if (lockx != null) {
				lockx.release();
				System.out.println("Implicitly released exclusive lock");
			}
			if (fis != null) {
				fis.close();
				System.out.println("Implicitly closed inputstream");
			}
			if (fos != null) {
				fos.close();
				System.out.println("Implicitly closed outputstream");
			}
		}
	}

	public static <T extends Enum<T>> T getEnumFromString(Class<T> c,
			String string) {
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
			}
		}
		return null;
	}
}

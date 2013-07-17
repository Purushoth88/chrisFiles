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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileOp {
	public enum OPS {
		write, cat, create, unlockx, unlock, lockx, lock, delete, closeOutputStream, closeInputStream, openInputStream, openOutputStream, quit
	};

	public static void main(String args[]) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel channel;
		FileLock lock = null;
		FileLock lockx = null;
		StringBuilder commands = new StringBuilder();
		Map<String, OPS> variant2command=new HashMap<>();
		for (OPS o : OPS.values()) {
			Iterator<String> variants = abbrevCamelCase(o.name()).iterator();
			String variant = variants.next();
			commands.append(variant);
			variant2command.put(variant,  o);
			if (variants.hasNext()) {
				commands.append("(");
				StringBuilder sb = new StringBuilder();
				sb.append(var.next());
					while (strings.hasNext()) {
						sb.append(delimiter);
						sb.append(strings.next());
					}
					return sb.toString();
				} else
					return "";

				commands.append(join(",", variants));
				commands.append(")");
			}
		}
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
				case cat:
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
				case closeInputStream:
					if (fis == null) {
						System.out.println("fatal: no inputstream created");
						break;
					}
					fis.close();
					fis = null;
					System.out.println("closed FileInputStream");
					break;
				case closeOutputStream:
					if (fos == null) {
						System.out.println("fatal: no outputstream created");
						break;
					}

					fos.close();
					fos = null;
					System.out.println("closed FileOutputStream");
					break;
				case create:
					boolean createRc = f.createNewFile();
					System.out.println("Creating new file " + f.getPath()
							+ " lead to rc:" + createRc);
					break;
				case openInputStream:
					if (fis != null) {
						System.out.println("fatal: inputstream already created");
						break;
					}
					fis = new FileInputStream(f);
					System.out.println("created FileInputStream");
					break;
				case openOutputStream:
					if (fos != null) {
						System.out.println("fatal: outputstream already created");
						break;
					}
					fos = new FileOutputStream(f);
					System.out.println("created FileOutputStream");
					break;
				case delete:
					boolean deleteRc = f.delete();
					System.out.println("Deleting file " + f.getPath()
							+ " lead to rc:" + deleteRc);
					break;
				case lock:
					System.out
							.println("About to lock a file through new RandomAccessFile(f, \"r\").getChannel().lock(0, 1, true)");
					channel = new RandomAccessFile(f, "r").getChannel();
					lock = channel.lock(0, 1, true);
					System.out
							.println("Locked file through new RandomAccessFile(f, \"r\").getChannel().lock(0, 1, true)");
					break;
				case lockx:
					System.out
							.println("About to lock a file through new RandomAccessFile(f, \"rw\").getChannel().lock()");
					channel = new RandomAccessFile(f, "rw").getChannel();
					lockx = channel.lock();
					System.out
							.println("Locked file through new RandomAccessFile(f, \"rw\").getChannel().lock()");
					break;
				case unlock:
					if (lock == null) {
						System.out.println("fatal: file not locked");
						break;
					}
					lock.release();
					lock = null;
					System.out.println("unlocked file which was shared locked");
					break;
				case unlockx:
					if (lockx == null) {
						System.out.println("fatal: file not locked");
						break;
					}
					lockx.release();
					lockx = null;
					System.out.println("unlocked file which was shared locked");
					break;
				case write:
					if (fos == null) {
						System.out
								.println("fatal: not fileoutputstream has been created");
						break;
					}
					PrintWriter pw = new PrintWriter(fos, true);
					pw.println("Writing content to stream from process "
							+ ManagementFactory.getRuntimeMXBean().getName());
					break;
				case quit:
					break;
				default:

					break;
				}
			} while (op != OPS.quit);
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

	public static List<String> abbrevCamelCase(String orig) {
		LinkedList<String> ret = new LinkedList<>();
		ret.add(orig);
		String[] split = orig.split("(?<=[a-z])(?=[A-Z])");
		if (split.length > 1) {
			ret.add(orig.toLowerCase());
			StringBuffer sb = new StringBuffer();
			for (String part : split)
				sb.append(part.substring(1, 1).toLowerCase());
			ret.add(sb.toString());
		}
		return ret;
	}

	public static String join(String delimiter, Iterator<String> strings) {
		if (strings.hasNext()) {
			StringBuilder sb = new StringBuilder();
			sb.append(strings.next());
			while (strings.hasNext()) {
				sb.append(delimiter);
				sb.append(strings.next());
			}
			return sb.toString();
		} else
			return "";
	}
}

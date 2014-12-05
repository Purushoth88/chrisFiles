import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileOp {
	public static void main(String args[]) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel channel;
		FileLock lock = null;
		FileLock lockx = null;
		String line;
		BufferedReader sysInReader = new BufferedReader(new InputStreamReader(
				System.in));

		StringBuilder usage = new StringBuilder("usage: "+FileOp.class.getName()+" <FileName>");
		if (args.length == 0 || args.length > 1
				|| "-h".equalsIgnoreCase(args[0])) {
			System.err.println(usage);
			System.exit(-1);
		}
		File f = new File(args[0]);

		try {
			String cmd = null;
			do {
				System.out
						.println("Possible commands: cat, closeFileInputStream|cfis, closeFileOutputStream|cfos, createNewFile|cnf, delete|del, exists, lock, lockx, newFileInputstream|nfis, newFileOutputStream|nfos, unlock, unlockx, write, rename <newFile>, quit");
				System.out.print("Enter command:");
				try {
					line = sysInReader.readLine();
					String[] tokens = line.split(" ");
					cmd = tokens[0];
					if (cmd.equalsIgnoreCase("cat")) {
						if (fis == null)
							System.out
									.println("fatal: no inputstream created");
						else {
							System.out.println("Content of file " + f.getPath()
									+ ":");
							for (;;) {
								int d = fis.read();
								if (d == -1)
									break;
								System.out.write(d);
							}
							System.out.println("EOF");
						}
					} else if (cmd.equalsIgnoreCase("closeFileInputStream")
							|| cmd.equalsIgnoreCase("cfis")) {
						if (fis == null)
							System.out.println("fatal: no fileInputStream created");
						else {
							fis.close();
							fis = null;
							System.out.println("closed FileInputStream");
						}
					} else if (cmd.equalsIgnoreCase("closeFileOutputStream")
							|| cmd.equalsIgnoreCase("cfos")) {
						if (fos == null)
							System.out
									.println("fatal: no outputstream created");
						else {
							fos.close();
							fos = null;
							System.out.println("closed FileOutputStream");
						}
					} else if (cmd.equalsIgnoreCase("createNewFile")
							|| cmd.equalsIgnoreCase("cnf")) {
						boolean createRc = f.createNewFile();
						System.out.println("Creating new file " + f.getPath()
								+ " lead to rc:" + createRc);
					} else if (cmd.equalsIgnoreCase("newFileInputstream")
							|| cmd.equalsIgnoreCase("nfis")) {
						if (fis != null)
							System.out
									.println("fatal: inputstream already created");
						else if (f == null)
							System.out
									.println("fatal: inputstream already created");
						else {
							fis = new FileInputStream(f);
							System.out.println("created FileInputStream");
						}
					} else if (cmd.equalsIgnoreCase("newFileOutputStream")
							|| cmd.equalsIgnoreCase("nfos")) {
						if (fos != null)
							System.out
									.println("fatal: outputstream already created");
						else {
							fos = new FileOutputStream(f);
							System.out.println("created FileOutputStream");
						}
					} else if (cmd.equalsIgnoreCase("delete")
							|| cmd.equalsIgnoreCase("del")) {
						boolean deleteRc = f.delete();
						System.out.println("Deleting file " + f.getPath()
								+ " lead to rc:" + deleteRc);
					} else if (cmd.equalsIgnoreCase("lock")) {
						System.out
								.println("About to lock a file through new RandomAccessFile(f, \"r\").getChannel().lock(0, 1, true)");
						channel = new RandomAccessFile(f, "r").getChannel();
						lock = channel.lock(0, 1, true);
						System.out
								.println("Locked file through new RandomAccessFile(f, \"r\").getChannel().lock(0, 1, true)");
					} else if (cmd.equalsIgnoreCase("lockx")) {
						System.out
								.println("About to lock a file through new RandomAccessFile(f, \"rw\").getChannel().lock()");
						channel = new RandomAccessFile(f, "rw").getChannel();
						lockx = channel.lock();
						System.out
								.println("Locked file through new RandomAccessFile(f, \"rw\").getChannel().lock()");
					} else if (cmd.equalsIgnoreCase("unlock")) {
						if (lock == null)
							System.out.println("fatal: file not locked");
						else {
							lock.release();
							lock = null;
							System.out
									.println("unlocked a file which was previously locked with a shared lock");
						}
					} else if (cmd.equalsIgnoreCase("unlockx")) {
						if (lockx == null)
							System.out.println("fatal: file not locked");
						else {
							lockx.release();
							lockx = null;
							System.out
									.println("unlocked a file which was previously locked exclusively");
						}
					} else if (cmd.equalsIgnoreCase("write")) {
						if (fos == null)
							System.out
									.println("fatal: not fileoutputstream has been created");
						else {
							String data="Writing content to stream from process "
									+ ManagementFactory.getRuntimeMXBean()
									.getName()+"\n";
							fos.write(data.getBytes());
						}
					} else if (cmd.equalsIgnoreCase("exists")) {
						boolean existsRc = f.exists();
						System.out.println("File.exists("+f.getPath()+").exists "
								+ " lead to rc:" + existsRc);
					} else if (cmd.equalsIgnoreCase("rename")) {
						if (tokens.length < 2)
							System.err.println("No target filename specified.");
						else {
							boolean ret = f.renameTo(new File(tokens[1]));
							System.out.println("renaming file to path <"
									+ tokens[1] + "> lead to rc=" + ret);
							System.out
									.println("Further commands will continue to work on the old File object with path: "
											+ f.getPath());
						}
					} else if (!cmd.equalsIgnoreCase("quit"))
						System.out.println("Unknown command: <" + line + ">");
				} catch (IOException e) {
					System.err.println("catched and IOException: "
							+ e.toString());
				}
				System.out.println();
			} while (!cmd.equalsIgnoreCase("quit"));
		} finally {
			System.out.println("Leaving");
			try {
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
			} catch (IOException e) {
				System.err.println("catched and IOException: " + e.toString());
			}
		}
	}
}

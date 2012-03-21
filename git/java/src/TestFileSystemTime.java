import java.io.File;
import java.io.IOException;

public class TestFileSystemTime {
	private static final int NrOfFiles = 10000;

	public static void main(String[] args) throws IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
				+ System.currentTimeMillis());
		tmpDir.mkdirs();
		
		File[] files=new File[NrOfFiles];
		long[] timestamps=new long[NrOfFiles+1];
		
		for(int i=0; i<NrOfFiles; i++) {
			timestamps[i]=System.currentTimeMillis();
			files[i]=new File(tmpDir, String.valueOf(i));
			files[i].createNewFile();
		}
		timestamps[NrOfFiles]=System.currentTimeMillis();
		
		for(int i=0; i<NrOfFiles; i++) {
			File f=new File(tmpDir, String.valueOf(i));
			System.out.println("#"+i+": tsBefore: "+timestamps[i]+"/"+(timestamps[i+1]-timestamps[i])+" lastModified:"+f.lastModified()+"/"+(f.lastModified()-timestamps[i]));
		}
		System.out.println("#"+NrOfFiles+": tsBefore"+timestamps[NrOfFiles]);
	}
}

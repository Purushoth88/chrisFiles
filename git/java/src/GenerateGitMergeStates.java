import java.util.LinkedList;
import java.util.List;

public class GenerateGitMergeStates {
	enum PathState {
		NonExisiting, SameFile, OtherFile, SameDir, OtherDir
	};

	public static void main(String[] args) {
		List<String> ret = new LinkedList<>();
		ret.add("");
		ret = append(ret, PathState.values());
		ret = append(ret, PathState.values());
		ret = append(ret, PathState.values());
		ret = append(ret, new PathState[] {PathState.NonExisiting, PathState.SameFile, PathState.OtherFile});
		ret = append(ret, PathState.values());
		System.out.println("Base, Our, Theirs, Index, Worktree");
		for(String s:ret)
			System.out.println(s);
	}

	public static List<String> append(List<String> input, PathState[] states) {
		List<String> ret = new LinkedList<String>();
		for (String s : input) {
			int nextFile = 0;
			int nextDir = 0;
			for (char c : s.toCharArray()) {
				if (c >= 'f' && c <= 'z' && c == 'f' + nextFile)
					nextFile++;
				if (c >= 'D' && c <= 'Z' && c == 'D' + nextDir)
					nextDir++;
			}
			for (PathState state : states) {
				switch (state) {
				case NonExisiting:
					ret.add(append(s, '%', 0));
					break;
				case SameFile:
					for (int i = 0; i < nextFile; i++)
						ret.add(append(s, 'f', i));
					break;
				case SameDir:
					for (int i = 0; i < nextDir; i++)
						ret.add(append(s, 'D', i));
					break;
				case OtherFile:
					ret.add(append(s, 'f', nextFile));
					break;
				case OtherDir:
					ret.add(append(s, 'D', nextDir));
					break;
				}
			}
		}
		return ret;
	}

	public static String append(String prefix, char ch, int i) {
		char c = (char) (ch + i);
		return prefix + (prefix.isEmpty() ? "" : ", ") + c;
	}
}

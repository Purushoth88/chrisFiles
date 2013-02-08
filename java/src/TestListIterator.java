import java.util.LinkedList;
import java.util.ListIterator;


public class TestListIterator {
	public static void main(String args[]) {
		System.out.println("test listiterator");
		LinkedList<String> list = new LinkedList<String>();
		list.add("A");
		list.add("B");
		list.add("C");
		list.add("D");
		list.add("E");
		int indexOfC = list.indexOf("C");
		ListIterator<String> listIterator = list.listIterator(indexOfC);
		while(listIterator.hasNext())
			System.out.println(listIterator.next());
	}

}

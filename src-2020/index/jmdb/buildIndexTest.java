package index.jmdb;

import util.EnhInvertedList;
import util.EnhListEntry;
import util.EnhTargetList;
import util.InvertedList;
import util.ListEntry;
import util.Params;
import util.TargetList;

public class buildIndexTest {

	public static void main(String[] args) {
		Params.setExpDB("jmdb");
		TargetList basicList = new TargetList(new InvertedList("korean"), 1);
		basicList.openToScan();
		ListEntry e;
		
		System.out.println("basic list for korean");
		while((e = basicList.getNextEntry()) != null) {
			System.out.println(e);
		}
		System.out.println("------------------------");

		EnhTargetList enhList = new EnhTargetList(new EnhInvertedList("korean"), 1);
		enhList.openToScan();
		EnhListEntry h;
		System.out.println("enhanced list for korean");
		while((h = enhList.getNextEntry()) != null) {
			System.out.println(h);
		}
		System.out.println("------------------------");
	}

}

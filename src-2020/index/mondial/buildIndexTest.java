package index.mondial;

import util.EnhInvertedList;
import util.EnhListEntry;
import util.EnhTargetList;
import util.InvertedList;
import util.ListEntry;
import util.Params;
import util.TargetList;

public class buildIndexTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Params.setExpDB("mondial");
		TargetList basicList = new TargetList(new InvertedList("islands"), 1);
//		InvertedList enhList = new InvertedList("islands");
		basicList.openToScan();
		ListEntry e;
		
		System.out.println("basic list for atlantic");
		while((e = basicList.getNextEntry()) != null) {
			System.out.println(e);
		}
		System.out.println("------------------------");

		EnhTargetList enhList = new EnhTargetList(new EnhInvertedList("islands"), 1);
//		EnhInvertedList enhList = new EnhInvertedList("islands");
		enhList.openToScan();
		EnhListEntry h;
		System.out.println("enhanced list for atlantic");
		while((h = enhList.getNextEntry()) != null) {
			System.out.println(h);
		}
		System.out.println("------------------------");

	}

}

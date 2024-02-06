package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntegerBinding extends TupleBinding<Integer> {
	@Override
	public Integer entryToObject(TupleInput ti) {
		return ti.readInt();
	}

	@Override
	public void objectToEntry(Integer arg0, TupleOutput to) {
        to.writeInt(arg0);
	}
}

package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class FloatBinding extends TupleBinding<Float> {
	@Override
	public Float entryToObject(TupleInput ti) {
		return ti.readFloat();
	}

	@Override
	public void objectToEntry(Float arg0, TupleOutput to) {
        to.writeFloat(arg0);
	}
}

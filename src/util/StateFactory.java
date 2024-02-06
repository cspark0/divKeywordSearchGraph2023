package util;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import util.State;

public class StateFactory extends BasePooledObjectFactory<State> {

	public StateFactory() {}

    @Override
    public State create() {
        return new State();
    }

    /**
     * Use the default PooledObject implementation.
     */
    @Override
    public PooledObject<State> wrap(State st) {
        return new DefaultPooledObject<State>(st);
    }

    /**
     * When an object is returned to the pool, clear the st.
     */
    @Override
    public void passivateObject(PooledObject<State> pooledObject) {
        pooledObject.getObject().reset();
//		st.answer.clear();	// clear the List
    }

    // for all other methods, the no-op implementation
    // in BasePooledObjectFactory will suffice
}

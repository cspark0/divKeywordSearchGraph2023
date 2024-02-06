package util;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import util.AnswerTreeSet;

public class AnswerTreeSetFactory extends BasePooledObjectFactory<AnswerTreeSet> {

    @Override
    public AnswerTreeSet create() {
        return new AnswerTreeSet();
    }

    /**
     * Use the default PooledObject implementation.
     */
    @Override
    public PooledObject<AnswerTreeSet> wrap(AnswerTreeSet s) {
        return new DefaultPooledObject<AnswerTreeSet>(s);
    }

    /**
     * When an object is returned to the pool, clear the st.
     */
    @Override
    public void passivateObject(PooledObject<AnswerTreeSet> pooledObject) {
        pooledObject.getObject().clear();
    }

    // for all other methods, the no-op implementation
    // in BasePooledObjectFactory will suffice
}

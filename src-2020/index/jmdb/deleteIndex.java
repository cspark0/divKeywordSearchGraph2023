package index.jmdb;

import nkmap.bdb.NKMapDatabasePut;
import util.Params;
import index.IndexBuilder;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Set;


public class deleteIndex {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Params.setExpDB("jmdb");
        IndexBuilder ib = new IndexBuilder();
        NKMapDatabasePut.deleteFromNKMap("8", 0);
    }
}

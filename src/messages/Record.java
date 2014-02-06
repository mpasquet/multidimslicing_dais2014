package messages;

import peersim.core.Node;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 26/12/11
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public class Record {
    
    public Node node;
    public long age;    // CHANGE TO AGE!!!
    public int attribute;
    
    public Record(Node n, long t, int a){
        this.node = n;
        this.age = t;
        this.attribute = a;
    }
    
    @Override
    public boolean equals(Object a){
        Record na = (Record)a;
        return na.node.equals(node);
    }
    
}

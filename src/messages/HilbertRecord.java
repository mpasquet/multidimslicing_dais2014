package messages;

import peersim.core.Node;
import java.math.BigInteger;

public class HilbertRecord extends Record {
    
    public BigInteger attribute2;
    
    public HilbertRecord(Node n, long t, BigInteger a){
        super(n, t, 0);
        this.attribute2 = a;
    }
    
    @Override
    public boolean equals(Object a){
        Record na = (Record)a;
        return na.node.equals(node);
    }
    
}

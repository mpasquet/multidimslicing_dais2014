package messages;

import peersim.core.Node;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 4/1/12
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
 */
public class CyclonRecord {

    public Node node;
    public long age;
    public int slice;

    public CyclonRecord(Node n, long age, int slice){
        this.node = n;
        this.age = age;
        this.slice = slice;
    }
    
    public boolean equals(Object o){
        boolean res = false;
        CyclonRecord other = (CyclonRecord) o;
        if(other.node.equals(this.node)){
            res = true;
        }
        return res;

    }

    public Object clone()
    {
        CyclonRecord res = new CyclonRecord(node,age,slice);

        return res;
    }
}

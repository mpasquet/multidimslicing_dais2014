package messages;

import peersim.core.Node;

public class SkylineRecord extends Record {

    public int[] attributes;

    public SkylineRecord(Node n, long t, int[] a) {
        super(n, t, 0);
        this.attributes = a;
    }
}

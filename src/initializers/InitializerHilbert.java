package initializers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import protocols.Hilbert;
import util.HilbertConverter;
import messages.Record;
import messages.HilbertRecord;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.math.BigInteger;

public class InitializerHilbert implements Control, NodeInitializer {


// ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The load at the peak node.
     *
     * @config
     */
    private static final String PAR_VALUE = "max_value";

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Value at the peak node.
     * Obtained from config property {@link #PAR_VALUE}. */
    private final int maxvalue;

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public InitializerHilbert(String prefix) {
        maxvalue = Configuration.getInt(prefix + "." + PAR_VALUE);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);

    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Return the expected position of each hilbert element in the curve
     */
    public int[] hsort(int[][] features) {
        class IComparable implements Comparable {
            BigInteger val;
            int idx;
            public IComparable(BigInteger val, int idx) {this.val = val; this.idx = idx;}
            public int compareTo(Object elem) {
                if (!(elem instanceof IComparable))
                    return -1;
                return val.compareTo(((IComparable)elem).val);
            }
        }
        IComparable[] recs = new IComparable[features.length];
        for (int i = 0; i < recs.length; i++) {
            recs[i] = new IComparable(HilbertConverter.Hilbert_to_int(features[i]),i);
        }
        ArrayList<IComparable> t = new ArrayList<IComparable>(recs.length);
        for (IComparable a: recs)
            t.add(a);
        java.util.Collections.sort(t);
        int[] v = new int[recs.length];
        for (int i = 0; i < recs.length; i++) {
            v[t.get(i).idx] = (int)Math.floor(((float)i/recs.length)*4);
        }
        return v;
    }

    public static int compare(int[] elems1, int[] elems2) {
        boolean inf = true, sup = true;
        int ret = 0;
        for (int i = 0; i < elems1.length; i++) {
            if (elems1[i] < elems2[i]) {
                sup = false;
            } else if (elems1[i] > elems2[i]) {
                inf = false;
            }
        }
        if ((sup && inf) || (!sup && !inf))
            ret = 0;
        else if (sup)
            ret = 1;
        else if (inf)
            ret = -1;
        return ret;
    }

    public double[] sort(int[][] elems, double[] values_) {
        double[] values = (double[]) values_.clone();

        int[] ret = new int[Network.size()];
        for (int i = 0; i < Network.size(); i++) {
            for (int j = 0; j < Network.size(); j++) {
                int cmp = compare(elems[i], elems[j]);
                double tmp;
                if ((cmp == 1 && values[i] < values[j]) ||
                        (cmp == -1 && values[i] > values[j])) {
                    tmp = values[i];
                    values[i] = values[j];
                    values[j] = tmp;
                }
            }
        }
        return values;
    }


    /**
     * Initialize an aggregation protocol using a peak distribution.
     * That is, one node will get the peek value, the others zero.
     * @return always false
     */
    public boolean execute() {
        int N = 2;
        int[][] elems = new int[Network.size()][];
        for (int i = 0; i < Network.size(); i++) {
            int[] newelem = new int[N];
            for (int j = 0; j < N; j++) {
                int rand = (int) Math.abs(CommonState.r.nextInt());
                newelem[j] = rand;
            }
            elems[i] = newelem;
        }

        int[] slices = hsort(elems);

        for (int i = 0; i < Network.size(); i++) {
            Hilbert prot = (Hilbert) Network.get(i).getProtocol(pid);
            prot.attributes = elems[i];
            prot.expected = slices[i];

            prot.recs = new LinkedBlockingQueue<HilbertRecord>(prot.viewsize);
            prot.view = new ArrayList<Node>();
            prot.position = 0;
            prot.slice = 0;
            prot.slicechanges = 0;
            prot.periodslicechanges = 0;
        }

        return false;
    }

    public void initialize(Node node) {
        Hilbert prot = (Hilbert) node.getProtocol(pid);
        prot.attributes = new int[]{Math.abs(CommonState.r.nextInt() % 1000), Math.abs(CommonState.r.nextInt() % 1000)};

        prot.recs = new LinkedBlockingQueue<HilbertRecord>(prot.viewsize);
        prot.view = new ArrayList<Node>();
        prot.position = 0;
        prot.slice = 0;
        prot.slicechanges = 0;
        prot.periodslicechanges = 0;
    }
}

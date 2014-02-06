package initializers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import protocols.Skyline;
import messages.Record;
import messages.SkylineRecord;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 29/12/11
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
public class InitializerSkyline implements Control, NodeInitializer {


// fields: bogomips cache disk free freq mem swap


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
    public InitializerSkyline(String prefix) {
        maxvalue = Configuration.getInt(prefix + "." + PAR_VALUE);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);

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

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Initialize an aggregation protocol using a peak distribution.
     * That is, one node will get the peek value, the others zero.
     * @return always false
     */
    public boolean execute() {
        /*
        int e = Math.abs(CommonState.r.nextInt() % 300);
        for (int i = 0; i < Network.size(); i++) {
            Skyline prot = (Skyline) Network.get(i).getProtocol(pid);
            //prot.attributes = features[i];
            prot.attributes = new int[]{features[i][0], features[i][1]};
            prot.expected = reference[i];
            prot.position = 0;
            prot.sorted = (double)i/Network.size();
            prot.slice = 0;
            prot.slicechanges = 0;
            prot.periodslicechanges = 0;
            if (e == i) {
                prot.elected = true;
            }
        }
        /*/

        int N = 2;
        int[][] elems = new int[Network.size()][];
        double[] values = new double[Network.size()];
        for (int i = 0; i < Network.size(); i++) {
            int[] newelem = new int[N];
            for (int j = 0; j < N; j++) {
                int rand = (int) Math.abs(CommonState.r.nextInt());
                newelem[j] = rand;
            }
            elems[i] = newelem;
            values[i] = CommonState.r.nextDouble();
        }

        double[] values_sorted = sort(elems, values);
        int[] sorted = new int[Network.size()];
        for (int i = 0; i < Network.size(); i++) {
            sorted[i] = (int) Math.floor(values[i]*4);
        }

        for (int i = 0; i < Network.size(); i++) {
            Skyline prot = (Skyline) Network.get(i).getProtocol(pid);
            //prot.attributes = new int[]{Math.abs(j + CommonState.r.nextInt() % 100), j + Math.abs(CommonState.r.nextInt() % 100)};
            prot.attributes = elems[i];
            prot.sorted = values[i];
            prot.expected = sorted[i];
            prot.sorted_expected = values_sorted[i];
            prot.slice = (int) Math.floor(4 * values[i]);

            prot.position = 0;
            prot.slicechanges = 0;
            prot.periodslicechanges = 0;
        }

        return false;
    }

    public void initialize(Node node) {
        Skyline prot = (Skyline) node.getProtocol(pid);
        prot.attributes = new int[]{Math.abs(CommonState.r.nextInt() % 1000), Math.abs(CommonState.r.nextInt() % 1000)};

        prot.position = 0;
        prot.slice = 0;
        prot.slicechanges = 0;
        prot.periodslicechanges = 0;
    }
}

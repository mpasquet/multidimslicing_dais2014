package initializers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import protocols.Cyclon;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 14/2/12
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class InitializerCyclon implements NodeInitializer{

    private static final String PAR_PROT = "protocol";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Value at the peak node.
     * Obtained from config property {@link #PAR_VALUE}. */


    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public InitializerCyclon(String prefix) {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);

    }

    public void initialize(Node node) {

            Cyclon cyc = (Cyclon) node.getProtocol(pid);
            for(int i=0;i<20;i++){
                int j = CommonState.r.nextInt(Network.size());
                cyc.addNeighbor(Network.get(j));
            }


        }

}

package observers;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import protocols.Slicing;
import protocols.Skyline;

import java.util.HashMap;

public class OmniscientObserver implements Control {


    private static final String PAR_SLICES = "slices";
    private static final String PAR_PROT = "slicing";
    private static final String PAR_PREFIX = "prefix";

    private final String name;
    private final String prefix;

    private final int syssize;

    private final int pid;
    private int i = 0;


    private HashMap<Integer,Integer> stats;


    public OmniscientObserver(String name) {
        this.name = name;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        prefix = Configuration.getString(name + "." + PAR_PREFIX);
        syssize = Network.size();
    }


    public boolean execute() {

        long time = peersim.core.CommonState.getTime();
        int violations = 0;
        double max = 0;
        int misplaced = 0;
        int violations_num = 0;
        for (int i = 0; i < Network.size(); i++) {

            int local_violations = 0;

            Skyline protocol = (Skyline) Network.get(i).getProtocol(pid);
            for (int j = 0; j < Network.size(); j++) {
                Skyline other = (Skyline) Network.get(j).getProtocol(pid);
                int cmp = protocol.compareTo(other.attributes);
                if ((cmp == 1 && protocol.sorted < other.sorted) || (cmp == -1 && protocol.sorted > other.sorted))
                    local_violations++;
            }
            if (local_violations != 0)
                violations_num++;
            max = Math.max(max, local_violations);
            violations += local_violations;
            if (protocol.slice != protocol.expected) {
                System.out.println(prefix + " NODE DIFF " + protocol.slice + " " + protocol.expected);
                misplaced++;
            }
        }

        System.out.println(prefix + " MISPLACED " + misplaced);
        System.out.println(prefix + " VIOLATIONS " + violations + " " + (int) max + " " + violations_num);
        return false;
    }
}

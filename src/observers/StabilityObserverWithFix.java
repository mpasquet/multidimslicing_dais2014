package observers;


import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import protocols.OurFix;

import java.util.HashMap;

public class StabilityObserverWithFix implements Control {

    private static final String PAR_PROT = "slicing";

    private int pid;

    private static final String PAR_SLICES = "slices";

    private static final String PAR_PREFIX = "prefix";


    private final String name;
    private final String pref;

    private final int slices;
    private final int syssize;

    private HashMap<Integer,Integer> stats;


    public StabilityObserverWithFix(String name) {
        pid = Configuration.getPid(name + "." + PAR_PROT);
        this.name = name;
        slices = Configuration.getInt(name + "." + PAR_SLICES, -1);
        pid = Configuration.getPid(name + "." + PAR_PROT);
        stats = new HashMap<Integer, Integer>();
        syssize = Network.size();
        pref = Configuration.getString(name+"."+PAR_PREFIX);
    }


    public boolean execute() {

        long time = peersim.core.CommonState.getTime();

        for (int i = 0; i < Network.size(); i++) {

            OurFix protocol = (OurFix) Network.get(i)
                    .getProtocol(pid);

            int s = protocol.currentSlice;
            Integer tot = stats.get(s);
            if(tot==null){
                stats.put(s,1);
            }
            else {
                tot = tot + 1;
                stats.put(s,tot);
            }

            System.out.println(pref+" CHANGES " + time + " " + i + " " + protocol.changes);
            protocol.changes = 0;
        }

        System.out.print(pref+" SLICES " + time + " " + Network.size() + " ");
        for(Integer i :stats.keySet()){
            Integer n = stats.get(i);
            System.out.print(n + " ");
        }
        System.out.println();

        stats = new HashMap<Integer, Integer>();

        return false;
    }
}

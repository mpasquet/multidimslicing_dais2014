package observers;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import protocols.Slicing;

import java.util.HashMap;

public class StabilityObserver implements Control {


    private static final String PAR_SLICES = "slices";
    private static final String PAR_PROT = "slicing";
    private static final String PAR_PREFIX = "prefix";

    private final String name;
    private final String prefix;

    private final int slices;
    private final int syssize;

    private final int pid;
    private int i = 0;


    private HashMap<Integer,Integer> stats;


    public StabilityObserver(String name) {
        this.name = name;
        slices = Configuration.getInt(name + "." + PAR_SLICES, -1);
        pid = Configuration.getPid(name + "." + PAR_PROT);
        stats = new HashMap<Integer, Integer>();
        prefix = Configuration.getString(name + "." + PAR_PREFIX);
        syssize = Network.size();
    }


    public boolean execute() {

        long time = peersim.core.CommonState.getTime();
        int changes = 0;
        int deviation = 0;
        int deviations = 0;
        for (int i = 0; i < Network.size(); i++) {


            Slicing protocol = (Slicing) Network.get(i)
                    .getProtocol(pid);

            deviation = (int) Math.abs(protocol.expected - protocol.slice);
            int s = protocol.slice;
            Integer tot = stats.get(s);
            if(tot==null){
                stats.put(s,1);
            }
            else {
                tot = tot + 1;
                stats.put(s,tot);
            }
            System.out.println(prefix+" CHANGES " + time + " " + i + " " + protocol.slicechanges + " " + s + " " + deviation);

            changes += protocol.slicechanges;
            deviations += deviation;
            protocol.slicechanges = 0;
        }

        System.out.println(prefix + " SUM " + changes);
        System.out.println(prefix + " DEVIATIONS " + deviations);
        System.out.print(prefix+" SLICES " + time + " " + Network.size() + " ");
        for(Integer i :stats.keySet()){
            Integer n = stats.get(i);
            System.out.print(n + " ");
        }
        System.out.println();
        for (Integer i: stats.keySet()) {
            System.out.print(i + " ");
        }
        System.out.println();

        stats = new HashMap<Integer, Integer>();

        i++;
        return false;
    }
}

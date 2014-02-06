package protocols;


import messages.CyclonRecord;
import messages.SkylineRecord;
import messages.SkylineMessage;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.core.CommonState;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

public class Skyline extends Slicing implements CDProtocol, EDProtocol {

    //view size
    private static final String PAR_VIEWSIZE = "memorylimit";
    //number of slices
    private static final String PAR_SLICES = "slices";


    public static int N = 20;
    public int k = -1;
    public int viewsize = -1;
    private boolean init = true;

    public LinkedBlockingQueue<SkylineRecord> recs;
    public ArrayList<SkylineRecord> supView;
    public ArrayList<SkylineRecord> infView;
    public int[] attributes;
    public float position;
    public boolean elected = false;

    // the sorted attribute
    public double sorted = 0;
    public double sorted_expected = 0;

    // a simple switch to alternate what we do each cycle
    public boolean toggle = true;

    public Skyline(String prefix) {
        super();
        k = Configuration.getInt(prefix+"."+PAR_SLICES, 4);
        viewsize = Configuration.getInt(prefix+"."+PAR_VIEWSIZE, 10);
        infView = new ArrayList<SkylineRecord>();
        supView = new ArrayList<SkylineRecord>();
        attributes = new int[2];
    }

    /**
     * Returns 1 if attributes1 is &gt; attributes2, -1 if attributes2 &gt; attributes1, and 0 otherwise
     */
    public static int compareSkylinePosition(int[] attributes1, int[] attributes2) {
        boolean inf = true, sup = true;
        int ret = 0;
        for (int i = 0; i < attributes1.length; i++) {
            if (attributes1[i] < attributes2[i]) {
                sup = false;
            } else if (attributes1[i] > attributes2[i]) {
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

    public int compareTo(int[] attributes2) {
        return compareSkylinePosition(attributes, attributes2);
    }

    public static boolean equals(int[] attributes1, int[] attributes2) {
        for (int i = 0 ; i < attributes1.length; i++) {
            if (attributes1[i] != attributes2[i])
                return false;
        }
        return true;
    }

    public boolean equals(int[] attributes2) {
        return equals(attributes, attributes2);
    }

    private void increaseAge() {
        for (SkylineRecord r: infView) {
            r.age++;
        }
        for (SkylineRecord r: supView) {
            r.age++;
        }
    }

    public void nextCycle( Node node, int pid )
    {
        Cyclon linkable = (Cyclon) node.getProtocol(FastConfig.getLinkable(pid));

        ArrayList<Node> psview = new ArrayList<Node>();

        if (init) {
            if (linkable.degree() > 0)
            {
                for(int i=0; i<linkable.degree() && i < 20;i++){
                    Node peern = linkable.getNeighbor(i);
                    psview.add(peern);
                }
            }

            // Value exchange for dissemination
            for(Node peern: psview) {
                if(!peern.isUp()) continue;

                ((Transport)node.getProtocol(FastConfig.getTransport(pid))).
                        send(node, peern,
                                new messages.SkylineMessage(attributes, sorted, node),
                                pid);
            }
        }
        double s = sorted;
        if (toggle) {
            ArrayList<Skyline> view = new ArrayList<Skyline>();
            for (Node n: psview) {
                Skyline elem = (Skyline) n.getProtocol(pid);
                int cmp = compareSkylinePosition(attributes, elem.attributes);
                if ((cmp == 1 && elem.sorted > sorted) || (cmp == -1 && elem.sorted < sorted))
                    view.add(elem);
            }
            Collections.shuffle(view, CommonState.r);
            if (!view.isEmpty()) {
                Skyline elem = view.remove(0);
                //System.out.println("EXCHANGE CYCLON" + print(elem.attributes) + print(attributes) + " " + elem.sorted + " " + sorted);
                double tmp = sorted;
                sorted = elem.sorted;
                elem.sorted = tmp;
                elem.slice = (int) Math.floor(k*elem.sorted);
            }
        } else {
            ArrayList<SkylineRecord> rview = new ArrayList<SkylineRecord>();
            rview.addAll(supView);
            rview.addAll(infView);
            ArrayList<Skyline> view = new ArrayList<Skyline>();
            for (SkylineRecord record: rview) {
                Skyline elem = (Skyline) record.node.getProtocol(pid);
                int cmp = compareSkylinePosition(attributes, elem.attributes);
                if ((cmp == 1 && elem.sorted > sorted) || (cmp == -1 && elem.sorted < sorted))
                    view.add(elem);
            }

            Collections.shuffle(view, CommonState.r);
            if (!view.isEmpty()) {
                Skyline elem = view.remove(0);
                //System.out.println("EXCHANGE VIEW " + print(elem.attributes) + print(attributes) + " " + elem.sorted + " " + sorted);
                double tmp = sorted;
                sorted = elem.sorted;
                elem.sorted = tmp;
                elem.slice = (int) Math.floor(k*elem.sorted);
            }
        }

        toggle = !toggle;

        int newslice = (int) Math.floor(k*sorted);

        if (newslice == k) {
            newslice = 0;
        }

        //OBSERVER INFO
        if(slice!=newslice){
            slicechanges++;
            periodslicechanges++;
        }

        slice = newslice;

        /*
        if (slice != expected)
            System.out.println("EXPECTED " + (Math.abs(sorted_expected - sorted) * 4));
        else
            System.out.println("EXPECTED 0");
        */
        increaseAge();
    }

    /**
     * Insert a record into one of the two views
     */
    public void conditionalInsert(SkylineMessage msg, int cmp) {

        ArrayList<SkylineRecord> view;
        if (cmp == 1)
            view = infView;
         else if (cmp == -1)
            view = supView;
         else
             return;

        int insertValue = cmp;
        boolean insert = false;
        SkylineRecord node= null;
        int res = 0;
        int i = -1;
        ArrayList<SkylineRecord> toRemove = new ArrayList<SkylineRecord>();


        for (int j = 0; j < view.size(); j++){
            SkylineRecord r = view.get(j);
            if (r.node.equals(msg.sender)) {
                view.set(j, new SkylineRecord(msg.sender, 0, msg.values));
                return;
            }
        }
        if (view.isEmpty()) {
            view.add(new SkylineRecord(msg.sender, 0, msg.values));
            return;
        }

        for (SkylineRecord n: view) {
            res = compareSkylinePosition(msg.values, n.attributes);
            if (res == 0 && !equals(msg.values, n.attributes)) {
                node = n;
                insert = true;
            } else if (res == insertValue) {
                toRemove.add(n);
                node = n;
                insert = true;
            } else {
                insert = false;
                break;
            }
        }
        if (insert)
        {
            for (SkylineRecord r: toRemove)
                view.remove(r);
            view.add(new SkylineRecord(msg.sender, 0, msg.values));
        }
    }

    public void processEvent(Node node, int pid, Object event) {

        messages.SkylineMessage msg = (messages.SkylineMessage) event;

        int result = compareSkylinePosition(attributes, msg.values);
        conditionalInsert(msg, result);
        discardOld();
    }

    public void discardOld() {
        while (infView.size() > viewsize) {
            infView.remove(0);
        }
        while (supView.size() > viewsize) {
            supView.remove(0);
        }
    }


    public String toString() {
        String build = " inf = [";
        for (SkylineRecord r: infView) {
            build += " " + print(r.attributes);
        }
        build += "] sup = [";
        for (SkylineRecord r: supView) {
            build += " " + print(r.attributes);
        }
        build += "]";

        return "Skyline(" + print(attributes) + ", " + supView.size() + ", " + infView.size()+ ", "+sorted +" )" + build;
    }

    public static String print(int[] args) {
        String a = "[";
        for (int i: args) {
            a += i + ", ";
        }
        return a+ "] ";
    }

    /**
     * Clones the value holder.
     */
    public Object clone()
    {
        Skyline svh=null;
        try {
            svh=(Skyline)super.clone();
            svh.slice = slice;
            svh.position = position;
            svh.k = k;
            svh.attributes = (int[]) attributes.clone();
            svh.viewsize = viewsize;
            svh.slicechanges = 0;
            svh.sorted = 0;
            svh.toggle = toggle;
            svh.sorted_expected = 0;
            svh.infView = (ArrayList<SkylineRecord>) infView.clone();
            svh.supView = (ArrayList<SkylineRecord>) supView.clone();
        }
        catch( CloneNotSupportedException e ) {} // never happens
        return svh;
    }
}


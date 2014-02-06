package protocols;


import messages.CyclonRecord;
import messages.Record;
import messages.HilbertMessage;
import messages.HilbertRecord;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import util.HilbertConverter;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.math.BigInteger;

/**
 * Multidimensional slicing protocol based on Sliver
 * using an Hilbert curve
 */
public class Hilbert extends Slicing implements CDProtocol, EDProtocol {

    //view size
    private static final String PAR_VIEWSIZE = "memorylimit";
    //number of slices
    private static final String PAR_SLICES = "slices";
    // friction
    private static final String PAR_FRICTION = "friction";

    public BigInteger hvalue = BigInteger.ZERO;

    public int k = -1;
    public int viewsize = -1;

    public LinkedBlockingQueue<HilbertRecord> recs;
    public ArrayList<Node> view;
    public int[] attributes;
    public float position;

    public float smaller;
    public float totalseen;

    public int reset_diff = 0;
    public int friction;
    public int current_difference;

    public ArrayList<CyclonRecord> mycolorView;

    public Hilbert(String prefix) {
        super();
        k = Configuration.getInt(prefix+"."+PAR_SLICES, -1);
        viewsize = Configuration.getInt(prefix+"."+PAR_VIEWSIZE, 0);
        friction = Configuration.getInt(prefix + "." + PAR_FRICTION);
        attributes = new int[]{};
        current_difference = 0;
    }

    public BigInteger getHilbert() {
        if (hvalue == BigInteger.ZERO)
            hvalue = HilbertConverter.Hilbert_to_int(attributes);
        return hvalue;
    }

    public void nextCycle( Node node, int pid )
    {
        Cyclon linkable =
                (Cyclon) node.getProtocol( FastConfig.getLinkable(pid) );

        ArrayList<Node> psview = new ArrayList<Node>();


        if (linkable.degree() > 0)
        {
            for(int i=0; i<linkable.degree();i++){
                Node peern = linkable.getNeighbor(i);
                psview.add(peern);
            }
        }

        if (view.size() > 0){

            for(int i = 0; i< view.size(); i++){
                Node peern = view.get(i);
                if(!peern.isUp()) return;

                ((Transport)node.getProtocol(FastConfig.getTransport(pid))).
                        send(
                                node,
                                peern,
                                new messages.HilbertMessage(getHilbert(),node),
                                pid);
            }
        }
        view = psview;
    }


    public void processEvent( Node node, int pid, Object event ) {


        messages.HilbertMessage aem = (messages.HilbertMessage)event;
        HilbertRecord novel = new HilbertRecord(aem.sender, 0, aem.value);

        //clean timed out records and add the received one
        discardOld(novel,node.getID());

        float n = smaller;

        position = n/recs.size();
        int newslice = Math.round(k*position);

        if (newslice == k) {
            newslice = 0;
        }

        current_difference = current_difference + slice - newslice;

        if(Math.abs(current_difference) > friction){
            slice = newslice;
            slicechanges++;
            current_difference = 0;
        }


        /*
        //OBSERVER INFO
        if(slice!=newslice){
            slicechanges++;
            periodslicechanges++;
        }


        slice = newslice;
*/
    }

    private void discardOld(HilbertRecord novel,long myid){

        if (recs.contains(novel)) {
            recs.remove(novel);
            recs.offer(novel);
            smaller = 0;
            totalseen = recs.size();
            for (HilbertRecord r : recs){
                int cmp = r.attribute2.compareTo(getHilbert());
                if(cmp == -1)
                    smaller++;
                else if (cmp == 0 && r.node.getID() < myid)
                    smaller++;
            }
        } else {
            boolean added = recs.offer(novel);
            if(added){
                int cmp = novel.attribute2.compareTo(getHilbert());
                if(cmp == -1)
                    smaller++;
                else if(cmp == 0 && novel.node.getID()<myid)
                    smaller++;

                totalseen++;
            }
            else {
                HilbertRecord old = recs.remove();
                recs.offer(novel);

                int cmp = novel.attribute2.compareTo(getHilbert());

                if (cmp == -1)
                    smaller++;
                else if (cmp == 0 && novel.node.getID() < myid)
                    smaller++;

                cmp = old.attribute2.compareTo(getHilbert());
                if(cmp == -1)
                    smaller--;
                else if (cmp == 0 && old.node.getID() < myid)
                    smaller--;
            }
        }

    }

    /**
     * Clones the value holder.
     */
    public Object clone()
    {
        Hilbert svh=null;
        try {
            svh=(Hilbert)super.clone();
            svh.mycolorView = new ArrayList<CyclonRecord>();
            svh.slice = slice;
            svh.position = position;
            svh.k = k;
            svh.recs = new LinkedBlockingQueue<HilbertRecord>(viewsize);
            svh.attributes = attributes.clone();
            svh.viewsize = viewsize;
            svh.slicechanges = 0;
            svh.expected = expected;
            svh.view = new ArrayList<Node>();
            svh.friction = friction;
        }
        catch( CloneNotSupportedException e ) {} // never happens
        return svh;
    }

}




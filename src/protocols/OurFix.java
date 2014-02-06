package protocols;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 17/1/12
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class OurFix implements CDProtocol {

    private static final String PAR_PARAMETER = "parameter";

    //key parameter
    public int change;
    public int currentSlice;
    public int changeDiff;
    public int changeDiffWStab;
    public int parameter;

    //OBSERVER INFO
    public int changes;
    public int underchanges;
    public int periodslicechanges;
    
    public OurFix(String prefix){
        this.change = 0;
        this.currentSlice = -1;
        this.changeDiff = 0;
        this.changes = 0;
        this.underchanges = 0;
        this.periodslicechanges = 0;
        this.parameter = Configuration.getInt(prefix+"."+PAR_PARAMETER);

    }
    
    public void nextCycle(Node node, int pid) {

        OurFix myprotocol = (OurFix) node.getProtocol(pid);
        Slicing linkable =  (Slicing) node.getProtocol( FastConfig.getLinkable(pid) );

        int oldslice = myprotocol.currentSlice;

        int nextSlice = linkable.slice;
        myprotocol.changeDiff = myprotocol.currentSlice-nextSlice;
        myprotocol.change = myprotocol.change + myprotocol.changeDiff;
        if(Math.abs(myprotocol.change)>parameter){
            myprotocol.currentSlice = nextSlice;
            myprotocol.change = 0;
            myprotocol.changes++;
            myprotocol.periodslicechanges++;

        }
        myprotocol.underchanges = linkable.slicechanges;
        myprotocol.changeDiffWStab = Math.abs(myprotocol.currentSlice-oldslice);
    }


    public Object clone()
    {
        OurFix svh=null;
        try { svh=(OurFix)super.clone();
            svh.change = 0;
            svh.currentSlice = -1;
            svh.changeDiff = 0;
            svh.changeDiffWStab = 0;
            svh.parameter = parameter;
            svh.periodslicechanges = 0;
            svh.changes =0;
        }
        catch( CloneNotSupportedException e ) {} // never happens
        return svh;
    }
}

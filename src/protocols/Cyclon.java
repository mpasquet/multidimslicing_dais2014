package protocols;

import messages.CyclonMessage;
import messages.CyclonRecord;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;


import java.util.Collections;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 3/1/12
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */
public class Cyclon implements CDProtocol, EDProtocol, Linkable {

    private static final String PAR_VIEWSIZE = "cache";
    private static final String PAR_EXCHANGE = "gossip";


    public ArrayList<CyclonRecord> view;
    public int viewsize;
    public int exchange;
    public int currentSlice;

    public int cycles;

    public ArrayList<CyclonRecord> sent;
    
    public Cyclon(String prefix) {

        super();

        exchange = Configuration.getInt(prefix + "." + PAR_EXCHANGE, 0);
        viewsize = Configuration.getInt(prefix+"."+PAR_VIEWSIZE, 0);
        cycles = 0;
    }
    
    public void nextCycle(Node node, int pid) {
        //System.out.println("HELLO i am cyclon!!!! at cycle " + peersim.core.CommonState.getTime());
        Cyclon myprotocol = (Cyclon) node.getProtocol(pid);

        myprotocol.cycles++;

        //1. Increase by one the age of all neighbours
        //2. Select neighbour Q with the highest age among all neighbours
        CyclonRecord oldest = null;
        for(CyclonRecord rec : myprotocol.view){
            rec.age = rec.age+1;
            if(oldest==null || rec.age>oldest.age){
                oldest = rec;
            }
        }
        
        //2. and (l-1) other random neighbours -> l==exchange
        ArrayList<CyclonRecord> toSend = selectToSend(myprotocol,myprotocol.exchange-1,oldest);

        //3. add my own information -> added with info from the Slicing protocol
        CyclonRecord myself = new CyclonRecord(node,0,myprotocol.currentSlice);
        toSend.add(myself);

        //System.out.print("cyclon "+node.getID() + " [");
        //for(CyclonRecord r : toSend){
        //    System.out.print(r.node.getID()+",");
        //}
        //System.out.print("]");
        //System.out.println();

        myprotocol.sent = toSend;

        //4. Send the subset to Q (oldest)
        ((Transport)node.getProtocol(FastConfig.getTransport(pid))).
                send(
                        node,
                        oldest.node,
                        new CyclonMessage(toSend,node,false),
                        pid);
        


    }

    public void processEvent(Node node, int pid, Object o) {
        
        CyclonMessage message = (CyclonMessage) o;
        //5. Receive from Q a subset of its entries.
        ArrayList<CyclonRecord> received = message.exchange;
       // System.out.print("cyclon "+node.getID() + " [");
       // for(CyclonRecord r : received){
        //    System.out.print(r.node.getID()+",");
        //}
        //System.out.print("]");
        //System.out.println();
                
        Cyclon myprotocol = (Cyclon) node.getProtocol(pid);

        if(message.answer == false){
            ArrayList<CyclonRecord> response = selectToSend(myprotocol,myprotocol.exchange-1,
                    new CyclonRecord(message.sender,-1,-1));
            response.add(new CyclonRecord(node,0,myprotocol.currentSlice));

            ((Transport)node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                            node,
                            message.sender,
                            new CyclonMessage(response, node, true),
                            pid);

            updateView(node,myprotocol,received,response);
            
        }
        else {
            myprotocol.view.remove(new CyclonRecord(message.sender,-1,-1)); //remove oldest
            updateView(node, myprotocol, received, myprotocol.sent);
        }


    }

    
    //AUX METHODS
    public void updateView(Node node, Cyclon myprotocol, ArrayList<CyclonRecord> received,
                           ArrayList<CyclonRecord> sent){

        for(CyclonRecord r : sent){
            myprotocol.view.remove(r);
        }

        //6. Discard entries pointing at P and entries already in P's view
        //7. Update protocols view by firstly using empty slots (if any)
        //and secondly replacing entries among the ones sent to Q.

        CyclonRecord myself = new CyclonRecord(node,-1,-1); //just to compare
        received.remove(myself);
        for(CyclonRecord r : received){
            if( !r.equals(myself) && myprotocol.view.size()<myprotocol.viewsize){  //no consideration about age!
                if(!myprotocol.view.contains(r)){
                    myprotocol.view.add(r);
                }
                else{
                    int index = myprotocol.view.indexOf(r);
                    CyclonRecord tmp = myprotocol.view.get(index);
                    if(r.age<tmp.age){
                        myprotocol.view.remove(tmp);
                        myprotocol.view.add(r);
                    }
                }
            }

        }

        for(CyclonRecord r : sent){
            if(myprotocol.view.size()<myprotocol.viewsize){
                if(!myprotocol.view.contains(r)){
                    myprotocol.view.add(r);
                }
                else{
                    int index = myprotocol.view.indexOf(r);
                    CyclonRecord tmp = myprotocol.view.get(index);
                    if(r.age<tmp.age){
                        myprotocol.view.remove(tmp);
                        myprotocol.view.add(r);
                    }
                }
            }
        }

        

    }

    
    public ArrayList<CyclonRecord> selectToSend(Cyclon protocol, int howmany, CyclonRecord exclude){

        ArrayList<CyclonRecord> res = new ArrayList<CyclonRecord>();

        ArrayList<CyclonRecord> pool = (ArrayList<CyclonRecord>) protocol.view.clone();

        pool.remove(exclude);

        Collections.shuffle(pool,CommonState.r);
        
        for(int i = 0; i < howmany; i++){
            res.add(pool.get(i));
        }

        return res;

    }

    public ArrayList<CyclonRecord> getRandom(int howmany){
        ArrayList<CyclonRecord> res = new ArrayList<CyclonRecord>();

        ArrayList<CyclonRecord> pool = (ArrayList<CyclonRecord>) view.clone();
        Collections.shuffle(pool,CommonState.r);

        for(int i = 0; i < howmany; i++){
            res.add(pool.get(i));
        }

        return res;
    }

    
    public Object clone()
    {
        Cyclon svh=null;
        try { svh=(Cyclon)super.clone();
            svh.view = new ArrayList<CyclonRecord>();
            svh.viewsize = viewsize;
            svh.exchange = exchange;
            svh.sent = new ArrayList<CyclonRecord>();
            svh.currentSlice = -1;
            svh.cycles = 0;
        }
        catch( CloneNotSupportedException e ) {} // never happens
        return svh;
    }

    public int degree() {
        return view.size();
    }

    public Node getNeighbor(int i) {
        return view.get(i).node;
    }

    public boolean addNeighbor(Node node) {
        CyclonRecord res = new CyclonRecord(node,0,-1);
        view.add(res);
        return true;
    }

    public boolean contains(Node node) {
        boolean res = false;
        for(CyclonRecord r: view){
            if(r.node.equals(node)){
                res = true;
            }
        }
        return res;
    }

    public void pack() {

    }

    public void onKill() {

    }
}

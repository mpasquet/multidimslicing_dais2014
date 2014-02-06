package messages;

import peersim.core.Node;

import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 2/1/12
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public class CyclonMessage {
    
    public final ArrayList<CyclonRecord> exchange;

    public final Node sender;

    public final boolean answer;

    public CyclonMessage( ArrayList<CyclonRecord> value, Node sender, boolean ans)
    {
        this.exchange = value;
        this.sender = sender;
        this.answer = ans;
    }
}

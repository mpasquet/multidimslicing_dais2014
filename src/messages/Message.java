package messages;

import peersim.core.Node;

/**
 * Created by IntelliJ IDEA.
 * User: fmaia
 * Date: 23/11/11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class Message {

    /**
* The type of a message. It contains a value of type Int and the
* sender node of type {@link peersim.core.Node}.
*/
        public final int value;
        /** If not null,
        this has to be answered, otherwise this is the answer. */
        public final Node sender;

        public Message( int value, Node sender )
        {
                this.value = value;
                this.sender = sender;
        }
}


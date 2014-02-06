package messages;

import peersim.core.Node;

import java.math.BigInteger;

public class HilbertMessage {

    /**
* The type of a message. It contains a value of type Int and the
* sender node of type {@link peersim.core.Node}.
*/
        public final BigInteger value;
        /** If not null,
        this has to be answered, otherwise this is the answer. */
        public final Node sender;

        public HilbertMessage(BigInteger value, Node sender )
        {
                this.value = value;
                this.sender = sender;
        }
}


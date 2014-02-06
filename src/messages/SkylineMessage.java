package messages;

import peersim.core.Node;

public class SkylineMessage {

        public final int[] values;

        public final double sorted;

        public final Node sender;

        public SkylineMessage(int[] values, double sorted, Node sender) {
                this.values = values;
                this.sender = sender;
                this.sorted = sorted;
        }
}


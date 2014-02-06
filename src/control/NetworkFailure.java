package controls;

import peersim.config.Configuration;
import peersim.core.*;

public class NetworkFailure implements Control {

    public int att;
    public int number;

    private final String name;
    private final String prefix;

    private int cycle = 0;

    public NetworkFailure(String s) {
        name = s;
        att = Configuration.getInt(s + ".at");
        number = Configuration.getInt(s + ".number");
        prefix = Configuration.getString(s + ".prefix");
        System.err.println("TEST " + cycle + " " + att + " " + number);
    }

    public boolean execute() {
        System.err.println("TEST " + cycle);
        if (cycle == att) {
            System.out.println("FAILURE");
            for (int i = 0; i < number; i++) {
                Network.remove(CommonState.r.nextInt(Network.size()));
            }
        } else {
            System.out.println("NO FAILURE");
        }
        cycle++;
        return false;
    }
}

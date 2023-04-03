package util;

import java.util.List;
import java.util.ArrayList;

public class LRRStrategy implements SchedulingAlgorithm{
    private List<Server> chosenServers;
    private int currentServerIndex = 0;

    /**
     * Constructs an LRRStrategy object with a given list of servers. It selects the server with the
     * highest number of cores as the primary server and stores it in the chosenServers list. If
     * multiple servers have the same number of cores, it selects the server of the same type as the
     * primary server and stores them in the chosenServers list.
     *
     * @param servers The list of servers to select from
     */
    public LRRStrategy(List<Server> servers) {
        chosenServers = new ArrayList<>();
        int maxCore = -1;

        for (Server server : servers) {
            if (maxCore < server.getCore()) {
                chosenServers.clear();
                chosenServers.add(server);
                maxCore = server.getCore();
            } else if (chosenServers.get(0).getServerType().equals(server.getServerType())) {
                chosenServers.add(server);
            }
        }
    }

    /**
     * Returns the next server in the chosenServers list in a round-robin fashion.
     *
     * @return The next server in the chosenServers list
     */
    public Server nextServer() {
        currentServerIndex = (currentServerIndex + 1) % chosenServers.size();
        return this.getCurrentServer();
    }

    /**
     * Returns the current server in use according to the LRR strategy.
     * 
     * @return The current server being used.
     */
    public Server getCurrentServer() {
        return chosenServers.get(currentServerIndex);
    }
}

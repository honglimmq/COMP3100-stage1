/**
 * A client side simulator that acts as a scheduler for a server-side simulator called ds-server.
 * 
 * <p>
 * The 'Client' class is responsible for scheduling job requests to be sent to the distributed
 * server simulator. It simulates the behaviour of a client by generating request data and sending
 * it to the server using 'serverCommunication.send' method. The current scheduler algorithm to pick
 * which server to dispatch job to is called Largest-Round-Robin(LRR), it sends each job to a server
 * of the largest type in a round-robin fashion.
 * 
 * <p>
 * Created by Hong Lim (Student ID: 44679440) on April 03, 2023.
 * </p>
 */

import java.util.List;
import java.util.ArrayList;
import util.*;

public class Client {
  // Current job information
  private int jobID = 0;
  private int reqCore = 0;
  private int reqMemory = 0;
  private int reqDisk = 0;

  private ClientServerConnection serverCommunication;
  private LRRStrategy scheduleStrategy;
  boolean firstPass = true;
  Algorithm currAlgorithm = Algorithm.BF;


  public Client() {
    serverCommunication = new ClientServerConnection();
  }

  public void run() {
    // Estabalish connection with ds-server
    serverCommunication.connect();

    // TCP handshake: Greeting + Authentication
    serverCommunication.send(Command.HELO);
    serverCommunication.recieve();
    serverCommunication.send(Command.AUTH, System.getProperty("user.name"));
    serverCommunication.recieve();

    while (!(serverCommunication.getReceivedMessage().equals(ServerCommand.NONE.toString()))) {
      // Signal ds-server for a job
      serverCommunication.send(Command.REDY);
      serverCommunication.recieve();

      String[] splittedMsg = serverCommunication.getReceivedMessage().split("\\s+");
      ServerCommand receivedCommand = ServerCommand.valueOf(splittedMsg[0]);

      switch (receivedCommand) {
        case JOBP:
        case JOBN:
          handleJob(splittedMsg);
          break;
        case JCPL:
        case NONE:
        default:
          break;
      }
    }
    // Exit gracefully
    System.exit(serverCommunication.close());
  }


  private void handleJob(String jobInfo[], ) {
    int[] jobInforArray = parseJobInfo(jobInfo);


    switch (currAlgorithm) {
      case BF:
        bestFitAlgorithm();
        break;
      default:

    }

    if (firstPass) {
      // Generate outgoing message for GETS All command
      serverCommunication.send(Command.GETS, "All");

      // DATA [nRecs] [recLen]
      serverCommunication.recieve();
      String[] spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");
      int numOfServer = Integer.parseInt(spiltedMsg[1]);
      serverCommunication.send(Command.OK);

      // Process all servers information
      List<Server> servers = new ArrayList<>();
      for (int i = 0; i < numOfServer; i++) {
        serverCommunication.recieve();
        spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");
        servers.add(parseServerInfo(spiltedMsg));
      }
      scheduleStrategy = new LRRStrategy(servers);
      servers = null;

      serverCommunication.send(Command.OK);
      serverCommunication.recieve(); // RECV .
      firstPass = false;
    }
    // Schedule a job based on LRR strategy
    String serverType = scheduleStrategy.getCurrentServer().getServerType();
    int serverID = scheduleStrategy.getCurrentServer().getServerID();

    serverCommunication.send(Command.SCHD, jobID + " " + serverType + " " + serverID);
    serverCommunication.recieve();
    scheduleStrategy.nextServer();
  }

  
  private Server bestFitAlgorithm(int reqCore, int reqMem, int reqDisk) {
    // Query and return available server with required resource
    List<Server> servers = null;
    servers = getServerInfo(GETSMode.Avail, reqCore, reqMem, reqDisk);
    if (servers != null && !servers.isEmpty()) {
      return servers.get(0);
    }

    // Query and return first capable server
    servers = getServerInfo(GETSMode.Capable, reqCore, reqMem, reqDisk);
    return servers.get(0);
  }


  private int parseJobInfo(String[] jobInfo) {
    try {
      jobID = Integer.parseInt(jobInfo[2]);
      reqCore = Integer.parseInt(jobInfo[4]);
      reqMemory = Integer.parseInt(jobInfo[5]);
      reqDisk = Integer.parseInt(jobInfo[6]);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("NumberFormatException ==> " + e.getMessage());
    }

    return jobID;
  }

  private List<Server> getServerInfo(GETSMode GetsMode, int reqCore, int reqMemory, int reqDisk) {
    // Generate outgoing message for GETS command with appropriate GETSMode
    String getModeStr = GetsMode.toString();
    serverCommunication.send(Command.GETS,
        getModeStr + " " + reqCore + " " + reqMemory + " " + reqDisk);

    // Should recieve DATA [nRecs] [recLen]
    serverCommunication.recieve();
    String[] spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");
    int numOfServer = Integer.parseInt(spiltedMsg[1]);
    serverCommunication.send(Command.OK);

    // Process servers information
    List<Server> servers = new ArrayList<>();
    for (int i = 0; i < numOfServer; i++) {
      serverCommunication.recieve();
      spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");
      servers.add(parseServerInfo(spiltedMsg));
    }

    serverCommunication.send(Command.OK);
    serverCommunication.recieve(); // RECV .
  }

  private Server parseServerInfo(String[] serverInfo) {
    Server server = null;
    try {
      String serverType = serverInfo[0];
      int serverID = Integer.parseInt(serverInfo[1]);
      String status = serverInfo[2];
      int currStartTime = Integer.parseInt(serverInfo[3]);
      int core = Integer.parseInt(serverInfo[4]);
      int memory = Integer.parseInt(serverInfo[5]);
      int disk = Integer.parseInt(serverInfo[6]);
      int waitingJobs = Integer.parseInt(serverInfo[7]);
      int runningJobs = Integer.parseInt(serverInfo[8]);

      server = new Server(serverType, serverID, status, currStartTime, core, memory, disk,
          waitingJobs, runningJobs);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("NumberFormatException ==> " + e.getMessage());
    }
    return server;
  }

  public static void main(String args[]) {
    new Client().run();
  }
}

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


import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import util.Server;
import util.ClientServerConnection;
import util.Command;
import util.ServerCommand;

public class Client {
  // Current job information
  private int jobID = 0;
  private int reqCore = 0;
  private int reqMemory = 0;
  private int reqDisk = 0;

  // Selected server information
  private List<Server> servers = new ArrayList<Server>();
  private int currentServerIndex = 0;

  private ClientServerConnection serverCommunication;
  boolean firstPass = true;

  public Client() {
    serverCommunication = new ClientServerConnection();
  }

  public void run() throws IOException {
    // Estabalish connection with ds-server
    serverCommunication.connect();

    // TCP handshake
    serverCommunication.send(Command.HELO);
    serverCommunication.recieve();
    serverCommunication.send(Command.AUTH, System.getProperty("user.name"));
    serverCommunication.recieve();

    while (!(serverCommunication.getReceivedMessage().equals(ServerCommand.NONE.toString()))) {
      // Signal server for a job
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


  private void handleJob(String jobInfo[]) throws IOException {
    parseJobInfo(jobInfo);

    if (firstPass) {
      // Generate outgoing message for GETS All command
      serverCommunication.send(Command.GETS, "All");
      serverCommunication.recieve();;

      // Determine jobID to schedule a server for
      String[] spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");

      int numOfServer = Integer.parseInt(spiltedMsg[1]);
      int maxCore = -1;
      serverCommunication.send(Command.OK);

      // LRR strategy:
      // Identifying the largest server type based on core
      // and count how many of that server type are there.
      for (int i = 0; i < numOfServer; i++) {
        serverCommunication.recieve();;
        spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");
        int core = Integer.parseInt(spiltedMsg[4]);
        String serverType = spiltedMsg[0];

        if (maxCore < core) {
          // case 1: A larger number of core server
          servers.clear();
          servers.add(parseServerInfo(spiltedMsg));
          maxCore = core;
        } else if (servers.get(0).getServerType().equals(serverType) && maxCore == core) {
          // case 2: A same server type with same largest number of core
          servers.add(parseServerInfo(spiltedMsg));
        } else {
          // case 3: A different server type that may have lower or same number of core
          // do nothing
        }
      }

      serverCommunication.send(Command.OK);
      serverCommunication.recieve(); // RECV .
      firstPass = false;
    }

    // Schedule a job based on LRR strategy
    String serverType = servers.get(currentServerIndex).getServerType();
    int serverID = servers.get(currentServerIndex).getServerID();


    serverCommunication.send(Command.SCHD, jobID + " " + serverType + " " + serverID);
    serverCommunication.recieve();

    // Manage server choice based on LRR strategy
    ++currentServerIndex;
    if (currentServerIndex >= servers.size()) {
      currentServerIndex = 0;
    }
  }

  private int parseJobInfo(String[] jobinfo) {
    try {
      jobID = Integer.parseInt(jobinfo[2]);
      reqCore = Integer.parseInt(jobinfo[4]);
      reqMemory = Integer.parseInt(jobinfo[5]);
      reqDisk = Integer.parseInt(jobinfo[6]);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("NumberFormatException ==> " + e.getMessage());
    }

    return jobID;
  }

  private Server parseServerInfo(String[] jobInfo) {
    Server server = null;
    try {
      String serverType = jobInfo[0];
      int serverID = Integer.parseInt(jobInfo[1]);
      String status = jobInfo[2];
      int currStartTime = Integer.parseInt(jobInfo[3]);
      int core = Integer.parseInt(jobInfo[4]);
      int memory = Integer.parseInt(jobInfo[5]);
      int disk = Integer.parseInt(jobInfo[6]);
      int waitingJobs = Integer.parseInt(jobInfo[7]);
      int runningJobs = Integer.parseInt(jobInfo[8]);

      server = new Server(serverType, serverID, status, currStartTime, core, memory, disk,
          waitingJobs, runningJobs);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("NumberFormatException ==> " + e.getMessage());
    }
    return server;
  }

  public static void main(String args[]) throws Exception {
    new Client().run();
  }
}

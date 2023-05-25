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


  private void handleJob(String jobInfo[]) {
    parseJobInfo(jobInfo);
    Server chosenServer = null;

    switch (currAlgorithm) {
      case BF:
        chosenServer = bestFitTwistAlgorithm(reqCore, reqMemory, reqDisk, GETSMode.Avail);
        break;
      default:
    }

    // SCHD
    if (chosenServer != null) {
      String serverType = chosenServer.getServerType();
      int serverID = chosenServer.getServerID();

      serverCommunication.send(Command.SCHD, jobID + " " + serverType + " " + serverID);
      serverCommunication.recieve();
    }
  }

  private Server bestFitTwistAlgorithm(int reqCore, int reqMem, int reqDisk, GETSMode mode) {
    // Query and return available server with required resource based on GETS mode
    List<Server> servers = getServerInfo(mode, reqCore, reqMem, reqDisk);

    if (servers != null && !servers.isEmpty()) {
      int chosenServerIndex = -1;
      int backupServerIndex = -1;
      int smallestFitnessValueCore = Integer.MAX_VALUE;
      int smallestFitnessValueMemory = Integer.MAX_VALUE;

      /* 
      for (int i = 0; i < servers.size(); i++) {
        int fitnessValueCore = servers.get(i).getCore() - reqCore;
        int fitnessValueMemory = servers.get(i).getMemory() - reqMem;

        if (fitnessValueCore < smallestFitnessValueCore && fitnessValueCore >= 0) {
          smallestFitnessValueCore = fitnessValueCore;
          smallestFitnessValueMemory = fitnessValueMemory;
          chosenServerIndex = i;
        } else if (chosenServerIndex == -1 && fitnessValueCore < smallestFitnessValueCore) {
          smallestFitnessValueCore = fitnessValueCore;
          smallestFitnessValueMemory = fitnessValueMemory;
          backupServerIndex = i;
        } else if (fitnessValueCore == smallestFitnessValueCore
            && fitnessValueMemory < smallestFitnessValueMemory) {
          smallestFitnessValueMemory = fitnessValueMemory;
          chosenServerIndex = i;
        }
      }*/

      for (int i = 0; i < servers.size(); i++) {
        int fitnessValueCore = servers.get(i).getCore() - reqCore;
        int fitnessValueMemory = servers.get(i).getMemory() - reqMem;

        if (fitnessValueCore < smallestFitnessValueCore && fitnessValueCore >= 0) {
          smallestFitnessValueCore = fitnessValueCore;
          smallestFitnessValueMemory = fitnessValueMemory;
          chosenServerIndex = i;
        } else if (fitnessValueCore == smallestFitnessValueCore) {
          if (fitnessValueMemory < smallestFitnessValueMemory && fitnessValueMemory >= 0) {
            smallestFitnessValueMemory = fitnessValueMemory;
            chosenServerIndex = i;
          }
        } else if (chosenServerIndex == -1 && fitnessValueCore < smallestFitnessValueCore) {
          smallestFitnessValueCore = fitnessValueCore;
          smallestFitnessValueMemory = fitnessValueMemory;
          backupServerIndex = i;
        }
      }

      if (chosenServerIndex == -1) {
        chosenServerIndex = backupServerIndex;
      }

      return servers.get(chosenServerIndex);
    }

    return bestFitTwistAlgorithm(reqCore, reqMem, reqDisk, GETSMode.Capable);
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
    List<Server> servers = new ArrayList<>();

    if (numOfServer != 0) {
      serverCommunication.send(Command.OK);

      // Process servers information
      for (int i = 0; i < numOfServer; i++) {
        serverCommunication.recieve();
        spiltedMsg = serverCommunication.getReceivedMessage().split("\\s++");
        servers.add(parseServerInfo(spiltedMsg));
      }
    }

    serverCommunication.send(Command.OK);
    serverCommunication.recieve(); // RECV .

    return servers;
  }

  private Server parseServerInfo(String[] serverInfo) {
    Server server = null;
    try {
      String serverType = serverInfo[0];
      int serverID = Integer.parseInt(serverInfo[1]);
      String state = serverInfo[2];
      int currStartTime = Integer.parseInt(serverInfo[3]);
      int core = Integer.parseInt(serverInfo[4]);
      int memory = Integer.parseInt(serverInfo[5]);
      int disk = Integer.parseInt(serverInfo[6]);
      int waitingJobs = Integer.parseInt(serverInfo[7]);
      int runningJobs = Integer.parseInt(serverInfo[8]);

      server = new Server(serverType, serverID, state, currStartTime, core, memory, disk,
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

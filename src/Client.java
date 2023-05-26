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
import util.enums.*;

public class Client {
  // Current job information
  private int jobID = 0;
  private int reqCore = 0;
  private int reqMemory = 0;
  private int reqDisk = 0;

  private ClientServerConnection serverCommunication;
  private Algorithm currAlgorithm;
  private List<ServerXML> serverXML = null;


  public Client() {
    serverCommunication = new ClientServerConnection();
    currAlgorithm = Algorithm.CF;
  }

  public Client(Algorithm algo) {
    serverCommunication = new ClientServerConnection();
    currAlgorithm = algo;
  }

  public void run() {
    // Estabalish connection with ds-server
    serverCommunication.connect();

    // TCP handshake: Greeting + Authentication
    serverCommunication.send(Command.HELO);
    serverCommunication.recieve();
    serverCommunication.send(Command.AUTH, System.getProperty("user.name"));
    serverCommunication.recieve();


    // Read ds-system.xml
    serverXML = ServerXML.parse("ds-system.xml");


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
        case JCPL:  // Compeleted job
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
      case FC:
        chosenServer = firstCapableAlgorithm(reqCore, reqMemory, reqDisk);
        break;
      case FF:
      case BF:
      case WF:
      case CF:
        chosenServer = closestFitAlgorithm(reqCore, reqMemory, reqDisk, GETSMode.Avail);
        break;
      default:
        chosenServer = closestFitAlgorithm(reqCore, reqMemory, reqDisk, GETSMode.Avail);
        break;
    }

    // SCHD
    if (chosenServer != null) {
      serverCommunication.send(Command.SCHD,
          jobID + " " + chosenServer.serverType + " " + chosenServer.serverID);
      serverCommunication.recieve();
    }
  }

  // ####################
  // Scheduling Algorithms
  // ####################

  Server firstCapableAlgorithm(int reqCore, int reqMem, int reqDisk) {
    List<Server> servers = getServerInfo(GETSMode.Capable, reqCore, reqMem, reqDisk);

    // Return first server from GETS Capable
    return servers.get(0);
  }

  Server closestFitAlgorithm(int reqCore, int reqMem, int reqDisk, GETSMode mode) {
    // Query and return available server with required resource based on GETS mode
    List<Server> servers = getServerInfo(mode, reqCore, reqMem, reqDisk);

    // If no server data is retrieved from GETS Avail, try getting data from GETS Capable instead.
    if (servers == null || servers.isEmpty()) {
      servers = getServerInfo(GETSMode.Capable, reqCore, reqMem, reqDisk);
      if (servers == null || servers.isEmpty()) {
        // No available servers

        return null;
      }
    }

    int chosenServerIndex = -1;
    int backupServerIndex = -1;
    int smallestFitnessValueCore = Integer.MAX_VALUE;
    int smallestFitnessValueMemory = Integer.MAX_VALUE;

    for (int i = 0; i < servers.size(); i++) {
      int fitnessValueCore = servers.get(i).core - reqCore;
      int fitnessValueMemory = servers.get(i).memory - reqMem;

      // Select a server with the smallest positive core fitness value. If given 2 servers of the
      // same
      // fitness value, pick the first one. If however, there is no positive fitness value server,
      // pick the closest negative fitness value server to 0.
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

  // ####################
  // Ulility Methods
  // ####################

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
        servers.add(Server.parseServerInfo(spiltedMsg));
      }
    }

    serverCommunication.send(Command.OK);
    serverCommunication.recieve(); // RECV .

    return servers;
  }

  public static void main(String args[]) {
    // Check if any command-line arguments are passed
    if (args.length == 0) {
      new Client().run();
    } else if (args.length == 2 && args[0].equals("-a")) {
      String arg = args[1];
      Algorithm algo = null;

      // Pick an algorithm
      switch (arg) {
        case "fc":
          algo = Algorithm.FC;
          break;
        case "bf":
          algo = Algorithm.BF;
          break;
        case "ff":
          algo = Algorithm.FF;
          break;
        case "wf":
          algo = Algorithm.WF;
          break;
        case "atl":
          algo = Algorithm.ATL;
          break;
        case "cf":
          algo = Algorithm.CF;
          break;
        default:
          algo = Algorithm.CF;
          break;
      }
      new Client(algo).run();
    }
  }
}

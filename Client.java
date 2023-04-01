import java.net.*;
import Client.Command;
import java.io.*;
import java.util.*;

enum Command {
  HELO, AUTH, REDY, OK, GETS, SCHD, ENQJ, DEQJ, LSTQ, CNTJ, EJWT, LSTJ, MIGJ, KILJ, TERM, QUIT
}

enum ServerCommand {
  DATA, JOBN, JOBP, JCPL, RESF, RESR, CHKQ, NONE, ERR, OK, QUIT
}

public class Client {
  private final String EMPTYSTRING = "";
  private final String BREAKLINE = "\n";
  private final String WHITESPACE = " ";
  private final int SERVERPORT = 50000;

  boolean debug = true;
  int count = 0;
  boolean firstPass = true;

  Socket socket;
  DataOutputStream out;
  BufferedReader in;
  String incomingMsg = EMPTYSTRING;
  String outgoingMsg = EMPTYSTRING;

  // job information
  int jobID = 0;
  int reqCore = 0;
  int reqMemory = 0;
  int reqDisk = 0;

  // server information

  public void run() throws IOException {
    // Connect
    socket = new Socket("localhost", SERVERPORT);
    out = new DataOutputStream(socket.getOutputStream());
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    // TCP handshake
    sendMsg(Command.HELO);
    recvMsg();
    sendMsg(Command.AUTH, "Ty");
    recvMsg();

    while (!incomingMsg.equals(ServerCommand.NONE.toString())) {
      // Signal server for a job
      sendMsg(Command.REDY);
      incomingMsg = recvMsg();

      String[] splittedMsg = incomingMsg.split("\\s+");
      ServerCommand recvCommand = ServerCommand.valueOf(splittedMsg[0]);

      if (recvCommand.equals(ServerCommand.JOBN)) {
        // handles JOBN case
      }

      switch (recvCommand) {
        case JOBP:
        case JOBN:
          handleJob(splittedMsg);
          break;
        case JCPL:
        case NONE:
        default:
          break;
      }
      count++;
    }
    sendMsg(Command.QUIT);
    recvMsg();

    out.close();
    socket.close();
  }

  void handleJob(String jobInfo[]) throws IOException {
    parseJobInfo(jobInfo);

    if (firstPass) {
      // Generate outgoing message for GETS All command
      sendMsg(Command.GETS, "All");
      incomingMsg = recvMsg();

      // Determine jobID to schedule a server for
      String[] spiltedMsg = incomingMsg.split("\\s++");
      
      int numOfServer = Integer.parseInt(spiltedMsg[1]);
      int maxCore = -1;
      int serverID = -1;
      String serverType = EMPTYSTRING;

      sendMsg(Command.OK);

      // Identifying the largest server type based on core 
      // and count how many of that server type are there. 
      // State information on each server is formmated as:
      // [serverType] [serverID] [state] [currStartTime] [core] [memory] [disk]
      for (int i = 0; i < numOfServer; i++) {
        incomingMsg = recvMsg();
        spiltedMsg = incomingMsg.split("\\s++");

        int core = Integer.parseInt(spiltedMsg[4]);
        if (maxCore < core) {
          serverType = spiltedMsg[0];
          serverID = Integer.parseInt(spiltedMsg[1]);
          maxCore = core;
        }
      }

      sendMsg(Command.OK);
      incomingMsg = recvMsg();

      firstPass = false;
    }
    // Schedule a job
    outgoingMsg = jobID + WHITESPACE + serverType + WHITESPACE + serverID;
    sendMsg(Command.SCHD, outgoingMsg);
    incomingMsg = recvMsg();
  }

  String recvMsg() throws IOException {
    String message = in.readLine();

    // print server message
    if (debug) {
      System.out.println("RCVD " + message);
    }
    return message;
  }

  void sendMsg(Command cmd) throws IOException {
    sendMsg(cmd, EMPTYSTRING);
  }

  void sendMsg(Command cmd, String parameters) throws IOException {
    String message;
    if (!parameters.isEmpty()) {
      message = cmd + WHITESPACE + parameters + BREAKLINE;
    } else {
      message = cmd + BREAKLINE;
    }

    out.write(message.getBytes());
    out.flush();

    // print client message
    if (debug) {
      System.out.print("SENT " + message);
    }
  }

  void parseJobInfo(String[] jobinfo) {
    try {
      jobID = Integer.parseInt(jobinfo[2]);
      reqCore = Integer.parseInt(jobinfo[4]);
      reqMemory = Integer.parseInt(jobinfo[5]);
      reqDisk = Integer.parseInt(jobinfo[6]);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      // TODO: handle exception
    }

  }

  void parseServerInfo(String[] jobInfo) {

  }

  public static void main(String args[]) throws Exception {
    new Client().run();
  }
}

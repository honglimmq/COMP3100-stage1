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

  Socket socket;
  DataOutputStream out;
  BufferedReader in;
  String incomingMsg = EMPTYSTRING;

  // job information
  int jobID;
  int reqCore;
  int reqMemory;
  int reqDisk;

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

      String[] tokens = incomingMsg.split("\\s+");
      ServerCommand recvCommand = ServerCommand.valueOf(tokens[0]);

      switch (recvCommand) {
        case JOBN:
          parseJobInfo(tokens);


          break;
      }

      // Generate GETS command
      String parametersForGETS =
          "Capable" + WHITESPACE + reqCore + WHITESPACE + reqMemory + WHITESPACE + reqDisk;

      sendMsg(Command.GETS, parametersForGETS);
      incomingMsg = recvMsg();
      tokens = incomingMsg.split("\\s++");
      int numOfServer = Integer.parseInt(tokens[1]);

      sendMsg(Command.OK);

      





    }



    out.close();
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
    if(!parameters.isEmpty()){
      message = cmd + WHITESPACE + parameters + BREAKLINE;
    } else {
      message = cmd + BREAKLINE;
    }

    out.write(message.getBytes());
    out.flush();

    // print client message
    if (debug) {
      System.out.println("SENT " + message);
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

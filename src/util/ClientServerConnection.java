package util;

import java.net.Socket;
import util.enums.Command;
import util.enums.ServerCommand;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class ClientServerConnection {
    private boolean debug = true;
    private final String EMPTY_STRING = "";
    private final String LINE_BREAK = "\n";
    private final String WHITESPACE = " ";
    private final int DEFAULT_SERVER_PORT = 50000;
    private final String DEFAULT_SERVER_ADDRESS = "localhost";

    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;
    private String receivedMsg = EMPTY_STRING;
    private String sentMsg = EMPTY_STRING;

    private int serverPort;
    private String serverAddress;
    private boolean connected = false;


    public ClientServerConnection() {
        this.setServerAddress(DEFAULT_SERVER_ADDRESS);
        this.setServerPort(DEFAULT_SERVER_PORT);
    }

    public ClientServerConnection(String serverAddress, int serverPort) {
        this.setServerAddress(serverAddress);
        this.setServerPort(serverPort);
    }

    public void connect() {
        if (connected) {
            // Connection has already been established
            return;
        }
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("IOException ==> " + e.getMessage());
        }
    }

    public void send(Command cmd) {
        send(cmd, EMPTY_STRING);
    }

    public void send(Command cmd, String params) {
        String message;
        if (!params.isEmpty()) {
            message = cmd + WHITESPACE + params + LINE_BREAK;
        } else {
            message = cmd + LINE_BREAK;
        }

        try {
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            System.err.println("An error occurred while sending the message to server: " + e.getMessage());
            e.printStackTrace();
        }

        if (debug) {
            System.out.print("SENT " + message);
        }
    }

    public String recieve()  {
        try {
            receivedMsg = in.readLine();
        } catch (IOException e) {
            System.err.println("An error occurred while recieving a message from server: " + e.getMessage());
            e.printStackTrace();
        }

        if (debug) {
            System.out.println("RECV " + receivedMsg);
        }
        return this.receivedMsg;
    }

    public int close() {
        int exit = 0;
        this.send(Command.QUIT);
        if (!this.recieve().equals(ServerCommand.QUIT.toString())) {
            exit = 1;
        }
        // Close socket/IO Stream
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e){
            System.err.println("An error occurred while closing socket/IO streams: " + e.getMessage());
            e.printStackTrace();
        }
        return exit;
    }

    private void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    private void setServerPort(int port) {
        this.serverPort = port;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public String getSentMessage() {
        return this.sentMsg;
    }

    public String getReceivedMessage() {
        return this.receivedMsg;
    }
}

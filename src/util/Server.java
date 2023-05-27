package util;

public class Server {
  String serverType;
  int serverID;
  String status;
  int currStartTime;
  int core;
  int memory;
  int disk;
  int waitingJobs;
  int runningJobs;

  public Server(String serverType, int serverID, String status, int currStartTime, int core,
      int memory, int disk, int waitingJobs, int runningJobs) {
    this.serverType = serverType;
    this.serverID = serverID;
    this.status = status;
    this.currStartTime = currStartTime;
    this.core = core;
    this.memory = memory;
    this.disk = disk;
    this.waitingJobs = waitingJobs;
    this.runningJobs = runningJobs;
  }

  public static Server parseServerInfo(String[] serverInfo) {
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
}

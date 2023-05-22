package util;

public class Server {
    public enum ServerState{
        INACTIVE,  BOOTING, IDLE, ACTIVE, UNAVAILABLE
    }

    final String serverType;
    final int serverID;
    private ServerState state;
    private int currStartTime;
    final int core;
    final int memory;
    final int disk;
    private int coreLeft;
    private int memoryLeft;
    private int diskLeft;
    private int hourlyRentalRate;

    public Server(String serverType, int serverID, ServerState state, int currStartTime, int core,
            int memory, int disk) {
                this.serverType = serverType;
                this.serverID = serverID;
                this.state = state;
                this.currStartTime = currStartTime;
                this.core = core;
                this.memory = memory;
                this.disk = disk;
                this.setCoreLeft(core);
                this.setMemoryLeft(memory);
                this.setDiskLeft(disk);
                this.hourlyRentalRate = -1;
    }

    public String getServerType() {
        return serverType;
    }


    public int getServerID() {
        return serverID;
    }


    public ServerState getState() {
        return state;
    }


    public int getCurrStartTime() {
        return currStartTime;
    }


    public void setCurrStartTime(int currStartTime) {
        this.currStartTime = currStartTime;
    }


    public int getCore() {
        return core;
    }


    public int getMemory() {
        return memory;
    }


    public int getDisk() {
        return disk;
    }


    public int getCoreLeft() {
        return coreLeft;
    }


    public void setCoreLeft(int coreLeft) {
        this.coreLeft = coreLeft;
    }


    public int getMemoryLeft() {
        return memoryLeft;
    }


    public void setMemoryLeft(int memoryLeft) {
        this.memoryLeft = memoryLeft;
    }


    public int getDiskLeft() {
        return diskLeft;
    }


    public void setDiskLeft(int diskLeft) {
        this.diskLeft = diskLeft;
    }

    public int getCoreAvailable(){
        return core - coreLeft;
    }

    public int getMemoryAvailable(){
        return memory - memoryLeft;
    }

    public int getDiskAvailable(){
        return disk - diskLeft;
    }

    public int getHourlyRentalRate(){
        return this.hourlyRentalRate;
    }
}

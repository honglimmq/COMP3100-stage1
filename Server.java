public class Server {
    private String serverType;
    private String serverID;
    private String status;
    private int currStartTime;
    private int core;
    private int memory;
    private int disk;
    private int waitingJobs;
    private int runningJobs;

    public Server(String serverType, String serverID, String status, int currStartTime, int core,
            int memory, int disk, int waitingJobs, int runningJobs) {
        setServerType(serverType);
        setServerID(serverID);
        setStatus(status);
        setCurrStartTime(currStartTime);
        setCore(core);
        setMemory(memory);
        setDisk(disk);
        setWaitingJobs(waitingJobs);
        setRunningJobs(runningJobs);
    }

    public String getServerType() {
        return serverType;
    }

    public String getServerID() {
        return serverID;
    }

    public String getStatus() {
        return status;
    }

    public int getCurrStartTime() {
        return currStartTime;
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

    public int getWaitingJobs() {
        return waitingJobs;
    }

    public int getRunningJobs() {
        return runningJobs;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCurrStartTime(int currStartTime) {
        this.currStartTime = currStartTime;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public void setWaitingJobs(int waitingJobs) {
        this.waitingJobs = waitingJobs;
    }

    public void setRunningJobs(int runningJobs) {
        this.runningJobs = runningJobs;
    }
}

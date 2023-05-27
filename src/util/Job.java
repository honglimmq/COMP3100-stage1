package util;

public class Job {
    int jobID;
    int jobState;
    int submitTime;
    int startTime;
    int estRunTime;
    int core;
    int memory;
    int disk;

    public Job(int jobID, int jobState, int submitTime, int startTime, int estRunTime, int core,
            int memory, int disk) {
        this.jobID = jobID;
        this.jobState = jobState;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.estRunTime = estRunTime;
        this.core = core;
        this.memory = memory;
        this.disk = disk;
    }
}

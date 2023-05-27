package util;

public class Job {
    public int jobID;
    public int jobState;
    public int submitTime;
    public int startTime;
    public int estRunTime;
    public int reqCore;
    public int reqMemory;
    public int reqDisk;

    public Job() {
        this.jobID = -1;
        this.jobState = -1;
        this.submitTime = -1;
        this.startTime = -1;
        this.estRunTime = -1;
        this.reqCore = -1;
        this.reqMemory = -1;
        this.reqDisk = -1;
    }

    public Job(int jobID, int submitTime, int estRunTime, int core, int memory, int disk) {
        this.jobID = jobID;
        this.jobState = -1;
        this.submitTime = submitTime;
        this.startTime = -1;
        this.estRunTime = estRunTime;
        this.reqCore = core;
        this.reqMemory = memory;
        this.reqDisk = disk;
    }

    public Job(int jobID, int jobState, int submitTime, int startTime, int estRunTime, int core,
            int memory, int disk) {
        this.jobID = jobID;
        this.jobState = jobState;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.estRunTime = estRunTime;
        this.reqCore = core;
        this.reqMemory = memory;
        this.reqDisk = disk;
    }



  // LSTJ job info reponse format
  // jobID jobState submitTime startTime estRunTime core memory disk
  public static Job parseJobInfoFromLSTJ(String[] jobInfo) {
    Job job = null;
    try {
      int jobID = Integer.parseInt(jobInfo[0]);
      int jobState = Integer.parseInt(jobInfo[1]);
      int submitTime = Integer.parseInt(jobInfo[2]);
      int startTime = Integer.parseInt(jobInfo[3]);
      int estRunTime = Integer.parseInt(jobInfo[4]);
      int core = Integer.parseInt(jobInfo[5]);
      int memory = Integer.parseInt(jobInfo[6]);
      int disk = Integer.parseInt(jobInfo[7]);

      job = new Job(jobID, jobState, submitTime, startTime, estRunTime, core, memory, disk);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("NumberFormatException ==> " + e.getMessage());
    }
    return job;
  }

  public static Job parseJobInfo(String[] jobInfo) {
    Job job = null;
    try {
      int submitTime = Integer.parseInt(jobInfo[1]);
      int jobID = Integer.parseInt(jobInfo[2]);
      int estRunTime = Integer.parseInt(jobInfo[4]);
      int core = Integer.parseInt(jobInfo[4]);
      int memory = Integer.parseInt(jobInfo[5]);
      int disk = Integer.parseInt(jobInfo[6]);

      job = new Job(jobID, submitTime, estRunTime, core, memory, disk);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ArrayIndexOutOfBoundsException ==> " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("NumberFormatException ==> " + e.getMessage());
    }
    return job;
  }
  
}

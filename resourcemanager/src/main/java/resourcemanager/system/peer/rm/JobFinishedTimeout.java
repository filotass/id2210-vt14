package resourcemanager.system.peer.rm;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class JobFinishedTimeout extends Timeout{

	private long jobID;
	
	public JobFinishedTimeout(ScheduleTimeout request, long jobID) {
		super(request);
		this.jobID = jobID;
	}
	
	public long getJobID(){
		return jobID;
	}

}

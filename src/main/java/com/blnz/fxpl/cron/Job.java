package com.blnz.fxpl.cron;


/**
 * the interface for a cron job, managed by the cron service.
 * objects managed by the cron service are run at a scheduled time
 * determined by the "unix-like" cron expression or refreshed
 * periodically at certain time interval
 */
public interface Job
{
    /**
     * get name for this cron job
     */
    public String getJobName();
    
    /**
     * get group name for this cron job, several jobs can belong to a group
     */
    public String getJobGroupName();
    
    /**
     * get cron expression for this job
     */
    public String getCronExpr();

    /**
     * method called by the cron job when its time to run
     */
    public void runJob() throws Exception;
    
}

	

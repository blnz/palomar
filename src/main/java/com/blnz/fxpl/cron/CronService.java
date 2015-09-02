// $Id: CronService.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.cron;

/**
 * manages the scheduling of tasks which are to be
 * run at some future time.
 */
public interface CronService
{
    /**
     * add an item to the set managed by the CronService
     */
    public void add(Refreshable cronItem);

    /**
     * remove an item from the set managed by the CronService
     */
    public void remove(Refreshable cronItem);

    /**
     * return an array containing all the refreshable items 
     */
    public Refreshable[] list();

    /**
     * remind the service to wake up and refresh any of its
     * managed items which are due
     */
    public void ping();

    /**
     * stop scheduling new tasks. 
     *  Currently executing tasks are permitted to complete
     */
    public void halt();
}

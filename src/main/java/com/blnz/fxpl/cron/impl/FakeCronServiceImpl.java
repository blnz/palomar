package com.blnz.fxpl.cron.impl;

import com.blnz.fxpl.cron.CronService;
import com.blnz.fxpl.cron.Refreshable;

/**
 * a null CronService that quitly does nothing
 */
public class FakeCronServiceImpl implements CronService
{
    /**
     *
     */
    public FakeCronServiceImpl()
    { }

    /**
     * add an item to the set managed by the CronService
     */
    public void add(Refreshable cronItem)
    { }

    /**
     * remove an item from the set managed by the CronService
     */
    public void remove(Refreshable cronItem)
    { }

    /**
     * return an array containing all the refreshable items 
     */
    public Refreshable[] list()
    { return new Refreshable[0]; } 

    /**
     * remind the service to wake up and refresh any of its
     * managed items which are due
     */
    public void ping()
    { }

    /**
     * stop scheduling new tasks. 
     *  Currently executing tasks are permitted to complete
     */
    public void halt()
    { }
}

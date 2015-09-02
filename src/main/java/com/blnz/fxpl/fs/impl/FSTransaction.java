package com.blnz.fxpl.fs.impl;

import com.blnz.fxpl.fs.Transaction;
import com.blnz.fxpl.fs.TransactionParticipant;

import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.sql.SQLException;
import java.sql.Connection;

/**
 *
 */
public class FSTransaction implements Transaction
{
    private File _fsResource = null;
    
    private Vector _participants = new Vector();

    /** constructor obtains for itself a  connection which can be rolled back */
    public FSTransaction() throws Exception
    {
	//set DAV connection ?
    }
    
    /** constructor obtains for itself a  connection which can be rolled back */
    public FSTransaction(File fsRes) throws Exception
    {
	//set DAV connection ?
	_fsResource = fsRes;
    }
    
    /**
     *
     */
    public void commit() throws SQLException 
    { 
	TransactionParticipant bo = null;
	
	// Success! now callback for all participants we've flushed
	for (Enumeration e = _participants.elements() ; e.hasMoreElements() ;) {
	    bo = (TransactionParticipant)e.nextElement();
	    bo.notifyCommit();
	}
        
	// remove all BOs from list
	_participants.clear();
    }
    
    public void rollback() throws SQLException
    {
	//do something
	
	// remove all BOs from list
	_participants.clear();
    }

    /**
     *
     */
    public void addParticipant(TransactionParticipant bo)
    {
	_participants.add(bo);
    }


    public void close() throws Exception
    {
	    _fsResource = null;
	}
    
    //FIXME: 
    public Connection getDBProxy() { return null; }
}

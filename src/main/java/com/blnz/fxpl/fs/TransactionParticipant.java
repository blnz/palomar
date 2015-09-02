package com.blnz.fxpl.fs;

import java.sql.SQLException;

/**
 *
 */
public interface TransactionParticipant
{

    /**
     *
     */
    public void notifyCommit() throws SQLException ;

    /**
     *
     */
    public void notifyRollback() throws SQLException;

}

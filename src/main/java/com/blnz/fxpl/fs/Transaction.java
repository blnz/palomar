package com.blnz.fxpl.fs;

import java.sql.SQLException;
import java.sql.Connection;

/**
 *
 */
public interface Transaction
{

    /**
     *
     */
    public void commit() throws SQLException ;

    /**
     *
     */
    public void rollback() throws SQLException;

    /**
     * add an item which will be interested in being
     * notified of commit and/or rollback actions
     */
    public void addParticipant(TransactionParticipant tp);

    /**
     *
     */
    public Connection getDBProxy();

    /**
     *
     */
    public void close() throws Exception;

}

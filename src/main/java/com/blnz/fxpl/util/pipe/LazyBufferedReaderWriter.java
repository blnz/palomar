//$Id: LazyBufferedReaderWriter.java 710 2006-02-23 23:21:24Z upayavira $

package com.blnz.fxpl.util.pipe;

import com.blnz.fxpl.log.Log;

import java.io.*;

/**
 * wraps an OutputStream for UTF-8 encoded characters 
 * with a buffering mechanism.
 * After closing, one may obtain a character Reader or 
 * InputStream to that contents
 */
public class LazyBufferedReaderWriter extends OutputStream 
{
    public static final String ENCODING = "UTF8";

    /**
     * The internal buffer where data is stored. 
     */
    private byte _cacheBuff[] = new byte[1000 * 8];
    
    /**
     * The number of valid bytes in the buffer. This value is always 
     * in the range <tt>0</tt> through <tt>buf.length</tt>; elements 
     * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid 
     * byte data.
     */
    private int _count;
    
    // flag for checking the cached stream status
    private boolean _sizeExceeded = false;
    
    private boolean _deleteOnExit = true;
    
    private File _file = null;

    private OutputStream _out = null;


    /**
     * Constructor with the outstream
     *
     * @param   out   the output stream into which bytes are sent.
     */
    public LazyBufferedReaderWriter(OutputStream out) 
    {
        _out = out;
    }
    
    /**
     * Constructor with File paramter
     * @param file   A File to write to
     */
    public LazyBufferedReaderWriter(File file)
    {
        setFile(file);
    }

    
    /**
     * Constructor with file location paramter and customized buffer size
     * @param file   A file output stream location
     * @param   size   the buffer size.
     */
    public LazyBufferedReaderWriter(File file,  int buffSize)
    {
        setFile(file);
        setBufferSize(buffSize);
    }
    
    /**
     * Constructor with outstream and the customized buffer size
     *
     * @param   out    the underlying output stream.
     * @param   size   the buffer size.
     */
    public LazyBufferedReaderWriter(OutputStream out, int buffSize) 
    {
        _out = out;
        setBufferSize(buffSize);
    }
    
    /** 
     * Sets the output stream for writing.
     * Will be called when the stream size exceeds the defined buffer size.
     * @see BufferedOutputStream
     * @see FileOutputStream. 
     */
    private void setOutStream()
    {        
        try {
            File toWrite = _file;

            if (toWrite.exists()) {
                toWrite.delete();
            }

            toWrite.createNewFile();

            if (_deleteOnExit) {
                toWrite.deleteOnExit();
            } 
            _out = new BufferedOutputStream( new FileOutputStream(toWrite) );
        } catch( Exception setOSEx) {
            Log.getLogger().error(setOSEx.getMessage());
        }
    }
    
    /** Sets the life time of the file for that VM only. 
     *  Defaults to true.
     */
    public void setDeleteOnExit(boolean deleteOnExit)
    {
        _deleteOnExit = deleteOnExit;
    }
    
    /** 
     *  Setter for the stream cache size 
     */
    public void setBufferSize( int buffSize)
    {
        if (buffSize > 0) {
            _cacheBuff = new byte[buffSize];
        }
    }
    
    /** 
     * Sets the location for output stream 
     */
    public void setFile( File file)
    {
        _file = file;
    }
    
    
    /** 
     * Returns the streamed location 
     */
    public File getFile()
    {
        return _file;
    }

    
    /**
     * Writes the specified byte to this buffered output stream. 
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void write(int b) throws IOException 
    {
        if (_sizeExceeded) {
            flushBuffer();
        } else {   
            if (_count >= _cacheBuff.length) {
                _sizeExceeded = true;               
                setOutStream();
                flushBuffer();
            }
            _cacheBuff[_count++] = (byte)b;
        }
    }
    
    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this buffered output stream.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void write(byte b[], 
                                   int off, int len) 
        throws IOException 
    {

        if (_sizeExceeded) {
            flushBuffer();
            _out.write(b, off, len);
            return;
        } else {
            if (len >= _cacheBuff.length ) {
                // If the request length exceeds the size of the output buffer 
                // write the data directly.
                _sizeExceeded = true; 
                setOutStream();
                flushBuffer();
                _out.write(b, off, len);
                return;
            }
            // if the request length plus my counter exceeds the custom set cache size
            // ,then set the flag and start flushing 
            if ( len > (_cacheBuff.length - _count) ) {
                _sizeExceeded = true;
                setOutStream();
                flushBuffer();
            }
            System.arraycopy(b, off, _cacheBuff, _count, len);
            _count += len;
        }        
    }
    
    
    /** Flush the internal buffer */
    private void flushBuffer() throws IOException 
    {
        if (_count > 0) {
            _out.write(_cacheBuff, 0, _count);
            _count = 0;
        }
    }
    
    /**
     * Flushes the underlying buffered output stream. This forces any buffered 
     * output bytes to be written out to the underlying output stream. 
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     */
    public synchronized void flush() throws IOException 
    {
        if (_sizeExceeded) {
            flushBuffer();
            _out.flush();
        }
    }
    
    /**
     * @return true if buffer has not yet reached the size before writing
     */
    public boolean isInMemory()
    {
        return !_sizeExceeded;
    }
    
    /**
     *  Returns a <code>Reader</code> form of cached buffer.
     *  @return Reader the reader of the buffer
     */
    private Reader getCacheBuff()
    { // not sure of its efficiency ....
        try {
            return new StringReader ( ( new String(_cacheBuff, ENCODING)).trim() );
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    
    /**
     *  Returns a <code>Reader</code> for the contents.
     *  @return Reader the reader of the buffer
     */
    public Reader getReader()
    { 
        Reader r = null;
        if (isInMemory()) {
            r = getCacheBuff();
        }else {
            try {
                File f = getFile();
                FileInputStream fi = new FileInputStream(f);
                r = new BufferedReader(new InputStreamReader(fi, ENCODING));
            } catch (Exception ex) {
                Log.getLogger().error("unable to get Reader", ex);
            }
        }
        return r;
    }
    
    /**
     * Closes this output stream
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#flush()
     * @see        java.io.FilterOutputStream#out
     */
    public void close() throws IOException 
    {
        try {
            flush();
        } catch (IOException ioE) {
            Log.getLogger().error("error closing", ioE);
        }
        if (!isInMemory()) {
            _out.close();  // we'll let any exception through
        }
    }  
    
    /**
     * remove the destination if it was a file
     */ 
    public synchronized void clean() //throws Exception
    {
        try {
            if ( isInMemory() ) {
                _cacheBuff = null; // make orphan pointer and do gc
            } else {
                close();
                File toDestroy = getFile();
                if (toDestroy.exists()) {
                    toDestroy.delete();
                }
            }
            _cacheBuff = null;
        } catch(Exception cleanEx) {
            Log.getLogger().error("unable to clean", cleanEx);
        } finally {
            //System.gc(); // just a try
        }
    }
    
    private void forceDelFile(File toDelete)
    {
        try {
            Runtime rt = Runtime.getRuntime();
            String cmdarr[] = {"del",toDelete.toString()};
            Process p = rt.exec(cmdarr);
            p.waitFor();
        } catch( Exception e ) {
            Log.getLogger().error("unable to del file " + toDelete.toString(), e);
        }
    }

}





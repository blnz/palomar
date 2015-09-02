package com.blnz.fxpl.util.pipe;

import java.io.*;

import com.blnz.fxpl.log.Log;

/**
 * class is an <code>OutputStream</code>, but write's into more than one sink.
 * @see OutputStream
 */
public class StreamSplitter extends FilterOutputStream 
{
        
    /** 
     * The output stream to be cached 
     */
    private BufferedOutputStream cacheBufStream = null;
        
    /**
     * constructor with the underlying sink.
     */
    public StreamSplitter(OutputStream out)
    {
        super(out);
    }

    /**
     * constructor with the underlying sink and a URL location to write to.
     *
     * @param   out   the normal output stream for application usage
     * @param   fileUrl file location to cache the stream
     */
    public StreamSplitter(OutputStream out, String fileUrl) 
    {
        super(out); 
        if (Log.getLogger().isDebugEnabled()) {
            Log.getLogger().debug("StreamSplitter: gonna open second stream to: " + fileUrl);
        }
        setOutStream2(fileUrl);    
    }
        
    /**
     * constructor with the underlying sink and an additional sink to write to.
     *
     * @param   out   the normal output stream for application usage
     * @param   out2  additional location to cache the stream
     */
    public StreamSplitter(OutputStream out, OutputStream out2)
    {
        super(out);
        setOutStream2(out2);
    }
        
    /** 
     * Method for setting the second output stream 
     */
    public void setOutStream2(String fileUrl)
    {
        try {
            cacheBufStream = 
                new BufferedOutputStream( new FileOutputStream(fileUrl, true) );
        } catch(FileNotFoundException fnfEx) {
            fnfEx.printStackTrace();
        }    
    }

    /** 
     * Sets the second stream 
     */
    public void setOutStream2(OutputStream out2)
    {
        try{
            cacheBufStream = new BufferedOutputStream(out2);
        } catch( Exception ioE) {
            ioE.printStackTrace();
        }
        
    }
           
    /**
     * Writes the specified <code>byte</code> to the output streams. 
     * <p>
     * The <code>write</code> method of <code>StreamSplitter</code> 
     * calls the <code>write</code> method of its underlying output stream, 
     * that is, it performs <tt>out.write(b)</tt>.
     * <p>
     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>. 
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException 
    {
        out.write(b);
        cacheBufStream.write(b);
    }
        
    /**
     * Writes <code>b.length</code> bytes to the output streams. 
     * <p>
     * The <code>write</code> method of <code>StreamSplitter</code> 
     * calls its <code>write</code> method of three arguments with the 
     * arguments <code>b</code>, <code>0</code>, and 
     * <code>b.length</code>. 
     * <p>
     * Note that this method does not call the one-argument 
     * <code>write</code> method of its underlying stream with the single 
     * argument <code>b</code>. 
     *
     * @param      b   the data to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        StreamSplitter#write(byte[], int, int)
     */
    public void write(byte b[]) throws IOException 
    {
        write(b, 0, b.length);
    }
        
    /**
     * Writes <code>len</code> bytes from the specified 
     * <code>byte</code> array starting at offset <code>off</code> to 
     * the output streams. 
     * <p>
     * The <code>write</code> method of <code>StreamSplitter</code> 
     * calls the <code>write</code> method of one argument on each 
     * <code>byte</code> to output. 
     * <p>
     * Note that this method does not call the <code>write</code> method 
     * of its underlying input stream with the same arguments. Subclasses 
     * of <code>StreamSplitter</code> should provide a more efficient 
     * implementation of this method. 
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     * @see        StreamSplitter#write(int)
     */
    public void write(byte b[], int off, int len) throws IOException 
    {
        out.write(b, off, len);
        cacheBufStream.write(b, off, len);
        //         for (int i = 0 ; i < len ; i++) {
        //             write(b[off + i]);
        //         }
        
    }
        
    /**
     * Flushes this output stream and forces any buffered output bytes 
     * to be written out to the stream. 
     * <p>
     * The <code>flush</code> method of <code>StreamSplitter</code> 
     * calls the <code>flush</code> method of its underlying output streams. 
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        StreamSplitter#out
     * @see        StreamSplitter#cacheBufStream
     */
    public void flush() throws IOException 
    {
        out.flush();
        cacheBufStream.flush();
    }
        
    /**
     * Closes this output stream and releases any system resources 
     * associated with the stream. 
     * <p>
     * The <code>close</code> method of <code>StreamSplitter</code> 
     * calls its <code>flush</code> method, and then calls the 
     * <code>close</code> method of its underlying output streams. 
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        StreamSplitter#flush()
     * @see        StreamSplitter#out
     * @see        StreamSplitter#cacheBufStream
     */
    public void close() throws IOException 
    {
        try {
            flush();
        } catch (IOException ignored) {
        }
        out.close();
        cacheBufStream.close();
    }
}


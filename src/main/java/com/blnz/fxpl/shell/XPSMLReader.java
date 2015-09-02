package com.blnz.fxpl.shell;

import java.io.Reader;
import java.io.IOException;

import java.io.File;
import java.io.Writer;

import java.io.FileReader;
import java.io.FileWriter;


/**
 * a Reader which wraps another Reader, and
 * silently transforms XPSML syntax (looks kinda like java) to XML
 */
public class XPSMLReader extends Reader
{

    private static final short START = 0;
    private static final short LITERAL = 1;
    private static final short TAGNAME = 2;
    private static final short PCDATA = 3;
    private static final short ATTSLIST = 4;
    private static final short PENDING_CONTENT = 5;
    private static final short POSTLOGUE = 6;
    private static final short IN_CONTENT = 7;
    private static final short MAYBE_DECL1 = 8;
    private static final short MAYBE_DECL2 = 9;
    private static final short MAYBE_DECL3 = 10;
    private static final short MAYBE_DECL4 = 11;
    private static final short MAYBE_DECL5 = 12;
    private static final short MAYBE_DECL6 = 13;
    private static final short MAYBE_DECL7 = 14;
    private static final short IN_DECL = 15;
    private static final short END_DECL = 16;
    private static final short CONTENT_READY = 17;
    private static final short IN_STAGNAME = 18;
    private static final short MAYBE_COMMENT = 19;
    private static final short IN_COMMENT1 = 20;
    private static final short IN_COMMENT2 = 21;
    private static final short MAYBE_END_COMMENT = 22;
    private static final short ATTLIST_READY = 23;
    private static final short IN_ATTNAME = 24;
    private static final short PENDING_ATTEQUAL = 25;
    private static final short PENDING_ATTLITERAL = 26;
    private static final short IN_ATTLITERAL = 27;

    private Reader _src;

    private char[] _pending = new char[256];
    private int _pendStart = 0;
    private int _pendEnd = 0;

    private String _stag;
    private String[] _stags = new String[256];
    private int _stagIx = 0;

    private short _fsmState = START;
    private boolean _eof = false;

    private int _newlines = 0;

    private int _next;
    char[] _chb = new char[1];
    private char _nextCh = '_';

    private char _litChar;

    char[] _tmp = new char[256];
    int _tmpIx = 0;

    private int _toOffset;
    private int _toLength;

    public XPSMLReader(Reader src)
    {
        _src = src;
    }


    private final void nextChar() throws IOException
    {
        if ( _src.read(_tmp, _tmpIx, 1) < 1) {
            _eof = true;
        } else {
            _nextCh = _tmp[_tmpIx++];
            if (_nextCh == '\n') {
                ++_newlines;
            }
        }
    }

    /**
     *
     */
    public int read(char[] toBuf, int toOffset, int toLength)
        throws IOException
    {

        int numSent = 0;
        char[] next = new char[1];
        
        while (toLength > 0) {
            if (_pendEnd > 0) {
                while ((_pendStart < _pendEnd)) {
                    if (toLength <= 0) {
                        return numSent;
                    }
                    toBuf[toOffset++] = _pending[_pendStart++];
                    --toLength;
                    ++numSent;
                    // debug("sent 1 with toLength = " + toLength + " of "  + toBuf[toOffset -1 ]);
                }
                _pendStart = 0;
                _pendEnd = 0;
            }
            
            if (_eof) {
                if (numSent == 0) {
                    return -1;
                } else {
                    // debug ("sent [" + new String(toBuf, 0, numSent) + "]");
                    return numSent; 
                }
            }

            // debug("lastChar: {" + _nextCh + "} state: " + _fsmState);
            switch (_fsmState) {
                
            case START:
                nextChar();
                if (_eof) {
                    break;
                }
                if (Character.isWhitespace(_nextCh)) {
                    _tmpIx = 0;
                    break;
                } else if ('<' == _nextCh) {
                    _fsmState = MAYBE_DECL1;
                } else {
                    tmpToPending();
                    _fsmState = POSTLOGUE;
                }
                break;
                
            case MAYBE_DECL1:
                nextChar();
                
                if (!_eof && '?' == _nextCh) {
                    _fsmState = MAYBE_DECL2;
                } else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                
            case MAYBE_DECL2:
                nextChar();
                
                if (!_eof && 'x' == _nextCh) {
                    _fsmState = MAYBE_DECL3;
                } else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                
            case MAYBE_DECL3:
                nextChar();
                
                if (!_eof && 'p' == _nextCh) {
                    _fsmState = MAYBE_DECL4;
                } else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                
            case MAYBE_DECL4:
                nextChar();
                if (!_eof && 's' == _nextCh) {
                    _fsmState = MAYBE_DECL5;
                } else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                
            case MAYBE_DECL5:
                nextChar();
                
                if (!_eof && 'm' == _nextCh) {
                    _fsmState = MAYBE_DECL6;
                } else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                
            case MAYBE_DECL6:
                nextChar();
                
                if (!_eof && 'l' == _nextCh) {
                    _fsmState = IN_DECL;
                } else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                

            case IN_DECL:
                nextChar();
                if (!_eof && '?' == _nextCh) {
                    _fsmState = END_DECL;
                } 
                break;

            case END_DECL:
                nextChar();
                if (!_eof && '>' == _nextCh) {
                    _tmpIx = 0;
                    _fsmState = CONTENT_READY;
                } 
                else {
                    _fsmState = POSTLOGUE;
                    tmpToPending();
                }
                break;
                
            case CONTENT_READY:
                nextChar();
                if (_eof) {
                    break;
                }

                if ( Character.isWhitespace(_nextCh) ) {
                    _tmpIx = 0;
                } else if ('/' == _nextCh) {
                    _tmpIx = 0;
                    //                    System.out.println("got first slash ");
                    _fsmState = MAYBE_COMMENT;
                } else if (';' == _nextCh) {
                    _tmpIx = 0;
                } else if ('\'' == _nextCh || '"' == _nextCh) {
                    _litChar = _nextCh;
                    _tmpIx = 0;
                    _fsmState = LITERAL;
                } else if ('}' == _nextCh) {
                    _pending[_pendEnd++] = '<';
                    _pending[_pendEnd++] = '/';
                    String tagname = _stags[--_stagIx];
                    //                     debug("gonna end {" + tagname + "} length: " +
                    //                                        tagname.length());
                    tagname.getChars(0, tagname.length(), _pending, _pendEnd);
                    _pendEnd += tagname.length();
                    newlines();
                    _pending[_pendEnd++] = '>';
                } else {
                    //                    _pending[_pendEnd++] = '<';
                    _fsmState = IN_STAGNAME;
                }
                break;


            case MAYBE_COMMENT:
                nextChar();
                if (_eof) {
                    break;
                }
                if ('/' == _nextCh) {
                    //                    System.out.println("got second slash ");
                    _tmpIx = 0;
                    _pending[_pendEnd++] = '<';
                    _pending[_pendEnd++] = '!';
                    _pending[_pendEnd++] = '-';
                    _pending[_pendEnd++] = '-';
                    _fsmState = IN_COMMENT1;
                }
                break;

            case IN_COMMENT1:
                nextChar();
                if (_eof || '\n' == _nextCh) {
                    _tmpIx = 0;
                    _pending[_pendEnd++] = '\n';
                    _pending[_pendEnd++] = '-';
                    _pending[_pendEnd++] = '-';
                    _pending[_pendEnd++] = '>';
                    _fsmState = CONTENT_READY;
                } else {
                    tmpToPending();
                }
                break;

            case IN_STAGNAME:
                nextChar();
                if (_eof) {
                    tmpToPending();
                    break;
                }
                if ( Character.isWhitespace(_nextCh) ) {
                    _stag = new String(_tmp, 0, _tmpIx - 1);
                    _tmpIx = 0;
                    _fsmState = ATTLIST_READY;
                } else if ('(' == _nextCh) {
                    _stag = new String(_tmp, 0, _tmpIx - 1);
                    _tmpIx = 0;

                    _pending[_pendEnd++] = '<';
                    _stag.getChars(0, _stag.length(), _pending, _pendEnd);
                    _pendEnd += _stag.length();
                    _pending[_pendEnd++] = ' ';

                    _fsmState = ATTSLIST;

                } else if ('{' == _nextCh) {
                    _stag = new String(_tmp, 0, _tmpIx - 1);
                    _tmpIx = 0;
                    _stags[_stagIx++] = _stag;

                    _pending[_pendEnd++] = '<';
                    _stag.getChars(0, _stag.length(), _pending, _pendEnd);
                    _pendEnd += _stag.length();
                    _pending[_pendEnd++] = '>';

                    _fsmState = CONTENT_READY;

                } else if (';' == _nextCh) {
                    _stag = new String(_tmp, 0, _tmpIx - 1);
                    _tmpIx = 0;

                    _pending[_pendEnd++] = '<';
                    _stag.getChars(0, _stag.length(), _pending, _pendEnd);
                    _pendEnd += _stag.length();
                    _pending[_pendEnd++] = '/';
                    _pending[_pendEnd++] = '>';

                    _fsmState = CONTENT_READY;

                }

                break;

            case ATTLIST_READY:
                nextChar();
                if (_eof) {
                    tmpToPending();
                    break;
                }
                if ( Character.isWhitespace(_nextCh) ) {
                    _tmpIx = 0;

                } else if ('(' == _nextCh) {

                    _tmpIx = 0;

                    _pending[_pendEnd++] = '<';
                    _stag.getChars(0, _stag.length(), _pending, _pendEnd);
                    _pendEnd += _stag.length();
                    _pending[_pendEnd++] = ' ';

                    _fsmState = ATTSLIST;

                } else if ('{' == _nextCh) {

                    _tmpIx = 0;
                    _stags[_stagIx++] = _stag;

                    _pending[_pendEnd++] = '<';
                    _stag.getChars(0, _stag.length(), _pending, _pendEnd);
                    _pendEnd += _stag.length();
                    _pending[_pendEnd++] = '>';

                    _fsmState = CONTENT_READY;

                } else if (';' == _nextCh) {

                    _tmpIx = 0;

                    _pending[_pendEnd++] = '<';
                    _stag.getChars(0, _stag.length(), _pending, _pendEnd);
                    _pendEnd += _stag.length();
                    newlines();
                    _pending[_pendEnd++] = '/';
                    _pending[_pendEnd++] = '>';

                    _fsmState = CONTENT_READY;

                }
                // else ??
                break;

            case ATTSLIST:
                nextChar();
                if (_eof) {
                    break;
                }
                if ( Character.isWhitespace(_nextCh) || ',' == _nextCh ) {
                    _tmpIx = 0;
                } else if (')' == _nextCh) {
                    _tmpIx = 0;
                    _fsmState = PENDING_CONTENT;
                } else {
                    _tmpIx = 0;
                    _pending[_pendEnd++] = _nextCh;
                    _fsmState = IN_ATTNAME;
                }
                break;

            case IN_ATTNAME:
                nextChar();
                if (_eof) {
                    break;
                }
                if ( Character.isWhitespace(_nextCh) || ',' == _nextCh ) {
                    _tmpIx = 0;
                    _fsmState = PENDING_ATTEQUAL;
                } else if ('=' == _nextCh) {
                    _tmpIx = 0;
                    _pending[_pendEnd++] = '=';
                    _fsmState = PENDING_ATTLITERAL;
                } else {
                    tmpToPending();
                }
                break;

            case PENDING_ATTEQUAL:
                nextChar();
                if (_eof) {
                    break;
                }
                if ( Character.isWhitespace(_nextCh) ) {
                    _tmpIx = 0;
                } else if ('=' == _nextCh) {
                    _tmpIx = 0;
                    _pending[_pendEnd++] = '=';
                    _fsmState = PENDING_ATTLITERAL;
                } else {
                    tmpToPending();
                    _fsmState = POSTLOGUE;
                }
                break;

            case PENDING_ATTLITERAL:
                nextChar();
                if (_eof) {
                    break;
                }
                if ( Character.isWhitespace(_nextCh) ) {
                    _tmpIx = 0;
                } else if (('\'' == _nextCh) || ('"' == _nextCh)) {
                    _tmpIx = 0;
                    _pending[_pendEnd++] = _nextCh;
                    _litChar = _nextCh;
                    _fsmState = IN_ATTLITERAL;
                } else {
                    tmpToPending();
                    _fsmState = POSTLOGUE;
                }
                break;

            case IN_ATTLITERAL:
                nextChar();
                if (_eof) {
                    break;
                }
                if (_nextCh == _litChar)  {
                    tmpToPending();
                    _pending[_pendEnd++] = ' ';
                    _fsmState = ATTSLIST;

                } else {
                    tmpToPending();
                }
                break;

            case LITERAL:
                nextChar();
                if (_eof) {
                    break;
                }
                if (_nextCh == _litChar)  {
                    _tmpIx = 0;
                    _fsmState = CONTENT_READY;

                } else {
                    tmpToPending();
                }
                break;

            case PENDING_CONTENT:

                nextChar();
                if (_eof) {
                    tmpToPending();
                    break;
                }
                if ( Character.isWhitespace(_nextCh) ) {
                    _tmpIx = 0;


                } else if ('{' == _nextCh) {
                    _tmpIx = 0;
                    newlines();
                    _pending[_pendEnd++] = '>';
                    _stags[_stagIx++] = _stag;
                    _fsmState = CONTENT_READY;

                } else if (';' == _nextCh) {
                    _tmpIx = 0;
                    newlines();
                    _pending[_pendEnd++] = '/';
                    _pending[_pendEnd++] = '>';
                    _fsmState = CONTENT_READY;

                }
                // else ??

                
                break;
                
            case POSTLOGUE:
                // debug("postlogue gonna read toLength:" + toLength);
                int more = _src.read(toBuf, toOffset, toLength);
                
                if (more <= 0) {
                    _eof = true;

                } else {
                    numSent += more;
                    toOffset += more;
                    toLength -= more;
                }
                break;
            }

        }
        return (numSent == 0 && _eof) ? -1 : numSent;

    }

    private void tmpToPending()
    {
        // FIXME: grow _pending if necessary
        for (int i = 0; i < _tmpIx; ++i) {
            _pending[_pendEnd++] = _tmp[i];
            if (_tmp[i] == '\n') {
                --_newlines;
            }
        }
        _tmpIx = 0;
    }
    
    private final void newlines()
    {
        for ( ; _newlines > 0 ; --_newlines) {
            _pending[_pendEnd++] = '\n';
        }
    }
    
    /**
     *
     */
    public void close()
        throws IOException
    {
        _src.close();
    }

    /**
     *
     */
    public static void main(String[] args)
    {
        if (args.length < 2) {
            System.err.println("need   file");
        }
        File f = new File(args[0]);
        File f2 = new File(args[1]);
        Reader fr;
        Writer fw;
        try {
            fr = new FileReader(f);
            fw = new FileWriter(f2);
            XPSMLReader xr = new XPSMLReader(fr);
            char[] cbuf = new char[256];
            int rl ;
            while ( (rl = xr.read(cbuf, 0, 256)) > 0) {
                fw.write(cbuf, 0, rl);
            }
            fr.close();
            fw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

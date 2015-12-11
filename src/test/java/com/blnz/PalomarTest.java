/*
 *
 */
package com.blnz;

import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXConnection;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;
import com.blnz.fxpl.shell.XPSMLReader;
import com.blnz.fxpl.xform.XForm;

import org.xml.sax.ContentHandler;

import java.io.StringWriter;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Logger Service tests.
 */
public class PalomarTest {
    
    private static FXConnection _xfy;

    /**
     * Initialize service before any tests are run.
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("beforeClass");
        _xfy = FXHome.getFXConnection("local", 
                                      "admin", 
                                      "admin");
        assertNotNull(_xfy);

    }
    
    /**
     * Tear down service after all tests complete.
     * @throws Exception
     */
    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("nothing to do afterClass");
    }
    

    /**
     * Positive test for something
     */
    @Test
    public void handleSomethingTest() throws Exception {

        String result = parseString("foo(a='1'){bar(b='2'){baz;}};");
        System.out.println(result);
        assertTrue("<foo a='1' ><bar b='2' ><baz/></bar></foo>" + " expected",
                   "<foo a='1' ><bar b='2' ><baz/></bar></foo>".equals(result));
        result = evalString(result);
        System.out.println(result);
        assert("<foo xmlns=\"http://namespaces.blnz.com/fxpl\" a=\"1\"><bar b=\"2\"><baz/></bar></foo>".equals(result));
    }
    

    @Test
    public void testEvals() throws Exception {
        for (int i = 0; i < evalTests.length ; ++i ) {
            String result = evalString(parseString(evalTests[i][0]));
            assert(evalTests[i][1].equals(result));
        }
    }

    private String parseString(String itxt)  throws Exception {
        char[] _buf = new char[1024];
        String result = null;

        StringReader sr = new StringReader("<?xpsml?>" + itxt + ";");
        XPSMLReader xr = new XPSMLReader(sr);
        StringWriter sw = new StringWriter();
        int len;
        while ((len = xr.read(_buf)) > 0) {
            sw.write(_buf, 0, len);
        }
        result = sw.toString();
        System.out.println(result);
        return result;
    }

    
    private String evalString(String itxt)  throws Exception {
        String result = null;
        
        StringWriter out = new StringWriter();

        FXContext context = _xfy.getExtendedContext();

        // someplace to put the output
        ContentHandler ch = XForm.createCharacterContentWriter(out);

        System.out.println("eval() will eval [" + itxt + "]");
        
        _xfy.evalFXString(itxt, ch, context);

        result = out.toString();
        System.out.println(result);
        return result;

    }

    private String[][] evalTests = {
        { "foo(a='1'){bar(b='2'){baz;}};", 
          "<foo xmlns=\"http://namespaces.blnz.com/fxpl\" a=\"1\"><bar b=\"2\"><baz/></bar></foo>" },
        { "foo(a='1'){bar(b='YABBA'){baz;}};", 
          "<foo xmlns=\"http://namespaces.blnz.com/fxpl\" a=\"1\"><bar b=\"YABBA\"><baz/></bar></foo>" },
        { "itemPut(name='/foo.xml'){bar(b='YABBA' c='123'){baz;}};", 
          "<success xmlns=\"http://namespaces.blnz.com/fxpl\" name=\"/foo.xml\"/>" },
        { "foo(xmlns='' a='1'){bar(b='YABBA'){baz;}};", 
          "<foo a=\"1\"><bar b=\"YABBA\"><baz/></bar></foo>" },
        { "itemEval(name='src/test/fxpl/test.fx');",
          "<success/>"}
    };
    
    /**
     * Positive test for something
     */
    @Test
    public void handleSomethingElseTest() throws Exception {

        for (int i = 0; i < evalTests.length ; ++i) {
            String src = evalTests[i][0];
            String expect = evalTests[i][1];
            System.out.println("testing: [" + src + "]");

            String result = evalString(parseString(src));
            assertTrue("Item " + (i + 1) + "\nexpected \n[" + expect + "]\n but got \n[" + result + "]",
                       expect.equals(result));

        }
    }
}

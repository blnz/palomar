/*
 *
 */
package com.blnz;

import com.blnz.fxpl.shell.XPSMLReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;


/**
 * XPSML unit tests.
 */
public class XPSMLTest {
    

    /**
     * Initialize service before any tests are run.
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("beforeClass");
    }
    
    /**
     * Tear down service after all tests complete.
     * @throws Exception
     */
    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("afterClass");
    }
    
    /**
     * Positive test for something
     */
    @Test
    public void handleSomethingTest() throws IOException {
        char[] _buf = new char[1024];
        StringReader sr = new StringReader("<?xpsml?>foo(a='1'){bar(b='2'){baz;}};");
        XPSMLReader xr = new XPSMLReader(sr);
        StringWriter sw = new StringWriter();
        int len;
        while ((len = xr.read(_buf)) > 0) {
            sw.write(_buf, 0, len);
        }
        String txt = sw.toString();
        System.out.println(txt);
        assert("<foo a='1' ><bar b='2' ><baz/></bar></foo>".equals(txt));
    }
    
    /**
     * Positive test for something
     */
    @Test
    public void handleSomethingElseTest() {
        System.out.println("testing something else .. testing");
    }
    
}

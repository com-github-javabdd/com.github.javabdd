// IOTests.java, created Nov 20, 2006 4:55:28 PM by jwhaley
// Copyright (C) 2006 jwhaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package bdd;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

/**
 * IOTests
 * 
 * @author jwhaley
 * @version $Id$
 */
public class IOTests extends BDDTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IOTests.class);
    }

    public void testLoad() {
        reset();
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            BDDFactory bdd = nextFactory();
            File tmp = null;
            Exception error = null;
            try {
                tmp = File.createTempFile("loadtest", "bdd");
                tmp.deleteOnExit();
                PrintWriter out = new PrintWriter(tmp);
                out.println("2 3");
                out.println("0 1 2");
                out.println("222 1 1 0");
                out.println("333 2 1 222");
                out.close();
                BDD x = bdd.load(tmp.getAbsolutePath());
                tmp.delete();
                //x.printDot();
                Assert.assertEquals(6.0, x.satCount(), 0.001);
                x.free();
            } catch (IOException x) {
                error = x;
            } finally {
                if (tmp != null) tmp.delete();
            }
            if (error != null)
                Assert.fail(error.toString());
        }
    }

}

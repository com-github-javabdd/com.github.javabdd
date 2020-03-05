// R2.java, created Jul 28, 2004 2:55:30 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package regression;

import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarSet;
import bdd.BDDTestCase;

/**
 * support() bug
 * 
 * @author John Whaley
 * @version $Id: R2.java 469 2006-11-29 08:07:31Z joewhaley $
 */
public class R2 extends BDDTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(R2.class);
    }
    
    public void testR2() {
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            BDDFactory bdd = nextFactory();
            BDD zero = bdd.zero();
            BDD one = bdd.universe();
            Assert.assertTrue(bdd.getVersion(), zero.isZero());
            Assert.assertTrue(bdd.getVersion(), one.isUniverse());
            BDDVarSet s0 = zero.support();
            BDDVarSet s1 = one.support();
            Assert.assertTrue(bdd.getVersion(), s0.isEmpty());
            Assert.assertTrue(bdd.getVersion(), s1.isEmpty());
            zero.free(); one.free();
            s0.free(); s1.free();
        }
    }
}

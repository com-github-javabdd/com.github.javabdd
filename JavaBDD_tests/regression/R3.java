// R3.java, created Jul 28, 2004 2:55:30 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package regression;

import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarSet;
import bdd.BDDTestCase;

/**
 * unique() and applyUni() bug
 * 
 * @author John Whaley
 * @version $Id: R3.java 467 2006-11-13 07:25:37Z joewhaley $
 */
public class R3 extends BDDTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(R3.class);
    }
    
    public void testR3() {
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            
            BDDFactory bdd = nextFactory();
            if (bdd.isZDD()) continue;
            BDD x0,x1,y0,y1,z0,z1,t,or,one;
            BDDVarSet xs0,xs1;
            bdd.setVarNum(5);
            x0 = bdd.ithVar(0);
            x1 = bdd.ithVar(1);
            xs0 = x0.toVarSet();
            xs1 = x1.toVarSet();
            one = bdd.one();
            or = x0.or(x1);

            try {
                z0 = or.unique(xs0);
            } catch (UnsupportedOperationException _) {
                System.err.println("Warning: "+bdd.getVersion()+" does not support unique()");
                continue;
            }
            t = x1.not();
            Assert.assertTrue(bdd.getVersion(), z0.equals(t));
            t.free();

            z1 = or.unique(xs1);
            t = x0.not();
            Assert.assertTrue(bdd.getVersion(), z1.equals(t));
            t.free();

            t = one.unique(xs0);
            Assert.assertTrue(bdd.getVersion(), t.isZero());
            t.free();

            y0 = x0.applyUni(x1, BDDFactory.or, xs0);
            t = x1.not();
            Assert.assertTrue(bdd.getVersion(), y0.equals(t));
            t.free();

            y1 = x0.applyUni(x1, BDDFactory.or, xs1);
            t = x0.not();
            //Assert.assertTrue(bdd.getVersion(), y1.equals(t));
            t.free();

            x0.free(); x1.free(); y0.free(); y1.free(); z0.free(); z1.free();
            xs0.free(); xs1.free();
            or.free(); one.free();
            
        }
    }
}

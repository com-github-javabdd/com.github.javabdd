// IteratorTests.java, created Oct 19, 2004 1:16:36 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package bdd;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarSet;
import net.sf.javabdd.TestBDDFactory;

/**
 * IteratorTests
 * 
 * @author jwhaley
 * @version $Id: IteratorTests.java 475 2006-12-05 10:59:01Z joewhaley $
 */
public class IteratorTests extends BDDTestCase {
    static Random random = new Random(1238);
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IteratorTests.class);
    }
    
    public void testOneZeroIterator() {
        reset();
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            BDDFactory bdd = nextFactory();
            int domainSize = 1024;
            BDDDomain[] ds = bdd.extDomain(new int[] { domainSize });
            BDDDomain d = ds[0];
            BDD b = bdd.zero();
            BDDVarSet var = d.set();
            Iterator i = b.iterator(var);
            b.free();
            Assert.assertEquals(bdd.getVersion(), i.hasNext(), false);
            try {
                i.next();
                Assert.fail();
            } catch (NoSuchElementException x) {
            }
            
            b = bdd.universe();
            Iterator i1 = b.iterator(var);
            Iterator i2 = new MyBDDIterator(b, var);
            b.free();
            Set s1 = new HashSet();
            Set s2 = new HashSet();
            while (i1.hasNext()) {
                BDD b1 = (BDD) i1.next();
                double sc = b1.satCount(var);
                Assert.assertEquals(bdd.getVersion(), 1., sc, 0.0000001);
                s1.add(b1);
            }
            while (i2.hasNext()) {
                BDD b2 = (BDD) i2.next();
                double sc = b2.satCount(var); 
                Assert.assertEquals(bdd.getVersion(), 1., sc, 0.0000001);
                s2.add(b2);
            }
            var.free();
            Assert.assertEquals(bdd.getVersion(), s1.size(), domainSize);
            Assert.assertEquals(bdd.getVersion(), s2.size(), domainSize);
            if (!s1.equals(s2)) {
                Set s1_minus_s2 = new HashSet(s1);
                s1_minus_s2.removeAll(s2);
                Set s2_minus_s1 = new HashSet(s2);
                s2_minus_s1.removeAll(s1);
                Assert.fail("iterator() contains these extras: "+s1_minus_s2+"\n"+
                    "iterator2() contains these extras: "+s2_minus_s1);
            }
            for (Iterator k = s1.iterator(); k.hasNext(); ) {
                BDD q = (BDD) k.next();
                q.free();
            }
            for (Iterator k = s2.iterator(); k.hasNext(); ) {
                BDD q = (BDD) k.next();
                q.free();
            }
        }
    }
    
    static BDD randomBDD(BDDFactory f) {
        BDD result = f.zero();
        for (int i = 0; i < f.varNum(); ++i) {
            BDD b = f.universe();
            for (int j = 0; j < f.varNum(); ++j) {
                int k = random.nextInt(3);
                if (k == 0) b.andWith(f.nithVar(j));
                else if (k == 1) b.andWith(f.ithVar(j));
            }
            result.orWith(b);
        }
        return result;
    }

    static BDDVarSet randomBDDVarSet(BDDFactory f) {
        BDDVarSet s = f.emptySet();
        for (int i = 0; i < f.varNum(); ++i) {
            if (random.nextBoolean())
                s.unionWith(i);
        }
        return s;
    }
    
    static BDD betterRandomBDD(BDDFactory f) {
        // Use a random truth table.
        byte[] bytes = new byte[(1 << f.varNum()) / 8 + 1];
        random.nextBytes(bytes);
        BDD result = f.zero();
        for (int i = 0; i < (1 << f.varNum()); ++i) {
            if ((bytes[i / 8] & (1<<(i%8))) != 0) {
                BDD b = f.universe();
                for (int j = 0; j < f.varNum(); ++j) {
                    if ((i & (1<<j)) != 0)
                        b.andWith(f.ithVar(j));
                    else
                        b.andWith(f.nithVar(j));
                }
                result.orWith(b);
            }
        }
        return result;
    }

    public void testAllsatIterator() {
        reset();
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            BDDFactory f = nextFactory();
            f.setVarNum(5);
            for (int kk = 0; kk < 10; ++kk) {
                BDD bdd1 = ((kk&1)==0)?randomBDD(f):betterRandomBDD(f);
                BDD bdd2 = f.zero();
                BDD.AllSatIterator i = bdd1.allsat();
                while (i.hasNext()) {
                    byte[] b = i.nextSat();
                    BDD t = f.universe();
                    for (int k = 0; k < b.length; ++k) {
                        if (b[k] == 0)
                            t.andWith(f.nithVar(k));
                        else if (b[k] == 1)
                            t.andWith(f.ithVar(k));
                    }
                    
                    BDD overlap = bdd2.and(t);
                    Assert.assertTrue(overlap.isZero());
                    overlap.free();
                    
                    bdd2.orWith(t);
                }
                Assert.assertEquals(bdd1, bdd2);
                bdd2.free();
                bdd1.free();
            }
        }
    }
    
    public void testRandomRelprod() {
        System.setProperty("bdd1", "zdd");
        System.setProperty("bdd2", "j");
        BDDFactory bdd = TestBDDFactory.init(10000, 1000);
        bdd.setVarNum(5);
        for (int i = 0; i < 1000; ++i) {
            BDD b = betterRandomBDD(bdd);
            BDD c = betterRandomBDD(bdd);
            BDDVarSet d = randomBDDVarSet(bdd);
            BDD e = b.relprod(c, d);
            
            BDD f = b.and(c);
            BDD g = f.exist(d);
            Assert.assertEquals(g, e);
            
            b.free(); c.free(); d.free(); e.free(); f.free(); g.free();
        }
    }
    
    public void testRandomIterator() {
        reset();
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            BDDFactory bdd = nextFactory();
            bdd.setNodeTableSize(200000);
            int domainSize = 1024;
            BDDDomain[] ds = bdd.extDomain(new int[] { domainSize, domainSize });
            BDDDomain d = ds[0]; d.setName("D0");
            BDDDomain d2 = ds[1]; d2.setName("D1");
            bdd.setVarOrder(bdd.makeVarOrdering(true, "D1xD0"));
            Random r = new Random(667);
            int times = 500;
            int combine = 400;
            boolean dual = true;
            for (int i = 0; i < times; ++i) {
                int count = r.nextInt(combine);
                BDD b = bdd.zero();
                for (int j = 0; j < count; ++j) {
                    int varNum = r.nextInt(domainSize);
                    BDD c = d.ithVar(varNum);
                    if (dual) c.andWith(d2.ithVar(r.nextInt(domainSize)));
                    b.orWith(c);
                }
                BDDVarSet var = d.set();
                if (dual) var.unionWith(d2.set());
                Iterator i1 = b.iterator(var);
                Iterator i2 = new MyBDDIterator(b, var);
                b.free();
                Set s1 = new HashSet();
                Set s2 = new HashSet();
                while (i1.hasNext()) {
                    BDD b1 = (BDD) i1.next();
                    double sc = b1.satCount(var);
                    Assert.assertEquals(bdd.getVersion(), 1., sc, 0.0000001);
                    s1.add(b1);
                }
                while (i2.hasNext()) {
                    BDD b2 = (BDD) i2.next();
                    double sc = b2.satCount(var); 
                    Assert.assertEquals(bdd.getVersion(), 1., sc, 0.0000001);
                    s2.add(b2);
                }
                var.free();
                if (!s1.equals(s2)) {
                    Set s1_minus_s2 = new HashSet(s1);
                    s1_minus_s2.removeAll(s2);
                    Set s2_minus_s1 = new HashSet(s2);
                    s2_minus_s1.removeAll(s1);
                    Assert.fail(bdd.getVersion()+": iterator() contains these extras: "+s1_minus_s2+"\n"+
                        "iterator2() contains these extras: "+s2_minus_s1);
                }
                for (Iterator k = s1.iterator(); k.hasNext(); ) {
                    BDD q = (BDD) k.next();
                    q.free();
                }
                for (Iterator k = s2.iterator(); k.hasNext(); ) {
                    BDD q = (BDD) k.next();
                    q.free();
                }
            }
        }
    }
    
    /**
     * <p>This is another version of iterator() that exists for testing purposes.
     * It is much slower than the other one.</p>
     */
    static class MyBDDIterator implements Iterator {

        BDD orig;
        BDD b = null;
        BDDVarSet myVar;
        BDD last = null;
        
        MyBDDIterator(BDD dis, BDDVarSet var) {
            orig = dis;
            if (!dis.isZero()) {
                b = dis.id();
                myVar = var.id();
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if (last != null) {
                orig.applyWith(last.id(), BDDFactory.diff);
                last = null;
            } else {
                throw new IllegalStateException();
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return b != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if (b == null)
                throw new NoSuchElementException();
            BDD c = b.satOne(myVar, false);
            b.applyWith(c.id(), BDDFactory.diff);
            if (b.isZero()) {
                myVar.free(); myVar = null;
                b.free(); b = null;
            }
            return last = c;
        }
        
    }
    
}

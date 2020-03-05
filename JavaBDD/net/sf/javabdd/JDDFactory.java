// JDDFactory.java, created Aug 1, 2003 7:06:47 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

import java.lang.reflect.Method;

/**
 * JDDFactory
 * 
 * @author John Whaley
 * @version $Id: JDDFactory.java 465 2006-07-26 16:42:44Z joewhaley $
 */
public class JDDFactory extends BDDFactoryIntImpl {

    public static final String REVISION = "$Revision: 465 $";
    
    public String getVersion() {
        return "JDDFactory "+REVISION.substring(11, REVISION.length()-2);
    }
    
    static final int INVALID_BDD = -1;
    
    // Redirection functions.
    
    protected void addref_impl(int v) { bdd.ref(v); }
    protected void delref_impl(int v) { bdd.deref(v); }
    protected int zero_impl() { return bdd.getZero(); }
    protected int one_impl() { return bdd.getOne(); }
    protected int invalid_bdd_impl() { return INVALID_BDD; }
    protected int var_impl(int index) {
        int v = level_impl(index);
        return level2var != null ? level2var[v] : v;
    }
    protected int level_impl(int index) {
        // NOTE: jdd seems to returns the total number of variables when
        //       calling getVar() on a terminal.
        int v = bdd.getVar(index);
        if (index == bdd.getOne() || index == bdd.getZero())
            throw new BDDException();
        if (v == -1)
            throw new BDDException();
        return v;
    }
    protected int low_impl(int v) {
        if (v == bdd.getOne() || v == bdd.getZero())
            throw new BDDException();
        return bdd.getLow(v);
    }
    protected int high_impl(int v) {
        if (v == bdd.getOne() || v == bdd.getZero())
            throw new BDDException();
        return bdd.getHigh(v);
    }
    protected int ithVar_impl(int var) {
        if (var >= bdd.numberOfVariables())
            throw new BDDException();
        return vars[var];
    }
    protected int nithVar_impl(int var) {
        if (var >= bdd.numberOfVariables())
            throw new BDDException();
        return bdd.not(vars[var]);
    }
    protected int makenode_impl(int lev, int lo, int hi) { return bdd.mk(lev, lo, hi); }
    protected int ite_impl(int v1, int v2, int v3) { return bdd.ite(v1, v2, v3); }
    protected int apply_impl(int x, int y, BDDOp opr) {
        int r;
        switch (opr.id) {
            case 0: r = bdd.and(x, y); break;
            case 1: r = bdd.xor(x, y); break;
            case 2: r = bdd.or(x, y); break;
            case 3: r = bdd.nand(x, y); break;
            case 4: r = bdd.nor(x, y); break;
            case 5: r = bdd.imp(x, y); break;
            case 6: r = bdd.biimp(x, y); break;
            case 7: r = bdd.and(x, bdd.not(y)); break; // diff
            default:
                throw new UnsupportedOperationException(); // TODO.
        }
        return r;
    }
    protected int not_impl(int v1) { return bdd.not(v1); }
    protected int applyAll_impl(int v1, int v2, BDDOp opr, int v3) {
        // todo: combine.
        int r = apply_impl(v1, v2, opr);
        bdd.ref(r);
        int r2 = bdd.forall(r, v3);
        bdd.deref(r);
        return r2;
    }
    protected int applyEx_impl(int v1, int v2, BDDOp opr, int v3) {
        if (opr == and)
            return bdd.relProd(v1, v2, v3);
        // todo: combine.
        int r = apply_impl(v1, v2, opr);
        bdd.ref(r);
        int r2 = bdd.exists(r, v3);
        bdd.deref(r);
        return r2;
    }
    protected int applyUni_impl(int v1, int v2, BDDOp opr, int v3) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected int compose_impl(int v1, int v2, int var) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected int constrain_impl(int v1, int v2) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected int restrict_impl(int v1, int v2) { return bdd.restrict(v1, v2); }
    protected int simplify_impl(int v1, int v2) { return bdd.simplify(v1, v2); }
    protected int support_impl(int v) { return bdd.support(v); }
    protected int exist_impl(int v1, int v2) { return bdd.exists(v1, v2); }
    protected int forAll_impl(int v1, int v2) { return bdd.forall(v1, v2); }
    protected int unique_impl(int v1, int v2) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected int fullSatOne_impl(int v) {
        if (v == bdd.getZero())
            return v;
        int[] res = bdd.oneSat(v, null);
        int result = bdd.getOne();
        for (int i = res.length - 1; i >= 0; --i) {
            int u;
            if (res[i] == 1) 
                u = bdd.mk(i, 0, result);
            else
                u = bdd.mk(i, result, 0);
            bdd.ref(u); bdd.deref(result);
            result = u;
        }
        bdd.deref(result);
        return result;
    }
    
    protected int replace_impl(int v, BDDPairing p) { return bdd.replace(v, ((bddPairing) p).pairing); }
    protected int veccompose_impl(int v, BDDPairing p) {
        throw new UnsupportedOperationException(); // todo.
    }
    
    protected int nodeCount_impl(int v) { return bdd.nodeCount(v); }
    protected double pathCount_impl(int v) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected double satCount_impl(int v) { return bdd.satCount(v); }
    protected int satOne_impl(int v) { return bdd.oneSat(v); }
    protected int satOne_impl2(int v1, int v2, boolean pol) {
        if (v1 == bdd.getZero())
            return v1;
        int[] res = bdd.oneSat(v1, null);
        int result = bdd.getOne();
        for (int i = res.length - 1; i >= 0; --i) {
            while (bdd.getVar(v2) < i)
                v2 = bdd.getHigh(v2);
            boolean p;
            if (res[i] == 1) p = true;
            else if (res[i] == 0) p = false;
            else {
                if (bdd.getVar(v2) != i)
                    continue;
                p = pol;
            }
            int u = bdd.mk(i, p?0:result, p?result:0);
            bdd.ref(u); bdd.deref(result);
            result = u;
        }
        bdd.deref(result);
        return result;
    }
    protected int nodeCount_impl2(int[] v) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected int[] varProfile_impl(int v) {
        throw new UnsupportedOperationException(); // todo.
    }
    protected void printTable_impl(int v) {
        throw new UnsupportedOperationException(); // todo.
    }
    
    // More redirection functions.
    
    protected void initialize(int initnodesize, int cs) {
        bdd = new jdd.bdd.BDD(initnodesize, cs);
        vars = new int[256];
    }
    public void addVarBlock(int first, int last, boolean fixed) {
        throw new UnsupportedOperationException();
    }
    public void varBlockAll() {
        throw new UnsupportedOperationException();
    }
    public void clearVarBlocks() {
        throw new UnsupportedOperationException();
    }
    public void printOrder() {
        throw new UnsupportedOperationException();
    }
    public int getNodeTableSize() {
        // todo.
        return bdd.countRootNodes();
    }
    public int setNodeTableSize(int x) {
        // TODO.
        return getNodeTableSize();
    }
    public int setCacheSize(int x) {
        // TODO.
        return 0;
    }
    public boolean isInitialized() { return true; }
    public void done() { super.done(); bdd.cleanup(); bdd = null; }
    public void setError(int code) {
        // todo: implement this
    }
    public void clearError() {
        // todo: implement this
    }
    public int setMaxNodeNum(int size) {
        // todo: implement this
        return 0;
    }
    public double setMinFreeNodes(double x) {
        int old = jdd.util.Configuration.minFreeNodesProcent;
        jdd.util.Configuration.minFreeNodesProcent = (int)(x * 100);
        return (double) old / 100.;
    }
    public int setMaxIncrease(int x) {
        int old = jdd.util.Configuration.maxNodeIncrease;
        jdd.util.Configuration.maxNodeIncrease = x;
        return old;
    }
    public double setIncreaseFactor(double x) {
        // todo: implement this
        return 0;
    }
    public int getNodeNum() {
        // todo.
        return bdd.countRootNodes();
    }
    public int getCacheSize() {
        // TODO Implement this.
        return 0;
    }
    public int reorderGain() {
        throw new UnsupportedOperationException();
    }
    public void printStat() {
        bdd.showStats();
    }
    public double setCacheRatio(double x) {
        // TODO Implement this.
        return 0;
    }
    public int varNum() {
        return bdd.numberOfVariables();
    }
    public int setVarNum(int num) {
        if (num > Integer.MAX_VALUE / 2)
            throw new BDDException();
        int old = bdd.numberOfVariables();
        int oldSize = vars.length;
        int newSize = oldSize;
        while (num > newSize) {
            newSize *= 2;
        }
        if (oldSize != newSize) {
            int[] oldVars = vars;
            vars = new int[newSize];
            System.arraycopy(oldVars, 0, vars, 0, old);
            
            if (level2var != null) {
                int[] oldlevel2var = level2var;
                level2var = new int[newSize];
                System.arraycopy(oldlevel2var, 0, level2var, 0, old);
                
                int[] oldvar2level = var2level;
                var2level = new int[newSize];
                System.arraycopy(oldvar2level, 0, var2level, 0, old);
            }
        }
        while (bdd.numberOfVariables() < num) {
            int k = bdd.numberOfVariables();
            vars[k] = bdd.createVar();
            bdd.ref(vars[k]);
            if (level2var != null) {
                level2var[k] = k;
                var2level[k] = k;
            }
        }
        return old;
    }
    public void printAll() {
        throw new UnsupportedOperationException();
    }
    public void setVarOrder(int[] neworder) {
        // todo: setting var order corrupts all existing BDDs!
        if (var2level != null)
            throw new UnsupportedOperationException();
        
        if (bdd.numberOfVariables() != neworder.length)
            throw new BDDException();
        
        int[] newvars = new int[vars.length];
        var2level = new int[vars.length];
        level2var = new int[vars.length];
        for (int i = 0; i < bdd.numberOfVariables(); ++i) {
            int k = neworder[i];
            //System.out.println("Var "+k+" (node "+vars[k]+") in original order -> var "+i+" (node "+vars[i]+") in new order");
            newvars[k] = vars[i];
            var2level[k] = i;
            level2var[i] = k;
        }
        vars = newvars;
        
        //System.out.println("Number of domains: "+numberOfDomains());
        for (int i = 0; i < numberOfDomains(); ++i) {
            BDDDomain d = getDomain(i);
            d.var = makeSet(d.ivar);
            //System.out.println("Set for domain "+d+": "+d.var.toStringWithDomains());
        }
    }
    public int level2Var(int level) { return level2var != null ? level2var[level] : level; }
    public int var2Level(int var) { return var2level != null ? var2level[var] : var; }
    public ReorderMethod getReorderMethod() {
        throw new UnsupportedOperationException();
    }
    public int getReorderTimes() {
        throw new UnsupportedOperationException();
    }
    public void disableReorder() {
        throw new UnsupportedOperationException();
    }
    public void enableReorder() {
        throw new UnsupportedOperationException();
    }
    public int reorderVerbose(int v) {
        throw new UnsupportedOperationException();
    }
    public void reorder(ReorderMethod m) {
        throw new UnsupportedOperationException();
    }
    public void autoReorder(ReorderMethod method) {
        throw new UnsupportedOperationException();
    }
    public void autoReorder(ReorderMethod method, int max) {
        throw new UnsupportedOperationException();
    }
    public void swapVar(int v1, int v2) {
        throw new UnsupportedOperationException();
    }
    
    private jdd.bdd.BDD bdd;
    private int[] vars; // indexed by EXTERNAL
    private int[] level2var; // internal -> external
    private int[] var2level; // external -> internal
    
    static {
        jdd.util.Options.verbose = true;
    }
    
    private JDDFactory(int nodenum, int cachesize) {
        initialize(nodenum, cachesize);
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#init(int, int)
     */
    public static BDDFactory init(int nodenum, int cachesize) {
        BDDFactory f = new JDDFactory(nodenum, cachesize);
        return f;
    }

    private class bddPairing extends BDDPairing {
        
        private int[] from;
        private int[] to;
        private jdd.bdd.Permutation pairing;
        
        private bddPairing() {
            reset();
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            for (int i = 0; i < from.length; ++i) {
                if (from[i] == vars[oldvar]) {
                    to[i] = vars[newvar];
                    pairing = bdd.createPermutation(from, to);
                    return;
                }
            }
            int[] oldfrom = from;
            from = new int[from.length + 1];
            System.arraycopy(oldfrom, 0, from, 0, oldfrom.length);
            from[oldfrom.length] = vars[oldvar];
            int[] oldto = to;
            to = new int[to.length + 1];
            System.arraycopy(oldto, 0, to, 0, oldto.length);
            to[oldto.length] = vars[newvar];
            pairing = bdd.createPermutation(from, to);
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, net.sf.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            throw new UnsupportedOperationException();
        }
        
        public void set(int[] oldvar, int[] newvar) {
            int[] oldfrom = from;
            from = new int[from.length + oldvar.length];
            System.arraycopy(oldfrom, 0, from, 0, oldfrom.length);
            for (int i = 0; i < oldvar.length; ++i) {
                from[i + oldfrom.length] = vars[oldvar[i]];
            }
            int[] oldto = to;
            to = new int[to.length + newvar.length];
            System.arraycopy(oldto, 0, to, 0, oldto.length);
            for (int i = 0; i < newvar.length; ++i) {
                to[i + oldto.length] = vars[newvar[i]];
            }
            //debug();
            pairing = bdd.createPermutation(from, to);
        }
        
        void debug() {
            for (int i = 0; i < from.length; ++i) {
                System.out.println(bdd.getVar(from[i])+" -> "+bdd.getVar(to[i]));
            }
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#reset()
         */
        public void reset() {
            from = to = new int[] { };
            pairing = null;
        }
        
    }
    
    public BDDPairing makePair() {
        return new bddPairing();
    }

    public void registerGCCallback(Object o, Method m) {
        throw new UnsupportedOperationException();
    }
    public void unregisterGCCallback(Object o, Method m) {
        throw new UnsupportedOperationException();
    }
    public void registerReorderCallback(Object o, Method m) {
        throw new UnsupportedOperationException();
    }
    public void unregisterReorderCallback(Object o, Method m) {
        throw new UnsupportedOperationException();
    }
    public void registerResizeCallback(Object o, Method m) {
        throw new UnsupportedOperationException();
    }
    public void unregisterResizeCallback(Object o, Method m) {
        throw new UnsupportedOperationException();
    }
}

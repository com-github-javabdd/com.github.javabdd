// MicroFactory.java, created Jan 29, 2005 8:24:17 PM by joewhaley
// Copyright (C) 2005 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package com.github.javabdd;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * <p>BDD factory where each node only takes 16 bytes.
 * This is accomplished by tightly packing the bits and limiting the
 * maximum number of BDD variables to 2^10 = 1024.</p>
 * 
 * <p>Because of the extra computation required for packing/unpacking,
 * this BDD factory is a little slower than JFactory, but it also uses
 * 20% less memory.</p>
 * 
 * @author jwhaley
 * @version $Id: MicroFactory.java 465 2006-07-26 16:42:44Z joewhaley $
 */
public class MicroFactory extends BDDFactoryIntImpl {

    // 16 bytes per BDD node
    //
    // Max BDD nodes: 2^27 = 134217728
    // Max variables: 2^10 = 1024
    // Max refcount: 2^9 = 512
    // 
    // 27(high)+27(low)+9(refcount)+1(mark)+27(hash)+27(next)+10(var) = 128 bits
    // 
    
    public static boolean FLUSH_CACHE_ON_GC = true;
    
    static final boolean VERIFY_ASSERTIONS = false;
    static final boolean ORDER_CACHE = false;
    static final int CACHESTATS = 0;
    static final boolean SWAPCOUNT = false;
    
    public static final String REVISION = "$Revision: 465 $";
    
    public String getVersion() {
        return "MicroFactory "+REVISION.substring(11, REVISION.length()-2);
    }
    
    private MicroFactory() { }
    
    /* (non-Javadoc)
     * @see com.github.javabdd.BDDFactory#init(int, int)
     */
    public static BDDFactory init(int nodenum, int cachesize) {
        BDDFactory f = new MicroFactory();
        f.initialize(nodenum, cachesize);
        if (CACHESTATS > 0) addShutdownHook(f);
        return f;
    }

    static void addShutdownHook(final BDDFactory f) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println(f.getCacheStats().toString());
            }
        });
    }
    
    /**
     * Implementation of BDDPairing used by JFactory.
     */
    class bddPair extends BDDPairing {
        int[] result;
        int last;
        int id;
        bddPair next;

        /* (non-Javadoc)
         * @see com.github.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            bdd_setpair(this, oldvar, newvar);
        }
        /* (non-Javadoc)
         * @see com.github.javabdd.BDDPairing#set(int, com.github.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            bdd_setbddpair(this, oldvar, unwrap(newvar));
        }
        /* (non-Javadoc)
         * @see com.github.javabdd.BDDPairing#reset()
         */
        public void reset() {
            bdd_resetpair(this);
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append('{');
            boolean any = false;
            for (int i = 0; i < result.length; ++i) {
                if (result[i] != bdd_ithvar(bddlevel2var[i])) {
                    if (any) sb.append(", ");
                    any = true;
                    sb.append(bddlevel2var[i]);
                    sb.append('=');
                    BDD b = makeBDD(result[i]);
                    sb.append(b);
                    b.free();
                }
            }
            sb.append('}');
            return sb.toString();
        }
    }
    
    /* (non-Javadoc)
     * @see com.github.javabdd.BDDFactory#makePair()
     */
    public BDDPairing makePair() {
        bddPair p = new bddPair();
        p.result = new int[bddvarnum];
        int n;
        for (n = 0; n < bddvarnum; n++)
            p.result[n] = bdd_ithvar(bddlevel2var[n]);

        p.id = update_pairsid();
        p.last = -1;

        bdd_register_pair(p);
        return p;
    }

    // Redirection functions.
    
    protected void addref_impl(int v) { bdd_addref(v); }
    protected void delref_impl(int v) { bdd_delref(v); }
    protected int zero_impl() { return BDDZERO; }
    protected int one_impl() { return BDDONE; }
    protected int invalid_bdd_impl() { return INVALID_BDD; }
    protected int var_impl(int v) { return bdd_var(v); }
    protected int level_impl(int v) { return LEVEL(v); }
    protected int low_impl(int v) { return bdd_low(v); }
    protected int high_impl(int v) { return bdd_high(v); }
    protected int ithVar_impl(int var) { return bdd_ithvar(var); }
    protected int nithVar_impl(int var) { return bdd_nithvar(var); }
    
    protected int makenode_impl(int lev, int lo, int hi) { return bdd_makenode(lev, lo, hi); }
    protected int ite_impl(int v1, int v2, int v3) { return bdd_ite(v1, v2, v3); }
    protected int apply_impl(int v1, int v2, BDDOp opr) { return bdd_apply(v1, v2, opr.id); }
    protected int not_impl(int v1) { return bdd_not(v1); }
    protected int applyAll_impl(int v1, int v2, BDDOp opr, int v3) { return bdd_appall(v1, v2, opr.id, v3); }
    protected int applyEx_impl(int v1, int v2, BDDOp opr, int v3) { return bdd_appex(v1, v2, opr.id, v3); }
    protected int applyUni_impl(int v1, int v2, BDDOp opr, int v3) { return bdd_appuni(v1, v2, opr.id, v3); }
    protected int compose_impl(int v1, int v2, int var) { return bdd_compose(v1, v2, var); }
    protected int constrain_impl(int v1, int v2) { return bdd_constrain(v1, v2); }
    protected int restrict_impl(int v1, int v2) { return bdd_restrict(v1, v2); }
    protected int simplify_impl(int v1, int v2) { return bdd_simplify(v1, v2); }
    protected int support_impl(int v) { return bdd_support(v); }
    protected int exist_impl(int v1, int v2) { return bdd_exist(v1, v2); }
    protected int forAll_impl(int v1, int v2) { return bdd_forall(v1, v2); }
    protected int unique_impl(int v1, int v2) { return bdd_unique(v1, v2); }
    protected int fullSatOne_impl(int v) { return bdd_fullsatone(v); }
    
    protected int replace_impl(int v, BDDPairing p) { return bdd_replace(v, (bddPair)p); }
    protected int veccompose_impl(int v, BDDPairing p) { return bdd_veccompose(v, (bddPair)p); }
    
    protected int nodeCount_impl(int v) { return bdd_nodecount(v); }
    protected double pathCount_impl(int v) { return bdd_pathcount(v); }
    protected double satCount_impl(int v) { return bdd_satcount(v); }
    protected int satOne_impl(int v) { return bdd_satone(v); }
    protected int satOne_impl2(int v1, int v2, boolean pol) { return bdd_satoneset(v1, v2, pol); }
    protected int nodeCount_impl2(int[] v) { return bdd_anodecount(v); }
    protected int[] varProfile_impl(int v) { return bdd_varprofile(v); }
    protected void printTable_impl(int v) { bdd_fprinttable(System.out, v); }
    
    // More redirection functions.
    
    protected void initialize(int initnodesize, int cs) { bdd_init(initnodesize, cs); }
    public void addVarBlock(int first, int last, boolean fixed) { bdd_intaddvarblock(first, last, fixed); }
    public void varBlockAll() { bdd_varblockall(); }
    public void clearVarBlocks() { bdd_clrvarblocks(); }
    public void printOrder() { bdd_fprintorder(System.out); }
    public int getNodeTableSize() { return bdd_getallocnum(); }
    public int setNodeTableSize(int size) { return bdd_setallocnum(size); }
    public int setCacheSize(int v) { return bdd_setcachesize(v); }
    public boolean isInitialized() { return bddrunning; }
    public void done() { super.done(); bdd_done(); }
    public void setError(int code) { bdderrorcond = code; }
    public void clearError() { bdderrorcond = 0; }
    public int setMaxNodeNum(int size) { return bdd_setmaxnodenum(size); }
    public double setMinFreeNodes(double x) { return bdd_setminfreenodes((int)(x * 100.)) / 100.; }
    public int setMaxIncrease(int x) { return bdd_setmaxincrease(x); }
    public double setIncreaseFactor(double x) { return bdd_setincreasefactor(x); }
    public int getNodeNum() { return bdd_getnodenum(); }
    public int getCacheSize() { return cachesize; }
    public int reorderGain() { return bdd_reorder_gain(); }
    public void printStat() { bdd_fprintstat(System.out); }
    public double setCacheRatio(double x) { return bdd_setcacheratio((int)(x * 100)) / 100.; }
    public int varNum() { return bdd_varnum(); }
    public int setVarNum(int num) { return bdd_setvarnum(num); }
    public void printAll() { bdd_fprintall(System.out); }
    public BDD load(BufferedReader in, int[] translate) throws IOException { return makeBDD(bdd_load(in, translate)); }
    public void save(BufferedWriter out, BDD b) throws IOException { bdd_save(out, unwrap(b)); }
    public void setVarOrder(int[] neworder) { bdd_setvarorder(neworder); }
    public int level2Var(int level) { return bddlevel2var[level]; }
    public int var2Level(int var) { return bddvar2level[var]; }
    public int getReorderTimes() { return bddreordertimes; }
    public void disableReorder() { bdd_disable_reorder(); }
    public void enableReorder() { bdd_enable_reorder(); }
    public int reorderVerbose(int v) { return bdd_reorder_verbose(v); }
    public void reorder(ReorderMethod m) { bdd_reorder(m.id); }
    public void autoReorder(ReorderMethod method) { bdd_autoreorder(method.id); }
    public void autoReorder(ReorderMethod method, int max) { bdd_autoreorder_times(method.id, max); }
    public void swapVar(int v1, int v2) { bdd_swapvar(v1, v2); }
    
    public ReorderMethod getReorderMethod() {
        switch (bddreordermethod) {
            case BDD_REORDER_NONE :
                return REORDER_NONE;
            case BDD_REORDER_WIN2 :
                return REORDER_WIN2;
            case BDD_REORDER_WIN2ITE :
                return REORDER_WIN2ITE;
            case BDD_REORDER_WIN3 :
                return REORDER_WIN3;
            case BDD_REORDER_WIN3ITE :
                return REORDER_WIN3ITE;
            case BDD_REORDER_SIFT :
                return REORDER_SIFT;
            case BDD_REORDER_SIFTITE :
                return REORDER_SIFTITE;
            case BDD_REORDER_RANDOM :
                return REORDER_RANDOM;
            default :
                throw new BDDException();
        }
    }

    // Experimental functions.
    
    public void validateAll() { bdd_validate_all(); }
    public void validateBDD(BDD b) { bdd_validate(unwrap(b)); }
    
    public MicroFactory cloneFactory() {
        MicroFactory INSTANCE = new MicroFactory();
        if (applycache != null)
            INSTANCE.applycache = this.applycache.copy();
        if (itecache != null)
            INSTANCE.itecache = this.itecache.copy();
        if (quantcache != null)
            INSTANCE.quantcache = this.quantcache.copy();
        if (appexcache != null)
            INSTANCE.appexcache = this.appexcache.copy();
        if (appex3cache != null)
            INSTANCE.appex3cache = this.appex3cache.copy();
        if (replacecache != null)
            INSTANCE.replacecache = this.replacecache.copy();
        if (misccache != null)
            INSTANCE.misccache = this.misccache.copy();
        if (countcache != null)
            INSTANCE.countcache = this.countcache.copy();
        // TODO: potential difference here (!)
        INSTANCE.rng = new Random();
        INSTANCE.verbose = this.verbose;
        INSTANCE.cachestats.copyFrom(this.cachestats);
        
        INSTANCE.bddrunning = this.bddrunning;
        INSTANCE.bdderrorcond = this.bdderrorcond;
        INSTANCE.bddnodesize = this.bddnodesize;
        INSTANCE.bddmaxnodesize = this.bddmaxnodesize;
        INSTANCE.bddmaxnodeincrease = this.bddmaxnodeincrease;
        INSTANCE.bddfreepos = this.bddfreepos;
        INSTANCE.bddfreenum = this.bddfreenum;
        INSTANCE.bddproduced = this.bddproduced;
        INSTANCE.bddvarnum = this.bddvarnum;

        INSTANCE.gbcollectnum = this.gbcollectnum;
        INSTANCE.cachesize = this.cachesize;
        INSTANCE.gbcclock = this.gbcclock;
        INSTANCE.usednodes_nextreorder = this.usednodes_nextreorder;
        
        INSTANCE.bddrefstacktop = this.bddrefstacktop;
        INSTANCE.bddresized = this.bddresized;
        INSTANCE.minfreenodes = this.minfreenodes;
        INSTANCE.bddnodes = new int[this.bddnodes.length];
        System.arraycopy(this.bddnodes, 0, INSTANCE.bddnodes, 0, this.bddnodes.length);
        INSTANCE.bddrefstack = new int[this.bddrefstack.length];
        System.arraycopy(this.bddrefstack, 0, INSTANCE.bddrefstack, 0, this.bddrefstack.length);
        INSTANCE.bddvar2level = new int[this.bddvar2level.length];
        System.arraycopy(this.bddvar2level, 0, INSTANCE.bddvar2level, 0, this.bddvar2level.length);
        INSTANCE.bddlevel2var = new int[this.bddlevel2var.length];
        System.arraycopy(this.bddlevel2var, 0, INSTANCE.bddlevel2var, 0, this.bddlevel2var.length);
        INSTANCE.bddvarset = new int[this.bddvarset.length];
        System.arraycopy(this.bddvarset, 0, INSTANCE.bddvarset, 0, this.bddvarset.length);
        
        INSTANCE.domain = new BDDDomain[this.domain.length];
        for (int i = 0; i < INSTANCE.domain.length; ++i) {
            INSTANCE.domain[i] = INSTANCE.createDomain(i, this.domain[i].realsize);
        }
        return INSTANCE;
    }
    
    /**
     * Use this function to translate BDD's from a JavaFactory into its clone.
     * This will only work immediately after cloneFactory() is called, and
     * before any other BDD operations are performed. 
     * 
     * @param that BDD in old factory
     * @return a BDD in the new factory
     */
    public BDD copyNode(BDD that) {
        return makeBDD(unwrap(that));
    }
    
    
    /***** IMPLEMENTATION BELOW *****/
    
    private int[] bddnodes;
    
    static final int __node_size = 4;
    static final int offset__lref = 0;
    static final int offset__href = 1;
    static final int offset__low = 2;
    static final int offset__high = 3;
    static final int offset__hash = 0;
    static final int offset__next = 1;
    static final int offset__llev = 2;
    static final int offset__hlev = 3;
    static final int offset__mark = 1;
    
    static final int NODE_MASK  = 0x07FFFFFF;
    static final int LEV_LMASK  = 0xF8000000;
    static final int LEV_HMASK  = 0xF8000000;
    
    static final int REF_LMASK  = 0xF8000000;
    static final int REF_HMASK  = 0xF0000000;
    static final int REF_LINC   = 0x08000000;
    static final int REF_HINC   = 0x10000000;
    static final int MARK_MASK  = 0x08000000;
    static final int HASH_MASK  = 0x07FFFFFF;
    static final int NEXT_MASK  = 0x07FFFFFF;
    
    static final int NODE_BITS = 27;
    static final int LEV_LPOS  = 27;
    static final int LEV_LBITS = 5;
    static final int LEV_HPOS  = 27;
    static final int LEV_HBITS = 5;
    static final int REF_LPOS  = 27;
    static final int REF_LBITS = 5;
    static final int REF_HPOS  = 28;
    static final int REF_HBITS = 4;
    
    /*
    static final int NODE_MASK  = 0x00FFFFFF;
    static final int LEV_LMASK  = 0xFF000000;
    static final int LEV_HMASK  = 0x00000000;
    
    static final int REF_LMASK  = 0xFE000000;
    static final int REF_HMASK  = 0x00000000;
    static final int REF_LINC   = 0x02000000;
    static final int REF_HINC   = 0x00000000;
    static final int MARK_MASK  = 0x01000000;
    static final int HASH_MASK  = 0x00FFFFFF;
    static final int NEXT_MASK  = 0x00FFFFFF;
    
    static final int NODE_BITS = 24;
    static final int LEV_LPOS  = 24;
    static final int LEV_LBITS = 8;
    static final int LEV_HPOS  = 32;
    static final int LEV_HBITS = 0;
    static final int REF_LPOS  = 24;
    static final int REF_LBITS = 8;
    static final int REF_HPOS  = 32;
    static final int REF_HBITS = 0;
    */
    
    static final int INVALID_BDD = NODE_MASK;
    static final int MAXVAR = (1 << (LEV_LBITS + LEV_HBITS)) - 1;
    static final int MAX_PAIRSID = MAXVAR;
    
    private final boolean HASREF(int node) {
        int a = bddnodes[node*__node_size + offset__lref] & REF_LMASK;
        if (a != 0) return true;
        if (REF_HMASK == 0) return false;
        int b = bddnodes[node*__node_size + offset__href] & REF_HMASK;
        return (b != 0);
    }

    private final void SETMAXREF(int node) {
        bddnodes[node*__node_size + offset__lref] |= REF_LMASK;
        if (REF_HMASK != 0)
            bddnodes[node*__node_size + offset__href] |= REF_HMASK;
    }

    private final void CLEARREF(int node) {
        bddnodes[node*__node_size + offset__lref] &= ~REF_LMASK;
        if (REF_HMASK != 0)
            bddnodes[node*__node_size + offset__href] &= ~REF_HMASK;
    }

    private final void INCREF(int node) {
        int a = bddnodes[node*__node_size + offset__lref] & REF_LMASK;
        if (a == REF_LMASK) {
            if (REF_HMASK == 0) return;
            int b = bddnodes[node*__node_size + offset__href] & REF_HMASK;
            if (b == REF_HMASK) return;
            bddnodes[node*__node_size + offset__href] += REF_HINC;
        }
        bddnodes[node*__node_size + offset__lref] += REF_LINC;
    }

    private final void DECREF(int node) {
        int a = bddnodes[node*__node_size + offset__lref] & REF_LMASK;
        if (a != REF_LMASK ||
            (REF_HMASK != 0 &&
                (bddnodes[node*__node_size + offset__href] & REF_HMASK) != REF_HMASK)) {
            if (REF_HMASK != 0 && a == 0) bddnodes[node*__node_size + offset__href] -= REF_HINC;
            bddnodes[node*__node_size + offset__lref] -= REF_LINC;
        }
    }

    private final int GETREF(int node) {
        int a = bddnodes[node*__node_size + offset__lref] & REF_LMASK;
        if (REF_HMASK == 0) return a >>> REF_LPOS;
        int b = bddnodes[node*__node_size + offset__href] & REF_HMASK;
        return a >>> REF_LPOS | b >>> (REF_HPOS-REF_LBITS);
    }

    private final int LEVEL(int node) {
        int a = bddnodes[node*__node_size + offset__llev] & LEV_LMASK;
        if (LEV_HMASK == 0) return a >>> LEV_LPOS;
        int b = bddnodes[node*__node_size + offset__hlev] & LEV_HMASK;
        int lev = a >>> LEV_LPOS | b >>> (LEV_HPOS-LEV_LBITS);
        return lev;
    }

    private final void SETLEVEL(int node, int val) {
        if (VERIFY_ASSERTIONS) _assert(val >= 0 && val < (1 << (LEV_LBITS + LEV_HBITS)));
        int a = bddnodes[node*__node_size + offset__llev] & ~LEV_LMASK;
        a |= (val << LEV_LPOS);
        bddnodes[node*__node_size + offset__llev] = a;
        if (LEV_HMASK == 0) return;
        int b = bddnodes[node*__node_size + offset__hlev] & ~LEV_HMASK;
        b |= (val << (LEV_HPOS-LEV_LBITS)) & LEV_HMASK;
        bddnodes[node*__node_size + offset__hlev] = b;
    }

    private final void SETMARK(int n) {
        bddnodes[n*__node_size + offset__mark] |= MARK_MASK;
    }
    
    private final void UNMARK(int n) {
        bddnodes[n*__node_size + offset__mark] &= ~MARK_MASK;
    }
    
    private final boolean MARKED(int n) {
        return (bddnodes[n*__node_size + offset__mark] & MARK_MASK) != 0;
    }

    private final int LOW(int r) {
        return bddnodes[r*__node_size + offset__low] & NODE_MASK;
    }

    private final void SETLOW(int r, int v) {
        if (VERIFY_ASSERTIONS) _assert(v >= 0 && v <= NODE_MASK);
        int a = bddnodes[r*__node_size + offset__low] & ~NODE_MASK;
        a |= v;
        bddnodes[r*__node_size + offset__low] = a;
    }
    
    private final int HIGH(int r) {
        return bddnodes[r*__node_size + offset__high] & NODE_MASK;
    }

    private final void SETHIGH(int r, int v) {
        if (VERIFY_ASSERTIONS) _assert(v >= 0 && v <= NODE_MASK);
        int a = bddnodes[r*__node_size + offset__high] & ~NODE_MASK;
        a |= v;
        bddnodes[r*__node_size + offset__high] = a;
    }
    
    private final int HASH(int r) {
        return bddnodes[r*__node_size + offset__hash] & HASH_MASK;
    }
    
    private final void SETHASH(int r, int v) {
        if (VERIFY_ASSERTIONS) _assert(v >= 0 && v <= HASH_MASK);
        int a = bddnodes[r*__node_size + offset__hash] & ~HASH_MASK;
        a |= v;
        bddnodes[r*__node_size + offset__hash] = a;
    }
    
    private final int NEXT(int r) {
        return bddnodes[r*__node_size + offset__next] & NEXT_MASK;
    }
    
    private final void SETNEXT(int r, int v) {
        if (VERIFY_ASSERTIONS) _assert(v >= 0 && v <= NEXT_MASK);
        int a = bddnodes[r*__node_size + offset__next] & ~NEXT_MASK;
        a |= v;
        bddnodes[r*__node_size + offset__next] = a;
    }
    
    private final void INIT_NODE(int r, int lev, int lo, int hi, int next) {
        bddnodes[r*__node_size + offset__next] = next;
        bddnodes[r*__node_size + offset__low] = lo | (lev << LEV_LPOS);
        int k;
        if (LEV_HMASK == 0) k = hi;
        else k = hi | ((lev << (LEV_HPOS-LEV_LBITS)) & LEV_HMASK);
        bddnodes[r*__node_size + offset__high] = k;
    }
    
    private final int VARr(int r) {
        return LEVEL(r);
    }
    private final void SETVARr(int n, int val) {
        SETLEVEL(n, val);
    }
    private final int LEVELANDMARK(int r) {
        return LEVEL(r);
    }
    private final void SETLEVELANDMARK(int n, int val) {
        SETLEVEL(n, val);
    }
    
    private static final void _assert(boolean b) {
        if (!b)
            throw new InternalError();
    }
    
    private class OpCache {
        int cacheHit;
        int cacheMiss;
        int compulsoryMiss;
        Set cache;
        
        OpCache() {
            if (CACHESTATS > 1) cache = new HashSet();
        }
        
        public String toString() {
            return "\tHit: "+cacheHit+"\tMiss: "+cacheMiss
                +" ("+compulsoryMiss+" compulsory)"
                +"\t("+((float)cacheHit/((float) cacheHit+cacheMiss))*100+"%)";
        }
        
        void checkCompulsory(int a) {
            if (!cache.contains(new Integer(a)))
                ++compulsoryMiss;
        }
        void checkCompulsory(int a, int b) {
            if (!cache.contains(new PairOfInts(a, b)))
                ++compulsoryMiss;
        }
        void checkCompulsory(int a, int b, int c) {
            if (!cache.contains(new TripleOfInts(a, b, c)))
                ++compulsoryMiss;
        }
        void checkCompulsory(int a, int b, int c, int d) {
            if (!cache.contains(new QuadOfInts(a, b, c, d)))
                ++compulsoryMiss;
        }
        void addCompulsory(int a) {
            cache.add(new Integer(a));
        }
        void addCompulsory(int a, int b) {
            cache.add(new PairOfInts(a, b));
        }
        void addCompulsory(int a, int b, int c) {
            cache.add(new TripleOfInts(a, b, c));
        }
        void addCompulsory(int a, int b, int c, int d) {
            cache.add(new QuadOfInts(a, b, c, d));
        }
        void removeCompulsory(int a) {
            cache.remove(new Integer(a));
        }
        void removeCompulsory(int a, int b) {
            cache.remove(new PairOfInts(a, b));
        }
        void removeCompulsory(int a, int b, int c) {
            cache.remove(new TripleOfInts(a, b, c));
        }
        void removeCompulsory(int a, int b, int c, int d) {
            cache.remove(new QuadOfInts(a, b, c, d));
        }
        void removeAll(int n) {
            for (Iterator i = cache.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof Integer) {
                    Integer a = (Integer) o;
                    if (n == a.intValue()) i.remove();
                } else if (o instanceof PairOfInts) {
                    PairOfInts a = (PairOfInts) o;
                    if (n == a.a || n == a.b) i.remove();
                } else if (o instanceof TripleOfInts) {
                    TripleOfInts a = (TripleOfInts) o;
                    if (n == a.a || n == a.b || n == a.c) i.remove();
                } else if (o instanceof QuadOfInts) {
                    QuadOfInts a = (QuadOfInts) o;
                    if (n == a.a || n == a.b || n == a.c || n == a.d) i.remove();
                }
            }
        }
    }
    
    public static class PairOfInts {
        int a, b;
        public PairOfInts(int x, int y) { a = x; b = y; }
        public boolean equals(PairOfInts o) {
            return a == o.a && b == o.b;
        }
        public boolean equals(Object o) {
            if (o instanceof PairOfInts)
                return equals((PairOfInts) o);
            return false;
        }
        public int hashCode() { return a ^ b; }
    }
    
    public static class TripleOfInts {
        int a, b, c;
        public TripleOfInts(int x, int y, int z) { a = x; b = y; c = z; }
        public boolean equals(TripleOfInts o) {
            return a == o.a && b == o.b && c == o.c;
        }
        public boolean equals(Object o) {
            if (o instanceof TripleOfInts)
                return equals((TripleOfInts) o);
            return false;
        }
        public int hashCode() { return a ^ b ^ c; }
    }
    
    public static class QuadOfInts {
        int a, b, c, d;
        public QuadOfInts(int x, int y, int z, int q) { a = x; b = y; c = z; d = q; }
        public boolean equals(QuadOfInts o) {
            return a == o.a && b == o.b && c == o.c && d == o.d;
        }
        public boolean equals(Object o) {
            if (o instanceof QuadOfInts)
                return equals((QuadOfInts) o);
            return false;
        }
        public int hashCode() { return a ^ b ^ c ^ d; }
    }
    
    private class OpCache1 extends OpCache {
        OpCache1Entry table[];
        
        OpCache1(int size) { alloc(size); }
        final void alloc(int size) {
            table = new OpCache1Entry[size];
            for (int i = 0; i < table.length; ++i) {
                table[i] = new OpCache1Entry();
            }
        }
        final OpCache1Entry lookup(int hash) {
            return (OpCache1Entry) table[Math.abs(hash % table.length)];
        }
        final void reset() {
            for (int i = 0; i < table.length; ++i) {
                table[i].a = -1;
            }
            if (CACHESTATS > 1) cache.clear();
        }
        final void clean() {
            for (int i = 0; i < table.length; ++i) {
                int a = table[i].a;
                if (a == -1) continue;
                if (LOW(a & NODE_MASK) == INVALID_BDD ||
                    LOW(table[i].res) == INVALID_BDD) {
                    if (CACHESTATS > 1) removeCompulsory(table[i].a);
                    table[i].a = -1;
                }
            }
        }
        final OpCache1 copy() {
            OpCache1 that = new OpCache1(this.table.length);
            for (int i = 0; i < this.table.length; ++i) {
                that.table[i].a = this.table[i].a;
                that.table[i].res = this.table[i].res;
            }
            if (CACHESTATS > 0) {
                that.cacheHit = this.cacheHit;
                that.cacheMiss = this.cacheMiss;
                if (CACHESTATS > 1)
                    that.cache.addAll(this.cache);
            }
            return that;
        }
        final int get_sid(OpCache1Entry e, int node, int id) {
            if (VERIFY_ASSERTIONS) {
                _assert(node == (node & NODE_MASK));
                _assert(id == (id & ~NODE_MASK));
            }
            int k = node | id;
            if (e.a != k) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(k);
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        final void set_sid(OpCache1Entry e, int node, int id, int r) {
            if (VERIFY_ASSERTIONS) {
                _assert(node == (node & NODE_MASK));
                _assert(id == (id & ~NODE_MASK));
            }
            e.a = node | id;
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a);
        }
        
    }

    private static class OpCache1Entry {
        int a;
        int res;
    }
    
    private class OpCache2 extends OpCache {
        OpCache2Entry table[];
        
        OpCache2(int size) { alloc(size); }
        final void alloc(int size) {
            table = new OpCache2Entry[size];
            for (int i = 0; i < table.length; ++i) {
                table[i] = new OpCache2Entry();
            }
        }
        final OpCache2Entry lookup(int hash) {
            return (OpCache2Entry) table[Math.abs(hash % table.length)];
        }
        final void reset() {
            for (int i = 0; i < table.length; ++i) {
                table[i].a = -1;
            }
            if (CACHESTATS > 1) cache.clear();
        }
        final void clean() {
            for (int i = 0; i < table.length; ++i) {
                int a = table[i].a;
                if (a == -1) continue;
                if (LOW(a & NODE_MASK) == INVALID_BDD ||
                    LOW(table[i].b) == INVALID_BDD ||
                    LOW(table[i].res) == INVALID_BDD) {
                    if (CACHESTATS > 1) removeCompulsory(table[i].a, table[i].b);
                    table[i].a = -1;
                }
            }
        }
        final OpCache2 copy() {
            OpCache2 that = new OpCache2(this.table.length);
            for (int i = 0; i < this.table.length; ++i) {
                that.table[i].a = this.table[i].a;
                that.table[i].b = this.table[i].b;
                that.table[i].res = this.table[i].res;
            }
            if (CACHESTATS > 0) {
                that.cacheHit = this.cacheHit;
                that.cacheMiss = this.cacheMiss;
                if (CACHESTATS > 1)
                    that.cache.addAll(this.cache);
            }
            return that;
        }
        
        final int get_id(OpCache2Entry e, int node1, int node2, int id) {
            if (VERIFY_ASSERTIONS) {
                _assert(node1 == (node1 & NODE_MASK));
                _assert(node2 == (node2 & NODE_MASK));
                _assert(id >= 0 && id < (1 << (LEV_LBITS + LEV_HBITS)));
            }
            int k1 = node1 | (id << LEV_LPOS);
            if (e.a != k1 ||
                e.b != (node2 | ((id << (LEV_HPOS-LEV_LBITS)) & LEV_HMASK))) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(k1, node2 | ((id << (LEV_HPOS-LEV_LBITS)) & LEV_HMASK));
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        
        final int get_sid(OpCache2Entry e, int node1, int node2, int id) {
            if (VERIFY_ASSERTIONS) {
                _assert(node1 == (node1 & NODE_MASK));
                _assert(node2 == (node2 & NODE_MASK));
                _assert(id == (id & ~NODE_MASK));
            }
            int k = node1 | id;
            if (e.a != k || e.b != node2) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(k, node2);
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        
        final int get(OpCache2Entry e, int node1, int node2) {
            if (VERIFY_ASSERTIONS) {
                _assert(node1 == (node1 & NODE_MASK));
                _assert(node2 == (node2 & NODE_MASK));
            }
            if (e.a != node1 || e.b != node2) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(node1, node2);
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        
        final void set_id(OpCache2Entry e, int node1, int node2, int id, int r) {
            if (VERIFY_ASSERTIONS) {
                _assert(node1 == (node1 & NODE_MASK));
                _assert(node2 == (node2 & NODE_MASK));
                _assert(id >= 0 && id < (1 << (LEV_LBITS + LEV_HBITS)));
            }
            e.a = node1 | (id << LEV_LPOS);
            e.b = node2 | ((id << (LEV_HPOS-LEV_LBITS)) & LEV_HMASK);
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a, e.b);
        }
        
        final void set_sid(OpCache2Entry e, int node1, int node2, int id, int r) {
            if (VERIFY_ASSERTIONS) {
                _assert(node1 == (node1 & NODE_MASK));
                _assert(node2 == (node2 & NODE_MASK));
                _assert(id == (id & ~NODE_MASK));
            }
            e.a = node1 | id;
            e.b = node2;
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a, e.b);
        }
        
        final void set(OpCache2Entry e, int node1, int node2, int r) {
            e.a = node1;
            e.b = node2;
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a, e.b);
        }
    }

    private static class OpCache2Entry {
        int a, b;
        int res;
    }
    
    private class OpCache3 extends OpCache {
        OpCache3Entry table[];
        
        OpCache3(int size) { alloc(size); }
        final void alloc(int size) {
            table = new OpCache3Entry[size];
            for (int i = 0; i < table.length; ++i) {
                table[i] = new OpCache3Entry();
            }
        }
        final OpCache3Entry lookup(int hash) {
            return (OpCache3Entry) table[Math.abs(hash % table.length)];
        }
        final void reset() {
            for (int i = 0; i < table.length; ++i) {
                table[i].a = -1;
            }
            if (CACHESTATS > 1) cache.clear();
        }
        final void clean() {
            for (int i = 0; i < table.length; ++i) {
                int a = table[i].a;
                if (a == -1) continue;
                if (LOW(a & NODE_MASK) == INVALID_BDD ||
                    LOW(table[i].b) == INVALID_BDD ||
                    LOW(table[i].c) == INVALID_BDD ||
                    LOW(table[i].res) == INVALID_BDD) {
                    if (CACHESTATS > 1) removeCompulsory(table[i].a, table[i].b, table[i].c);
                    table[i].a = -1;
                }
            }
        }
        final OpCache3 copy() {
            OpCache3 that = new OpCache3(this.table.length);
            for (int i = 0; i < this.table.length; ++i) {
                that.table[i].a = this.table[i].a;
                that.table[i].b = this.table[i].b;
                that.table[i].c = this.table[i].c;
                that.table[i].res = this.table[i].res;
            }
            if (CACHESTATS > 0) {
                that.cacheHit = this.cacheHit;
                that.cacheMiss = this.cacheMiss;
                if (CACHESTATS > 1)
                    that.cache.addAll(this.cache);
            }
            return that;
        }
        
        final int get(OpCache3Entry e, int node1, int node2, int node3) {
            if (e.a != node1 || e.b != node2 || e.c != node3) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(node1, node2, node3);
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        
        final void set(OpCache3Entry e, int node1, int node2, int node3, int r) {
            e.a = node1;
            e.b = node2;
            e.c = node3;
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a, e.b, e.c);
        }
    }
    
    private static class OpCache3Entry {
        int a, b, c;
        int res;
    }

    private class OpCache4 extends OpCache {
        OpCache4Entry table[];
        
        OpCache4(int size) { alloc(size); }
        final void alloc(int size) {
            table = new OpCache4Entry[size];
            for (int i = 0; i < table.length; ++i) {
                table[i] = new OpCache4Entry();
            }
        }
        final OpCache4Entry lookup(int hash) {
            return (OpCache4Entry) table[Math.abs(hash % table.length)];
        }
        final void reset() {
            for (int i = 0; i < table.length; ++i) {
                table[i].a = -1;
            }
            if (CACHESTATS > 1) cache.clear();
        }
        final void clean() {
            for (int i = 0; i < table.length; ++i) {
                int a = table[i].a;
                if (a == -1) continue;
                if (LOW(a & NODE_MASK) == INVALID_BDD ||
                    LOW(table[i].b) == INVALID_BDD ||
                    LOW(table[i].c) == INVALID_BDD ||
                    LOW(table[i].d) == INVALID_BDD ||
                    LOW(table[i].res) == INVALID_BDD) {
                    if (CACHESTATS > 1) removeCompulsory(table[i].a, table[i].b, table[i].c, table[i].d);
                    table[i].a = -1;
                }
            }
        }
        final OpCache4 copy() {
            OpCache4 that = new OpCache4(this.table.length);
            for (int i = 0; i < this.table.length; ++i) {
                that.table[i].a = this.table[i].a;
                that.table[i].b = this.table[i].b;
                that.table[i].c = this.table[i].c;
                that.table[i].d = this.table[i].d;
                that.table[i].res = this.table[i].res;
            }
            if (CACHESTATS > 0) {
                that.cacheHit = this.cacheHit;
                that.cacheMiss = this.cacheMiss;
                if (CACHESTATS > 1)
                    that.cache.addAll(this.cache);
            }
            return that;
        }
        
        final int get(OpCache4Entry e, int node1, int node2, int node3, int node4) {
            if (e.a != node1 || e.b != node2 || e.c != node3 || e.d != node4) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(node1, node2, node3, node4);
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        
        final void set(OpCache4Entry e, int node1, int node2, int node3, int node4, int r) {
            e.a = node1;
            e.b = node2;
            e.c = node3;
            e.d = node4;
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a, e.b, e.c, e.d);
        }
    }
    
    private static class OpCache4Entry {
        int a, b, c, d;
        int res;
    }
    
    private class OpCacheD extends OpCache {
        OpCacheDEntry table[];
        
        OpCacheD(int size) { alloc(size); }
        final void alloc(int size) {
            table = new OpCacheDEntry[size];
            for (int i = 0; i < table.length; ++i) {
                table[i] = new OpCacheDEntry();
            }
        }
        final OpCacheDEntry lookup(int hash) {
            return (OpCacheDEntry) table[Math.abs(hash % table.length)];
        }
        final void reset() {
            for (int i = 0; i < table.length; ++i) {
                table[i].a = -1;
            }
            if (CACHESTATS > 1) cache.clear();
        }
        final void clean() {
            for (int i = 0; i < table.length; ++i) {
                int a = table[i].a;
                if (a == -1) continue;
                if (LOW(a & NODE_MASK) == INVALID_BDD) {
                    if (CACHESTATS > 1) removeCompulsory(table[i].a);
                    table[i].a = -1;
                }
            }
        }
        final OpCacheD copy() {
            OpCacheD that = new OpCacheD(this.table.length);
            for (int i = 0; i < this.table.length; ++i) {
                that.table[i].a = this.table[i].a;
                that.table[i].res = this.table[i].res;
            }
            if (CACHESTATS > 0) {
                that.cacheHit = this.cacheHit;
                that.cacheMiss = this.cacheMiss;
                if (CACHESTATS > 1)
                    that.cache.addAll(this.cache);
            }
            return that;
        }
        
        final double get_sid(OpCacheDEntry e, int node, int id) {
            if (VERIFY_ASSERTIONS) {
                _assert(node == (node & NODE_MASK));
                _assert(id == (id & ~NODE_MASK));
            }
            int k = node | id;
            if (e.a != k) {
                if (CACHESTATS > 0) cacheMiss++;
                if (CACHESTATS > 1) checkCompulsory(k);
                return -1;
            }
            if (CACHESTATS > 0) cacheHit++;
            return e.res;
        }
        
        final void set_sid(OpCacheDEntry e, int node, int id, double r) {
            if (VERIFY_ASSERTIONS) {
                _assert(node == (node & NODE_MASK));
                _assert(id == (id & ~NODE_MASK));
            }
            e.a = node | id;
            e.res = r;
            if (CACHESTATS > 1) addCompulsory(e.a);
        }
    }
    
    private static class OpCacheDEntry {
        int a;
        double res;
    }
    
    private static class JavaBDDException extends BDDException {
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3257289123604607538L;

        public JavaBDDException(int x) {
            super(errorstrings[-x]);
        }
    }

    private static class ReorderException extends RuntimeException {
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3256727264505772345L;
    }
    
    static final int bddtrue = 1;
    static final int bddfalse = 0;

    static final int BDDONE = 1;
    static final int BDDZERO = 0;

    boolean bddrunning; /* Flag - package initialized */
    int bdderrorcond; /* Some error condition */
    int bddnodesize; /* Number of allocated nodes */
    int bddmaxnodesize; /* Maximum allowed number of nodes */
    int bddmaxnodeincrease; /* Max. # of nodes used to inc. table */
    int bddfreepos; /* First free node */
    int bddfreenum; /* Number of free nodes */
    int bddproduced; /* Number of new nodes ever produced */
    int bddvarnum; /* Number of defined BDD variables */
    int[] bddrefstack; /* Internal node reference stack */
    int bddrefstacktop; /* Internal node reference stack top */
    int[] bddvar2level; /* Variable -> level table */
    int[] bddlevel2var; /* Level -> variable table */
    boolean bddresized; /* Flag indicating a resize of the nodetable */

    int minfreenodes = 20;

    /*=== PRIVATE KERNEL VARIABLES =========================================*/

    int[] bddvarset; /* Set of defined BDD variables */
    int gbcollectnum; /* Number of garbage collections */
    int cachesize; /* Size of the operator caches */
    long gbcclock; /* Clock ticks used in GBC */
    int usednodes_nextreorder; /* When to do reorder next time */

    static final int BDD_MEMORY = (-1); /* Out of memory */
    static final int BDD_VAR = (-2); /* Unknown variable */
    static final int BDD_RANGE = (-3); /* Variable value out of range (not in domain) */
    static final int BDD_DEREF = (-4); /* Removing external reference to unknown node */
    static final int BDD_RUNNING = (-5); /* Called bdd_init() twice without bdd_done() */
    static final int BDD_FILE = (-6); /* Some file operation failed */
    static final int BDD_FORMAT = (-7); /* Incorrect file format */
    static final int BDD_ORDER = (-8); /* Vars. not in order for vector based functions */
    static final int BDD_BREAK = (-9); /* User called break */
    static final int BDD_VARNUM = (-10); /* Different number of vars. for vector pair */
    static final int BDD_NODES = (-11); /* Tried to set max. number of nodes to be fewer */
                                        /* than there already has been allocated */
    static final int BDD_OP = (-12); /* Unknown operator */
    static final int BDD_VARSET = (-13); /* Illegal variable set */
    static final int BDD_VARBLK = (-14); /* Bad variable block operation */
    static final int BDD_DECVNUM = (-15); /* Trying to decrease the number of variables */
    static final int BDD_REPLACE = (-16); /* Replacing to already existing variables */
    static final int BDD_NODENUM = (-17); /* Number of nodes reached user defined maximum */
    static final int BDD_ILLBDD = (-18); /* Illegal bdd argument */
    static final int BDD_SIZE = (-19); /* Illegal size argument */

    static final int BVEC_SIZE = (-20); /* Mismatch in bitvector size */
    static final int BVEC_SHIFT = (-21); /* Illegal shift-left/right parameter */
    static final int BVEC_DIVZERO = (-22); /* Division by zero */

    static final int BDD_ERRNUM = 24;

    /* Strings for all error mesages */
    static String errorstrings[] =
        {
            "",
            "Out of memory",
            "Unknown variable",
            "Value out of range",
            "Unknown BDD root dereferenced",
            "bdd_init() called twice",
            "File operation failed",
            "Incorrect file format",
            "Variables not in ascending order",
            "User called break",
            "Mismatch in size of variable sets",
            "Cannot allocate fewer nodes than already in use",
            "Unknown operator",
            "Illegal variable set",
            "Bad variable block operation",
            "Trying to decrease the number of variables",
            "Trying to replace with variables already in the bdd",
            "Number of nodes reached user defined maximum",
            "Unknown BDD - was not in node table",
            "Bad size argument",
            "Mismatch in bitvector size",
            "Illegal shift-left/right parameter",
            "Division by zero" };

    static final int DEFAULTMAXNODEINC = 10000000;

    /*=== OTHER INTERNAL DEFINITIONS =======================================*/

    static final int PAIR(int a, int b) {
        //return Math.abs((a + b) * (a + b + 1) / 2 + a);
        return ((a + b) * (a + b + 1) / 2 + a);
    }
    static final int TRIPLE(int a, int b, int c) {
        //return Math.abs(PAIR(c, PAIR(a, b)));
        return (PAIR(c, PAIR(a, b)));
    }

    final int NODEHASH(int lvl, int l, int h) {
        return Math.abs(TRIPLE(lvl, l, h) % bddnodesize);
    }

    int bdd_ithvar(int var) {
        if (var < 0 || var >= bddvarnum) {
            bdd_error(BDD_VAR);
            return bddfalse;
        }

        return bddvarset[var * 2];
    }

    int bdd_nithvar(int var) {
        if (var < 0 || var >= bddvarnum) {
            bdd_error(BDD_VAR);
            return bddfalse;
        }

        return bddvarset[var * 2 + 1];
    }

    int bdd_varnum() {
        return bddvarnum;
    }

    static int bdd_error(int v) {
        throw new JavaBDDException(v);
    }

    static boolean ISZERO(int r) {
        return r == bddfalse;
    }

    static boolean ISONE(int r) {
        return r == bddtrue;
    }

    static boolean ISCONST(int r) {
        //return r == bddfalse || r == bddtrue;
        return r < 2;
    }

    void CHECK(int r) {
        if (!bddrunning)
            bdd_error(BDD_RUNNING);
        else if (r < 0 || r >= bddnodesize)
            bdd_error(BDD_ILLBDD);
        else if (r >= 2 && LOW(r) == INVALID_BDD)
            bdd_error(BDD_ILLBDD);
    }
    void CHECKa(int r, int x) {
        CHECK(r);
    }

    int bdd_var(int root) {
        CHECK(root);
        if (root < 2)
            bdd_error(BDD_ILLBDD);

        return (bddlevel2var[LEVEL(root)]);
    }

    int bdd_low(int root) {
        CHECK(root);
        if (root < 2)
            return bdd_error(BDD_ILLBDD);

        return (LOW(root));
    }

    int bdd_high(int root) {
        CHECK(root);
        if (root < 2)
            return bdd_error(BDD_ILLBDD);

        return (HIGH(root));
    }

    void checkresize() {
        if (bddresized)
            bdd_operator_noderesize();
        bddresized = false;
    }

    static final int NOTHASH(int r) {
        return r;
    }
    static final int ANDHASH(int l, int r) {
        //return PAIR(l, r);
        return (l ^ r);
    }
    static final int ORHASH(int l, int r) {
        //return PAIR(l, r);
        return (l ^ r);
    }
    static final int APPLYHASH(int l, int r, int op) {
        return TRIPLE(l, r, op);
    }
    static final int ITEHASH(int f, int g, int h) {
        return TRIPLE(f, g, h);
    }
    static final int RESTRHASH(int r, int var) {
        return PAIR(r, var);
    }
    static final int CONSTRAINHASH(int f, int c) {
        return PAIR(f, c);
    }
    static final int QUANTHASH(int r) {
        return r;
    }
    static final int REPLACEHASH(int r) {
        return r;
    }
    static final int VECCOMPOSEHASH(int f) {
        return f;
    }
    static final int COMPOSEHASH(int f, int g) {
        return PAIR(f, g);
    }
    static final int SATCOUHASH(int r) {
        return r;
    }
    static final int PATHCOUHASH(int r) {
        return r;
    }
    static final int APPEXHASH(int l, int r, int op) {
        //return PAIR(l, r);
        return (l ^ r ^ op);
    }
    static final int APPEX3HASH(int a, int b, int c, int op) {
        //return PAIR(l, r);
        return (a ^ b ^ c ^ op);
    }

    static final double M_LN2 = 0.69314718055994530942;

    static double log1p(double a) {
        return Math.log(1.0 + a);
    }

    final boolean INVARSET(int a) {
        return (quantvarset[a] == quantvarsetID); /* unsigned check */
    }
    final boolean INSVARSET(int a) {
        return Math.abs(quantvarset[a]) == quantvarsetID; /* signed check */
    }

    static final int bddop_and = 0;
    static final int bddop_xor = 1;
    static final int bddop_or = 2;
    static final int bddop_nand = 3;
    static final int bddop_nor = 4;
    static final int bddop_imp = 5;
    static final int bddop_biimp = 6;
    static final int bddop_diff = 7;
    static final int bddop_less = 8;
    static final int bddop_invimp = 9;

    /* Should *not* be used in bdd_apply calls !!! */
    static final int bddop_not = 10;
    static final int bddop_simplify = 11;

    int bdd_not(int r) {
        int res;
        int numReorder = 1;
        CHECKa(r, bddfalse);

        if (singlecache == null) singlecache = new OpCache1(cachesize); // not_rec()
        
        again : for (;;) {
            try {
                INITREF();
                if (numReorder == 0) bdd_disable_reorder();
                res = not_rec(r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int not_rec(int r) {
        OpCache1Entry entry;
        int res;

        if (ISCONST(r))
            return 1 - r;

        entry = singlecache.lookup(NOTHASH(r));
        if ((res = singlecache.get_sid(entry, r, bddop_not << NODE_BITS)) >= 0) {
            return res;
        }

        PUSHREF(not_rec(LOW(r)));
        PUSHREF(not_rec(HIGH(r)));
        res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
        POPREF(2);

        singlecache.set_sid(entry, r, bddop_not << NODE_BITS, res);

        return res;
    }

    int bdd_ite(int f, int g, int h) {
        int res;
        int numReorder = 1;

        CHECKa(f, bddfalse);
        CHECKa(g, bddfalse);
        CHECKa(h, bddfalse);

        if (itecache == null) itecache = new OpCache3(cachesize); // ite_rec()
        if (singlecache == null) singlecache = new OpCache1(cachesize); // not_rec()
        
        again : for (;;) {
            try {
                INITREF();
                if (numReorder == 0) bdd_disable_reorder();
                res = ite_rec(f, g, h);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int ite_rec(int f, int g, int h) {
        OpCache3Entry entry;
        int res;

        if (ISONE(f)) return g;
        if (ISZERO(f)) return h;
        if (g == h) return g;
        if (ISONE(g) && ISZERO(h)) return f;
        if (ISZERO(g) && ISONE(h)) return not_rec(f);

        entry = itecache.lookup(ITEHASH(f, g, h));
        if ((res = itecache.get(entry, f, g, h)) >= 0) {
            return res;
        }

        int LEVEL_f = LEVEL(f);
        int LEVEL_g = LEVEL(g);
        int LEVEL_h = LEVEL(h);
        if (LEVEL_f == LEVEL_g) {
            if (LEVEL_f == LEVEL_h) {
                PUSHREF(ite_rec(LOW(f), LOW(g), LOW(h)));
                PUSHREF(ite_rec(HIGH(f), HIGH(g), HIGH(h)));
                res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
            } else if (LEVEL_f < LEVEL_h) {
                PUSHREF(ite_rec(LOW(f), LOW(g), h));
                PUSHREF(ite_rec(HIGH(f), HIGH(g), h));
                res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
            } else /* f > h */ {
                PUSHREF(ite_rec(f, g, LOW(h)));
                PUSHREF(ite_rec(f, g, HIGH(h)));
                res = bdd_makenode(LEVEL_h, READREF(2), READREF(1));
            }
        } else if (LEVEL_f < LEVEL_g) {
            if (LEVEL_f == LEVEL_h) {
                PUSHREF(ite_rec(LOW(f), g, LOW(h)));
                PUSHREF(ite_rec(HIGH(f), g, HIGH(h)));
                res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
            } else if (LEVEL_f < LEVEL_h) {
                PUSHREF(ite_rec(LOW(f), g, h));
                PUSHREF(ite_rec(HIGH(f), g, h));
                res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
            } else /* f > h */ {
                PUSHREF(ite_rec(f, g, LOW(h)));
                PUSHREF(ite_rec(f, g, HIGH(h)));
                res = bdd_makenode(LEVEL_h, READREF(2), READREF(1));
            }
        } else /* f > g */ {
            if (LEVEL_g == LEVEL_h) {
                PUSHREF(ite_rec(f, LOW(g), LOW(h)));
                PUSHREF(ite_rec(f, HIGH(g), HIGH(h)));
                res = bdd_makenode(LEVEL_g, READREF(2), READREF(1));
            } else if (LEVEL_g < LEVEL_h) {
                PUSHREF(ite_rec(f, LOW(g), h));
                PUSHREF(ite_rec(f, HIGH(g), h));
                res = bdd_makenode(LEVEL_g, READREF(2), READREF(1));
            } else /* g > h */ {
                PUSHREF(ite_rec(f, g, LOW(h)));
                PUSHREF(ite_rec(f, g, HIGH(h)));
                res = bdd_makenode(LEVEL_h, READREF(2), READREF(1));
            }
        }

        POPREF(2);

        itecache.set(entry, f, g, h, res);

        return res;
    }

    int bdd_replace(int r, bddPair pair) {
        int res;
        int numReorder = 1;

        CHECKa(r, bddfalse);

        if (replacecache == null) replacecache = new OpCache2(cachesize); // replace_rec()
        
        again : for (;;) {
            try {
                INITREF();
                replacepair = pair.result;
                replacelast = pair.last;
                replaceid = (pair.id << 1) | CACHEID_REPLACE;
                if (numReorder == 0) bdd_disable_reorder();
                res = replace_rec(r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int replace_rec(int r) {
        OpCache2Entry entry;
        int res;

        if (ISCONST(r) || LEVEL(r) > replacelast) return r;

        entry = replacecache.lookup(REPLACEHASH(r));
        if ((res = replacecache.get(entry, r, replaceid)) >= 0) {
            return res;
        }

        PUSHREF(replace_rec(LOW(r)));
        PUSHREF(replace_rec(HIGH(r)));

        res = bdd_correctify(LEVEL(replacepair[LEVEL(r)]), READREF(2), READREF(1));
        POPREF(2);

        replacecache.set(entry, r, replaceid, res);

        return res;
    }

    int bdd_correctify(int level, int l, int r) {
        int res;

        if (level < LEVEL(l) && level < LEVEL(r))
            return bdd_makenode(level, l, r);

        if (level == LEVEL(l) || level == LEVEL(r)) {
            bdd_error(BDD_REPLACE);
            return 0;
        }

        if (LEVEL(l) == LEVEL(r)) {
            PUSHREF(bdd_correctify(level, LOW(l), LOW(r)));
            PUSHREF(bdd_correctify(level, HIGH(l), HIGH(r)));
            res = bdd_makenode(LEVEL(l), READREF(2), READREF(1));
        } else if (LEVEL(l) < LEVEL(r)) {
            PUSHREF(bdd_correctify(level, LOW(l), r));
            PUSHREF(bdd_correctify(level, HIGH(l), r));
            res = bdd_makenode(LEVEL(l), READREF(2), READREF(1));
        } else {
            PUSHREF(bdd_correctify(level, l, LOW(r)));
            PUSHREF(bdd_correctify(level, l, HIGH(r)));
            res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
        }
        POPREF(2);

        return res; /* FIXME: cache ? */
    }

    int bdd_apply(int l, int r, int op) {
        int res;
        int numReorder = 1;

        CHECKa(l, bddfalse);
        CHECKa(r, bddfalse);

        if (op < 0 || op > bddop_invimp) {
            bdd_error(BDD_OP);
            return bddfalse;
        }

        switch (op) {
            case bddop_and:
                if (andcache == null) andcache = new OpCache2(cachesize);
                break;
            case bddop_or:
                if (orcache == null) orcache = new OpCache2(cachesize);
                break;
            default:
                if (applycache == null) applycache = new OpCache2(cachesize);
                break;
        }
        
        again : for (;;) {
            try {
                INITREF();
                applyop = op;
                if (numReorder == 0) bdd_disable_reorder();
                switch (op) {
                    case bddop_and: res = and_rec(l, r); break;
                    case bddop_or: res = or_rec(l, r); break;
                    default: res = apply_rec(l, r); break;
                }
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int apply_rec(int l, int r) {
        OpCache2Entry entry;
        int res;

        if (VERIFY_ASSERTIONS) _assert(applyop != bddop_and && applyop != bddop_or);
        
        switch (applyop) {
            case bddop_xor :
                if (l == r)
                    return 0;
                if (ISZERO(l))
                    return r;
                if (ISZERO(r))
                    return l;
                break;
            case bddop_nand :
                if (ISZERO(l) || ISZERO(r))
                    return 1;
                break;
            case bddop_nor :
                if (ISONE(l) || ISONE(r))
                    return 0;
                break;
            case bddop_imp :
                if (ISZERO(l))
                    return 1;
                if (ISONE(l))
                    return r;
                if (ISONE(r))
                    return 1;
                break;
        }

        if (ISCONST(l) && ISCONST(r))
            res = oprres[applyop][l << 1 | r];
        else {
            entry = applycache.lookup(APPLYHASH(l, r, applyop));
            if ((res = applycache.get_sid(entry, l, r, applyop << NODE_BITS)) >= 0) {
                return res;
            }
            
            int LEVEL_l = LEVEL(l);
            int LEVEL_r = LEVEL(r);
            if (LEVEL_l == LEVEL_r) {
                PUSHREF(apply_rec(LOW(l), LOW(r)));
                PUSHREF(apply_rec(HIGH(l), HIGH(r)));
                res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
            } else if (LEVEL_l < LEVEL_r) {
                PUSHREF(apply_rec(LOW(l), r));
                PUSHREF(apply_rec(HIGH(l), r));
                res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
            } else {
                PUSHREF(apply_rec(l, LOW(r)));
                PUSHREF(apply_rec(l, HIGH(r)));
                res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
            }

            POPREF(2);

            applycache.set_sid(entry, l, r, applyop << NODE_BITS, res);
        }

        return res;
    }

    int and_rec(int l, int r) {
        OpCache2Entry entry;
        int res;

        if (l == r) return l;
        if (ISZERO(l) || ISZERO(r)) return 0;
        if (ISONE(l)) return r;
        if (ISONE(r)) return l;
        
        if (ORDER_CACHE && l < r) { int t = l; l = r; r = t; }
        
        entry = andcache.lookup(ANDHASH(l, r));
        if ((res = andcache.get(entry, l, r)) >= 0) {
            return res;
        }
        
        int LEVEL_l = LEVEL(l);
        int LEVEL_r = LEVEL(r);
        if (LEVEL_l == LEVEL_r) {
            PUSHREF(and_rec(LOW(l), LOW(r)));
            PUSHREF(and_rec(HIGH(l), HIGH(r)));
            res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
        } else if (LEVEL_l < LEVEL_r) {
            PUSHREF(and_rec(LOW(l), r));
            PUSHREF(and_rec(HIGH(l), r));
            res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
        } else {
            PUSHREF(and_rec(l, LOW(r)));
            PUSHREF(and_rec(l, HIGH(r)));
            res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
        }

        POPREF(2);

        andcache.set(entry, l, r, res);

        return res;
    }
    
    int or_rec(int l, int r) {
        OpCache2Entry entry;
        int res;

        if (l == r) return l;
        if (ISONE(l) || ISONE(r)) return 1;
        if (ISZERO(l)) return r;
        if (ISZERO(r)) return l;
        
        if (l < r) { int t = l; l = r; r = t; }
        
        entry = orcache.lookup(ORHASH(l, r));
        if ((res = orcache.get(entry, l, r)) >= 0) {
            return res;
        }

        int LEVEL_l = LEVEL(l);
        int LEVEL_r = LEVEL(r);
        if (LEVEL_l == LEVEL_r) {
            PUSHREF(or_rec(LOW(l), LOW(r)));
            PUSHREF(or_rec(HIGH(l), HIGH(r)));
            res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
        } else if (LEVEL_l < LEVEL_r) {
            PUSHREF(or_rec(LOW(l), r));
            PUSHREF(or_rec(HIGH(l), r));
            res = bdd_makenode(LEVEL_l, READREF(2), READREF(1));
        } else {
            PUSHREF(or_rec(l, LOW(r)));
            PUSHREF(or_rec(l, HIGH(r)));
            res = bdd_makenode(LEVEL_r, READREF(2), READREF(1));
        }

        POPREF(2);

        orcache.set(entry, l, r, res);

        return res;
    }

    int bdd_relprod(int a, int b, int var) {
        return bdd_appex(a, b, bddop_and, var);
    }

    int bdd_relprod3(int a, int b, int c, int var) {
        int res;
        int numReorder = 1;

        CHECKa(a, bddfalse);
        CHECKa(b, bddfalse);
        CHECKa(c, bddfalse);
        CHECKa(var, bddfalse);

        if (ISCONST(var)) {
            /* Empty set */
            res = bdd_apply(a, b, bddop_and);
            return bdd_apply(res, c, bddop_and);
        }

        if (andcache == null) andcache = new OpCache2(cachesize);
        if (appexcache == null) appexcache = new OpCache3(cachesize);
        if (appex3cache == null) appex3cache = new OpCache4(cachesize);
        if (orcache == null) orcache = new OpCache2(cachesize); // or_rec()
        if (quantcache == null) quantcache = new OpCache2(cachesize); // quant_rec()
        
        again : for (;;) {
            if (varset2vartable(var) < 0)
                return bddfalse;
            try {
                INITREF();
                applyop = bddop_or;
                appexop = bddop_and;
                appexid = (var << 7) | (appexop << 3) | CACHEID_APPEX;
                quantid = appexid;
                if (numReorder == 0) bdd_disable_reorder();
                res = relprod3_rec(a, b, c);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }
    
    int bdd_appex(int l, int r, int opr, int var) {
        int res;
        int numReorder = 1;

        CHECKa(l, bddfalse);
        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (opr < 0 || opr > bddop_invimp) {
            bdd_error(BDD_OP);
            return bddfalse;
        }

        if (ISCONST(var)) /* Empty set */
            return bdd_apply(l, r, opr);

        switch (opr) {
            case bddop_and:
                if (andcache == null) andcache = new OpCache2(cachesize);
                break;
            default:
                if (applycache == null) applycache = new OpCache2(cachesize);
                break;
        }
        if (appexcache == null) appexcache = new OpCache3(cachesize);
        if (orcache == null) orcache = new OpCache2(cachesize); // or_rec()
        if (quantcache == null) quantcache = new OpCache2(cachesize); // quant_rec()
        
        again : for (;;) {
            if (varset2vartable(var) < 0)
                return bddfalse;
            try {
                INITREF();
                applyop = bddop_or;
                appexop = opr;
                appexid = (var << 7) | (appexop << 3) | CACHEID_APPEX;
                quantid = appexid;
                if (numReorder == 0) bdd_disable_reorder();
                if (opr == bddop_and)
                    res = relprod_rec(l, r);
                else
                    res = appquant_rec(l, r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int bdd_appall(int l, int r, int opr, int var) {
        int res;
        int numReorder = 1;

        CHECKa(l, bddfalse);
        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (opr < 0 || opr > bddop_invimp) {
            bdd_error(BDD_OP);
            return bddfalse;
        }

        if (var < 2) /* Empty set */
            return bdd_apply(l, r, opr);

        switch (opr) {
            case bddop_or:
                if (orcache == null) orcache = new OpCache2(cachesize); // or_rec()
                break;
            default:
                if (applycache == null) applycache = new OpCache2(cachesize); // apply_rec()
                break;
        }
        if (appexcache == null) appexcache = new OpCache3(cachesize); // appquant_rec()
        if (andcache == null) andcache = new OpCache2(cachesize); // and_rec()
        if (quantcache == null) quantcache = new OpCache2(cachesize); // quant_rec()
        
        again : for (;;) {
            if (varset2vartable(var) < 0)
                return bddfalse;
            try {
                INITREF();
                applyop = bddop_and;
                appexop = opr;
                appexid = (var << 7) | (appexop << 3) | CACHEID_APPAL;
                quantid = appexid;
                if (numReorder == 0) bdd_disable_reorder();
                res = appquant_rec(l, r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int bdd_appuni(int l, int r, int opr, int var) {
        int res;
        int numReorder = 1;

        CHECKa(l, bddfalse);
        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (opr < 0 || opr > bddop_invimp) {
            bdd_error(BDD_OP);
            return bddfalse;
        }

        if (var < 2) /* Empty set */
            return bdd_apply(l, r, opr);

        switch (opr) {
            case bddop_and:
                if (andcache == null) andcache = new OpCache2(cachesize); // and_rec()
                break;
            case bddop_or:
                if (orcache == null) orcache = new OpCache2(cachesize); // or_rec()
                break;
            default:
                if (applycache == null) applycache = new OpCache2(cachesize); // apply_rec()
                break;
        }
        if (appexcache == null) appexcache = new OpCache3(cachesize); // appquant_rec()
        if (quantcache == null) quantcache = new OpCache2(cachesize); // quant_rec()
        
        again : for (;;) {
            try {
                INITREF();
                applyop = bddop_xor;
                appexop = opr;
                appexid = (var << 7) | (appexop << 3) | CACHEID_APPUN; /* FIXME: range! */
                quantid = appexid;
                if (numReorder == 0) bdd_disable_reorder();
                res = appuni_rec(l, r, var);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int varset2vartable(int r) {
        int n;

        if (r < 2) return bdd_error(BDD_VARSET);

        quantvarsetID++;
        if (quantvarsetID == Integer.MAX_VALUE) {
            for (int i = 0; i < bddvarnum; ++i)
                quantvarset[i] = 0;
            quantvarsetID = 1;
        }

        quantlast = -1;
        for (n = r; n > 1; n = HIGH(n)) {
            quantvarset[LEVEL(n)] = quantvarsetID;
            if (VERIFY_ASSERTIONS) _assert(quantlast < LEVEL(n));
            quantlast = LEVEL(n);
        }

        return 0;
    }

    int varset2svartable(int r) {
        int n;

        if (r < 2) return bdd_error(BDD_VARSET);

        quantvarsetID++;

        if (quantvarsetID == Integer.MAX_VALUE / 2) {
            for (int i = 0; i < bddvarnum; ++i)
                quantvarset[i] = 0;
            quantvarsetID = 1;
        }

        quantlast = 0;
        for (n = r; !ISCONST(n); ) {
            if (ISZERO(LOW(n))) {
                quantvarset[LEVEL(n)] = quantvarsetID;
                n = HIGH(n);
            } else {
                quantvarset[LEVEL(n)] = -quantvarsetID;
                n = LOW(n);
            }
            if (VERIFY_ASSERTIONS) _assert(quantlast < LEVEL(n));
            quantlast = LEVEL(n);
        }

        return 0;
    }

    int appquant_rec(int l, int r) {
        OpCache3Entry entry;
        int res;

        if (VERIFY_ASSERTIONS) _assert(appexop != bddop_and);
        
        switch (appexop) {
            case bddop_or :
                if (l == 1 || r == 1) return 1;
                if (l == r) return quant_rec(l);
                if (l == 0) return quant_rec(r);
                if (r == 0) return quant_rec(l);
                break;
            case bddop_xor :
                if (l == r) return 0;
                if (l == 0) return quant_rec(r);
                if (r == 0) return quant_rec(l);
                break;
            case bddop_nand :
                if (l == 0 || r == 0) return 1;
                break;
            case bddop_nor :
                if (l == 1 || r == 1) return 0;
                break;
        }

        if (ISCONST(l) && ISCONST(r))
            return oprres[appexop][(l << 1) | r];
        
        int LEVEL_l = LEVEL(l);
        int LEVEL_r = LEVEL(r);
        if (LEVEL_l > quantlast && LEVEL_r > quantlast) {
            int oldop = applyop;
            applyop = appexop;
            switch (applyop) {
            case bddop_and: res = and_rec(l, r); break;
            case bddop_or: res = or_rec(l, r); break;
            default: res = apply_rec(l, r); break;
            }
            applyop = oldop;
            return res;
        }
        entry = appexcache.lookup(APPEXHASH(l, r, appexop));
        if ((res = appexcache.get(entry, l, r, appexid)) >= 0) {
            return res;
        }

        int lev;
        if (LEVEL_l == LEVEL_r) {
            PUSHREF(appquant_rec(LOW(l), LOW(r)));
            PUSHREF(appquant_rec(HIGH(l), HIGH(r)));
            lev = LEVEL_l;
        } else if (LEVEL_l < LEVEL_r) {
            PUSHREF(appquant_rec(LOW(l), r));
            PUSHREF(appquant_rec(HIGH(l), r));
            lev = LEVEL_l;
        } else {
            PUSHREF(appquant_rec(l, LOW(r)));
            PUSHREF(appquant_rec(l, HIGH(r)));
            lev = LEVEL_r;
        }
        if (INVARSET(lev)) {
            int r2 = READREF(2), r1 = READREF(1);
            switch (applyop) {
            case bddop_and: res = and_rec(r2, r1); break;
            case bddop_or: res = or_rec(r2, r1); break;
            default: res = apply_rec(r2, r1); break;
            }
        } else {
            res = bdd_makenode(lev, READREF(2), READREF(1));
        }

        POPREF(2);

        appexcache.set(entry, l, r, appexid, res);

        return res;
    }

    int relprod_rec(int l, int r) {
        OpCache3Entry entry;
        int res;

        if (l == 0 || r == 0) return 0;
        if (l == r) return quant_rec(l);
        if (l == 1) return quant_rec(r);
        if (r == 1) return quant_rec(l);
        
        if (l < r) { int t = l; l = r; r = t; }
        
        int LEVEL_l = LEVEL(l);
        int LEVEL_r = LEVEL(r);
        if (LEVEL_l > quantlast && LEVEL_r > quantlast) {
            applyop = bddop_and;
            res = and_rec(l, r);
            applyop = bddop_or;
            return res;
        }
        
        entry = appexcache.lookup(APPEXHASH(l, r, appexop));
        if ((res = appexcache.get(entry, l, r, appexid)) >= 0) {
            return res;
        }

        int lev;
        if (LEVEL_l == LEVEL_r) {
            PUSHREF(relprod_rec(LOW(l), LOW(r)));
            PUSHREF(relprod_rec(HIGH(l), HIGH(r)));
            lev = LEVEL_l;
        } else if (LEVEL_l < LEVEL_r) {
            PUSHREF(relprod_rec(LOW(l), r));
            PUSHREF(relprod_rec(HIGH(l), r));
            lev = LEVEL_l;
        } else {
            PUSHREF(relprod_rec(l, LOW(r)));
            PUSHREF(relprod_rec(l, HIGH(r)));
            lev = LEVEL_r;
        }
        if (INVARSET(lev))
            res = or_rec(READREF(2), READREF(1));
        else
            res = bdd_makenode(lev, READREF(2), READREF(1));

        POPREF(2);

        appexcache.set(entry, l, r, appexid, res);

        return res;
    }
    
    int relprod3_rec(int a, int b, int c) {
        OpCache4Entry entry;
        int res;

        if (a == 0 || b == 0 || c == 0) return 0;
        if (a == b || a == 1) return relprod_rec(b, c);
        if (b == c || b == 1) return relprod_rec(a, c);
        if (c == a || c == 1) return relprod_rec(a, b);
        
        int LEVEL_a = LEVEL(a);
        int LEVEL_b = LEVEL(b);
        int LEVEL_c = LEVEL(c);
        if (LEVEL_a > quantlast && LEVEL_b > quantlast && LEVEL_c > quantlast) {
            applyop = bddop_and;
            res = and_rec(a, b);
            res = and_rec(res, c);
            applyop = bddop_or;
            return res;
        }
        
        entry = appex3cache.lookup(APPEX3HASH(a, b, c, appexop));
        if ((res = appex3cache.get(entry, a, b, c, appexid)) >= 0) {
            return res;
        }

        int x1, x2, x3, y1, y2, y3, lev;
        x1 = y1 = a;
        x2 = y2 = b;
        x3 = y3 = c;
        if (LEVEL_b < LEVEL_c) {
            if (LEVEL_a < LEVEL_b) {
                // a b c
                x1 = LOW(a); y1 = HIGH(a); lev = LEVEL_a;
            } else {
                x2 = LOW(b); y2 = HIGH(b); lev = LEVEL_b;
                if (LEVEL_a == LEVEL_b) {
                    // ab c
                    x1 = LOW(a); y1 = HIGH(a);
                }
            }
        } else if (LEVEL_b == LEVEL_c) {
            if (LEVEL_a < LEVEL_b) {
                // a bc
                x1 = LOW(a); y1 = HIGH(a); lev = LEVEL_a;
            } else {
                x2 = LOW(b); y2 = HIGH(b); lev = LEVEL_b;
                x3 = LOW(c); y3 = HIGH(c);
                if (LEVEL_a == LEVEL_b) {
                    // abc
                    x1 = LOW(a); y1 = HIGH(a);
                }
            }
        } else if (LEVEL_a < LEVEL_c) {
            // a c b
            x1 = LOW(a); y1 = HIGH(a); lev = LEVEL_a;
        } else {
            x3 = LOW(c); y3 = HIGH(c); lev = LEVEL_c;
            if (LEVEL_a == LEVEL_c) {
                x1 = LOW(a); y1 = HIGH(a);
            }
        }
        
        PUSHREF(relprod3_rec(x1, x2, x3));
        PUSHREF(relprod3_rec(y1, y2, y3));
        if (INVARSET(lev)) {
            res = or_rec(READREF(2), READREF(1));
        } else {
            res = bdd_makenode(lev, READREF(2), READREF(1));
        }

        POPREF(2);

        appex3cache.set(entry, a, b, c, appexid, res);

        return res;
    }
    
    int appuni_rec(int l, int r, int var) {
        OpCache3Entry entry;
        int res;

        int LEVEL_l, LEVEL_r, LEVEL_var;
        LEVEL_l = LEVEL(l);
        LEVEL_r = LEVEL(r);
        LEVEL_var = LEVEL(var);

        if (LEVEL_l > LEVEL_var && LEVEL_r > LEVEL_var) {
            // Skipped a quantified node, answer is zero.
            return BDDZERO;
        }

        if (ISCONST(l) && ISCONST(r)) {
            res = oprres[appexop][(l << 1) | r];
            return res;
        } else if (ISCONST(var)) {
            int oldop = applyop;
            applyop = appexop;
            switch (applyop) {
            case bddop_and: res = and_rec(l, r); break;
            case bddop_or: res = or_rec(l, r); break;
            default: res = apply_rec(l, r); break;
            }
            applyop = oldop;
            return res;
        }
        entry = appexcache.lookup(APPEXHASH(l, r, appexop));
        if ((res = appexcache.get(entry, l, r, appexid)) >= 0) {
            return res;
        }

        int lev;
        if (LEVEL_l == LEVEL_r) {
            if (LEVEL_l == LEVEL_var) {
                lev = -1;
                var = HIGH(var);
            } else {
                lev = LEVEL_l;
            }
            PUSHREF(appuni_rec(LOW(l), LOW(r), var));
            PUSHREF(appuni_rec(HIGH(l), HIGH(r), var));
            lev = LEVEL_l;
        } else if (LEVEL_l < LEVEL_r) {
            if (LEVEL_l == LEVEL_var) {
                lev = -1;
                var = HIGH(var);
            } else {
                lev = LEVEL_l;
            }
            PUSHREF(appuni_rec(LOW(l), r, var));
            PUSHREF(appuni_rec(HIGH(l), r, var));
        } else {
            if (LEVEL_r == LEVEL_var) {
                lev = -1;
                var = HIGH(var);
            } else {
                lev = LEVEL_r;
            }
            PUSHREF(appuni_rec(l, LOW(r), var));
            PUSHREF(appuni_rec(l, HIGH(r), var));
        }
        if (lev == -1) {
            int r2 = READREF(2), r1 = READREF(1);
            switch (applyop) {
            case bddop_and: res = and_rec(r2, r1); break;
            case bddop_or: res = or_rec(r2, r1); break;
            default: res = apply_rec(r2, r1); break;
            }
        } else {
            res = bdd_makenode(lev, READREF(2), READREF(1));
        }

        POPREF(2);

        appexcache.set(entry, l, r, appexid, res);

        return res;
    }
    
    int bdd_constrain(int f, int c) {
        int res;
        int numReorder = 1;

        CHECKa(f, bddfalse);
        CHECKa(c, bddfalse);

        if (misccache == null) misccache = new OpCache2(cachesize);
        
        again : for (;;) {
            try {
                INITREF();
                miscid = CACHEID_CONSTRAIN << NODE_BITS;
                if (numReorder == 0) bdd_disable_reorder();
                res = constrain_rec(f, c);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int constrain_rec(int f, int c) {
        OpCache2Entry entry;
        int res;

        if (ISONE(c)) return f;
        if (ISCONST(f)) return f;
        if (c == f) return BDDONE;
        if (ISZERO(c)) return BDDZERO;

        entry = misccache.lookup(CONSTRAINHASH(f, c));
        if ((res = misccache.get_sid(entry, f, c, miscid)) >= 0) {
            return res;
        }
        
        int LEVEL_f = LEVEL(f);
        int LEVEL_c = LEVEL(c);
        if (LEVEL_f == LEVEL_c) {
            int LOW_c = LOW(c);
            int HIGH_c = HIGH(c);
            if (ISZERO(LOW_c))
                res = constrain_rec(HIGH(f), HIGH_c);
            else if (ISZERO(HIGH_c))
                res = constrain_rec(LOW(f), LOW_c);
            else {
                PUSHREF(constrain_rec(LOW(f), LOW_c));
                PUSHREF(constrain_rec(HIGH(f), HIGH_c));
                res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
                POPREF(2);
            }
        } else if (LEVEL_f < LEVEL_c) {
            PUSHREF(constrain_rec(LOW(f), c));
            PUSHREF(constrain_rec(HIGH(f), c));
            res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
            POPREF(2);
        } else {
            int LOW_c = LOW(c);
            int HIGH_c = HIGH(c);
            if (ISZERO(LOW_c))
                res = constrain_rec(f, HIGH_c);
            else if (ISZERO(HIGH_c))
                res = constrain_rec(f, LOW_c);
            else {
                PUSHREF(constrain_rec(f, LOW_c));
                PUSHREF(constrain_rec(f, HIGH_c));
                res = bdd_makenode(LEVEL_c, READREF(2), READREF(1));
                POPREF(2);
            }
        }

        misccache.set_sid(entry, f, c, miscid, res);

        return res;
    }

    int bdd_compose(int f, int g, int var) {
        int res;
        int numReorder = 1;

        CHECKa(f, bddfalse);
        CHECKa(g, bddfalse);
        if (var < 0 || var >= bddvarnum) {
            bdd_error(BDD_VAR);
            return bddfalse;
        }

        if (appexcache == null) appexcache = new OpCache3(cachesize);
        if (itecache == null) itecache = new OpCache3(cachesize);
        
        again : for (;;) {
            try {
                INITREF();
                composelevel = bddvar2level[var];
                appexid = (composelevel << 3) | CACHEID_COMPOSE;
                if (numReorder == 0) bdd_disable_reorder();
                res = compose_rec(f, g);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int compose_rec(int f, int g) {
        OpCache3Entry entry;
        int res;

        int LEVEL_f = LEVEL(f);
        if (LEVEL_f > composelevel) return f;

        entry = appexcache.lookup(COMPOSEHASH(f, g));
        if ((res = appexcache.get(entry, f, g, appexid)) >= 0) {
            return res;
        }

        if (LEVEL_f < composelevel) {
            int LEVEL_g = LEVEL(g);
            int lev;
            if (LEVEL_f == LEVEL_g) {
                PUSHREF(compose_rec(LOW(f), LOW(g)));
                PUSHREF(compose_rec(HIGH(f), HIGH(g)));
                lev = LEVEL_f;
            } else if (LEVEL_f < LEVEL_g) {
                PUSHREF(compose_rec(LOW(f), g));
                PUSHREF(compose_rec(HIGH(f), g));
                lev = LEVEL_f;
            } else {
                PUSHREF(compose_rec(f, LOW(g)));
                PUSHREF(compose_rec(f, HIGH(g)));
                lev = LEVEL_g;
            }
            res = bdd_makenode(lev, READREF(2), READREF(1));
            POPREF(2);
        } else
            /*if (LEVEL_f == composelevel) changed 2-nov-98 */ {
            res = ite_rec(g, HIGH(f), LOW(f));
        }

        appexcache.set(entry, f, g, appexid, res);

        return res;
    }

    int bdd_veccompose(int f, bddPair pair) {
        int res;
        int numReorder = 1;

        CHECKa(f, bddfalse);

        if (singlecache == null) singlecache = new OpCache1(cachesize);
        if (replacecache == null) replacecache = new OpCache2(cachesize);
        if (itecache == null) itecache = new OpCache3(cachesize);
        
        again : for (;;) {
            try {
                INITREF();
                replacepair = pair.result;
                replacelast = pair.last;
                replaceid = (pair.id << 1) | CACHEID_VECCOMPOSE;
                if (numReorder == 0) bdd_disable_reorder();
                res = veccompose_rec(f);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int veccompose_rec(int f) {
        OpCache2Entry entry;
        int res;

        int LEVEL_f = LEVEL(f);
        if (LEVEL_f > replacelast) return f;

        entry = replacecache.lookup(VECCOMPOSEHASH(f));
        if ((res = replacecache.get(entry, f, replaceid)) >= 0) {
            return res;
        }
        
        PUSHREF(veccompose_rec(LOW(f)));
        PUSHREF(veccompose_rec(HIGH(f)));
        res = ite_rec(replacepair[LEVEL(f)], READREF(1), READREF(2));
        POPREF(2);

        replacecache.set(entry, f, replaceid, res);

        return res;
    }

    int bdd_exist(int r, int var) {
        int res;
        int numReorder = 1;

        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (ISCONST(var)) /* Empty set */
            return r;

        if (quantcache == null) quantcache = new OpCache2(cachesize); // quant_rec()
        if (orcache == null) orcache = new OpCache2(cachesize); // or_rec()
        
        again : for (;;) {
            if (varset2vartable(var) < 0) return bddfalse;
            try {
                INITREF();
                quantid = (var << 3) | CACHEID_EXIST;
                applyop = bddop_or;
                if (numReorder == 0) bdd_disable_reorder();
                res = quant_rec(r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int bdd_forall(int r, int var) {
        int res;
        int numReorder = 1;

        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (var < 2) /* Empty set */
            return r;

        if (quantcache == null) quantcache = new OpCache2(cachesize); // quant_rec()
        if (andcache == null) andcache = new OpCache2(cachesize); // and_rec()
        
        again : for (;;) {
            if (varset2vartable(var) < 0) return bddfalse;
            try {
                INITREF();
                quantid = (var << 3) | CACHEID_FORALL;
                applyop = bddop_and;
                if (numReorder == 0) bdd_disable_reorder();
                res = quant_rec(r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int bdd_unique(int r, int var) {
        int res;
        int numReorder = 1;

        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (var < 2) /* Empty set */
            return r;

        if (quantcache == null) quantcache = new OpCache2(cachesize);
        if (applycache == null) applycache = new OpCache2(cachesize);
        
        again : for (;;) {
            try {
                INITREF();
                quantid = (var << 3) | CACHEID_UNIQUE;
                applyop = bddop_xor;
                if (numReorder == 0) bdd_disable_reorder();
                res = unique_rec(r, var);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int unique_rec(int r, int q) {
        OpCache2Entry entry;
        int res;
        int LEVEL_r, LEVEL_q;

        LEVEL_r = LEVEL(r);
        LEVEL_q = LEVEL(q);
        if (LEVEL_r > LEVEL_q) {
            // Skipped a quantified node, answer is zero.
            return BDDZERO;
        }
        
        if (r < 2 || q < 2)
            return r;
        
        entry = quantcache.lookup(QUANTHASH(r));
        if ((res = quantcache.get(entry, r, quantid)) >= 0) {
            return res;
        }

        if (LEVEL_r == LEVEL_q) {
            PUSHREF(unique_rec(LOW(r), HIGH(q)));
            PUSHREF(unique_rec(HIGH(r), HIGH(q)));
            res = apply_rec(READREF(2), READREF(1));
        } else {
            PUSHREF(unique_rec(LOW(r), q));
            PUSHREF(unique_rec(HIGH(r), q));
            res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
        }

        POPREF(2);

        quantcache.set(entry, r, quantid, res);

        return res;
    }
    
    int quant_rec(int r) {
        OpCache2Entry entry;
        int res;

        if (ISCONST(r) || LEVEL(r) > quantlast) return r;

        entry = quantcache.lookup(QUANTHASH(r));
        if ((res = quantcache.get(entry, r, quantid)) >= 0) {
            return res;
        }

        PUSHREF(quant_rec(LOW(r)));
        PUSHREF(quant_rec(HIGH(r)));

        if (INVARSET(LEVEL(r))) {
            int r2 = READREF(2), r1 = READREF(1);
            switch (applyop) {
            case bddop_and: res = and_rec(r2, r1); break;
            case bddop_or: res = or_rec(r2, r1); break;
            default: res = apply_rec(r2, r1); break;
            }
        } else {
            res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
        }

        POPREF(2);

        quantcache.set(entry, r, quantid, res);

        return res;
    }

    int bdd_restrict(int r, int var) {
        int res;
        int numReorder = 1;

        CHECKa(r, bddfalse);
        CHECKa(var, bddfalse);

        if (var < 2) /* Empty set */
            return r;

        if (quantcache == null) quantcache = new OpCache2(cachesize);
        
        again : for (;;) {
            if (varset2svartable(var) < 0)
                return bddfalse;
            try {
                INITREF();
                quantid = (var << 3) | CACHEID_RESTRICT;
                if (numReorder == 0) bdd_disable_reorder();
                res = restrict_rec(r);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int restrict_rec(int r) {
        OpCache2Entry entry;
        int res;

        if (ISCONST(r) || LEVEL(r) > quantlast) return r;
        entry = quantcache.lookup(RESTRHASH(r, quantid));
        if ((res = quantcache.get(entry, r, quantid)) >= 0) {
            return res;
        }

        if (INSVARSET(LEVEL(r))) {
            if (quantvarset[LEVEL(r)] > 0) {
                res = restrict_rec(HIGH(r));
            } else {
                res = restrict_rec(LOW(r));
            }
        } else {
            PUSHREF(restrict_rec(LOW(r)));
            PUSHREF(restrict_rec(HIGH(r)));
            res = bdd_makenode(LEVEL(r), READREF(2), READREF(1));
            POPREF(2);
        }

        quantcache.set(entry, r, quantid, res);

        return res;
    }

    int bdd_simplify(int f, int d) {
        int res;
        int numReorder = 1;

        CHECKa(f, bddfalse);
        CHECKa(d, bddfalse);

        if (applycache == null) applycache = new OpCache2(cachesize);
        if (orcache == null) orcache = new OpCache2(cachesize);
        
        again : for (;;) {
            try {
                INITREF();
                applyop = bddop_or;
                if (numReorder == 0) bdd_disable_reorder();
                res = simplify_rec(f, d);
                if (numReorder == 0) bdd_enable_reorder();
            } catch (ReorderException x) {
                bdd_checkreorder();
                numReorder--;
                continue again;
            }
            break;
        }

        checkresize();
        return res;
    }

    int simplify_rec(int f, int d) {
        OpCache2Entry entry;
        int res;

        if (ISONE(d) || ISCONST(f)) return f;
        if (d == f) return BDDONE;
        if (ISZERO(d)) return BDDZERO;

        entry = applycache.lookup(APPLYHASH(f, d, bddop_simplify));
        if ((res = applycache.get_sid(entry, f, d, bddop_simplify << NODE_BITS)) >= 0) {
            return res;
        }

        int LEVEL_f = LEVEL(f);
        int LEVEL_d = LEVEL(d);
        if (LEVEL_f == LEVEL_d) {
            int LOW_d = LOW(d);
            int HIGH_d = HIGH(d);
            if (ISZERO(LOW_d))
                res = simplify_rec(HIGH(f), HIGH_d);
            else if (ISZERO(HIGH_d))
                res = simplify_rec(LOW(f), LOW_d);
            else {
                PUSHREF(simplify_rec(LOW(f), LOW_d));
                PUSHREF(simplify_rec(HIGH(f), HIGH_d));
                res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
                POPREF(2);
            }
        } else if (LEVEL_f < LEVEL_d) {
            PUSHREF(simplify_rec(LOW(f), d));
            PUSHREF(simplify_rec(HIGH(f), d));
            res = bdd_makenode(LEVEL_f, READREF(2), READREF(1));
            POPREF(2);
        } else /* LEVEL_d < LEVEL_f */ {
            PUSHREF(or_rec(LOW(d), HIGH(d))); /* Exist quant */
            res = simplify_rec(f, READREF(1));
            POPREF(1);
        }

        applycache.set_sid(entry, f, d, bddop_simplify << NODE_BITS, res);

        return res;
    }

    int bdd_support(int r) {
        int n;
        int res = 1;

        CHECKa(r, bddfalse);

        if (ISCONST(r)) return bddtrue;

        /* On-demand allocation of support set */
        if (supportSet == null || supportSet.length < bddvarnum) {
            supportSet = new int[bddvarnum];
            supportID = 0;
        }

        /* Update global variables used to speed up bdd_support()
         * - instead of always memsetting support to zero, we use
         *   a change counter.
         * - and instead of reading the whole array afterwards, we just
         *   look from 'min' to 'max' used BDD variables.
         */
        if (supportID == 0x0FFFFFFF) {
            /* We probably don't get here -- but let's just be sure */
            for (int i = 0; i < bddvarnum; ++i)
                supportSet[i] = 0;
            supportID = 0;
        }
        ++supportID;
        supportMin = LEVEL(r);
        supportMax = supportMin;

        support_rec(r);
        bdd_unmark(r);

        bdd_disable_reorder();

        for (n = supportMax; n >= supportMin; --n)
            if (supportSet[n] == supportID) {
                int tmp;
                bdd_addref(res);
                tmp = bdd_makenode(n, 0, res);
                bdd_delref(res);
                res = tmp;
            }

        bdd_enable_reorder();

        return res;
    }

    void support_rec(int r) {
        if (ISCONST(r) ||
            MARKED(r))
            return;

        supportSet[LEVEL(r)] = supportID;
        if (LEVEL(r) > supportMax) supportMax = LEVEL(r);
        SETMARK(r);

        support_rec(LOW(r));
        support_rec(HIGH(r));
    }

    int bdd_satone(int r) {
        int res;

        CHECKa(r, bddfalse);
        if (ISCONST(r)) return r;

        bdd_disable_reorder();

        INITREF();
        res = satone_rec(r);

        bdd_enable_reorder();

        checkresize();
        return res;
    }

    int satone_rec(int r) {
        if (ISCONST(r)) return r;

        if (ISZERO(LOW(r))) {
            int res = satone_rec(HIGH(r));
            int m = bdd_makenode(LEVEL(r), BDDZERO, res);
            PUSHREF(m);
            return m;
        } else {
            int res = satone_rec(LOW(r));
            int m = bdd_makenode(LEVEL(r), res, BDDZERO);
            PUSHREF(m);
            return m;
        }
    }

    int bdd_satoneset(int r, int var, boolean pol) {
        int res;

        CHECKa(r, bddfalse);
        if (ISZERO(r)) return r;

        bdd_disable_reorder();

        INITREF();
        satPolarity = pol;
        res = satoneset_rec(r, var);

        bdd_enable_reorder();

        checkresize();
        return res;
    }

    int satoneset_rec(int r, int var) {
        if (ISCONST(r) && ISCONST(var)) return r;

        int LEVEL_r = LEVEL(r);
        int LEVEL_var = LEVEL(var);
        if (LEVEL_r < LEVEL_var) {
            int LOW_r = LOW(r);
            if (ISZERO(LOW_r)) {
                int res = satoneset_rec(HIGH(r), var);
                int m = bdd_makenode(LEVEL_r, BDDZERO, res);
                PUSHREF(m);
                return m;
            } else {
                int res = satoneset_rec(LOW_r, var);
                int m = bdd_makenode(LEVEL_r, res, BDDZERO);
                PUSHREF(m);
                return m;
            }
        } else if (LEVEL_var < LEVEL_r) {
            int res = satoneset_rec(r, HIGH(var));
            if (satPolarity) {
                int m = bdd_makenode(LEVEL_var, BDDZERO, res);
                PUSHREF(m);
                return m;
            } else {
                int m = bdd_makenode(LEVEL_var, res, BDDZERO);
                PUSHREF(m);
                return m;
            }
        } else /* LEVEL_r == LEVEL_var */ {
            int LOW_r = LOW(r);
            int HIGH_var = HIGH(var);
            if (ISZERO(LOW_r)) {
                int res = satoneset_rec(HIGH(r), HIGH_var);
                int m = bdd_makenode(LEVEL_r, BDDZERO, res);
                PUSHREF(m);
                return m;
            } else {
                int res = satoneset_rec(LOW_r, HIGH_var);
                int m = bdd_makenode(LEVEL_r, res, BDDZERO);
                PUSHREF(m);
                return m;
            }
        }

    }

    int bdd_fullsatone(int r) {
        int res;
        int v;

        CHECKa(r, bddfalse);
        if (ISZERO(r)) return 0;

        bdd_disable_reorder();

        INITREF();
        res = fullsatone_rec(r);

        for (v = LEVEL(r) - 1; v >= 0; v--) {
            res = PUSHREF(bdd_makenode(v, res, 0));
        }

        bdd_enable_reorder();

        checkresize();
        return res;
    }

    int fullsatone_rec(int r) {
        if (ISCONST(r)) return r;

        int LOW_r = LOW(r);
        int LEVEL_r = LEVEL(r);
        if (LOW_r != 0) {
            int res = fullsatone_rec(LOW_r);
            for (int v = LEVEL(LOW_r) - 1; v > LEVEL_r; v--) {
                res = PUSHREF(bdd_makenode(v, res, 0));
            }
            return PUSHREF(bdd_makenode(LEVEL_r, res, 0));
        } else {
            int HIGH_r = HIGH(r);
            int res = fullsatone_rec(HIGH_r);
            for (int v = LEVEL(HIGH_r) - 1; v > LEVEL_r; v--) {
                res = PUSHREF(bdd_makenode(v, res, 0));
            }
            return PUSHREF(bdd_makenode(LEVEL_r, 0, res));
        }
    }

    void bdd_gbc_rehash() {
        int n;

        bddfreepos = 0;
        bddfreenum = 0;

        for (n = bddnodesize - 1; n >= 2; n--) {
            int LOW_n = LOW(n);
            if (LOW_n != INVALID_BDD) {
                int hash2 = NODEHASH(LEVEL(n), LOW_n, HIGH(n));
                SETNEXT(n, HASH(hash2));
                SETHASH(hash2, n);
            } else {
                SETNEXT(n, bddfreepos);
                bddfreepos = n;
                bddfreenum++;
            }
        }
    }

    final long clock() {
        return System.currentTimeMillis();
    }

    final void INITREF() {
        bddrefstacktop = 0;
    }
    final int PUSHREF(int a) {
        bddrefstack[bddrefstacktop++] = a;
        return a;
    }
    final int READREF(int a) {
        return bddrefstack[bddrefstacktop - a];
    }
    final void POPREF(int a) {
        bddrefstacktop -= a;
    }

    int bdd_nodecount(int r) {
        int[] num = new int[1];

        CHECK(r);

        bdd_markcount(r, num);
        bdd_unmark(r);

        return num[0];
    }

    int bdd_anodecount(int[] r) {
        int n;
        int[] cou = new int[1];

        for (n = 0; n < r.length; n++)
            bdd_markcount(r[n], cou);

        for (n = 0; n < r.length; n++)
            bdd_unmark(r[n]);

        return cou[0];
    }

    int[] bdd_varprofile(int r) {
        CHECK(r);

        int[] varprofile = new int[bddvarnum];
        varprofile_rec(r, varprofile);
        bdd_unmark(r);
        return varprofile;
    }

    void varprofile_rec(int r, int[] varprofile) {

        if (ISCONST(r) || MARKED(r)) return;

        varprofile[bddlevel2var[LEVEL(r)]]++;
        SETMARK(r);

        varprofile_rec(LOW(r), varprofile);
        varprofile_rec(HIGH(r), varprofile);
    }

    double bdd_pathcount(int r) {
        CHECK(r);

        miscid = CACHEID_PATHCOU << NODE_BITS;

        if (countcache == null) countcache = new OpCacheD(cachesize);
        
        return bdd_pathcount_rec(r);
    }

    double bdd_pathcount_rec(int r) {
        OpCacheDEntry entry;
        double size;

        if (ISZERO(r)) return 0f;
        if (ISONE(r)) return 1f;

        entry = countcache.lookup(PATHCOUHASH(r));
        if ((size = countcache.get_sid(entry, r, miscid)) >= 0) {
            return size;
        }

        size = bdd_pathcount_rec(LOW(r)) + bdd_pathcount_rec(HIGH(r));

        countcache.set_sid(entry, r, miscid, size);

        return size;
    }

    double bdd_satcount(int r) {
        double size = 1;

        CHECK(r);

        if (countcache == null) countcache = new OpCacheD(cachesize);
        
        miscid = CACHEID_SATCOU << NODE_BITS;
        size = Math.pow(2.0, (double) LEVEL(r));

        return size * satcount_rec(r);
    }

    double bdd_satcountset(int r, int varset) {
        double unused = bddvarnum;
        int n;

        if (ISCONST(varset) || ISZERO(r)) /* empty set */
            return 0.0;

        for (n = varset; !ISCONST(n); n = HIGH(n))
            unused--;

        unused = bdd_satcount(r) / Math.pow(2.0, unused);

        return unused >= 1.0 ? unused : 1.0;
    }

    double satcount_rec(int r) {
        OpCacheDEntry entry;
        double size, s;

        if (ISCONST(r)) return r;

        entry = countcache.lookup(SATCOUHASH(r));
        if ((size = countcache.get_sid(entry, r, miscid)) >= 0) {
            return size;
        }

        size = 0;
        s = 1;

        int LEVEL_r = LEVEL(r);
        int LOW_r = LOW(r);
        int HIGH_r = HIGH(r);
        s *= Math.pow(2.0, (float) (LEVEL(LOW_r) - LEVEL_r - 1));
        size += s * satcount_rec(LOW_r);

        s = 1;
        s *= Math.pow(2.0, (float) (LEVEL(HIGH_r) - LEVEL_r - 1));
        size += s * satcount_rec(HIGH_r);

        countcache.set_sid(entry, r, miscid, size);

        return size;
    }

    void bdd_gbc() {
        int r;
        int n;
        long c2, c1 = clock();

        gcstats.nodes = bddnodesize;
        gcstats.freenodes = bddfreenum;
        gcstats.time = 0;
        gcstats.sumtime = gbcclock;
        gcstats.num = gbcollectnum;
        gbc_handler(true, gcstats);

        // Handle nodes that were marked as free by finalizer.
        handleDeferredFree();
        
        for (r = 0; r < bddrefstacktop; r++)
            bdd_mark(bddrefstack[r]);

        for (n = 0; n < bddnodesize; n++) {
            if (HASREF(n))
                bdd_mark(n);
            SETHASH(n, 0);
        }

        bddfreepos = 0;
        bddfreenum = 0;

        for (n = bddnodesize - 1; n >= 2; n--) {
            int LOW_n = LOW(n);
            if (MARKED(n) && LOW_n != INVALID_BDD) {
                int hash2;
                UNMARK(n);
                hash2 = NODEHASH(LEVEL(n), LOW_n, HIGH(n));
                SETNEXT(n, HASH(hash2));
                SETHASH(hash2, n);
            } else {
                SETLOW(n, INVALID_BDD);
                SETNEXT(n, bddfreepos);
                bddfreepos = n;
                bddfreenum++;
            }
        }

        if (FLUSH_CACHE_ON_GC) {
            bdd_operator_reset();
        } else {
            bdd_operator_clean();
        }

        c2 = clock();
        gbcclock += c2 - c1;
        gbcollectnum++;

        gcstats.nodes = bddnodesize;
        gcstats.freenodes = bddfreenum;
        gcstats.time = c2 - c1;
        gcstats.sumtime = gbcclock;
        gcstats.num = gbcollectnum;
        gbc_handler(false, gcstats);
        
        //validate_all();
    }

    int bdd_addref(int root) {
        if (root == INVALID_BDD)
            bdd_error(BDD_BREAK); /* distinctive */
        if (root < 2 || !bddrunning)
            return root;
        if (root >= bddnodesize)
            return bdd_error(BDD_ILLBDD);
        if (LOW(root) == INVALID_BDD)
            return bdd_error(BDD_ILLBDD);

        INCREF(root);
        if (false) System.out.println("INCREF("+root+") = "+GETREF(root));
        return root;
    }

    int bdd_delref(int root) {
        if (root == INVALID_BDD)
            bdd_error(BDD_BREAK); /* distinctive */
        if (root < 2 || !bddrunning)
            return root;
        if (root >= bddnodesize)
            return bdd_error(BDD_ILLBDD);
        if (LOW(root) == INVALID_BDD)
            return bdd_error(BDD_ILLBDD);

        /* if the following line is present, fails there much earlier */
        if (!HASREF(root))
            bdd_error(BDD_BREAK); /* distinctive */

        DECREF(root);
        if (false) System.out.println("DECREF("+root+") = "+GETREF(root));
        return root;
    }

    void bdd_mark(int i) {
        
        if (ISCONST(i) || MARKED(i))
            return;

        SETMARK(i);

        bdd_mark(LOW(i));
        bdd_mark(HIGH(i));
    }

    void bdd_markcount(int i, int[] cou) {

        if (ISCONST(i) || MARKED(i))
            return;

        SETMARK(i);
        cou[0] += 1;

        bdd_markcount(LOW(i), cou);
        bdd_markcount(HIGH(i), cou);
    }

    void bdd_unmark(int i) {

        if (ISCONST(i) || !MARKED(i))
            return;
        
        UNMARK(i);

        bdd_unmark(LOW(i));
        bdd_unmark(HIGH(i));
    }

    int bdd_makenode(int level, int low, int high) {
        int hash2;
        int res;

        if (CACHESTATS > 0) cachestats.uniqueAccess++;

        /* check whether childs are equal */
        if (low == high) return low;

        /* Try to find an existing node of this kind */
        hash2 = NODEHASH(level, low, high);
        res = HASH(hash2);

        if (res != 0) {
            int m1 = (level << LEV_LPOS) | low;
            int m2 = ((level << (LEV_HPOS-LEV_LBITS)) & LEV_HMASK) | high;
            do {
                if (bddnodes[res*__node_size + offset__low] == m1 &&
                    bddnodes[res*__node_size + offset__high] == m2) {
                    if (CACHESTATS > 0) cachestats.uniqueHit++;
                    return res;
                }
                res = NEXT(res);
                if (CACHESTATS > 0) cachestats.uniqueChain++;
            } while (res != 0);
        }

        /* No existing node => build one */
        if (CACHESTATS > 0) cachestats.uniqueMiss++;

        /* Any free nodes to use ? */
        if (bddfreepos == 0) {
            if (bdderrorcond != 0) return 0;

            /* Try to allocate more nodes */
            bdd_gbc();

            if ((bddnodesize-bddfreenum) >= usednodes_nextreorder &&
                bdd_reorder_ready()) {
                throw new ReorderException();
            }

            if ((bddfreenum * 100) / bddnodesize <= minfreenodes) {
                bdd_noderesize(true);
                hash2 = NODEHASH(level, low, high);
            }

            /* Panic if that is not possible */
            if (bddfreepos == 0) {
                bdderrorcond = Math.abs(BDD_NODENUM);
                bdd_error(BDD_NODENUM);
                return 0;
            }
        }

        /* Build new node */
        res = bddfreepos;
        bddfreepos = NEXT(bddfreepos);
        bddfreenum--;
        bddproduced++;

        INIT_NODE(res, level, low, high, HASH(hash2));
        SETHASH(hash2, res);

        return res;
    }

    int bdd_noderesize(boolean doRehash) {
        int oldsize = bddnodesize;
        int newsize = bddnodesize;

        if (bddmaxnodesize > 0) {
            if (newsize >= bddmaxnodesize)
                return -1;
        }

        if (increasefactor > 0) {
            newsize += (int)(newsize * increasefactor);
        } else {
            newsize = newsize << 1;
        }

        if (bddmaxnodeincrease > 0) {
            if (newsize > oldsize + bddmaxnodeincrease)
                newsize = oldsize + bddmaxnodeincrease;
        }

        if (bddmaxnodesize > 0) {
            if (newsize > bddmaxnodesize)
                newsize = bddmaxnodesize;
        }

        return doResize(doRehash, oldsize, newsize);
    }
    
    int bdd_setallocnum(int size) {
        int old = bddnodesize;
        doResize(true, old, size);
        return old;
    }
    
    int doResize(boolean doRehash, int oldsize, int newsize) {
        
        newsize = bdd_prime_lte(newsize);

        if (oldsize > newsize) return 0;
        if (newsize >= NODE_MASK) newsize = NODE_MASK-1;
        
        resize_handler(oldsize, newsize);
        
        int[] newnodes;
        int n;
        newnodes = new int[newsize*__node_size];
        System.arraycopy(bddnodes, 0, newnodes, 0, bddnodes.length);
        bddnodes = newnodes;
        bddnodesize = newsize;

        if (doRehash)
            for (n = 0; n < oldsize; n++)
                SETHASH(n, 0);

        for (n = oldsize; n < bddnodesize; n++) {
            SETLOW(n, INVALID_BDD);
            SETNEXT(n, n+1);
        }
        SETNEXT(bddnodesize-1, bddfreepos);
        bddfreepos = oldsize;
        bddfreenum += bddnodesize - oldsize;

        if (doRehash)
            bdd_gbc_rehash();

        bddresized = true;

        return 0;
    }

    void bdd_init(int initnodesize, int cs) {
        int n;

        if (bddrunning)
            throw new JavaBDDException(BDD_RUNNING);

        bddnodesize = bdd_prime_gte(initnodesize);
        cachesize = bdd_prime_gte(cs);

        bddnodes = new int[bddnodesize*__node_size];

        bddresized = false;

        for (n = 0; n < bddnodesize; n++) {
            SETLOW(n, INVALID_BDD);
            SETNEXT(n, n+1);
        }
        SETNEXT(bddnodesize-1, 0);

        SETMAXREF(0);
        SETMAXREF(1);
        SETLOW(0, 0); SETHIGH(0, 0);
        SETLOW(1, 1); SETHIGH(1, 1);

        bdd_operator_init(cachesize);

        bddfreepos = 2;
        bddfreenum = bddnodesize - 2;
        bddrunning = true;
        bddvarnum = 0;
        gbcollectnum = 0;
        gbcclock = 0;
        usednodes_nextreorder = bddnodesize;
        bddmaxnodeincrease = DEFAULTMAXNODEINC;

        bdderrorcond = 0;

        bdd_pairs_init();
        bdd_reorder_init();
    }

    /* Hash value modifiers to distinguish between entries in misccache */
    static final int CACHEID_CONSTRAIN = 0x0;
    static final int CACHEID_SATCOU = 0x2;
    static final int CACHEID_SATCOULN = 0x3;
    static final int CACHEID_PATHCOU = 0x4;

    /* Hash value modifiers for replace/veccompose */
    static final int CACHEID_REPLACE = 0x0;
    static final int CACHEID_VECCOMPOSE = 0x1;

    /* Hash value modifiers for quantification */
    static final int CACHEID_EXIST = 0x0;
    static final int CACHEID_FORALL = 0x1;
    static final int CACHEID_UNIQUE = 0x2;
    static final int CACHEID_APPEX = 0x3;
    static final int CACHEID_APPAL = 0x4;
    static final int CACHEID_APPUN = 0x5;
    static final int CACHEID_RESTRICT = 0x6;
    static final int CACHEID_COMPOSE = 0x7;

    /* Operator results - entry = left<<1 | right  (left,right in {0,1}) */
    static int oprres[][] =
        { { 0, 0, 0, 1 }, /* and                       ( & )         */ {
            0, 1, 1, 0 }, /* xor                       ( ^ )         */ {
            0, 1, 1, 1 }, /* or                        ( | )         */ {
            1, 1, 1, 0 }, /* nand                                    */ {
            1, 0, 0, 0 }, /* nor                                     */ {
            1, 1, 0, 1 }, /* implication               ( >> )        */ {
            1, 0, 0, 1 }, /* bi-implication                          */ {
            0, 0, 1, 0 }, /* difference /greater than  ( - ) ( > )   */ {
            0, 1, 0, 0 }, /* less than                 ( < )         */ {
            1, 0, 1, 1 }, /* inverse implication       ( << )        */ {
            1, 1, 0, 0 } /* not                       ( ! )         */
    };

    int applyop; /* Current operator for apply */
    int appexop; /* Current operator for appex */
    int appexid; /* Current cache id for appex */
    int quantid; /* Current cache id for quantifications */
    int[] quantvarset; /* Current variable set for quant. */
    int quantvarsetID; /* Current id used in quantvarset */
    int quantlast; /* Current last variable to be quant. */
    int replaceid; /* Current cache id for replace */
    int[] replacepair; /* Current replace pair */
    int replacelast; /* Current last var. level to replace */
    int composelevel; /* Current variable used for compose */
    int miscid; /* Current cache id for other results */
    int supportID; /* Current ID (true value) for support */
    int supportMin; /* Min. used level in support calc. */
    int supportMax; /* Max. used level in support calc. */
    int[] supportSet; /* The found support set */
    int cacheratio;
    boolean satPolarity;

    OpCache1 singlecache;  /* not(), exist(), forAll() */
    OpCache2 replacecache; /* replace(), veccompose() */
    OpCache2 andcache;     /* and() */
    OpCache2 orcache;      /* or() */
    OpCache2 applycache;   /* xor(), imp(), etc. */
    OpCache2 quantcache;   /* exist(), forall(), unique(), restrict() */
    OpCache3 appexcache;   /* appex(), appall(), appuni(), constrain(), compose() */
    OpCache4 appex3cache;  /* relprod3() */
    OpCache3 itecache;     /* ite() */
    OpCache2 misccache;    /* other functions */
    OpCacheD countcache;   /* satcount(), pathcount() */
    
    void bdd_operator_init(int cachesize) {
        quantvarsetID = 0;
        quantvarset = null;
        cacheratio = 0;
        supportSet = null;
    }

    void bdd_operator_done() {
        quantvarset = null;
        supportSet = null;
        
        singlecache = null;
        replacecache = null;
        andcache = null;
        orcache = null;
        applycache = null;
        quantcache = null;
        appexcache = null;
        appex3cache = null;
        itecache = null;
        countcache = null;
        misccache = null;
    }

    void bdd_operator_reset() {
        if (singlecache != null)
            singlecache.reset();
        if (replacecache != null)
            replacecache.reset();
        if (andcache != null)
            andcache.reset();
        if (orcache != null)
            orcache.reset();
        if (applycache != null)
            applycache.reset();
        if (quantcache != null)
            quantcache.reset();
        if (appexcache != null)
            appexcache.reset();
        if (appex3cache != null)
            appex3cache.reset();
        if (itecache != null)
            itecache.reset();
        if (countcache != null)
            countcache.reset();
        if (misccache != null)
            misccache.reset();
    }

    void bdd_operator_clean() {
        if (singlecache != null)
            singlecache.clean();
        if (replacecache != null)
            replacecache.clean();
        if (andcache != null)
            andcache.clean();
        if (orcache != null)
            orcache.clean();
        if (applycache != null)
            applycache.clean();
        if (quantcache != null)
            quantcache.clean();
        if (appexcache != null)
            appexcache.clean();
        if (appex3cache != null)
            appex3cache.reset();
        if (itecache != null)
            itecache.clean();
        if (countcache != null)
            countcache.clean();
        if (misccache != null)
            misccache.clean();
    }
    
    void bdd_operator_varresize() {
        quantvarset = new int[bddvarnum];
        quantvarsetID = 0;
        if (countcache != null) countcache.reset();
    }

    int bdd_setcachesize(int newcachesize) {
        int old = cachesize;
        cachesize = bdd_prime_lte(cachesize);
        singlecache = null;
        replacecache = null;
        andcache = null;
        orcache = null;
        applycache = null;
        quantcache = null;
        appexcache = null;
        appex3cache = null;
        itecache = null;
        countcache = null;
        misccache = null;
        return old;
    }
    
    void bdd_operator_noderesize() {
        if (cacheratio > 0) {
            cachesize = bddnodesize / cacheratio;
            singlecache = null;
            replacecache = null;
            andcache = null;
            orcache = null;
            applycache = null;
            quantcache = null;
            appexcache = null;
            appex3cache = null;
            itecache = null;
            countcache = null;
            misccache = null;
        }
    }

    void bdd_setpair(bddPair pair, int oldvar, int newvar) {
        if (pair == null) return;

        if (oldvar < 0 || oldvar > bddvarnum - 1)
            bdd_error(BDD_VAR);
        if (newvar < 0 || newvar > bddvarnum - 1)
            bdd_error(BDD_VAR);

        bdd_delref(pair.result[bddvar2level[oldvar]]);
        pair.result[bddvar2level[oldvar]] = bdd_ithvar(newvar);
        pair.id = update_pairsid();

        if (bddvar2level[oldvar] > pair.last)
            pair.last = bddvar2level[oldvar];

    }

    void bdd_setbddpair(bddPair pair, int oldvar, int newvar) {
        int oldlevel;

        if (pair == null) return;

        CHECK(newvar);
        if (oldvar < 0 || oldvar >= bddvarnum)
            bdd_error(BDD_VAR);
        oldlevel = bddvar2level[oldvar];

        bdd_delref(pair.result[oldlevel]);
        pair.result[oldlevel] = bdd_addref(newvar);
        pair.id = update_pairsid();

        if (oldlevel > pair.last)
            pair.last = oldlevel;

    }

    void bdd_resetpair(bddPair p) {
        for (int n = 0; n < bddvarnum; n++) {
            bdd_delref(p.result[n]);
            p.result[n] = bdd_ithvar(n);
        }
        p.last = 0;
    }

    bddPair pairs; /* List of all replacement pairs in use */
    int pairsid; /* Pair identifier */

    /*************************************************************************
    *************************************************************************/

    void bdd_pairs_init() {
        pairsid = 0;
        pairs = null;
    }

    void bdd_pairs_done() {
        bddPair p = pairs;
        int n;

        while (p != null) {
            bddPair next = p.next;
            for (n = 0; n < bddvarnum; n++)
                bdd_delref(p.result[n]);
            p.result = null;
            p = next;
        }
    }

    int update_pairsid() {
        pairsid++;

        if (pairsid == MAX_PAIRSID) {
            pairsid = 0;
            for (bddPair p = pairs; p != null; p = p.next)
                p.id = pairsid++;
            if (pairsid >= MAX_PAIRSID)
                throw new BDDException("Too many pairs!");
            if (replacecache != null) replacecache.reset();
        }

        return pairsid;
    }

    void bdd_register_pair(bddPair p) {
        p.next = pairs;
        pairs = p;
    }

    void bdd_pairs_vardown(int level) {
        bddPair p;

        for (p = pairs; p != null; p = p.next) {
            int tmp;

            tmp = p.result[level];
            p.result[level] = p.result[level + 1];
            p.result[level + 1] = tmp;

            if (p.last == level)
                p.last++;
        }
    }

    int bdd_pairs_resize(int oldsize, int newsize) {
        bddPair p;
        int n;

        for (p = pairs; p != null; p = p.next) {
            int[] new_result = new int[newsize];
            System.arraycopy(p.result, 0, new_result, 0, oldsize);
            p.result = new_result;

            for (n = oldsize; n < newsize; n++)
                p.result[n] = bdd_ithvar(bddlevel2var[n]);
        }

        return 0;
    }

    void bdd_disable_reorder() {
        reorderdisabled = 1;
    }
    void bdd_enable_reorder() {
        reorderdisabled = 0;
    }
    void bdd_checkreorder() {
        bdd_reorder_auto();

        /* Do not reorder before twice as many nodes have been used */
        usednodes_nextreorder = 2 * (bddnodesize - bddfreenum);

        /* And if very little was gained this time (< 20%) then wait until
         * even more nodes (upto twice as many again) have been used */
        if (bdd_reorder_gain() < 20)
            usednodes_nextreorder
                += (usednodes_nextreorder * (20 - bdd_reorder_gain())) / 20;
    }

    boolean bdd_reorder_ready() {
        if ((bddreordermethod == BDD_REORDER_NONE)
            || (vartree == null)
            || (bddreordertimes == 0)
            || (reorderdisabled != 0))
            return false;
        return true;
    }

    void bdd_reorder(int method) {
        BddTree top;
        int savemethod = bddreordermethod;
        int savetimes = bddreordertimes;

        bddreordermethod = method;
        bddreordertimes = 1;

        if ((top = bddtree_new(-1)) != null) {
            if (reorder_init() >= 0) {
                
                usednum_before = bddnodesize - bddfreenum;
        
                top.first = 0;
                top.last = bdd_varnum() - 1;
                top.fixed = false;
                top.next = null;
                top.nextlevel = vartree;
        
                reorder_block(top, method);
                vartree = top.nextlevel;
        
                usednum_after = bddnodesize - bddfreenum;
        
                reorder_done();
                bddreordermethod = savemethod;
                bddreordertimes = savetimes;
            }
        }
    }

    BddTree bddtree_new(int id) {
        BddTree t = new BddTree();

        t.first = t.last = -1;
        t.fixed = true;
        t.next = t.prev = t.nextlevel = null;
        t.seq = null;
        t.id = id;
        return t;
    }

    BddTree reorder_block(BddTree t, int method) {
        BddTree dis;

        if (t == null)
            return null;

        if (!t.fixed /*BDD_REORDER_FREE*/
            && t.nextlevel != null) {
            switch (method) {
                case BDD_REORDER_WIN2 :
                    t.nextlevel = reorder_win2(t.nextlevel);
                    break;
                case BDD_REORDER_WIN2ITE :
                    t.nextlevel = reorder_win2ite(t.nextlevel);
                    break;
                case BDD_REORDER_SIFT :
                    t.nextlevel = reorder_sift(t.nextlevel);
                    break;
                case BDD_REORDER_SIFTITE :
                    t.nextlevel = reorder_siftite(t.nextlevel);
                    break;
                case BDD_REORDER_WIN3 :
                    t.nextlevel = reorder_win3(t.nextlevel);
                    break;
                case BDD_REORDER_WIN3ITE :
                    t.nextlevel = reorder_win3ite(t.nextlevel);
                    break;
                case BDD_REORDER_RANDOM :
                    t.nextlevel = reorder_random(t.nextlevel);
                    break;
            }
        }

        for (dis = t.nextlevel; dis != null; dis = dis.next)
            reorder_block(dis, method);

        if (t.seq != null) {
            varseq_qsort(t.seq, 0, t.last-t.first + 1);
        }

        return t;
    }
    
    // due to Akihiko Tozawa
    void varseq_qsort(int[] target, int from, int to) {
        
        int x, i, j;
        
        switch (to - from) {
            case 0 :
                return;
    
            case 1 :
                return;
    
            case 2 :
                if (bddvar2level[target[from]] <= bddvar2level[target[from + 1]])
                    return;
                else {
                    x = target[from];
                    target[from] = target[from + 1];
                    target[from + 1] = x;
                }
                return;
        }
    
        int r = target[from];
        int s = target[(from + to) / 2];
        int t = target[to - 1];
    
        if (bddvar2level[r] <= bddvar2level[s]) {
            if (bddvar2level[s] <= bddvar2level[t]) {
            } else if (bddvar2level[r] <= bddvar2level[t]) {
                target[to - 1] = s;
                target[(from + to) / 2] = t;
            } else {
                target[to - 1] = s;
                target[from] = t;
                target[(from + to) / 2] = r;
            }
        } else {
            if (bddvar2level[r] <= bddvar2level[t]) {
                target[(from + to) / 2] = r;
                target[from] = s;
            } else if (bddvar2level[s] <= bddvar2level[t]) {
                target[to - 1] = r;
                target[(from + to) / 2] = t;
                target[from] = s;
            } else {
                target[to - 1] = r;
                target[from] = t;
            }
        }
        
        int mid = target[(from + to) / 2];
        
        for (i = from + 1, j = to - 1; i + 1 != j;) {
            if (target[i] == mid) {
                target[i] = target[i + 1];
                target[i + 1] = mid;
            }
            
            x = target[i];
            
            if (x <= mid)
                i++;
            else {
                x = target[--j];
                target[j] = target[i];
                target[i] = x;
            }
        }
    
        varseq_qsort(target, from, i);
        varseq_qsort(target, i + 1, to);
    }
         
    BddTree reorder_win2(BddTree t) {
        BddTree dis = t, first = t;

        if (t == null)
            return t;

        if (verbose > 1) {
            System.out.println("Win2 start: " + reorder_nodenum() + " nodes");
            System.out.flush();
        }

        while (dis.next != null) {
            int best = reorder_nodenum();
            blockdown(dis);

            if (best < reorder_nodenum()) {
                blockdown(dis.prev);
                dis = dis.next;
            } else if (first == dis)
                first = dis.prev;

            if (verbose > 1) {
                System.out.print(".");
                System.out.flush();
            }
        }

        if (verbose > 1) {
            System.out.println();
            System.out.println("Win2 end: " + reorder_nodenum() + " nodes");
            System.out.flush();
        }

        return first;
    }

    BddTree reorder_win3(BddTree t) {
        BddTree dis = t, first = t;

        if (t == null)
            return t;

        if (verbose > 1) {
            System.out.println("Win3 start: " + reorder_nodenum() + " nodes");
            System.out.flush();
        }

        while (dis.next != null) {
            BddTree[] f = new BddTree[1];
            f[0] = first;
            dis = reorder_swapwin3(dis, f);
            first = f[0];

            if (verbose > 1) {
                System.out.print(".");
                System.out.flush();
            }
        }

        if (verbose > 1) {
            System.out.println();
            System.out.println("Win3 end: " + reorder_nodenum() + " nodes");
            System.out.flush();
        }

        return first;
    }

    BddTree reorder_win3ite(BddTree t) {
        BddTree dis = t, first = t;
        int lastsize;

        if (t == null)
            return t;

        if (verbose > 1)
            System.out.println(
                "Win3ite start: " + reorder_nodenum() + " nodes");

        do {
            lastsize = reorder_nodenum();
            dis = first;

            while (dis.next != null && dis.next.next != null) {
                BddTree[] f = new BddTree[1];
                f[0] = first;
                dis = reorder_swapwin3(dis, f);
                first = f[0];

                if (verbose > 1) {
                    System.out.print(".");
                    System.out.flush();
                }
            }

            if (verbose > 1)
                System.out.println(" " + reorder_nodenum() + " nodes");
        }
        while (reorder_nodenum() != lastsize);

        if (verbose > 1)
            System.out.println("Win3ite end: " + reorder_nodenum() + " nodes");

        return first;
    }

    BddTree reorder_swapwin3(BddTree dis, BddTree[] first) {
        boolean setfirst = dis.prev == null;
        BddTree next = dis;
        int best = reorder_nodenum();

        if (dis.next.next == null) /* Only two blocks left => win2 swap */ {
            blockdown(dis.prev);

            if (best < reorder_nodenum()) {
                blockdown(dis.prev);
                next = dis.next;
            } else {
                next = dis;
                if (setfirst)
                    first[0] = dis.prev;
            }
        } else /* Real win3 swap */ {
            int pos = 0;
            blockdown(dis); /* B A* C (4) */
            pos++;
            if (best > reorder_nodenum()) {
                pos = 0;
                best = reorder_nodenum();
            }

            blockdown(dis); /* B C A* (3) */
            pos++;
            if (best > reorder_nodenum()) {
                pos = 0;
                best = reorder_nodenum();
            }

            dis = dis.prev.prev;
            blockdown(dis); /* C B* A (2) */
            pos++;
            if (best > reorder_nodenum()) {
                pos = 0;
                best = reorder_nodenum();
            }

            blockdown(dis); /* C A B* (1) */
            pos++;
            if (best > reorder_nodenum()) {
                pos = 0;
                best = reorder_nodenum();
            }

            dis = dis.prev.prev;
            blockdown(dis); /* A C* B (0)*/
            pos++;
            if (best > reorder_nodenum()) {
                pos = 0;
                best = reorder_nodenum();
            }

            if (pos >= 1) /* A C B -> C A* B */ {
                dis = dis.prev;
                blockdown(dis);
                next = dis;
                if (setfirst)
                    first[0] = dis.prev;
            }

            if (pos >= 2) /* C A B -> C B A* */ {
                blockdown(dis);
                next = dis.prev;
                if (setfirst)
                    first[0] = dis.prev.prev;
            }

            if (pos >= 3) /* C B A -> B C* A */ {
                dis = dis.prev.prev;
                blockdown(dis);
                next = dis;
                if (setfirst)
                    first[0] = dis.prev;
            }

            if (pos >= 4) /* B C A -> B A C* */ {
                blockdown(dis);
                next = dis.prev;
                if (setfirst)
                    first[0] = dis.prev.prev;
            }

            if (pos >= 5) /* B A C -> A B* C */ {
                dis = dis.prev.prev;
                blockdown(dis);
                next = dis;
                if (setfirst)
                    first[0] = dis.prev;
            }
        }

        return next;
    }

    BddTree reorder_sift_seq(BddTree t, BddTree seq[], int num) {
        BddTree dis;
        int n;

        if (t == null)
            return t;

        for (n = 0; n < num; n++) {
            long c2, c1 = clock();

            if (verbose > 1) {
                System.out.print("Sift ");
                //if (reorder_filehandler)
                //   reorder_filehandler(stdout, seq[n].id);
                //else
                System.out.print(seq[n].id);
                System.out.print(": ");
            }

            reorder_sift_bestpos(seq[n], num / 2);

            if (verbose > 1) {
                System.out.println();
                System.out.print("> " + reorder_nodenum() + " nodes");
            }

            c2 = clock();
            if (verbose > 1)
                System.out.println(
                    " (" + (float) (c2 - c1) / (float) 1000 + " sec)\n");
        }

        /* Find first block */
        for (dis = t; dis.prev != null; dis = dis.prev)
            /* nil */;

        return dis;
    }

    void reorder_sift_bestpos(BddTree blk, int middlePos) {
        int best = reorder_nodenum();
        int maxAllowed;
        int bestpos = 0;
        boolean dirIsUp = true;
        int n;

        if (bddmaxnodesize > 0)
            maxAllowed =
                MIN(best / 5 + best, bddmaxnodesize - bddmaxnodeincrease - 2);
        else
            maxAllowed = best / 5 + best;

        /* Determine initial direction */
        if (blk.pos > middlePos)
            dirIsUp = false;

        /* Move block back and forth */
        for (n = 0; n < 2; n++) {
            int first = 1;

            if (dirIsUp) {
                while (blk.prev != null
                    && (reorder_nodenum() <= maxAllowed || first != 0)) {
                    first = 0;
                    blockdown(blk.prev);
                    bestpos--;

                    if (verbose > 1) {
                        System.out.print("-");
                        System.out.flush();
                    }

                    if (reorder_nodenum() < best) {
                        best = reorder_nodenum();
                        bestpos = 0;

                        if (bddmaxnodesize > 0)
                            maxAllowed =
                                MIN(
                                    best / 5 + best,
                                    bddmaxnodesize - bddmaxnodeincrease - 2);
                        else
                            maxAllowed = best / 5 + best;
                    }
                }
            } else {
                while (blk.next != null
                    && (reorder_nodenum() <= maxAllowed || first != 0)) {
                    first = 0;
                    blockdown(blk);
                    bestpos++;

                    if (verbose > 1) {
                        System.out.print("+");
                        System.out.flush();
                    }

                    if (reorder_nodenum() < best) {
                        best = reorder_nodenum();
                        bestpos = 0;

                        if (bddmaxnodesize > 0)
                            maxAllowed =
                                MIN(
                                    best / 5 + best,
                                    bddmaxnodesize - bddmaxnodeincrease - 2);
                        else
                            maxAllowed = best / 5 + best;
                    }
                }
            }

            if (reorder_nodenum() > maxAllowed && verbose > 1) {
                System.out.print("!");
                System.out.flush();
            }

            dirIsUp = !dirIsUp;
        }

        /* Move to best pos */
        while (bestpos < 0) {
            blockdown(blk);
            bestpos++;
        }
        while (bestpos > 0) {
            blockdown(blk.prev);
            bestpos--;
        }
    }

    BddTree reorder_random(BddTree t) {
        BddTree dis;
        BddTree[] seq;
        int n, num = 0;

        if (t == null)
            return t;

        for (dis = t; dis != null; dis = dis.next)
            num++;
        seq = new BddTree[num];
        for (dis = t, num = 0; dis != null; dis = dis.next)
            seq[num++] = dis;

        for (n = 0; n < 4 * num; n++) {
            int blk = rng.nextInt(num);
            if (seq[blk].next != null)
                blockdown(seq[blk]);
        }

        /* Find first block */
        for (dis = t; dis.prev != null; dis = dis.prev)
            /* nil */;

        if (verbose != 0)
            System.out.println("Random order: " + reorder_nodenum() + " nodes");
        return dis;
    }

    static int siftTestCmp(Object aa, Object bb) {
        sizePair a = (sizePair) aa;
        sizePair b = (sizePair) bb;

        if (a.val < b.val)
            return -1;
        if (a.val > b.val)
            return 1;
        return 0;
    }

    static class sizePair {
        int val;
        BddTree block;
    }

    BddTree reorder_sift(BddTree t) {
        BddTree dis, seq[];
        sizePair[] p;
        int n, num;

        for (dis = t, num = 0; dis != null; dis = dis.next)
            dis.pos = num++;

        p = new sizePair[num];
        seq = new BddTree[num];

        for (dis = t, n = 0; dis != null; dis = dis.next, n++) {
            int v;

            /* Accumulate number of nodes for each block */
            p[n].val = 0;
            for (v = dis.first; v <= dis.last; v++)
                p[n].val -= levels[v].nodenum;

            p[n].block = dis;
        }

        /* Sort according to the number of nodes at each level */
        Arrays.sort(p, 0, num, new Comparator() {

            public int compare(Object o1, Object o2) {
                return siftTestCmp(o1, o2);
            }

        });

        /* Create sequence */
        for (n = 0; n < num; n++)
            seq[n] = p[n].block;

        /* Do the sifting on this sequence */
        t = reorder_sift_seq(t, seq, num);

        return t;
    }

    BddTree reorder_siftite(BddTree t) {
        BddTree first = t;
        int lastsize;
        int c = 1;

        if (t == null)
            return t;

        do {
            if (verbose > 1)
                System.out.println("Reorder " + (c++) + "\n");

            lastsize = reorder_nodenum();
            first = reorder_sift(first);
        } while (reorder_nodenum() != lastsize);

        return first;
    }

    void blockdown(BddTree left) {
        BddTree right = left.next;
        int n;
        int leftsize = left.last - left.first;
        int rightsize = right.last - right.first;
        int leftstart = bddvar2level[left.seq[0]];
        int[] lseq = left.seq;
        int[] rseq = right.seq;

        /* Move left past right */
        while (bddvar2level[lseq[0]] < bddvar2level[rseq[rightsize]]) {
            for (n = 0; n < leftsize; n++) {
                if (bddvar2level[lseq[n]] + 1 != bddvar2level[lseq[n + 1]]
                    && bddvar2level[lseq[n]] < bddvar2level[rseq[rightsize]]) {
                    reorder_vardown(lseq[n]);
                }
            }

            if (bddvar2level[lseq[leftsize]] < bddvar2level[rseq[rightsize]]) {
                reorder_vardown(lseq[leftsize]);
            }
        }

        /* Move right to where left started */
        while (bddvar2level[rseq[0]] > leftstart) {
            for (n = rightsize; n > 0; n--) {
                if (bddvar2level[rseq[n]] - 1 != bddvar2level[rseq[n - 1]]
                    && bddvar2level[rseq[n]] > leftstart) {
                    reorder_varup(rseq[n]);
                }
            }

            if (bddvar2level[rseq[0]] > leftstart)
                reorder_varup(rseq[0]);
        }

        /* Swap left and right data in the order */
        left.next = right.next;
        right.prev = left.prev;
        left.prev = right;
        right.next = left;

        if (right.prev != null)
            right.prev.next = right;
        if (left.next != null)
            left.next.prev = left;

        n = left.pos;
        left.pos = right.pos;
        right.pos = n;
    }

    BddTree reorder_win2ite(BddTree t) {
        BddTree dis, first = t;
        int lastsize;
        //int c = 1;

        if (t == null)
            return t;

        if (verbose > 1)
            System.out.println(
                "Win2ite start: " + reorder_nodenum() + " nodes");

        do {
            lastsize = reorder_nodenum();

            dis = t;
            while (dis.next != null) {
                int best = reorder_nodenum();

                blockdown(dis);

                if (best < reorder_nodenum()) {
                    blockdown(dis.prev);
                    dis = dis.next;
                } else if (first == dis)
                    first = dis.prev;
                if (verbose > 1) {
                    System.out.print(".");
                    System.out.flush();
                }
            }

            if (verbose > 1)
                System.out.println(" " + reorder_nodenum() + " nodes");
            //c++;
        }
        while (reorder_nodenum() != lastsize);

        return first;
    }

    void bdd_reorder_auto() {
        if (!bdd_reorder_ready())
            return;

        bdd_reorder(bddreordermethod);
        bddreordertimes--;
    }

    int bdd_reorder_gain() {
        if (usednum_before == 0)
            return 0;

        return (100 * (usednum_before - usednum_after)) / usednum_before;
    }

    void bdd_done() {
        /*sanitycheck(); FIXME */
        //bdd_fdd_done();
        //bdd_reorder_done();
        bdd_pairs_done();

        bddnodes = null;
        bddrefstack = null;
        bddvarset = null;
        bddvar2level = null;
        bddlevel2var = null;

        bdd_operator_done();

        bddrunning = false;
        bddnodesize = 0;
        bddmaxnodesize = 0;
        bddvarnum = 0;
        bddproduced = 0;

        //err_handler = null;
        //gbc_handler = null;
        //resize_handler = null;
    }

    int bdd_setmaxnodenum(int size) {
        if (size > bddnodesize || size == 0) {
            int old = bddmaxnodesize;
            bddmaxnodesize = size;
            return old;
        }

        return bdd_error(BDD_NODES);
    }

    int bdd_setminfreenodes(int mf) {
        int old = minfreenodes;

        if (mf < 0 || mf > 100)
            return bdd_error(BDD_RANGE);

        minfreenodes = mf;
        return old;
    }

    int bdd_setmaxincrease(int size) {
        int old = bddmaxnodeincrease;

        if (size < 0)
            return bdd_error(BDD_SIZE);

        bddmaxnodeincrease = size;
        return old;
    }

    double increasefactor;
    
    double bdd_setincreasefactor(double x) {
        if (x < 0)
            return bdd_error(BDD_RANGE);
        double old = increasefactor;
        increasefactor = x;
        return old;
    }
    
    int bdd_setcacheratio(int r) {
        int old = cacheratio;

        if (r <= 0)
            return bdd_error(BDD_RANGE);
        if (bddnodesize == 0)
            return old;

        cacheratio = r;
        bdd_operator_noderesize();
        return old;
    }

    int bdd_setvarnum(int num) {
        int bdv;
        int oldbddvarnum = bddvarnum;

        bdd_disable_reorder();

        if (num < 1 || num > MAXVAR) {
            bdd_error(BDD_RANGE);
            return bddfalse;
        }

        if (num < bddvarnum)
            return bdd_error(BDD_DECVNUM);
        if (num == bddvarnum)
            return 0;

        if (bddvarset == null) {
            bddvarset = new int[num * 2];
            bddlevel2var = new int[num + 1];
            bddvar2level = new int[num + 1];
        } else {
            int[] bddvarset2 = new int[num * 2];
            System.arraycopy(bddvarset, 0, bddvarset2, 0, bddvarset.length);
            bddvarset = bddvarset2;
            int[] bddlevel2var2 = new int[num + 1];
            System.arraycopy(
                bddlevel2var,
                0,
                bddlevel2var2,
                0,
                bddlevel2var.length);
            bddlevel2var = bddlevel2var2;
            int[] bddvar2level2 = new int[num + 1];
            System.arraycopy(
                bddvar2level,
                0,
                bddvar2level2,
                0,
                bddvar2level.length);
            bddvar2level = bddvar2level2;
        }

        bddrefstack = new int[num * 2 + 1];
        bddrefstacktop = 0;

        for (bdv = bddvarnum; bddvarnum < num; bddvarnum++) {
            bddvarset[bddvarnum * 2] = PUSHREF(bdd_makenode(bddvarnum, 0, 1));
            bddvarset[bddvarnum * 2 + 1] = bdd_makenode(bddvarnum, 1, 0);
            POPREF(1);

            if (bdderrorcond != 0) {
                bddvarnum = bdv;
                return -bdderrorcond;
            }

            SETMAXREF(bddvarset[bddvarnum * 2]);
            SETMAXREF(bddvarset[bddvarnum * 2 + 1]);
            bddlevel2var[bddvarnum] = bddvarnum;
            bddvar2level[bddvarnum] = bddvarnum;
        }

        SETLEVEL(0, num);
        SETLEVEL(1, num);
        bddvar2level[num] = num;
        bddlevel2var[num] = num;

        bdd_pairs_resize(oldbddvarnum, bddvarnum);
        bdd_operator_varresize();

        bdd_enable_reorder();

        return 0;
    }

    static class BddTree {
        int first, last; /* First and last variable in this block */
        int pos; /* Sifting position */
        int[] seq; /* Sequence of first...last in the current order */
        boolean fixed; /* Are the sub-blocks fixed or may they be reordered */
        int id; /* A sequential id number given by addblock */
        BddTree next, prev;
        BddTree nextlevel;
    }

    /* Current auto reord. method and number of automatic reorderings left */
    int bddreordermethod;
    int bddreordertimes;

    /* Flag for disabling reordering temporarily */
    int reorderdisabled;

    BddTree vartree;
    int blockid;

    int[] extroots;
    int extrootsize;

    levelData levels[]; /* Indexed by variable! */

    static class levelData {
        int start; /* Start of this sub-table (entry in "bddnodes") */
        int size; /* Size of this sub-table */
        int maxsize; /* Max. allowed size of sub-table */
        int nodenum; /* Number of nodes in this level */
    }

    static class imatrix {
        byte rows[][];
        int size;
    }

    /* Interaction matrix */
    imatrix iactmtx;

    int verbose;
    //bddinthandler reorder_handler;
    //bddfilehandler reorder_filehandler;
    //bddsizehandler reorder_nodenum;

    /* Number of live nodes before and after a reordering session */
    int usednum_before;
    int usednum_after;

    void bdd_reorder_init() {
        reorderdisabled = 0;
        vartree = null;

        bdd_clrvarblocks();
        //bdd_reorder_hook(bdd_default_reohandler);
        bdd_reorder_verbose(0);
        bdd_autoreorder_times(BDD_REORDER_NONE, 0);
        //reorder_nodenum = bdd_getnodenum;
        usednum_before = usednum_after = 0;
        blockid = 0;
    }

    int reorder_nodenum() {
        return bdd_getnodenum();
    }

    int bdd_getnodenum() {
        return bddnodesize - bddfreenum;
    }

    int bdd_reorder_verbose(int v) {
        int tmp = verbose;
        verbose = v;
        return tmp;
    }

    int bdd_autoreorder(int method) {
        int tmp = bddreordermethod;
        bddreordermethod = method;
        bddreordertimes = -1;
        return tmp;
    }

    int bdd_autoreorder_times(int method, int num) {
        int tmp = bddreordermethod;
        bddreordermethod = method;
        bddreordertimes = num;
        return tmp;
    }

    static final int BDD_REORDER_NONE = 0;
    static final int BDD_REORDER_WIN2 = 1;
    static final int BDD_REORDER_WIN2ITE = 2;
    static final int BDD_REORDER_SIFT = 3;
    static final int BDD_REORDER_SIFTITE = 4;
    static final int BDD_REORDER_WIN3 = 5;
    static final int BDD_REORDER_WIN3ITE = 6;
    static final int BDD_REORDER_RANDOM = 7;

    static final int BDD_REORDER_FREE = 0;
    static final int BDD_REORDER_FIXED = 1;

    static long c1;

    void bdd_reorder_done() {
        bddtree_del(vartree);
        bdd_operator_reset();
        vartree = null;
    }

    void bddtree_del(BddTree t) {
        if (t == null)
            return;

        bddtree_del(t.nextlevel);
        bddtree_del(t.next);
        t.seq = null;
    }

    void bdd_clrvarblocks() {
        bddtree_del(vartree);
        vartree = null;
        blockid = 0;
    }

    int NODEHASHr(int var, int l, int h) {
        return (Math.abs(PAIR(l, (h)) % levels[var].size) + levels[var].start);
    }

    void bdd_setvarorder(int[] neworder) {
        int level;

        /* Do not set order when variable-blocks are used */
        if (vartree != null) {
            bdd_error(BDD_VARBLK);
            return;
        }

        reorder_init();

        for (level = 0; level < bddvarnum; level++) {
            int lowvar = neworder[level];

            while (bddvar2level[lowvar] > level)
                reorder_varup(lowvar);
        }

        reorder_done();
    }

    int reorder_varup(int var) {
        if (var < 0 || var >= bddvarnum)
            return bdd_error(BDD_VAR);
        if (bddvar2level[var] == 0)
            return 0;
        return reorder_vardown(bddlevel2var[bddvar2level[var] - 1]);
    }

    int reorder_vardown(int var) {
        int n, level;

        if (var < 0 || var >= bddvarnum)
            return bdd_error(BDD_VAR);
        if ((level = bddvar2level[var]) >= bddvarnum - 1)
            return 0;

        resizedInMakenode = false;

        if (imatrixDepends(iactmtx, var, bddlevel2var[level + 1])) {
            int toBeProcessed = reorder_downSimple(var);
            levelData l = levels[var];

            if (l.nodenum < (l.size) / 3
                || l.nodenum >= (l.size * 3) / 2
                && l.size < l.maxsize) {
                reorder_swapResize(toBeProcessed, var);
                reorder_localGbcResize(toBeProcessed, var);
            } else {
                reorder_swap(toBeProcessed, var);
                reorder_localGbc(var);
            }
        }

        /* Swap the var<->level tables */
        n = bddlevel2var[level];
        bddlevel2var[level] = bddlevel2var[level + 1];
        bddlevel2var[level + 1] = n;

        n = bddvar2level[var];
        bddvar2level[var] = bddvar2level[bddlevel2var[level]];
        bddvar2level[bddlevel2var[level]] = n;

        /* Update all rename pairs */
        bdd_pairs_vardown(level);

        if (resizedInMakenode)
            reorder_rehashAll();

        return 0;
    }

    boolean imatrixDepends(imatrix mtx, int a, int b) {
        return (mtx.rows[a][b / 8] & (1 << (b % 8))) != 0;
    }

    void reorder_setLevellookup() {
        int n;

        for (n = 0; n < bddvarnum; n++) {
            levels[n].maxsize = bddnodesize / bddvarnum;
            levels[n].start = n * levels[n].maxsize;
            levels[n].size =
                Math.min(levels[n].maxsize, (levels[n].nodenum * 5) / 4);

            if (levels[n].size >= 4)
                levels[n].size = bdd_prime_lte(levels[n].size);

        }
    }

    void reorder_rehashAll() {
        int n;

        reorder_setLevellookup();
        bddfreepos = 0;

        for (n = bddnodesize - 1; n >= 0; n--)
            SETHASH(n, 0);

        for (n = bddnodesize - 1; n >= 2; n--) {

            if (HASREF(n)) {
                int hash2;

                hash2 = NODEHASH2(VARr(n), LOW(n), HIGH(n));
                SETNEXT(n, hash2);
                SETHASH(hash2, n);
            } else {
                SETNEXT(n, bddfreepos);
                bddfreepos = n;
            }
        }
    }

    void reorder_localGbc(int var0) {
        int var1 = bddlevel2var[bddvar2level[var0] + 1];
        int vl1 = levels[var1].start;
        int size1 = levels[var1].size;
        int n;

        for (n = 0; n < size1; n++) {
            int hash = n + vl1;
            int r = HASH(hash);
            SETHASH(hash, 0);

            while (r != 0) {
                int next = NEXT(r);

                if (HASREF(r)) {
                    SETNEXT(r, HASH(hash));
                    SETHASH(hash, r);
                } else {
                    DECREF(LOW(r));
                    DECREF(HIGH(r));

                    SETLOW(r, INVALID_BDD);
                    SETNEXT(r, bddfreepos);
                    bddfreepos = r;
                    levels[var1].nodenum--;
                    bddfreenum++;
                }

                r = next;
            }
        }
    }

    int reorder_downSimple(int var0) {
        int toBeProcessed = 0;
        int var1 = bddlevel2var[bddvar2level[var0] + 1];
        int vl0 = levels[var0].start;
        int size0 = levels[var0].size;
        int n;

        levels[var0].nodenum = 0;

        for (n = 0; n < size0; n++) {
            int r;

            r = HASH(n + vl0);
            SETHASH(n + vl0, 0);

            while (r != 0) {
                int next = NEXT(r);

                if (VARr(LOW(r)) != var1 && VARr(HIGH(r)) != var1) {
                    /* Node does not depend on next var, let it stay in the chain */
                    SETNEXT(r, HASH(n + vl0));
                    SETHASH(n + vl0, r);
                    levels[var0].nodenum++;
                } else {
                    /* Node depends on next var - save it for later procesing */
                    SETNEXT(r, toBeProcessed);
                    toBeProcessed = r;
                    if (SWAPCOUNT)
                        cachestats.swapCount++;

                }

                r = next;
            }
        }

        return toBeProcessed;
    }

    void reorder_swapResize(int toBeProcessed, int var0) {
        int var1 = bddlevel2var[bddvar2level[var0] + 1];

        while (toBeProcessed != 0) {
            int next = NEXT(toBeProcessed);
            int f0 = LOW(toBeProcessed);
            int f1 = HIGH(toBeProcessed);
            int f00, f01, f10, f11;

            /* Find the cofactors for the new nodes */
            if (VARr(f0) == var1) {
                f00 = LOW(f0);
                f01 = HIGH(f0);
            } else
                f00 = f01 = f0;

            if (VARr(f1) == var1) {
                f10 = LOW(f1);
                f11 = HIGH(f1);
            } else
                f10 = f11 = f1;

            /* Note: makenode does refcou. */
            f0 = reorder_makenode(var0, f00, f10);
            f1 = reorder_makenode(var0, f01, f11);
            //node = bddnodes[toBeProcessed]; /* Might change in makenode */

            /* We know that the refcou of the grandchilds of this node
            * is greater than one (these are f00...f11), so there is
            * no need to do a recursive refcou decrease. It is also
            * possible for the node.low/high nodes to come alive again,
            * so deref. of the childs is delayed until the local GBC. */

            DECREF(LOW(toBeProcessed));
            DECREF(HIGH(toBeProcessed));

            /* Update in-place */
            SETVARr(toBeProcessed, var1);
            SETLOW(toBeProcessed, f0);
            SETHIGH(toBeProcessed, f1);

            levels[var1].nodenum++;

            /* Do not rehash yet since we are going to resize the hash table */

            toBeProcessed = next;
        }
    }

    static final int MIN(int a, int b) {
        return Math.min(a, b);
    }

    void reorder_localGbcResize(int toBeProcessed, int var0) {
        int var1 = bddlevel2var[bddvar2level[var0] + 1];
        int vl1 = levels[var1].start;
        int size1 = levels[var1].size;
        int n;

        for (n = 0; n < size1; n++) {
            int hash = n + vl1;
            int r = HASH(hash);
            SETHASH(hash, 0);

            while (r != 0) {
                int next = NEXT(r);

                if (HASREF(r)) {
                    SETNEXT(r, toBeProcessed);
                    toBeProcessed = r;
                } else {
                    DECREF(LOW(r));
                    DECREF(HIGH(r));

                    SETLOW(r, INVALID_BDD);
                    SETNEXT(r, bddfreepos);
                    bddfreepos = r;
                    levels[var1].nodenum--;
                    bddfreenum++;
                }

                r = next;
            }
        }

        /* Resize */
        if (levels[var1].nodenum < levels[var1].size)
            levels[var1].size =
                MIN(levels[var1].maxsize, levels[var1].size / 2);
        else
            levels[var1].size =
                MIN(levels[var1].maxsize, levels[var1].size * 2);

        if (levels[var1].size >= 4)
            levels[var1].size = bdd_prime_lte(levels[var1].size);

        /* Rehash the remaining live nodes */
        while (toBeProcessed != 0) {
            int next = NEXT(toBeProcessed);
            int hash = NODEHASH2(VARr(toBeProcessed), LOW(toBeProcessed), HIGH(toBeProcessed));

            SETNEXT(toBeProcessed, HASH(hash));
            SETHASH(hash, toBeProcessed);

            toBeProcessed = next;
        }
    }

    void reorder_swap(int toBeProcessed, int var0) {
        int var1 = bddlevel2var[bddvar2level[var0] + 1];

        while (toBeProcessed != 0) {
            int next = NEXT(toBeProcessed);
            int f0 = LOW(toBeProcessed);
            int f1 = HIGH(toBeProcessed);
            int f00, f01, f10, f11, hash;

            /* Find the cofactors for the new nodes */
            if (VARr(f0) == var1) {
                f00 = LOW(f0);
                f01 = HIGH(f0);
            } else
                f00 = f01 = f0;

            if (VARr(f1) == var1) {
                f10 = LOW(f1);
                f11 = HIGH(f1);
            } else
                f10 = f11 = f1;

            /* Note: makenode does refcou. */
            f0 = reorder_makenode(var0, f00, f10);
            f1 = reorder_makenode(var0, f01, f11);
            //node = bddnodes[toBeProcessed]; /* Might change in makenode */

            /* We know that the refcou of the grandchilds of this node
            * is greater than one (these are f00...f11), so there is
            * no need to do a recursive refcou decrease. It is also
            * possible for the node.low/high nodes to come alive again,
            * so deref. of the childs is delayed until the local GBC. */

            DECREF(LOW(toBeProcessed));
            DECREF(HIGH(toBeProcessed));

            /* Update in-place */
            SETVARr(toBeProcessed, var1);
            SETLOW(toBeProcessed, f0);
            SETHIGH(toBeProcessed, f1);

            levels[var1].nodenum++;

            /* Rehash the node since it got new childs */
            hash = NODEHASH2(VARr(toBeProcessed), LOW(toBeProcessed), HIGH(toBeProcessed));
            SETNEXT(toBeProcessed, HASH(hash));
            SETHASH(hash, toBeProcessed);

            toBeProcessed = next;
        }
    }

    int NODEHASH2(int var, int l, int h) {
        return (Math.abs(PAIR(l, h) % levels[var].size) + levels[var].start);
    }

    boolean resizedInMakenode;

    int reorder_makenode(int var, int low, int high) {
        int hash;
        int res;

        if (CACHESTATS > 0) cachestats.uniqueAccess++;

        /* Note: We know that low,high has a refcou greater than zero, so
        there is no need to add reference *recursively* */

        /* check whether childs are equal */
        if (low == high) {
            INCREF(low);
            return low;
        }

        /* Try to find an existing node of this kind */
        hash = NODEHASH2(var, low, high);
        res = HASH(hash);

        while (res != 0) {
            if (LOW(res) == low && HIGH(res) == high) {
                if (CACHESTATS > 0) cachestats.uniqueHit++;
                INCREF(res);
                return res;
            }
            res = NEXT(res);

            if (CACHESTATS > 0) cachestats.uniqueChain++;
        }

        /* No existing node -> build one */
        if (CACHESTATS > 0) cachestats.uniqueMiss++;

        /* Any free nodes to use ? */
        if (bddfreepos == 0) {
            if (bdderrorcond != 0)
                return 0;

            /* Try to allocate more nodes - call noderesize without
            * enabling rehashing.
             * Note: if ever rehashing is allowed here, then remember to
            * update local variable "hash" */
            bdd_noderesize(false);
            resizedInMakenode = true;

            /* Panic if that is not possible */
            if (bddfreepos == 0) {
                bdd_error(BDD_NODENUM);
                bdderrorcond = Math.abs(BDD_NODENUM);
                return 0;
            }
        }

        /* Build new node */
        res = bddfreepos;
        bddfreepos = NEXT(bddfreepos);
        levels[var].nodenum++;
        bddproduced++;
        bddfreenum--;

        SETVARr(res, var);
        SETLOW(res, low);
        SETHIGH(res, high);

        /* Insert node in hash chain */
        SETNEXT(res, HASH(hash));
        SETHASH(hash, res);

        /* Make sure it is reference counted */
        CLEARREF(res);
        INCREF(res);
        INCREF(LOW(res));
        INCREF(HIGH(res));

        return res;
    }

    int reorder_init() {
        int n;

        reorder_handler(true, reorderstats);
        
        levels = new levelData[bddvarnum];

        for (n = 0; n < bddvarnum; n++) {
            levels[n] = new levelData();
            levels[n].start = -1;
            levels[n].size = 0;
            levels[n].nodenum = 0;
        }

        /* First mark and recursive refcou. all roots and childs. Also do some
         * setup here for both setLevellookup and reorder_gbc */
        if (mark_roots() < 0)
            return -1;

        /* Initialize the hash tables */
        reorder_setLevellookup();

        /* Garbage collect and rehash to new scheme */
        reorder_gbc();

        return 0;
    }

    int mark_roots() {
        boolean[] dep = new boolean[bddvarnum];
        int n;

        for (n = 2, extrootsize = 0; n < bddnodesize; n++) {
            /* This is where we go from .level to .var! */
            int lev = LEVEL(n);
            int var = bddlevel2var[lev];
            SETVARr(n, var);

            if (HASREF(n)) {
                SETMARK(n);
                extrootsize++;
            }
        }

        extroots = new int[extrootsize];

        iactmtx = imatrixNew(bddvarnum);

        for (n = 2, extrootsize = 0; n < bddnodesize; n++) {

            if (MARKED(n)) {
                UNMARK(n);
                extroots[extrootsize++] = n;

                for (int i = 0; i < bddvarnum; ++i)
                    dep[i] = false;
                dep[VARr(n)] = true;
                levels[VARr(n)].nodenum++;

                addref_rec(LOW(n), dep);
                addref_rec(HIGH(n), dep);

                addDependencies(dep);
            }

            /* Make sure the hash field is empty. This saves a loop in the
            initial GBC */
            SETHASH(n, 0);
        }

        SETHASH(0, 0);
        SETHASH(1, 0);

        return 0;
    }

    imatrix imatrixNew(int size) {
        imatrix mtx = new imatrix();
        int n;

        mtx.rows = new byte[size][];

        for (n = 0; n < size; n++) {
            mtx.rows[n] = new byte[size / 8 + 1];
        }

        mtx.size = size;

        return mtx;
    }

    void addref_rec(int r, boolean[] dep) {
        if (r < 2)
            return;

        if (!HASREF(r) || MARKED(r)) {
            bddfreenum--;

            /* Detect variable dependencies for the interaction matrix */
            dep[VARr(r) & ~MARK_MASK] = true;

            /* Make sure the nodenum field is updated. Used in the initial GBC */
            levels[VARr(r) & ~MARK_MASK].nodenum++;

            addref_rec(LOW(r), dep);
            addref_rec(HIGH(r), dep);
        } else {
            int n;

            /* Update (from previously found) variable dependencies
            * for the interaction matrix */
            for (n = 0; n < bddvarnum; n++)
                dep[n]
                    |= imatrixDepends(iactmtx, VARr(r) & ~MARK_MASK, n);
        }

        INCREF(r);
    }

    void addDependencies(boolean[] dep) {
        int n, m;

        for (n = 0; n < bddvarnum; n++) {
            for (m = n; m < bddvarnum; m++) {
                if ((dep[n]) && (dep[m])) {
                    imatrixSet(iactmtx, n, m);
                    imatrixSet(iactmtx, m, n);
                }
            }
        }
    }

    void imatrixSet(imatrix mtx, int a, int b) {
        mtx.rows[a][b / 8] |= 1 << (b % 8);
    }

    void reorder_gbc() {
        int n;

        bddfreepos = 0;
        bddfreenum = 0;

        /* No need to zero all hash fields - this is done in mark_roots */

        for (n = bddnodesize - 1; n >= 2; n--) {

            if (HASREF(n)) {
                int hash;

                hash = NODEHASH2(VARr(n), LOW(n), HIGH(n));
                SETNEXT(n, HASH(hash));
                SETHASH(hash, n);

            } else {
                SETLOW(n, INVALID_BDD);
                SETNEXT(n, bddfreepos);
                bddfreepos = n;
                bddfreenum++;
            }
        }
    }

    void reorder_done() {
        int n;

        for (n = 0; n < extrootsize; n++)
            SETMARK(extroots[n]);
        for (n = 2; n < bddnodesize; n++) {
            if (MARKED(n))
                UNMARK(n);
            else
                CLEARREF(n);

            /* This is where we go from .var to .level again! */
            SETLEVEL(n, bddvar2level[VARr(n)]);
        }

        imatrixDelete(iactmtx);
        bdd_gbc();
        
        reorder_handler(false, reorderstats);
    }

    void imatrixDelete(imatrix mtx) {
        int n;

        for (n = 0; n < mtx.size; n++) {
            mtx.rows[n] = null;
        }
        mtx.rows = null;
    }

    int bdd_getallocnum() {
        return bddnodesize;
    }

    int bdd_swapvar(int v1, int v2) {
        int l1, l2;

        /* Do not swap when variable-blocks are used */
        if (vartree != null)
            return bdd_error(BDD_VARBLK);

        /* Don't bother swapping x with x */
        if (v1 == v2)
            return 0;

        /* Make sure the variable exists */
        if (v1 < 0 || v1 >= bddvarnum || v2 < 0 || v2 >= bddvarnum)
            return bdd_error(BDD_VAR);

        l1 = bddvar2level[v1];
        l2 = bddvar2level[v2];

        /* Make sure v1 is before v2 */
        if (l1 > l2) {
            int tmp = v1;
            v1 = v2;
            v2 = tmp;
            l1 = bddvar2level[v1];
            l2 = bddvar2level[v2];
        }

        reorder_init();

        /* Move v1 to v2's position */
        while (bddvar2level[v1] < l2)
            reorder_vardown(v1);

        /* Move v2 to v1's position */
        while (bddvar2level[v2] > l1)
            reorder_varup(v2);

        reorder_done();

        return 0;
    }

    void bdd_fprintall(PrintStream out) {
        int n;

        for (n = 0; n < bddnodesize; n++) {
            if (LOW(n) != INVALID_BDD) {
                out.print(
                    "["
                        + right(n, 5)
                        + " - "
                        + right(GETREF(n), 2)
                        + "] ");
                // TODO: labelling of vars
                out.print(right(bddlevel2var[LEVEL(n)], 3));

                out.print(": " + right(LOW(n), 3));
                out.println(" " + right(HIGH(n), 3));
            }
        }
    }

    void bdd_fprinttable(PrintStream out, int r) {
        int n;

        out.println("ROOT: " + r);
        if (r < 2)
            return;

        bdd_mark(r);

        for (n = 0; n < bddnodesize; n++) {
            if (MARKED(n)) {
                UNMARK(n);

                out.print("[" + right(n, 5) + "] ");
                // TODO: labelling of vars
                out.print(right(bddlevel2var[LEVEL(n)], 3));

                out.print(": " + right(LOW(n), 3));
                out.println(" " + right(HIGH(n), 3));
            }
        }
    }

    int lh_nodenum;
    int lh_freepos;
    int[] loadvar2level;
    LoadHash[] lh_table;

    int bdd_load(BufferedReader ifile, int[] translate) throws IOException {
        int n, vnum, tmproot;
        int root;

        lh_nodenum = Integer.parseInt(readNext(ifile));
        vnum = Integer.parseInt(readNext(ifile));

        // Check for constant true / false
        if (lh_nodenum == 0 && vnum == 0) {
            root = Integer.parseInt(readNext(ifile));
            return root;
        }

        // Not actually used.
        loadvar2level = new int[vnum];
        for (n = 0; n < vnum; n++) {
            loadvar2level[n] = Integer.parseInt(readNext(ifile));
        }

        if (vnum > bddvarnum)
            bdd_setvarnum(vnum);

        lh_table = new LoadHash[lh_nodenum];

        for (n = 0; n < lh_nodenum; n++) {
            lh_table[n] = new LoadHash();
            lh_table[n].first = -1;
            lh_table[n].next = n + 1;
        }
        lh_table[lh_nodenum - 1].next = -1;
        lh_freepos = 0;

        tmproot = bdd_loaddata(ifile, translate);

        for (n = 0; n < lh_nodenum; n++)
            bdd_delref(lh_table[n].data);

        lh_table = null;
        loadvar2level = null;

        root = tmproot;
        return root;
    }

    static class LoadHash {
        int key;
        int data;
        int first;
        int next;
    }

    int bdd_loaddata(BufferedReader ifile, int[] translate) throws IOException {
        int key, var, low, high, root = 0, n;

        for (n = 0; n < lh_nodenum; n++) {
            key = Integer.parseInt(readNext(ifile));
            var = Integer.parseInt(readNext(ifile));
            if (translate != null)
                var = translate[var];
            low = Integer.parseInt(readNext(ifile));
            high = Integer.parseInt(readNext(ifile));

            if (low >= 2)
                low = loadhash_get(low);
            if (high >= 2)
                high = loadhash_get(high);

            if (low < 0 || high < 0 || var < 0)
                return bdd_error(BDD_FORMAT);

            root = bdd_addref(bdd_ite(bdd_ithvar(var), high, low));

            loadhash_add(key, root);
        }

        return root;
    }
    
    void loadhash_add(int key, int data) {
        int hash = key % lh_nodenum;
        int pos = lh_freepos;

        lh_freepos = lh_table[pos].next;
        lh_table[pos].next = lh_table[hash].first;
        lh_table[hash].first = pos;

        lh_table[pos].key = key;
        lh_table[pos].data = data;
    }

    int loadhash_get(int key) {
        int hash = lh_table[key % lh_nodenum].first;

        while (hash != -1 && lh_table[hash].key != key)
            hash = lh_table[hash].next;

        if (hash == -1)
            return -1;
        return lh_table[hash].data;
    }

    void bdd_save(BufferedWriter out, int r) throws IOException {
        int[] n = new int[1];

        if (r < 2) {
            out.write("0 0 " + r + "\n");
            return;
        }

        bdd_markcount(r, n);
        bdd_unmark(r);
        out.write(n[0] + " " + bddvarnum + "\n");

        for (int x = 0; x < bddvarnum; x++)
            out.write(bddvar2level[x] + " ");
        out.write("\n");

        bdd_save_rec(out, r);
        bdd_unmark(r);

        return;
    }

    void bdd_save_rec(BufferedWriter out, int root) throws IOException {

        if (root < 2)
            return;

        if (MARKED(root))
            return;
        SETMARK(root);

        bdd_save_rec(out, LOW(root));
        bdd_save_rec(out, HIGH(root));

        out.write(root + " ");
        out.write(bddlevel2var[LEVEL(root)] + " ");
        out.write(LOW(root) + " ");
        out.write(HIGH(root) + "\n");

        return;
    }

    static String right(int x, int w) {
        return right(Integer.toString(x), w);
    }
    static String right(String s, int w) {
        int n = s.length();
        //if (w < n) return s.substring(n - w);
        StringBuffer b = new StringBuffer(w);
        for (int i = n; i < w; ++i) {
            b.append(' ');
        }
        b.append(s);
        return b.toString();
    }

    int bdd_intaddvarblock(int first, int last, boolean fixed) {
        BddTree t;

        if (first < 0 || first >= bddvarnum || last < 0 || last >= bddvarnum)
            return bdd_error(BDD_VAR);

        if ((t = bddtree_addrange(vartree, first, last, fixed, blockid))
            == null)
            return bdd_error(BDD_VARBLK);

        vartree = t;
        return blockid++;
    }

    BddTree bddtree_addrange_rec(
        BddTree t,
        BddTree prev,
        int first,
        int last,
        boolean fixed,
        int id) {
        if (first < 0 || last < 0 || last < first)
            return null;

        /* Empty tree -> build one */
        if (t == null) {
            if ((t = bddtree_new(id)) == null)
                return null;
            t.first = first;
            t.fixed = fixed;
            t.seq = new int[last - first + 1];
            t.last = last;
            update_seq(t);
            t.prev = prev;
            return t;
        }

        /* Check for identity */
        if (first == t.first && last == t.last)
            return t;

        /* Before this section -> insert */
        if (last < t.first) {
            BddTree tnew = bddtree_new(id);
            if (tnew == null)
                return null;
            tnew.first = first;
            tnew.last = last;
            tnew.fixed = fixed;
            tnew.seq = new int[last - first + 1];
            update_seq(tnew);
            tnew.next = t;
            tnew.prev = t.prev;
            t.prev = tnew;
            return tnew;
        }

        /* After this this section -> go to next */
        if (first > t.last) {
            t.next = bddtree_addrange_rec(t.next, t, first, last, fixed, id);
            return t;
        }

        /* Inside this section -> insert in next level */
        if (first >= t.first && last <= t.last) {
            t.nextlevel =
                bddtree_addrange_rec(t.nextlevel, null, first, last, fixed, id);
            return t;
        }

        /* Covering this section -> insert above this level */
        if (first <= t.first) {
            BddTree tnew;
            BddTree dis = t;

            while (true) {
                /* Partial cover ->error */
                if (last >= dis.first && last < dis.last)
                    return null;

                if (dis.next == null || last < dis.next.first) {
                    tnew = bddtree_new(id);
                    if (tnew == null)
                        return null;
                    tnew.first = first;
                    tnew.last = last;
                    tnew.fixed = fixed;
                    tnew.seq = new int[last - first + 1];
                    update_seq(tnew);
                    tnew.nextlevel = t;
                    tnew.next = dis.next;
                    tnew.prev = t.prev;
                    if (dis.next != null)
                        dis.next.prev = tnew;
                    dis.next = null;
                    t.prev = null;
                    return tnew;
                }

                dis = dis.next;
            }

        }

        return null;
    }

    void update_seq(BddTree t) {
        int n;
        int low = t.first;

        for (n = t.first; n <= t.last; n++)
            if (bddvar2level[n] < bddvar2level[low])
                low = n;

        for (n = t.first; n <= t.last; n++)
            t.seq[bddvar2level[n] - bddvar2level[low]] = n;
    }

    BddTree bddtree_addrange(
        BddTree t,
        int first,
        int last,
        boolean fixed,
        int id) {
        return bddtree_addrange_rec(t, null, first, last, fixed, id);
    }

    void bdd_varblockall() {
        int n;

        for (n = 0; n < bddvarnum; n++)
            bdd_intaddvarblock(n, n, true);
    }

    void print_order_rec(PrintStream o, BddTree t, int level) {
        if (t == null)
            return;

        if (t.nextlevel != null) {
            for (int i = 0; i < level; ++i)
                o.print("   ");
            // todo: better reorder id printout
            o.print(right(t.id, 3));
            o.println("{\n");

            print_order_rec(o, t.nextlevel, level + 1);

            for (int i = 0; i < level; ++i)
                o.print("   ");
            // todo: better reorder id printout
            o.print(right(t.id, 3));
            o.println("}\n");

            print_order_rec(o, t.next, level);
        } else {
            for (int i = 0; i < level; ++i)
                o.print("   ");
            // todo: better reorder id printout
            o.println(right(t.id, 3));

            print_order_rec(o, t.next, level);
        }
    }

    void bdd_fprintorder(PrintStream ofile) {
        print_order_rec(ofile, vartree, 0);
    }

    void bdd_fprintstat(PrintStream out) {
        CacheStats s = cachestats;
        out.print(s.toString());
    }
    
    void bdd_validate_all() {
        int n;
        for (n = bddnodesize - 1; n >= 2; n--) {
            if (HASREF(n)) {
                bdd_validate(n);
            }
        }
    }
    void bdd_validate(int k) {
        bdd_validate(k, -1);
    }
    void bdd_validate(int k, int lastLevel) {
        if (k < 2) return;
        int lev = LEVEL(k);
        //System.out.println("Level("+k+") = "+lev);
        if (lev <= lastLevel)
            throw new BDDException(lev+" <= "+lastLevel);
        //System.out.println("Low:");
        bdd_validate(LOW(k), lev);
        //System.out.println("High:");
        bdd_validate(HIGH(k), lev);
    }
    
    //// Prime stuff below.

    Random rng = new Random();

    final int Random(int i) {
        return rng.nextInt(i) + 1;
    }

    static boolean isEven(int src) {
        return (src & 0x1) == 0;
    }

    static boolean hasFactor(int src, int n) {
        return (src != n) && (src % n == 0);
    }

    static boolean BitIsSet(int src, int b) {
        return (src & (1 << b)) != 0;
    }

    static final int CHECKTIMES = 20;

    static final int u64_mulmod(int a, int b, int c) {
        return (int) (((long) a * (long) b) % (long) c);
    }

    /*************************************************************************
      Miller Rabin check
    *************************************************************************/

    static int numberOfBits(int src) {
        int b;

        if (src == 0)
            return 0;

        for (b = 31; b > 0; --b)
            if (BitIsSet(src, b))
                return b + 1;

        return 1;
    }

    static boolean isWitness(int witness, int src) {
        int bitNum = numberOfBits(src - 1) - 1;
        int d = 1;
        int i;

        for (i = bitNum; i >= 0; --i) {
            int x = d;

            d = u64_mulmod(d, d, src);

            if (d == 1 && x != 1 && x != src - 1)
                return true;

            if (BitIsSet(src - 1, i))
                d = u64_mulmod(d, witness, src);
        }

        return d != 1;
    }

    boolean isMillerRabinPrime(int src) {
        int n;

        for (n = 0; n < CHECKTIMES; ++n) {
            int witness = Random(src - 1);

            if (isWitness(witness, src))
                return false;
        }

        return true;
    }

    /*************************************************************************
      Basic prime searching stuff
    *************************************************************************/

    static boolean hasEasyFactors(int src) {
        return hasFactor(src, 3)
            || hasFactor(src, 5)
            || hasFactor(src, 7)
            || hasFactor(src, 11)
            || hasFactor(src, 13);
    }

    boolean isPrime(int src) {
        if (hasEasyFactors(src))
            return false;

        return isMillerRabinPrime(src);
    }

    /*************************************************************************
      External interface
    *************************************************************************/

    int bdd_prime_gte(int src) {
        if (isEven(src))
            ++src;

        while (!isPrime(src))
            src += 2;

        return src;
    }

    int bdd_prime_lte(int src) {
        if (isEven(src))
            --src;

        while (!isPrime(src))
            src -= 2;

        return src;
    }

    /* (non-Javadoc)
     * @see com.github.javabdd.BDDFactory#getCacheStats()
     */
    public CacheStats getCacheStats() {
        cachestats.opHit = 0;
        cachestats.opMiss = 0;
        if (singlecache != null) {
            System.out.println("Single cache: "+singlecache);
            cachestats.opHit += singlecache.cacheHit;
            cachestats.opMiss += singlecache.cacheMiss;
        }
        if (replacecache != null) {
            System.out.println("Replace cache: "+replacecache);
            cachestats.opHit += replacecache.cacheHit;
            cachestats.opMiss += replacecache.cacheMiss;
        }
        if (andcache != null) {
            System.out.println("And cache: "+andcache);
            cachestats.opHit += andcache.cacheHit;
            cachestats.opMiss += andcache.cacheMiss;
        }
        if (orcache != null) {
            System.out.println("Or cache: "+orcache);
            cachestats.opHit += orcache.cacheHit;
            cachestats.opMiss += orcache.cacheMiss;
        }
        if (applycache != null) {
            System.out.println("Apply cache: "+applycache);
            cachestats.opHit += applycache.cacheHit;
            cachestats.opMiss += applycache.cacheMiss;
        }
        if (quantcache != null) {
            System.out.println("Quant cache: "+quantcache);
            cachestats.opHit += quantcache.cacheHit;
            cachestats.opMiss += quantcache.cacheMiss;
        }
        if (appexcache != null) {
            System.out.println("Appex cache: "+appexcache);
            cachestats.opHit += appexcache.cacheHit;
            cachestats.opMiss += appexcache.cacheMiss;
        }
        if (appex3cache != null) {
            System.out.println("Appex3 cache: "+appex3cache);
            cachestats.opHit += appex3cache.cacheHit;
            cachestats.opMiss += appex3cache.cacheMiss;
        }
        if (itecache != null) {
            System.out.println("ITE cache: "+itecache);
            cachestats.opHit += itecache.cacheHit;
            cachestats.opMiss += itecache.cacheMiss;
        }
        if (countcache != null) {
            System.out.println("Count cache: "+countcache);
            cachestats.opHit += countcache.cacheHit;
            cachestats.opMiss += countcache.cacheMiss;
        }
        if (misccache != null) {
            System.out.println("Misc cache: "+misccache);
            cachestats.opHit += misccache.cacheHit;
            cachestats.opMiss += misccache.cacheMiss;
        }
        return cachestats;
    }
}

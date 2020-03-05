// BuDDyFactory.java, created Jan 29, 2003 9:50:57 PM by jwhaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <p>An implementation of BDDFactory that relies on the BuDDy library through a
 * native interface.  You can use this by calling the "BuDDyFactory.init()"
 * method with the desired arguments.  This will return you an instance of the
 * BDDFactory class that you can use.  Call "done()" on that instance when you
 * are finished.</p>
 * 
 * <p>This class (and the BuDDy library) do NOT support multithreading.
 * Furthermore, there can be only one instance active at a time.  You can only
 * call "init()" again after you have called "done()" on the original instance.
 * It is not recommended to call "init()" again after calling "done()" unless
 * you are _completely_ sure that all BDD objects that reference the old
 * factory have been freed.</p>
 * 
 * <p>If you really need multiple BDD factories, consider using the JFactory
 * class for the additional BDD factories --- JFactory can have multiple
 * factory instances active at a time.</p>
 * 
 * @see net.sf.javabdd.BDDFactory
 * 
 * @author John Whaley
 * @version $Id: BuDDyFactory.java 480 2010-11-16 01:29:49Z robimalik $
 */
public class BuDDyFactory extends BDDFactoryIntImpl {

    public static BDDFactory init(int nodenum, int cachesize) {
        BuDDyFactory f = new BuDDyFactory();
        f.initialize(nodenum, cachesize);
        return f;
    }
    
    /**
     * Single factory instance.  Only one factory object is enabled at a time.
     */
    private static BuDDyFactory INSTANCE;
    
    static {
        String libname = getProperty("buddylib", "buddy");
        try {
            System.loadLibrary(libname);
        } catch (java.lang.UnsatisfiedLinkError x) {
            // Cannot find library, try loading it from the current directory...
            libname = System.mapLibraryName(libname);
            String currentdir = getProperty("user.dir", ".");
            String sep = getProperty("file.separator", "/");
            String filename = currentdir+sep+libname;
            try {
                System.load(filename);
            } catch (java.lang.UnsatisfiedLinkError y) {
                File f = new File(filename);
                if (!f.exists()) throw y;
                // Try to make a copy and use that.
                try {
                    File f2 = File.createTempFile("buddy", ".dll");
                    copyFile(f, f2);
                    f2.deleteOnExit();
                    System.out.println("buddy.dll is in use, linking temporary copy "+f2);
                    System.load(f2.getAbsolutePath());
                } catch (IOException z) {
                    throw y;
                }
            }
        }
        registerNatives();
    }
    
    private static void copyFile(File in, File out) throws IOException {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }
    
    private static native void registerNatives();
    
    private BuDDyFactory() {}

    /** An invalid id, for use in invalidating BDDs. */
    static final int INVALID_BDD = -1;
    
    // Redirection functions.
    
    protected void addref_impl(int v) { addRef(v); }
    private static native void addRef(int b);
    protected void delref_impl(int v) { delRef(v); }
    private static native void delRef(int b);
    protected int zero_impl() { return 0; }
    protected int one_impl() { return 1; }
    protected int invalid_bdd_impl() { return INVALID_BDD; }
    protected int var_impl(int v) { return var0(v); }
    private static native int var0(int b);
    protected int level_impl(int v) { return var2Level0(var0(v)); }
    protected int low_impl(int v) { return low0(v); }
    private static native int low0(int b);
    protected int high_impl(int v) { return high0(v); }
    private static native int high0(int b);
    protected int ithVar_impl(int var) { return ithVar0(var); }
    private static native int ithVar0(int var);
    protected int nithVar_impl(int var) { return nithVar0(var); }
    private static native int nithVar0(int var);
    
    protected int makenode_impl(int lev, int lo, int hi) { return ite0(ithVar_impl(level2Var0(lev)), hi, lo); }
    protected int ite_impl(int v1, int v2, int v3) { return ite0(v1, v2, v3); }
    private static native int ite0(int b, int c, int d);
    protected int apply_impl(int v1, int v2, BDDOp opr) { return apply0(v1, v2, opr.id); }
    private static native int apply0(int b, int c, int opr);
    protected int not_impl(int v1) { return not0(v1); }
    private static native int not0(int b);
    protected int applyAll_impl(int v1, int v2, BDDOp opr, int v3) { return applyAll0(v1, v2, opr.id, v3); }
    private static native int applyAll0(int b, int c, int opr, int d);
    protected int applyEx_impl(int v1, int v2, BDDOp opr, int v3) { return applyEx0(v1, v2, opr.id, v3); }
    private static native int applyEx0(int b, int c, int opr, int d);
    protected int applyUni_impl(int v1, int v2, BDDOp opr, int v3) { return applyUni0(v1, v2, opr.id, v3); }
    private static native int applyUni0(int b, int c, int opr, int d);
    protected int compose_impl(int v1, int v2, int var) { return compose0(v1, v2, var); }
    private static native int compose0(int b, int c, int var);
    protected int constrain_impl(int v1, int v2) { return constrain0(v1, v2); }
    private static native int constrain0(int b, int c);
    protected int restrict_impl(int v1, int v2) { return restrict0(v1, v2); }
    private static native int restrict0(int b, int var);
    protected int simplify_impl(int v1, int v2) { return simplify0(v1, v2); }
    private static native int simplify0(int b, int d);
    protected int support_impl(int v) { return support0(v); }
    private static native int support0(int b);
    protected int exist_impl(int v1, int v2) { return exist0(v1, v2); }
    private static native int exist0(int b, int var);
    protected int forAll_impl(int v1, int v2) { return forAll0(v1, v2); }
    private static native int forAll0(int b, int var);
    protected int unique_impl(int v1, int v2) { return unique0(v1, v2); }
    private static native int unique0(int b, int var);
    protected int fullSatOne_impl(int v) { return fullSatOne0(v); }
    private static native int fullSatOne0(int b);
    
    protected int replace_impl(int v, BDDPairing p) { return replace0(v, unwrap(p)); }
    private static native int replace0(int b, long p);
    protected int veccompose_impl(int v, BDDPairing p) { return veccompose0(v, unwrap(p)); }
    private static native int veccompose0(int b, long p);
    
    protected int nodeCount_impl(int v) { return nodeCount0(v); }
    private static native int nodeCount0(int b);
    protected double pathCount_impl(int v) { return pathCount0(v); }
    private static native double pathCount0(int b);
    protected double satCount_impl(int v) { return satCount0(v); }
    private static native double satCount0(int b);
    protected int satOne_impl(int v) { return satOne0(v); }
    private static native int satOne0(int b);
    protected int satOne_impl2(int v1, int v2, boolean pol) { return satOne1(v1, v2, pol?1:0); }
    private static native int satOne1(int b, int c, int d);
    protected int nodeCount_impl2(int[] v) { return nodeCount1(v); }
    private static native int nodeCount1(int[] a);
    protected int[] varProfile_impl(int v) { return varProfile0(v); }
    private static native int[] varProfile0(int b);
    protected void printTable_impl(int v) { printTable0(v); }
    private static native void printTable0(int b);
    
    // More redirection functions.
    
    public void addVarBlock(int first, int last, boolean fixed) { addVarBlock1(first, last, fixed); }
    private static native void addVarBlock1(int first, int last, boolean fixed);
    public void varBlockAll() { varBlockAll0(); }
    private static native void varBlockAll0();
    public void clearVarBlocks() { clearVarBlocks0(); }
    private static native void clearVarBlocks0();
    public void printOrder() { printOrder0(); }
    private static native void printOrder0();
    public int getNodeTableSize() { return getAllocNum0(); }
    private static native int getAllocNum0();
    public int getNodeNum() { return getNodeNum0(); }
    private static native int getNodeNum0();
    public int getCacheSize() { return getCacheSize0(); }
    private static native int getCacheSize0();
    public int reorderGain() { return reorderGain0(); }
    private static native int reorderGain0();
    public void printStat() { printStat0(); }
    private static native void printStat0();
    public void printAll() { printAll0(); }
    private static native void printAll0();
    public void setVarOrder(int[] neworder) { setVarOrder0(neworder); }
    private static native void setVarOrder0(int[] neworder);
    public int level2Var(int level) { return level2Var0(level); }
    private static native int level2Var0(int level);
    public int var2Level(int var) { return var2Level0(var); }
    private static native int var2Level0(int var);
    public int getReorderTimes() { return getReorderTimes0(); }
    private static native int getReorderTimes0();
    public void disableReorder() { disableReorder0(); }
    private static native void disableReorder0();
    public void enableReorder() { enableReorder0(); }
    private static native void enableReorder0();
    public int reorderVerbose(int v) { return reorderVerbose0(v); }
    private static native int reorderVerbose0(int v);
    public void reorder(ReorderMethod m) { if (varNum() > 1) reorder0(m.id); }
    private static native void reorder0(int method);
    public void autoReorder(ReorderMethod method) { autoReorder0(method.id); }
    private static native void autoReorder0(int method);
    public void autoReorder(ReorderMethod method, int max) { autoReorder1(method.id, max); }
    private static native void autoReorder1(int method, int max);
    public void swapVar(int v1, int v2) { swapVar0(v1, v2); }
    private static native void swapVar0(int v1, int v2);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#initialize(int, int)
     */
    protected void initialize(int nodenum, int cachesize) {
        if (INSTANCE != null)
            throw new InternalError("Error: BDDFactory already initialized.");
        INSTANCE = this;
        initialize0(nodenum, cachesize);
    }
    private static native void initialize0(int nodenum, int cachesize);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#isInitialized()
     */
    public boolean isInitialized() {
        return isInitialized0();
    }
    private static native boolean isInitialized0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#done()
     */
    public void done() {
        super.done();
        INSTANCE = null;
        done0();
    }
    private static native void done0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setError(int)
     */
    public void setError(int code) {
        setError0(code);
    }
    private static native void setError0(int code);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearError()
     */
    public void clearError() {
        clearError0();
    }
    private static native void clearError0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxNodeNum(int)
     */
    public int setMaxNodeNum(int size) {
        return setMaxNodeNum0(size);
    }
    private static native int setMaxNodeNum0(int size);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMinFreeNodes(double)
     */
    public double setMinFreeNodes(double x) {
        return setMinFreeNodes0((int)(x * 100.)) / 100.;
    }
    private static native int setMinFreeNodes0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxIncrease(int)
     */
    public int setMaxIncrease(int x) {
        return setMaxIncrease0(x);
    }
    private static native int setMaxIncrease0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setIncreaseFactor(double)
     */
    public double setIncreaseFactor(double x) {
        return setIncreaseFactor0(x);
    }
    private static native double setIncreaseFactor0(double x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheRatio(int)
     */
    public double setCacheRatio(double x) {
        return setCacheRatio0((int)x);
    }
    private static native int setCacheRatio0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setNodeTableSize(int)
     */
    public int setNodeTableSize(int x) {
        return setNodeTableSize0(x);
    }
    private static native int setNodeTableSize0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheSize(int)
     */
    public int setCacheSize(int x) {
        return setCacheSize0(x);
    }
    private static native int setCacheSize0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varNum()
     */
    public int varNum() {
        return varNum0();
    }
    private static native int varNum0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarNum(int)
     */
    public int setVarNum(int num) {
        return setVarNum0(num);
    }
    private static native int setVarNum0(int num);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#makePair()
     */
    public BDDPairing makePair() {
        long ptr = makePair0();
        if (USE_FINALIZER) {
            return new BuDDyPairingWithFinalizer(ptr);
        } else {
            return new BuDDyPairing(ptr);
        }
    }
    private static native long makePair0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#load(java.lang.String)
     */
    public BDD load(String filename) {
        int id = load0(filename);
        return makeBDD(id);
    }
    private static native int load0(String filename);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#save(java.lang.String, net.sf.javabdd.BDD)
     */
    public void save(String filename, BDD b) {
        save0(filename, unwrap(b));
    }
    private static native void save0(String filename, int b);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderMethod()
     */
    public BDDFactory.ReorderMethod getReorderMethod() {
        int method = getReorderMethod0();
        switch (method) {
            case 0: return REORDER_NONE;
            case 1: return REORDER_WIN2;
            case 2: return REORDER_WIN2ITE;
            case 3: return REORDER_WIN3;
            case 4: return REORDER_WIN3ITE;
            case 5: return REORDER_SIFT;
            case 6: return REORDER_SIFTITE;
            case 7: return REORDER_RANDOM;
            default: throw new BDDException();
        }
    }
    private static native int getReorderMethod0();

    static long unwrap(BDDPairing p) {
        return ((BuDDyPairing)p)._ptr;
    }
    
    /* (non-Javadoc)
     * An implementation of a BDDPairing, used by the BuDDy interface.
     */
    private static class BuDDyPairing extends BDDPairing {
        
        private long _ptr;
        
        private BuDDyPairing(long ptr) {
                this._ptr = ptr;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            set0(_ptr, oldvar, newvar);
        }
        private static native void set0(long p, int oldvar, int newvar);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int[], int[])
         */
        public void set(int[] oldvar, int[] newvar) {
            set1(_ptr, oldvar, newvar);
        }
        private static native void set1(long p, int[] oldvar, int[] newvar);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, net.sf.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            set2(_ptr, oldvar, unwrap(newvar));
        }
        private static native void set2(long p, int oldvar, int newbdd);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int[], net.sf.javabdd.BDD[])
         */
        public void set(int[] oldvar, BDD[] newvar) {
            set3(_ptr, oldvar, unwrap(Arrays.asList(newvar)));
        }
        private static native void set3(long p, int[] oldvar, int[] newbdds);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#reset()
         */
        public void reset() {
            reset0(_ptr);
        }
        private static native void reset0(long ptr);
        
        /**
         * Free the memory allocated for this pair.
         */
        public void free() {
            if (_ptr != 0) free0(_ptr);
            _ptr = 0;
        }
        private static native void free0(long p);
        
    }
    
    private static class BuDDyPairingWithFinalizer extends BuDDyPairing {
        
        private BuDDyPairingWithFinalizer(long ptr) {
            super(ptr);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#finalize()
         */
        protected void finalize() throws Throwable {
            super.finalize();
            free();
        }

    }
    
    public static final String REVISION = "$Revision: 480 $";
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getVersion()
     */
    public String getVersion() {
        return getVersion0()+" rev"+REVISION.substring(11, REVISION.length()-2);
    }
    private static native String getVersion0();
    
    // Called by native code.
    private static void gc_callback(int i) {
        INSTANCE.gbc_handler(i!=0, INSTANCE.gcstats);
    }
    
    // Called by native code.
    private static void reorder_callback(int i) {
        INSTANCE.reorder_handler(i!=0, INSTANCE.reorderstats);
    }
    
    // Called by native code.
    private static void resize_callback(int i, int j) {
        INSTANCE.resize_handler(i, j);
    }
}

//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2003-2024 John Whaley and com.github.javabdd contributors
//
// See the CONTRIBUTORS file(s) distributed with this work for additional
// information regarding copyright ownership.
//
// This program and the accompanying materials are made available under the
// terms of the GNU Library General Public License v2 or later, which is
// available at https://spdx.org/licenses/LGPL-2.0-or-later.html
//
// SPDX-License-Identifier: LGPL-2.0-or-later
//////////////////////////////////////////////////////////////////////////////

package com.github.javabdd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Interface for the creation and manipulation of BDDs.
 *
 * @see com.github.javabdd.BDD
 */
public abstract class BDDFactory {
    private static final boolean DEBUG = false;

    private static final boolean ASSERT_FILL_IN_VAR_INDICES = false;

    public static final String getProperty(String key, String def) {
        try {
            return System.getProperty(key, def);
        } catch (AccessControlException e) {
            return def;
        }
    }

    /**
     * Initializes a BDD factory with the given initial node table size and operation cache size. Uses the "java"
     * factory.
     *
     * @param nodenum initial node table size
     * @param cachesize operation cache size
     * @return BDD factory object
     */
    public static BDDFactory init(int nodenum, int cachesize) {
        String bddpackage = getProperty("bdd", "java");
        return init(bddpackage, nodenum, cachesize);
    }

    /**
     * Initializes a BDD factory of the given type with the given initial node table size and operation cache size. The
     * type is a string that can be "j", "java", "test", "typed", or a name of a class that has an init() method that
     * returns a BDDFactory. If it fails, it falls back to the "java" factory.
     *
     * @param bddpackage BDD package string identifier
     * @param nodenum initial node table size
     * @param cachesize operation cache size
     * @return BDD factory object
     */
    public static BDDFactory init(String bddpackage, int nodenum, int cachesize) {
        try {
            if (bddpackage.equals("j") || bddpackage.equals("java")) {
                return JFactory.init(nodenum, cachesize);
            } else if (bddpackage.equals("zdd")) {
                BDDFactory bdd = JFactory.init(nodenum, cachesize);
                ((JFactory)bdd).ZDD = true;
                return bdd;
            } else {
                System.err.println("Unknown BDD package: " + bddpackage);
            }
        } catch (LinkageError e) {
            System.err.println("Could not load BDD package " + bddpackage + ": " + e.getLocalizedMessage());
        }
        try {
            Class<?> c = Class.forName(bddpackage);
            Method m = c.getMethod("init", new Class[] {int.class, int.class});
            return (BDDFactory)m.invoke(null, new Object[] {nodenum, cachesize});
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        // falling back to default java implementation.
        return JFactory.init(nodenum, cachesize);
    }

    /**
     * Logical 'and'.
     */
    public static final BDDOp and = new BDDOp(0, "and");

    /**
     * Logical 'xor'.
     */
    public static final BDDOp xor = new BDDOp(1, "xor");

    /**
     * Logical 'or'.
     */
    public static final BDDOp or = new BDDOp(2, "or");

    /**
     * Logical 'nand'.
     */
    public static final BDDOp nand = new BDDOp(3, "nand");

    /**
     * Logical 'nor'.
     */
    public static final BDDOp nor = new BDDOp(4, "nor");

    /**
     * Logical 'implication'.
     */
    public static final BDDOp imp = new BDDOp(5, "imp");

    /**
     * Logical 'bi-implication'.
     */
    public static final BDDOp biimp = new BDDOp(6, "biimp");

    /**
     * Set difference.
     */
    public static final BDDOp diff = new BDDOp(7, "diff");

    /**
     * Less than.
     */
    public static final BDDOp less = new BDDOp(8, "less");

    /**
     * Inverse implication.
     */
    public static final BDDOp invimp = new BDDOp(9, "invimp");

    /**
     * Enumeration class for binary operations on BDDs. Use the static fields in BDDFactory to access the different
     * binary operations.
     */
    public static class BDDOp {
        final int id;

        final String name;

        private BDDOp(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Construct a new BDDFactory.
     */
    protected BDDFactory() {
        String s = this.getClass().toString();
        if (DEBUG) {
            s = s.substring(s.lastIndexOf('.') + 1);
            System.out.println("Using BDD package: " + s);
        }
    }

    /**
     * Returns true if this is a ZDD factory, false otherwise.
     *
     * @return true if this is a ZDD factory, false otherwise
     */
    public boolean isZDD() {
        return false;
    }

    /**
     * Get the constant false BDD.
     *
     * <p>
     * Compare to bdd_false.
     * </p>
     *
     * @return constant false BDD
     */
    public abstract BDD zero();

    /**
     * Get the constant true BDD.
     *
     * <p>
     * Compare to bdd_true.
     * </p>
     *
     * @return constant true BDD
     */
    public abstract BDD one();

    /**
     * Get the constant universe BDD. (The universe BDD differs from the one BDD in ZDD mode.)
     *
     * <p>
     * Compare to bdd_true.
     * </p>
     *
     * @return constant universe BDD
     */
    public BDD universe() {
        return one();
    }

    /**
     * Get an empty BDDVarSet.
     *
     * <p>
     * Compare to bdd_true.
     * </p>
     *
     * @return empty BDDVarSet
     */
    public BDDVarSet emptySet() {
        return new BDDVarSet.DefaultImpl(one());
    }

    /**
     * Build a cube from an array of variables.
     *
     * <p>
     * Compare to bdd_buildcube.
     * </p>
     *
     * @param value bitset
     * @param variables BDDs for variables
     * @return cube
     */
    public BDD buildCube(int value, List<BDD> variables) {
        BDD result = universe();
        Iterator<BDD> i = variables.iterator();
        // int z = 0;
        while (i.hasNext()) {
            BDD var = i.next();
            if ((value & 0x1) != 0) {
                var = var.id();
            } else {
                var = var.not();
            }
            result.andWith(var);
            // ++z;
            value >>= 1;
        }
        return result;
    }

    /**
     * Build a cube from an array of variables.
     *
     * <p>
     * Compare to bdd_ibuildcube.
     * </p>
     *
     * @param value bitset
     * @param variables variables indices
     * @return cube
     */
    public BDD buildCube(int value, int[] variables) {
        BDD result = universe();
        for (int z = 0; z < variables.length; z++, value >>= 1) {
            BDD v;
            if ((value & 0x1) != 0) {
                v = ithVar(variables[variables.length - z - 1]);
            } else {
                v = nithVar(variables[variables.length - z - 1]);
            }
            result.andWith(v);
        }
        return result;
    }

    /**
     * Builds a BDD variable set from an integer array. The integer array {@code varset} holds the variable numbers. The
     * BDD variable set is represented by a conjunction of all the variables in their positive form.
     *
     * <p>
     * Compare to bdd_makeset.
     * </p>
     *
     * @param varset variable array
     * @return BDD variable set
     */
    public BDDVarSet makeSet(int[] varset) {
        BDDVarSet res = emptySet();
        int varnum = varset.length;
        for (int v = varnum - 1; v >= 0; --v) {
            res.unionWith(varset[v]);
        }
        return res;
    }

    /**** STARTUP / SHUTDOWN ****/

    /**
     * Compare to bdd_init.
     *
     * @param nodenum the initial number of BDD nodes
     * @param cachesize the size of caches used by the BDD operators
     */
    protected abstract void initialize(int nodenum, int cachesize);

    /**
     * Returns true if this BDD factory is initialized, false otherwise.
     *
     * <p>
     * Compare to bdd_isrunning.
     * </p>
     *
     * @return true if this BDD factory is initialized
     */
    public abstract boolean isInitialized();

    /**
     * Reset the BDD factory to its initial state. Everything is reallocated from scratch. This is like calling done()
     * followed by initialize().
     */
    public void reset() {
        int nodes = getNodeTableSize();
        int cache = getCacheSize();
        domain = null;
        fdvarnum = 0;
        firstbddvar = 0;
        done();
        initialize(nodes, cache);
    }

    /**
     * This function frees all memory used by the BDD package and resets the package to its uninitialized state. The BDD
     * package is no longer usable after this call.
     *
     * <p>
     * Compare to bdd_done.
     * </p>
     */
    public abstract void done();

    /**
     * Sets the error condition. This will cause the BDD package to throw an exception at the next garbage collection.
     *
     * @param code the error code to set
     */
    public abstract void setError(int code);

    /**
     * Clears any outstanding error condition.
     */
    public abstract void clearError();

    /**** CACHE/TABLE PARAMETERS ****/

    /**
     * Set the maximum available number of BDD nodes.
     *
     * <p>
     * Compare to bdd_setmaxnodenum.
     * </p>
     *
     * @param size maximum number of nodes
     * @return old value
     */
    public abstract int setMaxNodeNum(int size);

    /**
     * Set minimum percentage of nodes to be reclaimed after a garbage collection. If this percentage is not reclaimed,
     * the node table will be grown. The range of x is 0..1. The default is .20.
     *
     * <p>
     * Compare to bdd_setminfreenodes.
     * </p>
     *
     * @param x number from 0 to 1
     * @return old value
     */
    public abstract double setMinFreeNodes(double x);

    /**
     * Set maximum number of nodes by which to increase node table after a garbage collection.
     *
     * <p>
     * Compare to bdd_setmaxincrease.
     * </p>
     *
     * @param x maximum number of nodes by which to increase node table
     * @return old value
     */
    public abstract int setMaxIncrease(int x);

    /**
     * Set factor by which to increase node table after a garbage collection. The amount of growth is still limited by
     * {@code setMaxIncrease()}.
     *
     * @param x factor by which to increase node table after GC
     * @return old value
     */
    public abstract double setIncreaseFactor(double x);

    /**
     * Sets the cache ratio for the operator caches. When the node table grows, operator caches will also grow to
     * maintain the ratio. A ratio of {@code 0.5} leads to caches that are half the node table size, while a ratio
     * of {@code 2.0} leads to caches that are twice the node table size.
     *
     * <p>
     * Compare to bdd_setcacheratio.
     * </p>
     *
     * @param x cache ratio
     * @return old cache ratio
     */
    public abstract double setCacheRatio(double x);

    /**
     * Sets the node table size.
     *
     * @param n new size of table
     * @return old size of table
     */
    public abstract int setNodeTableSize(int n);

    /**
     * Sets cache size.
     *
     * @param n new cache size
     * @return old cache size
     */
    public abstract int setCacheSize(int n);

    /**** VARIABLE NUMBERS ****/

    /**
     * Returns the number of defined variables.
     *
     * <p>
     * Compare to bdd_varnum.
     * </p>
     *
     * @return number of defined variables
     */
    public abstract int varNum();

    /**
     * Set the number of used BDD variables. It can be called more than one time, but only to increase the number of
     * variables.
     *
     * <p>
     * Compare to bdd_setvarnum.
     * </p>
     *
     * @param num new number of BDD variables
     * @return old number of BDD variables
     */
    public abstract int setVarNum(int num);

    /**
     * Add extra BDD variables. Extends the current number of allocated BDD variables with num extra variables.
     *
     * <p>
     * Compare to bdd_extvarnum.
     * </p>
     *
     * @param num number of BDD variables to add
     * @return old number of BDD variables
     */
    public int extVarNum(int num) {
        int start = varNum();
        if (num < 0 || num > 0x3FFFFFFF) {
            throw new BDDException();
        }
        setVarNum(start + num);
        return start;
    }

    /**
     * Returns a BDD representing the I'th variable. (One node with the children true and false.) The requested variable
     * must be in the (zero-indexed) range defined by {@code setVarNum}.
     *
     * <p>
     * Compare to bdd_ithvar.
     * </p>
     *
     * @param var the variable number
     * @return the I'th variable on success, otherwise the constant false BDD
     */
    public abstract BDD ithVar(int var);

    /**
     * Returns a BDD representing the negation of the I'th variable. (One node with the children false and true.) The
     * requested variable must be in the (zero-indexed) range defined by {@code setVarNum}.
     *
     * <p>
     * Compare to bdd_nithvar.
     * </p>
     *
     * @param var the variable number
     * @return the negated I'th variable on success, otherwise the constant false BDD
     */
    public abstract BDD nithVar(int var);

    /**** INPUT / OUTPUT ****/

    /**
     * Prints all used entries in the node table.
     *
     * <p>
     * Compare to bdd_printall.
     * </p>
     */
    public abstract void printAll();

    /**
     * Prints the node table entries used by a BDD.
     *
     * <p>
     * Compare to bdd_printtable.
     * </p>
     *
     * @param b BDD
     */
    public abstract void printTable(BDD b);

    /**
     * Loads a BDD from a file.
     *
     * <p>
     * Compare to bdd_load.
     * </p>
     *
     * @param filename filename
     * @return BDD
     * @throws IOException In case of an I/O error.
     */
    public BDD load(String filename) throws IOException {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(filename));
            BDD result = load(r);
            return result;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                }
            }
        }
    }
    // TODO: error code from bdd_load (?)

    /**
     * Loads a BDD from the given input.
     *
     * <p>
     * Compare to bdd_load.
     * </p>
     *
     * @param ifile reader
     * @return BDD
     * @throws IOException In case of an I/O error.
     */
    public BDD load(BufferedReader ifile) throws IOException {
        return load(ifile, null);
    }

    /**
     * Loads a BDD from the given input, translating BDD variables according to the given map.
     *
     * <p>
     * Compare to bdd_load.
     * </p>
     *
     * @param ifile reader
     * @param translate variable translation map
     * @return BDD
     * @throws IOException In case of an I/O error.
     */
    public BDD load(BufferedReader ifile, int[] translate) throws IOException {
        tokenizer = null;

        int lh_nodenum = Integer.parseInt(readNext(ifile));
        int vnum = Integer.parseInt(readNext(ifile));

        // Check for constant true / false
        if (lh_nodenum == 0 && vnum == 0) {
            int r = Integer.parseInt(readNext(ifile));
            return r == 0 ? zero() : universe();
        }

        // Not actually used.
        int[] loadvar2level = new int[vnum];
        for (int n = 0; n < vnum; n++) {
            loadvar2level[n] = Integer.parseInt(readNext(ifile));
        }

        if (vnum > varNum()) {
            setVarNum(vnum);
        }

        LoadHash[] lh_table = new LoadHash[lh_nodenum];
        for (int n = 0; n < lh_nodenum; n++) {
            lh_table[n] = new LoadHash();
            lh_table[n].first = -1;
            lh_table[n].next = n + 1;
        }
        lh_table[lh_nodenum - 1].next = -1;
        int lh_freepos = 0;

        BDD root = null;
        for (int n = 0; n < lh_nodenum; n++) {
            int key = Integer.parseInt(readNext(ifile));
            int var = Integer.parseInt(readNext(ifile));
            if (translate != null) {
                var = translate[var];
            }
            int lowi = Integer.parseInt(readNext(ifile));
            int highi = Integer.parseInt(readNext(ifile));

            BDD low, high;

            low = loadhash_get(lh_table, lh_nodenum, lowi);
            high = loadhash_get(lh_table, lh_nodenum, highi);

            if (low == null || high == null || var < 0) {
                throw new BDDException("Incorrect file format");
            }

            BDD b = ithVar(var);
            root = b.ite(high, low);
            b.free();
            if (low.isZero() || low.isOne()) {
                low.free();
            }
            if (high.isZero() || high.isOne()) {
                high.free();
            }

            int hash = key % lh_nodenum;
            int pos = lh_freepos;

            lh_freepos = lh_table[pos].next;
            lh_table[pos].next = lh_table[hash].first;
            lh_table[hash].first = pos;

            lh_table[pos].key = key;
            lh_table[pos].data = root;
        }
        @SuppressWarnings("null")
        BDD tmproot = root.id();

        for (int n = 0; n < lh_nodenum; n++) {
            lh_table[n].data.free();
        }

        lh_table = null;
        loadvar2level = null;

        return tmproot;
    }

    /**
     * Used for tokenization during loading.
     */
    protected StringTokenizer tokenizer;

    /**
     * Read the next token from the file.
     *
     * @param ifile reader
     * @return next string token
     * @throws IOException In case of an I/O error.
     */
    protected String readNext(BufferedReader ifile) throws IOException {
        while (tokenizer == null || !tokenizer.hasMoreTokens()) {
            String s = ifile.readLine();
            if (s == null) {
                throw new BDDException("Incorrect file format");
            }
            tokenizer = new StringTokenizer(s);
        }
        return tokenizer.nextToken();
    }

    /**
     * LoadHash is used to hash during loading.
     */
    protected static class LoadHash {
        int key;

        BDD data;

        int first;

        int next;
    }

    /**
     * Gets a BDD from the load hash table.
     *
     * @param lh_table load hash table
     * @param lh_nodenum node number
     * @param key key
     * @return BDD
     */
    protected BDD loadhash_get(LoadHash[] lh_table, int lh_nodenum, int key) {
        if (key < 0) {
            return null;
        }
        if (key == 0) {
            return zero();
        }
        if (key == 1) {
            return universe();
        }

        int hash = lh_table[key % lh_nodenum].first;

        while (hash != -1 && lh_table[hash].key != key) {
            hash = lh_table[hash].next;
        }

        if (hash == -1) {
            return null;
        }
        return lh_table[hash].data;
    }

    /**
     * Saves a BDD to a file.
     *
     * <p>
     * Compare to bdd_save.
     * </p>
     *
     * @param filename filename
     * @param var BDD
     * @throws IOException In case of an I/O error.
     */
    public void save(String filename, BDD var) throws IOException {
        BufferedWriter is = null;
        try {
            is = new BufferedWriter(new FileWriter(filename));
            save(is, var);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
    // TODO: error code from bdd_save (?)

    /**
     * Saves a BDD to an output writer.
     *
     * <p>
     * Compare to bdd_save.
     * </p>
     *
     * @param out writer
     * @param r BDD
     * @throws IOException In case of an I/O error.
     */
    public void save(BufferedWriter out, BDD r) throws IOException {
        if (r.isOne() || r.isZero()) {
            out.write("0 0 " + (r.isOne() ? 1 : 0) + "\n");
            return;
        }

        out.write(r.nodeCount() + " " + varNum() + "\n");

        for (int x = 0; x < varNum(); x++) {
            out.write(var2Level(x) + " ");
        }
        out.write("\n");

        // Map visited = new HashMap();
        BitSet visited = new BitSet(getNodeTableSize());
        save_rec(out, visited, r.id());

        // for (Iterator it = visited.keySet().iterator(); it.hasNext(); ) {
        // BDD b = (BDD) it.next();
        // if (b != r) b.free();
        // }
    }

    /**
     * Helper function for save().
     *
     * @param out writer
     * @param visited visited nodes bitset
     * @param root root BDD
     * @return bitset index
     * @throws IOException In case of an I/O error.
     */
    protected int save_rec(BufferedWriter out, BitSet visited, BDD root) throws IOException {
        if (root.isZero()) {
            root.free();
            return 0;
        }
        if (root.isOne()) {
            root.free();
            return 1;
        }
        int i = root.hashCode();
        if (visited.get(i)) {
            root.free();
            return i;
        }
        int v = i;
        visited.set(i);

        BDD h = root.high();

        BDD l = root.low();

        int rootvar = root.var();
        root.free();

        int lo = save_rec(out, visited, l);

        int hi = save_rec(out, visited, h);

        out.write(i + " ");
        out.write(rootvar + " ");
        out.write(lo + " ");
        out.write(hi + "\n");

        return v;
    }

    // TODO: bdd_blockfile_hook
    // TODO: bdd_versionnum, bdd_versionstr

    /**** REORDERING ****/

    /**
     * Convert from a BDD level to a BDD variable.
     *
     * <p>
     * Compare to bdd_level2var.
     * </p>
     *
     * @param level BDD level
     * @return BDD variable
     */
    public abstract int level2Var(int level);

    /**
     * Convert from a BDD variable to a BDD level.
     *
     * <p>
     * Compare to bdd_var2level.
     * </p>
     *
     * @param var BDD variable
     * @return BDD level
     */
    public abstract int var2Level(int var);

    /**
     * No reordering.
     */
    public static final ReorderMethod REORDER_NONE = new ReorderMethod(0, "NONE");

    /**
     * Reordering using a sliding window of 2.
     */
    public static final ReorderMethod REORDER_WIN2 = new ReorderMethod(1, "WIN2");

    /**
     * Reordering using a sliding window of 2, iterating until no further progress.
     */
    public static final ReorderMethod REORDER_WIN2ITE = new ReorderMethod(2, "WIN2ITE");

    /**
     * Reordering using a sliding window of 3.
     */
    public static final ReorderMethod REORDER_WIN3 = new ReorderMethod(5, "WIN3");

    /**
     * Reordering using a sliding window of 3, iterating until no further progress.
     */
    public static final ReorderMethod REORDER_WIN3ITE = new ReorderMethod(6, "WIN3ITE");

    /**
     * Reordering where each block is moved through all possible positions. The best of these is then used as the new
     * position. Potentially a very slow but good method.
     */
    public static final ReorderMethod REORDER_SIFT = new ReorderMethod(3, "SIFT");

    /**
     * Same as REORDER_SIFT, but the process is repeated until no further progress is done. Can be extremely slow.
     */
    public static final ReorderMethod REORDER_SIFTITE = new ReorderMethod(4, "SIFTITE");

    /**
     * Selects a random position for each variable. Mostly used for debugging purposes.
     */
    public static final ReorderMethod REORDER_RANDOM = new ReorderMethod(7, "RANDOM");

    /**
     * Enumeration class for method reordering techniques. Use the static fields in BDDFactory to access the different
     * reordering techniques.
     */
    public static class ReorderMethod {
        final int id;

        final String name;

        private ReorderMethod(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Reorder the BDD with the given method.
     *
     * <p>
     * Compare to bdd_reorder.
     * </p>
     *
     * @param m reorder method
     */
    public abstract void reorder(ReorderMethod m);

    /**
     * Enables automatic reordering. If method is REORDER_NONE then automatic reordering is disabled.
     *
     * <p>
     * Compare to bdd_autoreorder.
     * </p>
     *
     * @param method reorder method
     */
    public abstract void autoReorder(ReorderMethod method);

    /**
     * Enables automatic reordering with the given (maximum) number of reorderings. If method is REORDER_NONE then
     * automatic reordering is disabled.
     *
     * <p>
     * Compare to bdd_autoreorder_times.
     * </p>
     *
     * @param method reorder method
     * @param max maximum number of reorderings
     */
    public abstract void autoReorder(ReorderMethod method, int max);

    /**
     * Returns the current reorder method as defined by autoReorder.
     *
     * <p>
     * Compare to bdd_getreorder_method.
     * </p>
     *
     * @return ReorderMethod
     */
    public abstract ReorderMethod getReorderMethod();

    /**
     * Returns the number of allowed reorderings left. This value can be defined by autoReorder.
     *
     * <p>
     * Compare to bdd_getreorder_times.
     * </p>
     *
     * @return number of allowed reorderings left
     */
    public abstract int getReorderTimes();

    /**
     * Disable automatic reordering until enableReorder is called. Reordering is enabled by default as soon as any
     * variable blocks have been defined.
     *
     * <p>
     * Compare to bdd_disable_reorder.
     * </p>
     */
    public abstract void disableReorder();

    /**
     * Enable automatic reordering after a call to disableReorder.
     *
     * <p>
     * Compare to bdd_enable_reorder.
     * </p>
     */
    public abstract void enableReorder();

    /**
     * Enables verbose information about reordering. A value of zero means no information, one means some information
     * and greater than one means lots of information.
     *
     * @param v the new verbose level
     * @return the old verbose level
     */
    public abstract int reorderVerbose(int v);

    /**
     * This function sets the current variable order to be the one defined by neworder. The variable parameter neworder
     * is interpreted as a sequence of variable indices and the new variable order is exactly this sequence. The array
     * must contain all the variables defined so far. If, for instance the current number of variables is 3 and neworder
     * contains [1; 0; 2] then the new variable order is v1 &lt; v0 &lt; v2.
     *
     * <p>
     * Note that this operation must walk through the node table many times, and therefore it is much more efficient to
     * call this when the node table is small.
     * </p>
     *
     * @param neworder new variable order
     */
    public abstract void setVarOrder(int[] neworder);

    /**
     * Gets the current variable order.
     *
     * @return variable order
     */
    public int[] getVarOrder() {
        int n = varNum();
        int[] result = new int[n];
        for (int i = 0; i < n; ++i) {
            result[i] = level2Var(i);
        }
        return result;
    }

    /**
     * Make a new BDDPairing object.
     *
     * <p>
     * Compare to bdd_newpair.
     * </p>
     *
     * @return BDD pairing
     */
    public abstract BDDPairing makePair();

    /**
     * Make a new pairing that maps from one variable to another.
     *
     * @param oldvar old variable
     * @param newvar new variable
     * @return BDD pairing
     */
    public BDDPairing makePair(int oldvar, int newvar) {
        BDDPairing p = makePair();
        p.set(oldvar, newvar);
        return p;
    }

    /**
     * Make a new pairing that maps from one variable to another BDD.
     *
     * @param oldvar old variable
     * @param newvar new BDD
     * @return BDD pairing
     */
    public BDDPairing makePair(int oldvar, BDD newvar) {
        BDDPairing p = makePair();
        p.set(oldvar, newvar);
        return p;
    }

    /**
     * Make a new pairing that maps from one BDD domain to another.
     *
     * @param oldvar old BDD domain
     * @param newvar new BDD domain
     * @return BDD pairing
     */
    public BDDPairing makePair(BDDDomain oldvar, BDDDomain newvar) {
        BDDPairing p = makePair();
        p.set(oldvar, newvar);
        return p;
    }

    /**
     * Swap two variables.
     *
     * <p>
     * Compare to bdd_swapvar.
     * </p>
     *
     * @param v1 first variable
     * @param v2 second variable
     */
    public abstract void swapVar(int v1, int v2);

    /**** VARIABLE BLOCKS ****/

    /**
     * Adds a new variable block for reordering.
     *
     * <p>
     * Creates a new variable block with the variables in the variable set var. The variables in var must be contiguous.
     * </p>
     *
     * <p>
     * The fixed parameter sets the block to be fixed (no reordering of its child blocks is allowed) or free.
     * </p>
     *
     * <p>
     * Compare to bdd_addvarblock.
     * </p>
     *
     * @param var variable
     * @param fixed fixed or free
     */
    public void addVarBlock(BDDVarSet var, boolean fixed) {
        int[] v = var.toArray();
        int first, last;
        if (v.length < 1) {
            throw new BDDException("Invalid parameter for addVarBlock");
        }

        first = last = v[0];

        for (int n = 1; n < v.length; n++) {
            if (v[n] < first) {
                first = v[n];
            }
            if (v[n] > last) {
                last = v[n];
            }
        }

        addVarBlock(first, last, fixed);
    }
    // TODO: handle error code for addVarBlock.

    /**
     * Adds a new variable block for reordering.
     *
     * <p>
     * Creates a new variable block with the variables numbered first through last, inclusive.
     * </p>
     *
     * <p>
     * The fixed parameter sets the block to be fixed (no reordering of its child blocks is allowed) or free.
     * </p>
     *
     * <p>
     * Compare to bdd_intaddvarblock.
     * </p>
     *
     * @param first first variable number
     * @param last last variable number
     * @param fixed fixed or free
     */
    public abstract void addVarBlock(int first, int last, boolean fixed);
    // TODO: handle error code for addVarBlock.
    // TODO: fdd_intaddvarblock (?)

    /**
     * Add a variable block for all variables.
     *
     * <p>
     * Adds a variable block for all BDD variables declared so far. Each block contains one variable only. More variable
     * blocks can be added later with the use of addVarBlock -- in this case the tree of variable blocks will have the
     * blocks of single variables as the leafs.
     * </p>
     *
     * <p>
     * Compare to bdd_varblockall.
     * </p>
     */
    public abstract void varBlockAll();

    /**
     * Clears all the variable blocks that have been defined by calls to addVarBlock.
     *
     * <p>
     * Compare to bdd_clrvarblocks.
     * </p>
     */
    public abstract void clearVarBlocks();

    /**
     * Prints an indented list of the variable blocks.
     *
     * <p>
     * Compare to bdd_printorder.
     * </p>
     */
    public abstract void printOrder();

    /**** BDD STATS ****/

    /**
     * Counts the number of shared nodes in a collection of BDDs. Counts all distinct nodes that are used in the BDDs --
     * if a node is used in more than one BDD then it only counts once.
     *
     * <p>
     * Compare to bdd_anodecount.
     * </p>
     *
     * @param r collection of BDDs
     * @return number of shared nodes
     */
    public abstract int nodeCount(Collection<BDD> r);

    /**
     * Get the number of allocated nodes. This includes both dead and active nodes.
     *
     * <p>
     * Compare to bdd_getallocnum.
     * </p>
     *
     * @return number of allocated nodes
     */
    public abstract int getNodeTableSize();

    /**
     * Get the number of active nodes in use. Note that dead nodes that have not been reclaimed yet by a garbage
     * collection are counted as active.
     *
     * <p>
     * Compare to bdd_getnodenum.
     * </p>
     *
     * @return number of active nodes in use
     */
    public abstract int getNodeNum();

    /**
     * Get the current size of the cache, in entries.
     *
     * @return size of cache
     */
    public abstract int getCacheSize();

    /**
     * Calculate the gain in size after a reordering. The value returned is (100*(A-B))/A, where A is previous number of
     * used nodes and B is current number of used nodes.
     *
     * <p>
     * Compare to bdd_reorder_gain.
     * </p>
     *
     * @return gain in size after a reordering
     */
    public abstract int reorderGain();

    /**
     * Print cache statistics.
     *
     * <p>
     * Compare to bdd_printstat.
     * </p>
     */
    public abstract void printStat();

    /**
     * Stores statistics about garbage collections.
     */
    public static class GCStats {
        public int nodes;

        public int freenodes;

        public long time;

        public long sumtime;

        public int num;

        protected GCStats() {
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Garbage collection #");
            sb.append(num);
            sb.append(": ");
            sb.append(nodes);
            sb.append(" nodes / ");
            sb.append(freenodes);
            sb.append(" free");

            sb.append(" / ");
            sb.append((float)time / (float)1000);
            sb.append("s / ");
            sb.append((float)sumtime / (float)1000);
            sb.append("s total");
            return sb.toString();
        }
    }

    /**
     * Singleton object for GC statistics.
     */
    protected GCStats gcstats = new GCStats();

    /**
     * Stores statistics about the last variable reordering.
     */
    public static class ReorderStats {
        public long time;

        public int usednum_before, usednum_after;

        protected ReorderStats() {
        }

        public int gain() {
            if (usednum_before == 0) {
                return 0;
            }

            return (100 * (usednum_before - usednum_after)) / usednum_before;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Went from ");
            sb.append(usednum_before);
            sb.append(" to ");
            sb.append(usednum_after);
            sb.append(" nodes, gain = ");
            sb.append(gain());
            sb.append("% (");
            sb.append(time / 1000f);
            sb.append(" sec)");
            return sb.toString();
        }
    }

    /**
     * Singleton object for reorder statistics.
     */
    protected ReorderStats reorderstats = new ReorderStats();

    /**
     * Stores statistics about the operator cache.
     */
    public static class CacheStats {
        protected boolean enabled = false;

        public long uniqueAccess;

        public long uniqueChain;

        public long uniqueHit;

        public long uniqueMiss;

        public long opAccess;

        public long opHit;

        public long opMiss;

        public long swapCount;

        protected CacheStats() {
        }

        void copyFrom(CacheStats that) {
            this.uniqueAccess = that.uniqueAccess;
            this.uniqueChain = that.uniqueChain;
            this.uniqueHit = that.uniqueHit;
            this.uniqueMiss = that.uniqueMiss;
            this.opAccess = that.opAccess;
            this.opHit = that.opHit;
            this.opMiss = that.opMiss;
            this.swapCount = that.swapCount;
        }

        public void enableMeasurements() {
            enabled = true;
        }

        public void disableMeasurements() {
            enabled = false;
        }

        public void resetMeasurements() {
            uniqueAccess = 0;
            uniqueChain = 0;
            uniqueHit = 0;
            uniqueMiss = 0;
            opAccess = 0;
            opHit = 0;
            opMiss = 0;
            swapCount = 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String newLine = getProperty("line.separator", "\n");
            sb.append(newLine);
            sb.append("Cache statistics");
            sb.append(newLine);
            sb.append("----------------");
            sb.append(newLine);

            sb.append("Unique Access:  ");
            sb.append(uniqueAccess);
            sb.append(newLine);
            sb.append("Unique Chain:   ");
            sb.append(uniqueChain);
            sb.append(newLine);
            sb.append("=> Ave. chain = ");
            if (uniqueAccess > 0) {
                sb.append(((float)uniqueChain) / ((float)uniqueAccess));
            } else {
                sb.append((float)0);
            }
            sb.append(newLine);
            sb.append("Unique Hit:     ");
            sb.append(uniqueHit);
            sb.append(newLine);
            sb.append("Unique Miss:    ");
            sb.append(uniqueMiss);
            sb.append(newLine);
            sb.append("=> Hit rate =   ");
            if (uniqueHit + uniqueMiss > 0) {
                sb.append((uniqueHit) / ((float)uniqueHit + uniqueMiss));
            } else {
                sb.append((float)0);
            }
            sb.append(newLine);
            sb.append("Operator Access:  ");
            sb.append(opAccess);
            sb.append(newLine);
            sb.append("Operator Hits:  ");
            sb.append(opHit);
            sb.append(newLine);
            sb.append("Operator Miss:  ");
            sb.append(opMiss);
            sb.append(newLine);
            sb.append("=> Hit rate =   ");
            if (opHit + opMiss > 0) {
                sb.append((opHit) / ((float)opHit + opMiss));
            } else {
                sb.append((float)0);
            }
            sb.append(newLine);
            sb.append("Swap count =    ");
            sb.append(swapCount);
            sb.append(newLine);
            return sb.toString();
        }
    }

    /**
     * Singleton object for operator cache statistics.
     */
    protected CacheStats cachestats = new CacheStats();

    /**
     * Return the current operator cache statistics for this BDD factory.
     *
     * @return operator cache statistics
     */
    public CacheStats getCacheStats() {
        return cachestats;
    }

    /**
     * Stores statistics about the maximum BDD nodes usage.
     */
    public static class MaxUsedBddNodesStats {
        protected boolean enabled = false;

        protected int maxUsedBddNodes;

        protected MaxUsedBddNodesStats() {
        }

        void copyFrom(MaxUsedBddNodesStats that) {
            this.maxUsedBddNodes = that.maxUsedBddNodes;
        }

        public void enableMeasurements() {
            enabled = true;
        }

        public void disableMeasurements() {
            enabled = false;
        }

        public void resetMeasurements() {
            maxUsedBddNodes = 0;
        }

        public void newMeasurement(int newUsedBddNodes) {
            maxUsedBddNodes = Math.max(newUsedBddNodes, maxUsedBddNodes);
        }

        public int getMaxUsedBddNodes() {
            return maxUsedBddNodes;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Max used BDD nodes: ");
            sb.append(maxUsedBddNodes);
            return sb.toString();
        }
    }

    /**
     * Singleton object for maximum used BDD nodes statistics.
     */
    protected MaxUsedBddNodesStats maxusedbddnodesstats = new MaxUsedBddNodesStats();

    /**
     * Return the current maximum used BDD nodes statistics for this BDD factory.
     *
     * @return maximum used BDD nodes statistics
     */
    public MaxUsedBddNodesStats getMaxUsedBddNodesStats() {
        return maxusedbddnodesstats;
    }

    /**
     * Stores statistics about the maximum memory usage. The data is obtained through best effort, and may not be
     * entirely accurate.
     */
    public static class MaxMemoryStats {
        protected boolean enabled = false;

        protected long maxMemoryBytes;

        protected MaxMemoryStats() {
        }

        void copyFrom(MaxMemoryStats that) {
            this.maxMemoryBytes = that.maxMemoryBytes;
        }

        public void enableMeasurements() {
            enabled = true;
        }

        public void disableMeasurements() {
            enabled = false;
        }

        public void resetMeasurements() {
            maxMemoryBytes = 0;
        }

        public void newMeasurement() {
            long newMemoryBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            maxMemoryBytes = Math.max(newMemoryBytes, maxMemoryBytes);
        }

        public long getMaxMemoryBytes() {
            return maxMemoryBytes;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Max memory: ");
            sb.append(maxMemoryBytes);
            sb.append(" bytes");
            return sb.toString();
        }
    }

    /**
     * Singleton object for maximum memory usage statistics.
     */
    protected MaxMemoryStats maxmemorystats = new MaxMemoryStats();

    /**
     * Return the current maximum memory usage statistics for this BDD factory.
     *
     * @return maximum memory usage statistics
     */
    public MaxMemoryStats getMaxMemoryStats() {
        return maxmemorystats;
    }

    // TODO: bdd_sizeprobe_hook
    // TODO: bdd_reorder_probe

    ///// FINITE DOMAINS /////

    protected BDDDomain[] domain;

    protected int fdvarnum;

    protected int firstbddvar;

    /**
     * Implementors must implement this factory method to create BDDDomain objects of the correct type.
     *
     * @param a index of this domain
     * @param b size of this domain
     * @return BDD domain
     */
    protected BDDDomain createDomain(int a, BigInteger b) {
        return new BDDDomain(a, b) {
            @Override
            public BDDFactory getFactory() {
                return BDDFactory.this;
            }
        };
    }

    /**
     * Creates a new finite domain block of the given size. Allocates log 2 (|domainSize|) BDD variables for the domain.
     *
     * @param domainSize domainSize
     * @return BDD domain
     */
    public BDDDomain extDomain(long domainSize) {
        return extDomain(BigInteger.valueOf(domainSize));
    }

    public BDDDomain extDomain(BigInteger domainSize) {
        return extDomain(new BigInteger[] {domainSize})[0];
    }

    /**
     * Extends the set of finite domain blocks with domains of the given sizes. Each entry in domainSizes is the size of
     * a new finite domain which later on can be used for finite state machine traversal and other operations on finite
     * domains. Each domain allocates log 2 (|domainSizes[i]|) BDD variables to be used later. The ordering is
     * interleaved for the domains defined in each call to extDomain. This means that assuming domain D0 needs 2 BDD
     * variables x1 and x2 , and another domain D1 needs 4 BDD variables y1, y2, y3 and y4, then the order then will be
     * x1, y1, x2, y2, y3, y4. The new domains are returned in order. The BDD variables needed to encode the domain are
     * created for the purpose and do not interfere with the BDD variables already in use.
     *
     * <p>
     * Compare to fdd_extdomain.
     * </p>
     *
     * @param dom domain sizes
     * @return BDD domains
     */
    public BDDDomain[] extDomain(int[] dom) {
        BigInteger[] a = new BigInteger[dom.length];
        for (int i = 0; i < a.length; ++i) {
            a[i] = BigInteger.valueOf(dom[i]);
        }
        return extDomain(a);
    }

    public BDDDomain[] extDomain(long[] dom) {
        BigInteger[] a = new BigInteger[dom.length];
        for (int i = 0; i < a.length; ++i) {
            a[i] = BigInteger.valueOf(dom[i]);
        }
        return extDomain(a);
    }

    public BDDDomain[] extDomain(BigInteger[] domainSizes) {
        int offset = fdvarnum;
        int binoffset;
        int extravars = 0;
        int n, bn;
        boolean more;
        int num = domainSizes.length;

        /* Build domain table */
        if (domain == null) /* First time */ {
            domain = new BDDDomain[num];
        } else /* Allocated before */ {
            if (fdvarnum + num > domain.length) {
                int fdvaralloc = domain.length + Math.max(num, domain.length);
                BDDDomain[] d2 = new BDDDomain[fdvaralloc];
                System.arraycopy(domain, 0, d2, 0, domain.length);
                domain = d2;
            }
        }

        /* Create bdd variable tables */
        for (n = 0; n < num; n++) {
            domain[n + fdvarnum] = createDomain(n + fdvarnum, domainSizes[n]);
            extravars += domain[n + fdvarnum].varNum();
        }

        binoffset = firstbddvar;
        int bddvarnum = varNum();
        if (firstbddvar + extravars > bddvarnum) {
            setVarNum(firstbddvar + extravars);
        }

        /* Set correct variable sequence (interleaved) */
        for (bn = 0, more = true; more; bn++) {
            more = false;

            for (n = 0; n < num; n++) {
                if (bn < domain[n + fdvarnum].varNum()) {
                    more = true;
                    domain[n + fdvarnum].ivar[bn] = binoffset++;
                }
            }
        }

        if (isZDD()) {
            // Need to rebuild varsets for existing domains.
            for (n = 0; n < fdvarnum; n++) {
                domain[n].var.free();
                domain[n].var = makeSet(domain[n].ivar);
            }
        }
        for (n = 0; n < num; n++) {
            domain[n + fdvarnum].var = makeSet(domain[n + fdvarnum].ivar);
        }

        fdvarnum += num;
        firstbddvar += extravars;

        BDDDomain[] r = new BDDDomain[num];
        System.arraycopy(domain, offset, r, 0, num);
        return r;
    }

    /**
     * This function takes two finite domain blocks and merges them into a new one, such that the new one is encoded
     * using both sets of BDD variables.
     *
     * <p>
     * Compare to fdd_overlapdomain.
     * </p>
     *
     * @param d1 first domain
     * @param d2 second domain
     * @return BDD domain
     */
    public BDDDomain overlapDomain(BDDDomain d1, BDDDomain d2) {
        BDDDomain d;
        int n;

        int fdvaralloc = domain.length;
        if (fdvarnum + 1 > fdvaralloc) {
            fdvaralloc += fdvaralloc;
            BDDDomain[] domain2 = new BDDDomain[fdvaralloc];
            System.arraycopy(domain, 0, domain2, 0, domain.length);
            domain = domain2;
        }

        d = domain[fdvarnum];
        d.realsize = d1.realsize.multiply(d2.realsize);
        d.ivar = new int[d1.varNum() + d2.varNum()];

        for (n = 0; n < d1.varNum(); n++) {
            d.ivar[n] = d1.ivar[n];
        }
        for (n = 0; n < d2.varNum(); n++) {
            d.ivar[d1.varNum() + n] = d2.ivar[n];
        }

        d.var = makeSet(d.ivar);
        // bdd_addref(d.var);

        fdvarnum++;
        return d;
    }

    /**
     * Returns a BDD defining all the variable sets used to define the variable blocks in the given array.
     *
     * <p>
     * Compare to fdd_makeset.
     * </p>
     *
     * @param v variable block array
     * @return BDD variable set
     */
    public BDDVarSet makeSet(BDDDomain[] v) {
        BDDVarSet res = emptySet();
        int n;

        for (n = 0; n < v.length; n++) {
            res.unionWith(v[n].set());
        }

        return res;
    }

    /**
     * Clear all allocated finite domain blocks that were defined by extDomain() or overlapDomain().
     *
     * <p>
     * Compare to fdd_clearall.
     * </p>
     */
    public void clearAllDomains() {
        domain = null;
        fdvarnum = 0;
        firstbddvar = 0;
    }

    /**
     * Returns the number of finite domain blocks defined by calls to extDomain().
     *
     * <p>
     * Compare to fdd_domainnum.
     * </p>
     *
     * @return number of finite domain blocks
     */
    public int numberOfDomains() {
        return fdvarnum;
    }

    /**
     * Returns the ith finite domain block, as defined by calls to extDomain().
     *
     * @param i index
     * @return finite domain block
     */
    public BDDDomain getDomain(int i) {
        if (i < 0 || i >= fdvarnum) {
            throw new IndexOutOfBoundsException();
        }
        return domain[i];
    }

    // TODO: fdd_file_hook, fdd_strm_hook

    /**
     * Creates a variable ordering from a string. The resulting order can be passed into {@code setVarOrder()}. Example:
     * in the order "A_BxC_DxExF", the bits for A are first, followed by the bits for B and C interleaved, followed by
     * the bits for D, E, and F interleaved.
     *
     * <p>
     * Obviously, domain names cannot contain the 'x' or '_' characters.
     * </p>
     *
     * @param reverseLocal whether to reverse the bits of each domain
     * @param ordering string representation of ordering
     * @return int[] of ordering
     * @see com.github.javabdd.BDDFactory#setVarOrder(int[])
     */
    public int[] makeVarOrdering(boolean reverseLocal, String ordering) {
        int varnum = varNum();

        int nDomains = numberOfDomains();
        int[][] localOrders = new int[nDomains][];
        for (int i = 0; i < localOrders.length; ++i) {
            localOrders[i] = new int[getDomain(i).varNum()];
        }

        for (int i = 0; i < nDomains; ++i) {
            BDDDomain d = getDomain(i);
            int nVars = d.varNum();
            for (int j = 0; j < nVars; ++j) {
                if (reverseLocal) {
                    localOrders[i][j] = nVars - j - 1;
                } else {
                    localOrders[i][j] = j;
                }
            }
        }

        BDDDomain[] doms = new BDDDomain[nDomains];

        int[] varorder = new int[varnum];

        // System.out.println("Ordering: "+ordering);
        StringTokenizer st = new StringTokenizer(ordering, "x_", true);
        int numberOfDomains = 0, bitIndex = 0;
        boolean[] done = new boolean[nDomains];
        for (int i = 0;; ++i) {
            String s = st.nextToken();
            BDDDomain d;
            for (int j = 0;; ++j) {
                if (j == numberOfDomains()) {
                    throw new BDDException("bad domain: " + s);
                }
                d = getDomain(j);
                if (s.equals(d.getName())) {
                    break;
                }
            }
            if (done[d.getIndex()]) {
                throw new BDDException("duplicate domain: " + s);
            }
            done[d.getIndex()] = true;
            doms[i] = d;
            if (st.hasMoreTokens()) {
                s = st.nextToken();
                if (s.equals("x")) {
                    ++numberOfDomains;
                    continue;
                }
            }
            bitIndex = fillInVarIndices(doms, i - numberOfDomains, numberOfDomains + 1, localOrders, bitIndex,
                    varorder);
            if (!st.hasMoreTokens()) {
                break;
            }
            if (s.equals("_")) {
                numberOfDomains = 0;
            } else {
                throw new BDDException("bad token: " + s);
            }
        }

        for (int i = 0; i < doms.length; ++i) {
            if (!done[i]) {
                throw new BDDException("missing domain #" + i + ": " + getDomain(i));
            }
        }

        while (bitIndex < varorder.length) {
            varorder[bitIndex] = bitIndex;
            ++bitIndex;
        }

        int[] test = new int[varorder.length];
        System.arraycopy(varorder, 0, test, 0, varorder.length);
        Arrays.sort(test);
        for (int i = 0; i < test.length; ++i) {
            if (test[i] != i) {
                throw new BDDException(test[i] + " != " + i);
            }
        }

        return varorder;
    }

    /**
     * Helper function for makeVarOrder().
     *
     * @param doms domains
     * @param domainIndex domain index
     * @param numDomains number of domains
     * @param localOrders local orders
     * @param bitIndex bit index
     * @param varorder variable order
     * @return bit index
     */
    static int fillInVarIndices(BDDDomain[] doms, int domainIndex, int numDomains, int[][] localOrders, int bitIndex,
            int[] varorder)
    {
        // calculate size of largest domain to interleave
        int maxBits = 0;
        for (int i = 0; i < numDomains; ++i) {
            BDDDomain d = doms[domainIndex + i];
            maxBits = Math.max(maxBits, d.varNum());
        }
        // interleave the domains
        for (int bitNumber = 0; bitNumber < maxBits; ++bitNumber) {
            for (int i = 0; i < numDomains; ++i) {
                BDDDomain d = doms[domainIndex + i];
                if (bitNumber < d.varNum()) {
                    int di = d.getIndex();
                    int local = localOrders[di][bitNumber];
                    if (ASSERT_FILL_IN_VAR_INDICES) {
                        if (local >= d.vars().length) {
                            throw new AssertionError("bug!");
                        }
                        if (bitIndex >= varorder.length) {
                            throw new AssertionError("bug2!");
                        }
                    }
                    varorder[bitIndex++] = d.vars()[local];
                }
            }
        }
        return bitIndex;
    }

    /**** BIT VECTORS ****/

    /**
     * Implementors must implement this factory method to create BDDBitVector objects of the correct type.
     *
     * @param a bit number
     * @return BDD bit vector
     */
    protected BDDBitVector createBitVector(int a) {
        return new BDDBitVector(a) {
            @Override
            public BDDFactory getFactory() {
                return BDDFactory.this;
            }
        };
    }

    /**
     * Build a bit vector that is constant true or constant false.
     *
     * <p>
     * Compare to bvec_true, bvec_false.
     * </p>
     *
     * @param bitnum bit number
     * @param b bit value
     * @return BDD bit vector
     */
    public BDDBitVector buildVector(int bitnum, boolean b) {
        BDDBitVector v = createBitVector(bitnum);
        v.initialize(b);
        return v;
    }

    /**
     * Build a bit vector that corresponds to a constant value.
     *
     * <p>
     * Compare to bvec_con.
     * </p>
     *
     * @param bitnum bit number
     * @param val bit value
     * @return BDD bit vector
     */
    public BDDBitVector constantVector(int bitnum, long val) {
        BDDBitVector v = createBitVector(bitnum);
        v.initialize(val);
        return v;
    }

    public BDDBitVector constantVector(int bitnum, BigInteger val) {
        BDDBitVector v = createBitVector(bitnum);
        v.initialize(val);
        return v;
    }

    /**
     * Build a bit vector using variables offset, offset+step, offset+2*step, ... , offset+(bitnum-1)*step.
     *
     * <p>
     * Compare to bvec_var.
     * </p>
     *
     * @param bitnum bit number
     * @param offset offset
     * @param step step
     * @return BDD bit vector
     */
    public BDDBitVector buildVector(int bitnum, int offset, int step) {
        BDDBitVector v = createBitVector(bitnum);
        v.initialize(offset, step);
        return v;
    }

    /**
     * Build a bit vector using variables from the given BDD domain.
     *
     * <p>
     * Compare to bvec_varfdd.
     * </p>
     *
     * @param d BDD domain
     * @return BDD bit vector
     */
    public BDDBitVector buildVector(BDDDomain d) {
        BDDBitVector v = createBitVector(d.varNum());
        v.initialize(d);
        return v;
    }

    /**
     * Build a bit vector using the given variables.
     *
     * <p>
     * compare to bvec_varvec.
     * </p>
     *
     * @param var variables
     * @return BDD bit vector
     */
    public BDDBitVector buildVector(int[] var) {
        BDDBitVector v = createBitVector(var.length);
        v.initialize(var);
        return v;
    }

    ///// CALLBACKS /////

    /** Garbage collection statistics callback. */
    @FunctionalInterface
    public static interface GCStatsCallback {
        /**
         * Garbage collection statistics callback.
         *
         * @param stats The statistics.
         * @param pre Whether this callback is invoked before ({@code true}) or after ({@code false}) garbage
         *      collection.
         */
        public void gc(GCStats stats, boolean pre);
    }

    /** Variable reorder statistics callback. */
    @FunctionalInterface
    public static interface ReorderStatsCallback {
        /**
         * Variable reorder statistics callback.
         *
         * @param stats The statistics.
         * @param pre Whether this callback is invoked before ({@code true}) or after ({@code false}) reordering.
         */
        public void reorder(ReorderStats stats, boolean pre);
    }

    /** Node table resize statistics callback. */
    @FunctionalInterface
    public static interface ResizeStatsCallback {
        /**
         * Node table resize statistics callback.
         *
         * @param oldsize The old node table size.
         * @param newsize The new node table size.
         */
        public void resize(int oldsize, int newsize);
    }

    /** Operator cache statistics callback. */
    @FunctionalInterface
    public static interface CacheStatsCallback {
        /**
         * Operator cache statistics callback.
         *
         * @param stats The statistics.
         */
        public void cache(CacheStats stats);
    }

    /** Maximum BDD nodes usage statistics callback. */
    @FunctionalInterface
    public static interface MaxUsedBddNodesStatsCallback {
        /**
         * Maximum BDD nodes usage statistics callback.
         *
         * @param stats The statistics.
         */
        public void maxUsedBddNodes(MaxUsedBddNodesStats stats);
    }

    /** Maximum memory usage statistics callback. */
    @FunctionalInterface
    public static interface MaxMemoryStatsCallback {
        /**
         * Maximum memory usage statistics callback.
         *
         * @param stats The statistics.
         */
        public void maxMemory(MaxMemoryStats stats);
    }

    /** Continuously BDD nodes usage and BDD operations statistics callback. */
    @FunctionalInterface
    public static interface ContinuousStatsCallback {
        /**
         * Continuously BDD nodes usage and BDD operations statistics callback.
         *
         * @param usedBddNodes The number of currently used BDD nodes. Represents a platform-independent measure that
         *      approximates memory use.
         * @param opMiss The number of BDD operations performed until now that could not be taken from the operation
         *      cache. Represents a platform-independent measure of approximates running time.
         */
        public void continuous(int usedBddNodes, long opMiss);
    }

    /** The registered garbage collection statistics callbacks, or {@code null} if none registered. */
    protected List<GCStatsCallback> gcCallbacks = null;

    /** The registered variable reorder statistics callbacks, or {@code null} if none registered. */
    protected List<ReorderStatsCallback> reorderCallbacks = null;

    /** The registered node table resize statistics callbacks, or {@code null} if none registered. */
    protected List<ResizeStatsCallback> resizeCallbacks = null;

    /** The registered operator cache statistics callbacks, or {@code null} if none registered. */
    protected List<CacheStatsCallback> cacheCallbacks = null;

    /** The registered maximum BDD nodes usage statistics callbacks, or {@code null} if none registered. */
    protected List<MaxUsedBddNodesStatsCallback> maxUsedBddNodesCallbacks = null;

    /** The registered maximum memory usage statistics callbacks, or {@code null} if none registered. */
    protected List<MaxMemoryStatsCallback> maxMemoryCallbacks = null;

    /**
     * The registered continuously BDD nodes usage and BDD operations statistics callbacks, or {@code null} if none
     * registered.
     */
    protected List<ContinuousStatsCallback> continuousCallbacks = null;

    /**
     * Register a garbage collection statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerGcStatsCallback(GCStatsCallback callback) {
        if (gcCallbacks == null) {
            gcCallbacks = new LinkedList<>();
        }
        gcCallbacks.add(callback);
    }

    /**
     * Register a variable reorder statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerReorderStatsCallback(ReorderStatsCallback callback) {
        if (reorderCallbacks == null) {
            reorderCallbacks = new LinkedList<>();
        }
        reorderCallbacks.add(callback);
    }

    /**
     * Register a node table resize statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerResizeStatsCallback(ResizeStatsCallback callback) {
        if (resizeCallbacks == null) {
            resizeCallbacks = new LinkedList<>();
        }
        resizeCallbacks.add(callback);
    }

    /**
     * Register an operator cache statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerCacheStatsCallback(CacheStatsCallback callback) {
        if (cacheCallbacks == null) {
            cacheCallbacks = new LinkedList<>();
        }
        cacheCallbacks.add(callback);
    }

    /**
     * Register a maximum BDD nodes usage statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerMaxUsedBddNodesStatsCallback(MaxUsedBddNodesStatsCallback callback) {
        if (maxUsedBddNodesCallbacks == null) {
            maxUsedBddNodesCallbacks = new LinkedList<>();
        }
        maxUsedBddNodesCallbacks.add(callback);
    }

    /**
     * Register a maximum memory usage statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerMaxMemoryStatsCallback(MaxMemoryStatsCallback callback) {
        if (maxMemoryCallbacks == null) {
            maxMemoryCallbacks = new LinkedList<>();
        }
        maxMemoryCallbacks.add(callback);
    }

    /**
     * Register a continuously BDD nodes usage and BDD operations statistics callback.
     *
     * @param callback The callback to register.
     */
    public void registerContinuousStatsCallback(ContinuousStatsCallback callback) {
        if (continuousCallbacks == null) {
            continuousCallbacks = new LinkedList<>();
        }
        continuousCallbacks.add(callback);
    }

    /**
     * Unregister a garbage collection statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterGcStatsCallback(GCStatsCallback callback) {
        if (gcCallbacks != null) {
            for (Iterator<GCStatsCallback> iter = gcCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (gcCallbacks.isEmpty()) {
                        gcCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Unregister a variable reorder statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterReorderStatsCallback(ReorderStatsCallback callback) {
        if (reorderCallbacks != null) {
            for (Iterator<ReorderStatsCallback> iter = reorderCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (reorderCallbacks.isEmpty()) {
                        reorderCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Unregister a node table resize statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterResizeStatsCallback(ResizeStatsCallback callback) {
        if (resizeCallbacks != null) {
            for (Iterator<ResizeStatsCallback> iter = resizeCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (resizeCallbacks.isEmpty()) {
                        resizeCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Unregister an operator cache statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterCacheStatsCallback(CacheStatsCallback callback) {
        if (cacheCallbacks != null) {
            for (Iterator<CacheStatsCallback> iter = cacheCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (cacheCallbacks.isEmpty()) {
                        cacheCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Unregister a maximum BDD nodes usage statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterMaxUsedBddNodesStatsCallback(MaxUsedBddNodesStatsCallback callback) {
        if (maxUsedBddNodesCallbacks != null) {
            for (Iterator<MaxUsedBddNodesStatsCallback> iter = maxUsedBddNodesCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (maxUsedBddNodesCallbacks.isEmpty()) {
                        maxUsedBddNodesCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Unregister a maximum memory usage statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterMaxMemoryStatsCallback(MaxMemoryStatsCallback callback) {
        if (maxMemoryCallbacks != null) {
            for (Iterator<MaxMemoryStatsCallback> iter = maxMemoryCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (maxMemoryCallbacks.isEmpty()) {
                        maxMemoryCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Unregister a continuously BDD nodes usage and BDD operations statistics callback.
     *
     * @param callback The callback to unregister.
     * @throws IllegalArgumentException If callback is not registered.
     */
    public void unregisterContinuousStatsCallback(ContinuousStatsCallback callback) {
        if (continuousCallbacks != null) {
            for (Iterator<ContinuousStatsCallback> iter = continuousCallbacks.iterator(); iter.hasNext();) {
                if (iter.next() == callback) {
                    iter.remove();
                    if (continuousCallbacks.isEmpty()) {
                        continuousCallbacks = null;
                    }
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns whether this BDD factory has a registered garbage collection statistics callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasGcStatsCallback() {
        return gcCallbacks != null;
    }

    /**
     * Returns whether this BDD factory has a registered variable reorder statistics callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasReorderStatsCallback() {
        return reorderCallbacks != null;
    }

    /**
     * Returns whether this BDD factory has a registered node table resize statistics callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasResizeStatsCallback() {
        return resizeCallbacks != null;
    }

    /**
     * Returns whether this BDD factory has a registered operator cache statistics callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasCacheStatsCallback() {
        return cacheCallbacks != null;
    }

    /**
     * Returns whether this BDD factory has a registered maximum BDD nodes usage statistics callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasMaxUsedBddNodesStatsCallback() {
        return maxUsedBddNodesCallbacks != null;
    }

    /**
     * Returns whether this BDD factory has a registered maximum memory usage statistics callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasMaxMemoryStatsCallback() {
        return maxMemoryCallbacks != null;
    }

    /**
     * Returns whether this BDD factory has a registered continuously BDD nodes usage and BDD operations statistics
     * callback.
     *
     * @return {@code true} if such a callback is registered, {@code false} otherwise.
     */
    public boolean hasContinuousStatsCallback() {
        return continuousCallbacks != null;
    }

    /**
     * Invoke all registered garbage collection statistics callbacks.
     *
     * @param pre Whether this callback is invoked before ({@code true}) or after ({@code false}) garbage collection.
     */
    public void invokeGcStatsCallbacks(boolean pre) {
        if (gcCallbacks != null) {
            for (GCStatsCallback callback: gcCallbacks) {
                callback.gc(gcstats, pre);
            }
        }
    }

    /**
     * Invoke all registered variable reorder statistics callbacks.
     *
     * @param pre Whether this callback is invoked before ({@code true}) or after ({@code false}) reordering.
     */
    public void invokeReorderStatsCallbacks(boolean pre) {
        if (reorderCallbacks != null) {
            for (ReorderStatsCallback callback: reorderCallbacks) {
                callback.reorder(reorderstats, pre);
            }
        }
    }

    /**
     * Invoke all registered node table resize statistics callbacks.
     *
     * @param oldsize The old node table size.
     * @param newsize The new node table size.
     */
    public void invokeResizeStatsCallbacks(int oldsize, int newsize) {
        if (resizeCallbacks != null) {
            for (ResizeStatsCallback callback: resizeCallbacks) {
                callback.resize(oldsize, newsize);
            }
        }
    }

    /** Invoke all registered operator cache statistics callbacks. */
    public void invokeCacheStatsCallbacks() {
        if (cacheCallbacks != null) {
            for (CacheStatsCallback callback: cacheCallbacks) {
                callback.cache(cachestats);
            }
        }
    }

    /** Invoke all registered maximum BDD nodes usage statistics callbacks. */
    public void invokeMaxUsedBddNodesStatsCallbacks() {
        if (maxUsedBddNodesCallbacks != null) {
            for (MaxUsedBddNodesStatsCallback callback: maxUsedBddNodesCallbacks) {
                callback.maxUsedBddNodes(maxusedbddnodesstats);
            }
        }
    }

    /** Invoke all registered maximum memory usage statistics callbacks. */
    public void invokeMaxMemoryStatsCallbacks() {
        if (maxMemoryCallbacks != null) {
            for (MaxMemoryStatsCallback callback: maxMemoryCallbacks) {
                callback.maxMemory(maxmemorystats);
            }
        }
    }

    /**
     * Invoke all registered continuously BDD nodes usage and BDD operations statistics callbacks.
     *
     * @param usedBddNodes The number of currently used BDD nodes. Represents a platform-independent measure that
     *      approximates memory use.
     * @param opMiss The number of BDD operations performed until now that could not be taken from the operation
     *      cache. Represents a platform-independent measure of approximates running time.
     */
    public void invokeContinuousStatsCallbacks(int usedBddNodes, long opMiss) {
        if (continuousCallbacks != null) {
            for (ContinuousStatsCallback callback: continuousCallbacks) {
                callback.continuous(usedBddNodes, opMiss);
            }
        }
    }

    /**
     * Default garbage collection statistics callback.
     *
     * @param stats The statistics.
     * @param pre Whether this callback is invoked before ({@code true}) or after ({@code false}) garbage collection.
     */
    public static void defaultGcStatsCallback(GCStats stats, boolean pre) {
        if (pre) {
            if (stats.freenodes != 0) {
                System.out.println("Starting GC cycle  #" + (stats.num + 1) + ": " + stats.nodes + " nodes / "
                        + stats.freenodes + " free");
            }
        } else {
            System.out.println(stats.toString());
        }
    }

    /**
     * Default variable reorder statistics callback.
     *
     * @param stats The statistics.
     * @param pre Whether this callback is invoked before ({@code true}) or after ({@code false}) reordering.
     */
    public static void defaultReorderStatsCallback(ReorderStats stats, boolean pre) {
        if (pre) {
            System.out.println("Start reordering");
        } else {
            System.out.println("End reordering. " + stats);
        }
    }

    /**
     * Default node table resize statistics callback.
     *
     * @param oldsize The old node table size.
     * @param newsize The new node table size.
     */
    public static void defaultResizeStatsCallback(int oldsize, int newsize) {
        StringBuilder sb = new StringBuilder();
        sb.append("Went from ");
        sb.append(oldsize);
        sb.append(" to ");
        sb.append(newsize);
        sb.append(" nodes");

        System.out.println(sb.toString());
    }

    /**
     * Default operator cache statistics callback.
     *
     * @param stats The statistics.
     */
    public static void defaultCacheStatsCallback(CacheStats stats) {
        System.out.println(stats.toString());
    }

    /**
     * Default maximum BDD nodes usage statistics callback.
     *
     * @param stats The statistics.
     */
    public static void defaultMaxUsedBddNodesStatsCallback(MaxUsedBddNodesStats stats) {
        System.out.println(stats.toString());
    }

    /**
     * Default maximum memory usage statistics callback.
     *
     * @param stats The statistics.
     */
    public static void defaultMaxMemoryStatsCallback(MaxMemoryStats stats) {
        System.out.println(stats.toString());
    }

    /**
     * Default continuously BDD nodes usage and BDD operations statistics callback.
     *
     * @param usedBddNodes The number of currently used BDD nodes. Represents a platform-independent measure that
     *      approximates memory use.
     * @param opMiss The number of BDD operations performed until now that could not be taken from the operation
     *      cache. Represents a platform-independent measure of approximates running time.
     */
    public static void defaultContinuousStatsCallback(int usedBddNodes, long opMiss) {
        StringBuilder sb = new StringBuilder();
        sb.append("Used BDD nodes: ");
        sb.append(usedBddNodes);
        sb.append(", operation count: ");
        sb.append(opMiss);

        System.out.println(sb.toString());
    }
}

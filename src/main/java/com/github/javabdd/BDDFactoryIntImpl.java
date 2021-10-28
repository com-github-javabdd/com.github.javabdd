// BDDFactoryIntImpl.java, created Jul 16, 2006 2:59:55 PM by jwhaley
// Copyright (C) 2004-2006 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.

package com.github.javabdd;

import java.util.Collection;
import java.util.Iterator;

/**
 * A shared superclass for BDD factories that refer to BDDs as ints.
 * 
 * @author jwhaley
 * @version $Id$
 */
public abstract class BDDFactoryIntImpl extends BDDFactory {
    static final boolean USE_FINALIZER = false;

    protected abstract void addref_impl(/* bdd */int v);

    protected abstract void delref_impl(/* bdd */int v);

    protected abstract /* bdd */int zero_impl();

    protected abstract /* bdd */int one_impl();

    protected /* bdd */int universe_impl() {
        return one_impl();
    }

    protected abstract /* bdd */int invalid_bdd_impl();

    protected abstract int var_impl(/* bdd */int v);

    protected abstract int level_impl(/* bdd */int v);

    protected abstract /* bdd */int low_impl(/* bdd */int v);

    protected abstract /* bdd */int high_impl(/* bdd */int v);

    protected abstract /* bdd */int ithVar_impl(int var);

    protected abstract /* bdd */int nithVar_impl(int var);

    protected abstract /* bdd */int makenode_impl(int lev, /* bdd */int lo, /* bdd */int hi);

    protected abstract /* bdd */int ite_impl(/* bdd */int v1, /* bdd */int v2, /* bdd */int v3);

    protected abstract /* bdd */int apply_impl(/* bdd */int v1, /* bdd */int v2, BDDOp opr);

    protected abstract /* bdd */int not_impl(/* bdd */int v1);

    protected abstract /* bdd */int applyAll_impl(/* bdd */int v1, /* bdd */int v2, BDDOp opr, /* bdd */int v3);

    protected abstract /* bdd */int applyEx_impl(/* bdd */int v1, /* bdd */int v2, BDDOp opr, /* bdd */int v3);

    protected abstract /* bdd */int applyUni_impl(/* bdd */int v1, /* bdd */int v2, BDDOp opr, /* bdd */int v3);

    protected abstract /* bdd */int compose_impl(/* bdd */int v1, /* bdd */int v2, int var);

    protected abstract /* bdd */int constrain_impl(/* bdd */int v1, /* bdd */int v2);

    protected abstract /* bdd */int restrict_impl(/* bdd */int v1, /* bdd */int v2);

    protected abstract /* bdd */int simplify_impl(/* bdd */int v1, /* bdd */int v2);

    protected abstract /* bdd */int support_impl(/* bdd */int v);

    protected abstract /* bdd */int exist_impl(/* bdd */int v1, /* bdd */int v2);

    protected abstract /* bdd */int forAll_impl(/* bdd */int v1, /* bdd */int v2);

    protected abstract /* bdd */int unique_impl(/* bdd */int v1, /* bdd */int v2);

    protected abstract /* bdd */int fullSatOne_impl(/* bdd */int v);

    protected abstract /* bdd */int replace_impl(/* bdd */int v, BDDPairing p);

    protected abstract /* bdd */int veccompose_impl(/* bdd */int v, BDDPairing p);

    protected abstract int nodeCount_impl(/* bdd */int v);

    protected abstract double pathCount_impl(/* bdd */int v);

    protected abstract double satCount_impl(/* bdd */int v);

    protected abstract /* bdd */int satOne_impl(/* bdd */int v);

    protected abstract /* bdd */int satOne_impl2(/* bdd */int v1, /* bdd */int v2, boolean pol);

    protected abstract int nodeCount_impl2(/* bdd */int[] v);

    protected abstract int[] varProfile_impl(/* bdd */int v);

    protected abstract void printTable_impl(/* bdd */int v);

    public class IntBDD extends BDD {
        protected /* bdd */int v;

        protected IntBDD(/* bdd */int v) {
            this.v = v;
            addref_impl(v);
        }

        public BDD apply(BDD that, BDDOp opr) {
            return makeBDD(apply_impl(v, unwrap(that), opr));
        }

        public BDD applyAll(BDD that, BDDOp opr, BDDVarSet var) {
            return makeBDD(applyAll_impl(v, unwrap(that), opr, unwrap(var)));
        }

        public BDD applyEx(BDD that, BDDOp opr, BDDVarSet var) {
            return makeBDD(applyEx_impl(v, unwrap(that), opr, unwrap(var)));
        }

        public BDD applyUni(BDD that, BDDOp opr, BDDVarSet var) {
            return makeBDD(applyUni_impl(v, unwrap(that), opr, unwrap(var)));
        }

        public BDD applyWith(BDD that, BDDOp opr) {
            /* bdd */int v2 = unwrap(that);
            /* bdd */int v3 = apply_impl(v, v2, opr);
            addref_impl(v3);
            delref_impl(v);
            if (this != that)
                that.free();
            v = v3;
            return this;
        }

        public BDD compose(BDD g, int var) {
            return makeBDD(compose_impl(v, unwrap(g), var));
        }

        public BDD constrain(BDD that) {
            return makeBDD(constrain_impl(v, unwrap(that)));
        }

        public boolean equals(BDD that) {
            return v == unwrap(that);
        }

        public BDD exist(BDDVarSet var) {
            return makeBDD(exist_impl(v, unwrap(var)));
        }

        public BDD forAll(BDDVarSet var) {
            return makeBDD(forAll_impl(v, unwrap(var)));
        }

        public void free() {
            delref_impl(v);
            v = invalid_bdd_impl();
        }

        public BDD fullSatOne() {
            return makeBDD(fullSatOne_impl(v));
        }

        public BDDFactory getFactory() {
            return BDDFactoryIntImpl.this;
        }

        public int hashCode() {
            return v;
        }

        public BDD high() {
            return makeBDD(high_impl(v));
        }

        public BDD id() {
            return makeBDD(v);
        }

        public boolean isOne() {
            return v == one_impl();
        }

        public boolean isUniverse() {
            return v == universe_impl();
        }

        public boolean isZero() {
            return v == zero_impl();
        }

        public BDD ite(BDD thenBDD, BDD elseBDD) {
            return makeBDD(ite_impl(v, unwrap(thenBDD), unwrap(elseBDD)));
        }

        public BDD low() {
            return makeBDD(low_impl(v));
        }

        public int level() {
            return level_impl(v);
        }

        public int nodeCount() {
            return nodeCount_impl(v);
        }

        public BDD not() {
            return makeBDD(not_impl(v));
        }

        public double pathCount() {
            return pathCount_impl(v);
        }

        public BDD replace(BDDPairing pair) {
            return makeBDD(replace_impl(v, pair));
        }

        public BDD replaceWith(BDDPairing pair) {
            /* bdd */int v3 = replace_impl(v, pair);
            addref_impl(v3);
            delref_impl(v);
            v = v3;
            return this;
        }

        public BDD restrict(BDD var) {
            return makeBDD(restrict_impl(v, unwrap(var)));
        }

        public BDD restrictWith(BDD that) {
            /* bdd */int v2 = unwrap(that);
            /* bdd */int v3 = restrict_impl(v, v2);
            addref_impl(v3);
            delref_impl(v);
            if (this != that)
                that.free();
            v = v3;
            return this;
        }

        public double satCount() {
            return satCount_impl(v);
        }

        public BDD satOne() {
            return makeBDD(satOne_impl(v));
        }

        public BDD satOne(BDDVarSet var, boolean pol) {
            return makeBDD(satOne_impl2(v, unwrap(var), pol));
        }

        public BDD simplify(BDD d) {
            return makeBDD(simplify_impl(v, unwrap(d)));
        }

        public BDDVarSet support() {
            return makeBDDVarSet(support_impl(v));
        }

        public BDD unique(BDDVarSet var) {
            return makeBDD(unique_impl(v, unwrap(var)));
        }

        public int var() {
            return var_impl(v);
        }

        public int[] varProfile() {
            return varProfile_impl(v);
        }

        public BDD veccompose(BDDPairing pair) {
            return makeBDD(veccompose_impl(v, pair));
        }

        public BDDVarSet toVarSet() {
            return makeBDDVarSet(v);
        }
    }

    public class IntBDDWithFinalizer extends IntBDD {
        protected IntBDDWithFinalizer(/* bdd */int v) {
            super(v);
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (false && v != invalid_bdd_impl()) {
                System.out.println("BDD not freed! " + System.identityHashCode(this));
            }
            deferredFree(v);
        }
    }

    protected IntBDD makeBDD(/* bdd */int v) {
        if (USE_FINALIZER)
            return new IntBDDWithFinalizer(v);
        else
            return new IntBDD(v);
    }

    protected static final /* bdd */int unwrap(BDD b) {
        return ((IntBDD)b).v;
    }

    protected static final /* bdd */int[] unwrap(Collection/* <BDD> */ c) {
        /* bdd */int[] result = new /* bdd */int[c.size()];
        int k = -1;
        for (Iterator i = c.iterator(); i.hasNext();) {
            result[++k] = ((IntBDD)i.next()).v;
        }
        return result;
    }

    public class IntBDDVarSet extends BDDVarSet {
        /* bdd */int v;

        protected IntBDDVarSet(/* bdd */int v) {
            this.v = v;
            addref_impl(v);
        }

        public boolean equals(BDDVarSet that) {
            return v == unwrap(that);
        }

        public void free() {
            delref_impl(v);
            v = invalid_bdd_impl();
        }

        public BDDFactory getFactory() {
            return BDDFactoryIntImpl.this;
        }

        public int hashCode() {
            return v;
        }

        public BDDVarSet id() {
            return makeBDDVarSet(v);
        }

        protected int do_intersect(int v1, int v2) {
            return apply_impl(v1, v2, or);
        }

        public BDDVarSet intersect(BDDVarSet b) {
            return makeBDDVarSet(do_intersect(v, unwrap(b)));
        }

        public BDDVarSet intersectWith(BDDVarSet b) {
            /* bdd */int v2 = unwrap(b);
            /* bdd */int v3 = do_intersect(v, v2);
            addref_impl(v3);
            delref_impl(v);
            if (this != b)
                b.free();
            v = v3;
            return this;
        }

        public boolean isEmpty() {
            return v == one_impl();
        }

        public int size() {
            int result = 0;
            for (/* bdd */int p = v; p != one_impl(); p = high_impl(p)) {
                if (p == zero_impl())
                    throw new BDDException("varset contains zero");
                ++result;
            }
            return result;
        }

        public int[] toArray() {
            int[] result = new int[size()];
            int k = -1;
            for (/* bdd */int p = v; p != one_impl(); p = high_impl(p)) {
                result[++k] = var_impl(p);
            }
            return result;
        }

        public BDD toBDD() {
            return makeBDD(v);
        }

        public int[] toLevelArray() {
            int[] result = new int[size()];
            int k = -1;
            for (int p = v; p != one_impl(); p = high_impl(p)) {
                result[++k] = level_impl(p);
            }
            return result;
        }

        protected int do_unionvar(int v, int var) {
            return apply_impl(v, ithVar_impl(var), and);
        }

        protected int do_union(int v1, int v2) {
            return apply_impl(v1, v2, and);
        }

        public BDDVarSet union(BDDVarSet b) {
            return makeBDDVarSet(do_union(v, unwrap(b)));
        }

        public BDDVarSet union(int var) {
            return makeBDDVarSet(do_unionvar(v, var));
        }

        public BDDVarSet unionWith(BDDVarSet b) {
            /* bdd */int v2 = unwrap(b);
            /* bdd */int v3 = do_union(v, v2);
            addref_impl(v3);
            delref_impl(v);
            if (this != b)
                b.free();
            v = v3;
            return this;
        }

        public BDDVarSet unionWith(int var) {
            /* bdd */int v3 = do_unionvar(v, var);
            addref_impl(v3);
            delref_impl(v);
            v = v3;
            return this;
        }
    }

    public class IntBDDVarSetWithFinalizer extends IntBDDVarSet {
        protected IntBDDVarSetWithFinalizer(int v) {
            super(v);
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (false && v != invalid_bdd_impl()) {
                System.out.println("BDD not freed! " + System.identityHashCode(this));
            }
            deferredFree(v);
        }
    }

    public class IntZDDVarSet extends IntBDDVarSet {
        protected IntZDDVarSet(/* bdd */int v) {
            super(v);
        }

        protected int do_intersect(int v1, int v2) {
            if (v1 == one_impl())
                return v2;
            if (v2 == one_impl())
                return v1;
            int l1, l2;
            l1 = level_impl(v1);
            l2 = level_impl(v2);
            for (;;) {
                if (v1 == v2)
                    return v1;
                if (l1 < l2) {
                    v1 = high_impl(v1);
                    if (v1 == one_impl())
                        return v2;
                    l1 = level_impl(v1);
                } else if (l1 > l2) {
                    v2 = high_impl(v2);
                    if (v2 == one_impl())
                        return v1;
                    l2 = level_impl(v2);
                } else {
                    int k = do_intersect(high_impl(v1), high_impl(v2));
                    addref_impl(k);
                    int result = makenode_impl(l1, zero_impl(), k);
                    delref_impl(k);
                    return result;
                }
            }
        }

        protected int do_union(int v1, int v2) {
            if (v1 == v2)
                return v1;
            if (v1 == one_impl())
                return v2;
            if (v2 == one_impl())
                return v1;
            int l1, l2;
            l1 = level_impl(v1);
            l2 = level_impl(v2);
            int vv1 = v1, vv2 = v2, lev = l1;
            if (l1 <= l2)
                vv1 = high_impl(v1);
            if (l1 >= l2) {
                vv2 = high_impl(v2);
                lev = l2;
            }
            int k = do_union(vv1, vv2);
            addref_impl(k);
            int result = makenode_impl(lev, zero_impl(), k);
            delref_impl(k);
            return result;
        }

        protected int do_unionvar(int v, int var) {
            return do_unionlevel(v, var2Level(var));
        }

        private int do_unionlevel(int v, int lev) {
            if (v == one_impl())
                return makenode_impl(lev, zero_impl(), one_impl());
            int l = level_impl(v);
            if (l == lev) {
                return v;
            } else if (l > lev) {
                return makenode_impl(lev, zero_impl(), v);
            } else {
                int k = do_unionlevel(high_impl(v), lev);
                addref_impl(k);
                int result = makenode_impl(l, zero_impl(), k);
                delref_impl(k);
                return result;
            }
        }
    }

    public class IntZDDVarSetWithFinalizer extends IntZDDVarSet {
        protected IntZDDVarSetWithFinalizer(int v) {
            super(v);
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (USE_FINALIZER) {
                if (false && v != invalid_bdd_impl()) {
                    System.out.println("BDD not freed! " + System.identityHashCode(this));
                }
                deferredFree(v);
            }
        }
    }

    protected IntBDDVarSet makeBDDVarSet(/* bdd */int v) {
        if (isZDD()) {
            if (USE_FINALIZER)
                return new IntZDDVarSetWithFinalizer(v);
            else
                return new IntZDDVarSet(v);
        } else {
            if (USE_FINALIZER)
                return new IntBDDVarSetWithFinalizer(v);
            else
                return new IntBDDVarSet(v);
        }
    }

    protected static final /* bdd */int unwrap(BDDVarSet b) {
        return ((IntBDDVarSet)b).v;
    }

    public class IntBDDBitVector extends BDDBitVector {
        protected IntBDDBitVector(int bitnum) {
            super(bitnum);
        }

        public BDDFactory getFactory() {
            return BDDFactoryIntImpl.this;
        }
    }

    public BDD ithVar(/* bdd */int var) {
        return makeBDD(ithVar_impl(var));
    }

    public BDD nithVar(/* bdd */int var) {
        return makeBDD(nithVar_impl(var));
    }

    public int nodeCount(Collection/* <BDD> */ r) {
        return nodeCount_impl2(unwrap(r));
    }

    public BDD one() {
        return makeBDD(one_impl());
    }

    public BDD universe() {
        return makeBDD(universe_impl());
    }

    public BDDVarSet emptySet() {
        return makeBDDVarSet(one_impl());
    }

    public void printTable(BDD b) {
        printTable_impl(unwrap(b));
    }

    public BDD zero() {
        return makeBDD(zero_impl());
    }

    public void done() {
        if (USE_FINALIZER) {
            System.gc();
            System.runFinalization();
            handleDeferredFree();
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        this.done();
    }

    protected /* bdd */int[] to_free = new /* bdd */int[8];

    protected /* bdd */int to_free_length = 0;

    public void deferredFree(int v) {
        if (v == invalid_bdd_impl())
            return;
        synchronized (to_free) {
            if (to_free_length == to_free.length) {
                /* bdd */int[] t = new /* bdd */int[to_free.length * 2];
                System.arraycopy(to_free, 0, t, 0, to_free.length);
                to_free = t;
            }
            to_free[to_free_length++] = v;
        }
    }

    public void handleDeferredFree() {
        synchronized (to_free) {
            while (to_free_length > 0) {
                delref_impl(to_free[--to_free_length]);
            }
        }
    }
}

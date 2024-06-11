//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2004-2024 John Whaley and com.github.javabdd contributors
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

import java.util.Collection;
import java.util.Iterator;

/**
 * A shared superclass for BDD factories that refer to BDDs as ints.
 */
public abstract class BDDFactoryIntImpl extends BDDFactory {
    static final boolean USE_FINALIZER = false;

    static final boolean FINALIZER_CHECK_BDD_NOT_FREED = false;

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

    protected abstract /* bdd */int relnext_impl(/* bdd */int states, /* bdd */int relation, /* bdd */int vars);

    protected abstract /* bdd */int relnextIntersection_impl(/* bdd */int states, /* bdd */int relation,
            /* bdd */int restriction, /* bdd */int vars);

    protected abstract /* bdd */int relprev_impl(/* bdd */int relation, /* bdd */int states, /* bdd */int vars);

    protected abstract /* bdd */int relprevIntersection_impl(/* bdd */int relation, /* bdd */int states,
            /* bdd */int restriction, /* bdd */int vars);

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

        @Override
        public BDD apply(BDD that, BDDOp opr) {
            return makeBDD(apply_impl(v, unwrap(that), opr));
        }

        @Override
        public BDD applyAll(BDD that, BDDOp opr, BDDVarSet var) {
            return makeBDD(applyAll_impl(v, unwrap(that), opr, unwrap(var)));
        }

        @Override
        public BDD applyEx(BDD that, BDDOp opr, BDDVarSet var) {
            return makeBDD(applyEx_impl(v, unwrap(that), opr, unwrap(var)));
        }

        @Override
        public BDD applyUni(BDD that, BDDOp opr, BDDVarSet var) {
            return makeBDD(applyUni_impl(v, unwrap(that), opr, unwrap(var)));
        }

        @Override
        public BDD applyWith(BDD that, BDDOp opr) {
            /* bdd */int v2 = unwrap(that);
            /* bdd */int v3 = apply_impl(v, v2, opr);
            addref_impl(v3);
            delref_impl(v);
            if (this != that) {
                that.free();
            }
            v = v3;
            return this;
        }

        @Override
        public BDD compose(BDD g, int var) {
            return makeBDD(compose_impl(v, unwrap(g), var));
        }

        @Override
        public BDD constrain(BDD that) {
            return makeBDD(constrain_impl(v, unwrap(that)));
        }

        @Override
        public boolean equalsBDD(BDD that) {
            return v == unwrap(that);
        }

        @Override
        public BDD exist(BDDVarSet var) {
            return makeBDD(exist_impl(v, unwrap(var)));
        }

        @Override
        public BDD forAll(BDDVarSet var) {
            return makeBDD(forAll_impl(v, unwrap(var)));
        }

        @Override
        public void free() {
            delref_impl(v);
            v = invalid_bdd_impl();
        }

        @Override
        public BDD fullSatOne() {
            return makeBDD(fullSatOne_impl(v));
        }

        @Override
        public BDDFactory getFactory() {
            return BDDFactoryIntImpl.this;
        }

        @Override
        public int hashCode() {
            return v;
        }

        @Override
        public BDD high() {
            return makeBDD(high_impl(v));
        }

        @Override
        public BDD id() {
            return makeBDD(v);
        }

        @Override
        public boolean isOne() {
            return v == one_impl();
        }

        @Override
        public boolean isUniverse() {
            return v == universe_impl();
        }

        @Override
        public boolean isZero() {
            return v == zero_impl();
        }

        @Override
        public BDD ite(BDD thenBDD, BDD elseBDD) {
            return makeBDD(ite_impl(v, unwrap(thenBDD), unwrap(elseBDD)));
        }

        @Override
        public BDD low() {
            return makeBDD(low_impl(v));
        }

        @Override
        public int level() {
            return level_impl(v);
        }

        @Override
        public int nodeCount() {
            return nodeCount_impl(v);
        }

        @Override
        public BDD not() {
            return makeBDD(not_impl(v));
        }

        @Override
        public double pathCount() {
            return pathCount_impl(v);
        }

        @Override
        public BDD replace(BDDPairing pair) {
            return makeBDD(replace_impl(v, pair));
        }

        @Override
        public BDD replaceWith(BDDPairing pair) {
            /* bdd */int v3 = replace_impl(v, pair);
            addref_impl(v3);
            delref_impl(v);
            v = v3;
            return this;
        }

        @Override
        public BDD restrict(BDD var) {
            return makeBDD(restrict_impl(v, unwrap(var)));
        }

        @Override
        public BDD restrictWith(BDD that) {
            /* bdd */int v2 = unwrap(that);
            /* bdd */int v3 = restrict_impl(v, v2);
            addref_impl(v3);
            delref_impl(v);
            if (this != that) {
                that.free();
            }
            v = v3;
            return this;
        }

        @Override
        public double satCount() {
            return satCount_impl(v);
        }

        @Override
        public BDD satOne() {
            return makeBDD(satOne_impl(v));
        }

        @Override
        public BDD satOne(BDDVarSet var, boolean pol) {
            return makeBDD(satOne_impl2(v, unwrap(var), pol));
        }

        @Override
        public BDD simplify(BDD d) {
            return makeBDD(simplify_impl(v, unwrap(d)));
        }

        @Override
        public BDDVarSet support() {
            return makeBDDVarSet(support_impl(v));
        }

        @Override
        public BDD unique(BDDVarSet var) {
            return makeBDD(unique_impl(v, unwrap(var)));
        }

        @Override
        public int var() {
            return var_impl(v);
        }

        @Override
        public int[] varProfile() {
            return varProfile_impl(v);
        }

        @Override
        public BDD veccompose(BDDPairing pair) {
            return makeBDD(veccompose_impl(v, pair));
        }

        @Override
        public BDDVarSet toVarSet() {
            return makeBDDVarSet(v);
        }

        @Override
        public BDD relnext(BDD states, BDDVarSet vars) {
            return makeBDD(relnext_impl(unwrap(states), v, unwrap(vars)));
        }

        @Override
        public BDD relnextIntersection(BDD states, BDD restriction, BDDVarSet vars) {
            return makeBDD(relnextIntersection_impl(unwrap(states), v, unwrap(restriction), unwrap(vars)));
        }

        @Override
        public BDD relprev(BDD states, BDDVarSet vars) {
            return makeBDD(relprev_impl(v, unwrap(states), unwrap(vars)));
        }

        @Override
        public BDD relprevIntersection(BDD states, BDD restriction, BDDVarSet vars) {
            return makeBDD(relprevIntersection_impl(v, unwrap(states), unwrap(restriction), unwrap(vars)));
        }
    }

    public class IntBDDWithFinalizer extends IntBDD {
        protected IntBDDWithFinalizer(/* bdd */int v) {
            super(v);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (FINALIZER_CHECK_BDD_NOT_FREED) {
                if (v != invalid_bdd_impl()) {
                    System.out.println("BDD not freed! " + System.identityHashCode(this));
                }
            }
            deferredFree(v);
        }
    }

    protected IntBDD makeBDD(/* bdd */int v) {
        if (USE_FINALIZER) {
            return new IntBDDWithFinalizer(v);
        } else {
            return new IntBDD(v);
        }
    }

    protected static final /* bdd */int unwrap(BDD b) {
        return ((IntBDD)b).v;
    }

    protected static final /* bdd */int[] unwrap(Collection<BDD> c) {
        /* bdd */int[] result = new /* bdd */int[c.size()];
        int k = -1;
        for (Iterator<BDD> i = c.iterator(); i.hasNext();) {
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

        @Override
        public boolean equalsBDDVarSet(BDDVarSet that) {
            return v == unwrap(that);
        }

        @Override
        public void free() {
            delref_impl(v);
            v = invalid_bdd_impl();
        }

        @Override
        public BDDFactory getFactory() {
            return BDDFactoryIntImpl.this;
        }

        @Override
        public int hashCode() {
            return v;
        }

        @Override
        public BDDVarSet id() {
            return makeBDDVarSet(v);
        }

        protected int do_intersect(int v1, int v2) {
            return apply_impl(v1, v2, or);
        }

        @Override
        public BDDVarSet intersect(BDDVarSet b) {
            return makeBDDVarSet(do_intersect(v, unwrap(b)));
        }

        @Override
        public BDDVarSet intersectWith(BDDVarSet b) {
            /* bdd */int v2 = unwrap(b);
            /* bdd */int v3 = do_intersect(v, v2);
            addref_impl(v3);
            delref_impl(v);
            if (this != b) {
                b.free();
            }
            v = v3;
            return this;
        }

        @Override
        public boolean isEmpty() {
            return v == one_impl();
        }

        @Override
        public int size() {
            int result = 0;
            for (/* bdd */int p = v; p != one_impl(); p = high_impl(p)) {
                if (p == zero_impl()) {
                    throw new BDDException("varset contains zero");
                }
                ++result;
            }
            return result;
        }

        @Override
        public int[] toArray() {
            int[] result = new int[size()];
            int k = -1;
            for (/* bdd */int p = v; p != one_impl(); p = high_impl(p)) {
                result[++k] = var_impl(p);
            }
            return result;
        }

        @Override
        public BDD toBDD() {
            return makeBDD(v);
        }

        @Override
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

        @Override
        public BDDVarSet union(BDDVarSet b) {
            return makeBDDVarSet(do_union(v, unwrap(b)));
        }

        @Override
        public BDDVarSet union(int var) {
            return makeBDDVarSet(do_unionvar(v, var));
        }

        @Override
        public BDDVarSet unionWith(BDDVarSet b) {
            /* bdd */int v2 = unwrap(b);
            /* bdd */int v3 = do_union(v, v2);
            addref_impl(v3);
            delref_impl(v);
            if (this != b) {
                b.free();
            }
            v = v3;
            return this;
        }

        @Override
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

        @SuppressWarnings("deprecation")
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (FINALIZER_CHECK_BDD_NOT_FREED) {
                if (v != invalid_bdd_impl()) {
                    System.out.println("BDD not freed! " + System.identityHashCode(this));
                }
            }
            deferredFree(v);
        }
    }

    public class IntZDDVarSet extends IntBDDVarSet {
        protected IntZDDVarSet(/* bdd */int v) {
            super(v);
        }

        @Override
        protected int do_intersect(int v1, int v2) {
            if (v1 == one_impl()) {
                return v2;
            }
            if (v2 == one_impl()) {
                return v1;
            }
            int l1, l2;
            l1 = level_impl(v1);
            l2 = level_impl(v2);
            for (;;) {
                if (v1 == v2) {
                    return v1;
                }
                if (l1 < l2) {
                    v1 = high_impl(v1);
                    if (v1 == one_impl()) {
                        return v2;
                    }
                    l1 = level_impl(v1);
                } else if (l1 > l2) {
                    v2 = high_impl(v2);
                    if (v2 == one_impl()) {
                        return v1;
                    }
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

        @Override
        protected int do_union(int v1, int v2) {
            if (v1 == v2) {
                return v1;
            }
            if (v1 == one_impl()) {
                return v2;
            }
            if (v2 == one_impl()) {
                return v1;
            }
            int l1, l2;
            l1 = level_impl(v1);
            l2 = level_impl(v2);
            int vv1 = v1, vv2 = v2, lev = l1;
            if (l1 <= l2) {
                vv1 = high_impl(v1);
            }
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

        @Override
        protected int do_unionvar(int v, int var) {
            return do_unionlevel(v, var2Level(var));
        }

        private int do_unionlevel(int v, int lev) {
            if (v == one_impl()) {
                return makenode_impl(lev, zero_impl(), one_impl());
            }
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

        @SuppressWarnings("deprecation")
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (USE_FINALIZER) {
                if (FINALIZER_CHECK_BDD_NOT_FREED && v != invalid_bdd_impl()) {
                    System.out.println("BDD not freed! " + System.identityHashCode(this));
                }
                deferredFree(v);
            }
        }
    }

    protected IntBDDVarSet makeBDDVarSet(/* bdd */int v) {
        if (isZDD()) {
            if (USE_FINALIZER) {
                return new IntZDDVarSetWithFinalizer(v);
            } else {
                return new IntZDDVarSet(v);
            }
        } else {
            if (USE_FINALIZER) {
                return new IntBDDVarSetWithFinalizer(v);
            } else {
                return new IntBDDVarSet(v);
            }
        }
    }

    protected static final /* bdd */int unwrap(BDDVarSet b) {
        return ((IntBDDVarSet)b).v;
    }

    public class IntBDDBitVector extends BDDBitVector {
        protected IntBDDBitVector(int bitnum) {
            super(bitnum);
        }

        @Override
        public BDDFactory getFactory() {
            return BDDFactoryIntImpl.this;
        }
    }

    @Override
    public BDD ithVar(/* bdd */int var) {
        return makeBDD(ithVar_impl(var));
    }

    @Override
    public BDD nithVar(/* bdd */int var) {
        return makeBDD(nithVar_impl(var));
    }

    @Override
    public int nodeCount(Collection<BDD> r) {
        return nodeCount_impl2(unwrap(r));
    }

    @Override
    public BDD one() {
        return makeBDD(one_impl());
    }

    @Override
    public BDD universe() {
        return makeBDD(universe_impl());
    }

    @Override
    public BDDVarSet emptySet() {
        return makeBDDVarSet(one_impl());
    }

    @Override
    public void printTable(BDD b) {
        printTable_impl(unwrap(b));
    }

    @Override
    public BDD zero() {
        return makeBDD(zero_impl());
    }

    @Override
    public void done() {
        if (USE_FINALIZER) {
            System.gc();
            System.runFinalization();
            handleDeferredFree();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.done();
    }

    protected /* bdd */int[] to_free = new /* bdd */int[8];

    protected /* bdd */int to_free_length = 0;

    public void deferredFree(int v) {
        if (v == invalid_bdd_impl()) {
            return;
        }
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

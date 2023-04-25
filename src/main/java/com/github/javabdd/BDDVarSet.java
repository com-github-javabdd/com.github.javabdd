//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2004-2023 John Whaley and others
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

/**
 * Some BDD methods, namely {@code exist()}, {@code forall()}, {@code unique()}, {@code relprod()}, {@code applyAll()},
 * {@code applyEx()}, {@code applyUni()}, and {@code satCount()} take a BDDVarSet argument.
 */
public abstract class BDDVarSet {
    /**
     * Returns the factory that created this BDDVarSet.
     *
     * @return factory that created this BDDVarSet
     */
    public abstract BDDFactory getFactory();

    public abstract BDD toBDD();

    public abstract BDDVarSet id();

    public abstract void free();

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract int[] toArray();

    public abstract int[] toLevelArray();

    @Override
    public String toString() {
        // return Arrays.toString(toArray());
        int[] a = toArray();
        StringBuffer sb = new StringBuffer(a.length * 4 + 2);
        sb.append('[');
        for (int i = 0; i < a.length; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(a[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Scans this BDD and copies the stored variables into an array of BDDDomains. The domains returned are guaranteed
     * to be in ascending order.
     *
     * <p>
     * Compare to fdd_scanset.
     * </p>
     *
     * @return int[]
     */
    public BDDDomain[] getDomains() {
        int[] fv;
        BDDDomain[] varset;
        int fn;
        int num, n, m, i;

        fv = this.toArray();
        fn = fv.length;

        BDDFactory factory = getFactory();

        for (n = 0, num = 0; n < factory.numberOfDomains(); n++) {
            BDDDomain dom = factory.getDomain(n);
            int[] ivar = dom.vars();
            boolean found = false;
            for (m = 0; m < dom.varNum() && !found; m++) {
                for (i = 0; i < fn && !found; i++) {
                    if (ivar[m] == fv[i]) {
                        num++;
                        found = true;
                    }
                }
            }
        }

        varset = new BDDDomain[num];

        for (n = 0, num = 0; n < factory.numberOfDomains(); n++) {
            BDDDomain dom = factory.getDomain(n);
            int[] ivar = dom.vars();
            boolean found = false;
            for (m = 0; m < dom.varNum() && !found; m++) {
                for (i = 0; i < fn && !found; i++) {
                    if (ivar[m] == fv[i]) {
                        varset[num++] = dom;
                        found = true;
                    }
                }
            }
        }

        return varset;
    }

    /**
     * Returns a new BDDVarSet that is the union of the current BDDVarSet and the given BDDVarSet. This constructs a new
     * set; neither the current nor the given BDDVarSet is modified.
     *
     * @param b BDDVarSet to union with
     * @return a new BDDVarSet that is the union of the two sets
     */
    public abstract BDDVarSet union(BDDVarSet b);

    /**
     * Returns a new BDDVarSet that is the union of the current BDDVarSet and the given variable. This constructs a new
     * set; the current BDDVarSet is not modified.
     *
     * @param var variable to add to set
     * @return a new BDDVarSet that includes the given variable
     */
    public abstract BDDVarSet union(int var);

    /**
     * Modifies this BDDVarSet to include all of the vars in the given set. This modifies the current set in place and
     * consumes the given set.
     *
     * @param b BDDVarSet to union in
     * @return this
     */
    public abstract BDDVarSet unionWith(BDDVarSet b);

    /**
     * Modifies this BDDVarSet to include the given variable. This modifies the current set in place.
     *
     * @param var variable to add to set
     * @return this
     */
    public abstract BDDVarSet unionWith(int var);

    /**
     * Returns a new BDDVarSet that is the union of the current BDDVarSet and the given BDDVarSet. This constructs a new
     * set; neither the current nor the given BDDVarSet is modified.
     *
     * @param b BDDVarSet to union with
     * @return a new BDDVarSet that is the union of the two sets
     */
    public abstract BDDVarSet intersect(BDDVarSet b);

    /**
     * Modifies this BDDVarSet to include all of the vars in the given set. This modifies the current set in place and
     * consumes the given set.
     *
     * @param b BDDVarSet to union in
     * @return this
     */
    public abstract BDDVarSet intersectWith(BDDVarSet b);

    @Override
    public abstract int hashCode();

    /**
     * Returns true if the sets are equal.
     *
     * @param that other set
     * @return true if the sets are equal
     */
    public abstract boolean equalsBDDVarSet(BDDVarSet that);

    @Override
    public final boolean equals(Object o) {
        if (o instanceof BDDVarSet) {
            return equalsBDDVarSet((BDDVarSet)o);
        }
        return false;
    }

    /**
     * Default implementation of BDDVarSet based on BDDs.
     */
    public static class DefaultImpl extends BDDVarSet {
        /**
         * BDD representation of the set of variables. Treated like a linked list of variables.
         */
        protected BDD b;

        /**
         * Construct a BDDVarSet backed by the given BDD. Ownership of the given BDD is transferred to this BDDVarSet,
         * so you should not touch it after construction!
         *
         * @param b BDD to use in constructing BDDVarSet
         */
        public DefaultImpl(BDD b) {
            this.b = b;
        }

        @Override
        public void free() {
            if (b != null) {
                b.free();
                b = null;
            }
        }

        @Override
        public BDDFactory getFactory() {
            return b.getFactory();
        }

        @Override
        public BDDVarSet id() {
            return new DefaultImpl(b.id());
        }

        @Override
        public BDDVarSet intersect(BDDVarSet s) {
            DefaultImpl i = (DefaultImpl)s;
            return new DefaultImpl(b.or(i.b));
        }

        @Override
        public BDDVarSet intersectWith(BDDVarSet s) {
            DefaultImpl i = (DefaultImpl)s;
            b.orWith(i.b);
            i.b = null;
            return this;
        }

        @Override
        public boolean isEmpty() {
            return b.isOne();
        }

        @Override
        public int size() {
            int result = 0;
            BDD r = b.id();
            while (!r.isOne()) {
                if (r.isZero()) {
                    throw new BDDException("varset contains zero");
                }
                ++result;
                BDD q = r.high();
                r.free();
                r = q;
            }
            r.free();
            return result;
        }

        @Override
        public int[] toArray() {
            int[] result = new int[size()];
            int i = -1;
            BDD r = b.id();
            while (!r.isOne()) {
                if (r.isZero()) {
                    throw new BDDException("varset contains zero");
                }
                result[++i] = r.var();
                BDD q = r.high();
                r.free();
                r = q;
            }
            r.free();
            return result;
        }

        @Override
        public BDD toBDD() {
            return b.id();
        }

        @Override
        public int[] toLevelArray() {
            int[] result = new int[size()];
            int i = -1;
            BDD r = b.id();
            while (!r.isOne()) {
                if (r.isZero()) {
                    throw new BDDException("varset contains zero");
                }
                result[++i] = r.level();
                BDD q = r.high();
                r.free();
                r = q;
            }
            r.free();
            return result;
        }

        @Override
        public BDDVarSet union(BDDVarSet s) {
            DefaultImpl i = (DefaultImpl)s;
            return new DefaultImpl(b.and(i.b));
        }

        @Override
        public BDDVarSet union(int var) {
            BDD ith = b.getFactory().ithVar(var);
            DefaultImpl j = new DefaultImpl(b.and(ith));
            ith.free();
            return j;
        }

        @Override
        public BDDVarSet unionWith(BDDVarSet s) {
            DefaultImpl i = (DefaultImpl)s;
            b.andWith(i.b);
            i.b = null;
            return this;
        }

        @Override
        public BDDVarSet unionWith(int var) {
            b.andWith(b.getFactory().ithVar(var));
            return this;
        }

        @Override
        public int hashCode() {
            return b.hashCode();
        }

        @Override
        public boolean equalsBDDVarSet(BDDVarSet s) {
            if (s instanceof DefaultImpl) {
                return equalsDefaultImpl((DefaultImpl)s);
            }
            return false;
        }

        public boolean equalsDefaultImpl(DefaultImpl s) {
            return b.equals(s.b);
        }
    }
}

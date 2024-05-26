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

/**
 * Encodes a table of variable pairs. This is used for replacing variables in a BDD.
 */
public abstract class BDDPairing {
    /**
     * Adds the pair (oldvar, newvar) to this table of pairs. This results in oldvar being substituted with newvar in a
     * call to BDD.replace().
     *
     * <p>
     * Compare to bdd_setpair.
     * </p>
     *
     * @param oldvar old variable
     * @param newvar new variable
     */
    public abstract void set(int oldvar, int newvar);

    /**
     * Like set(), but with a whole list of pairs.
     *
     * <p>
     * Compare to bdd_setpairs.
     * </p>
     *
     * @param oldvar old variables
     * @param newvar new variables
     */
    public void set(int[] oldvar, int[] newvar) {
        if (oldvar.length != newvar.length) {
            throw new BDDException();
        }

        for (int n = 0; n < oldvar.length; n++) {
            this.set(oldvar[n], newvar[n]);
        }
    }

    /**
     * Adds the pair (oldvar, newvar) to this table of pairs. This results in oldvar being substituted with newvar in a
     * call to bdd.replace(). The variable oldvar is substituted with the BDD newvar. The possibility to substitute with
     * any BDD as newvar is utilized in BDD.compose(), whereas only the topmost variable in the BDD is used in
     * BDD.replace().
     *
     * <p>
     * Compare to bdd_setbddpair.
     * </p>
     *
     * @param oldvar old variable
     * @param newvar new BDD
     */
    public abstract void set(int oldvar, BDD newvar);

    /**
     * Like set(), but with a whole list of pairs.
     *
     * <p>
     * Compare to bdd_setbddpairs.
     * </p>
     *
     * @param oldvar old variables
     * @param newvar new BDDs
     */
    public void set(int[] oldvar, BDD[] newvar) {
        if (oldvar.length != newvar.length) {
            throw new BDDException();
        }

        for (int n = 0; n < newvar.length; n++) {
            this.set(oldvar[n], newvar[n]);
        }
    }

    /**
     * Defines each variable in the finite domain block p1 to be paired with the corresponding variable in p2.
     *
     * <p>
     * Compare to fdd_setpair.
     * </p>
     *
     * @param p1 first finite domain block
     * @param p2 second finite domain block
     */
    public void set(BDDDomain p1, BDDDomain p2) {
        int[] ivar1 = p1.vars();
        int[] ivar2 = p2.vars();
        this.set(ivar1, ivar2);
    }

    /**
     * Like set(), but with a whole list of pairs.
     *
     * <p>
     * Compare to fdd_setpairs.
     * </p>
     *
     * @param p1 first finite domain blocks
     * @param p2 second finite domain blocks
     */
    public void set(BDDDomain[] p1, BDDDomain[] p2) {
        if (p1.length != p2.length) {
            throw new BDDException();
        }

        for (int n = 0; n < p1.length; n++) {
            if (p1[n].varNum() != p2[n].varNum()) {
                throw new BDDException();
            }
        }

        for (int n = 0; n < p1.length; n++) {
            this.set(p1[n], p2[n]);
        }
    }

    /**
     * Resets this table of pairs by setting all substitutions to their default values (that is, no change).
     *
     * <p>
     * Compare to bdd_resetpair.
     * </p>
     */
    public abstract void reset();
}

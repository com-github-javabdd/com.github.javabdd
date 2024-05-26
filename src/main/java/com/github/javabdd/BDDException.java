//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2003-2023 John Whaley and com.github.javabdd contributors
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
 * An exception caused by an invalid BDD operation.
 */
public class BDDException extends RuntimeException {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3761969363112243251L;

    public BDDException() {
        super();
    }

    public BDDException(String s) {
        super(s);
    }
}

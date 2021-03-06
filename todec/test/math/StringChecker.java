/*
 * Copyright (c) 2018, Raffaello Giulietti. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * This particular file is subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package math;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;

abstract class StringChecker {

    private String s;
    private long c;
    private int q;
    private int len10;

    StringChecker(String s) {
        this.s = s;
    }

    /*
    Returns whether s syntactically meets the expected output of
    Double::toString. It is restricted to finite positive outputs.
    It is an unusually long method but rather straightforward, too.
    Many conditionals could be merged, but KISS here.
     */
    private boolean parse() {
        try {
            // first determine interesting boundaries in the string
            StringReader r = new StringReader(s);
            int ch = r.read();

            int i = 0;
            while (ch == '0') {
                ++i;
                ch = r.read();
            }
            // i is just after zeroes starting the integer

            int p = i;
            while ('0' <= ch && ch <= '9') {
                c = 10 * c + (ch - '0');
                if (c < 0) {
                    return false;
                }
                ++len10;
                ++p;
                ch = r.read();
            }
            // p is just after digits ending the integer

            int fz = p;
            if (ch == '.') {
                ++fz;
                ch = r.read();
            }
            // fz is just after a decimal '.'

            int f = fz;
            while (ch == '0') {
                c = 10 * c + (ch - '0');
                if (c < 0) {
                    return false;
                }
                ++len10;
                ++f;
                ch = r.read();
            }
            // f is just after zeroes starting the fraction

            if (c == 0) {
                len10 = 0;
            }
            int x = f;
            while ('0' <= ch && ch <= '9') {
                c = 10 * c + (ch - '0');
                if (c < 0) {
                    return false;
                }
                ++len10;
                ++x;
                ch = r.read();
            }
            // x is just after digits ending the fraction

            int g = x;
            if (ch == 'E') {
                ++g;
                ch = r.read();
            }
            // g is just after an exponent indicator 'E'

            int ez = g;
            if (ch == '-') {
                ++ez;
                ch = r.read();
            }
            // ez is just after a '-' sign in the exponent

            int e = ez;
            while (ch == '0') {
                ++e;
                ch = r.read();
            }
            // e is just after zeroes starting the exponent

            int z = e;
            while ('0' <= ch && ch <= '9') {
                q = 10 * q + (ch - '0');
                if (q < 0) {
                    return false;
                }
                ++z;
                ch = r.read();
            }
            // z is just after digits ending the exponent

            // No other char after the number
            if (z != s.length()) {
                return false;
            }

            // The integer must be present
            if (p == 0) {
                return false;
            }

            // The decimal '.' must be present
            if (fz == p) {
                return false;
            }

            // The fraction must be present
            if (x == fz) {
                return false;
            }

            // The fraction is not 0 or it consists of exactly one 0
            if (f == x && f - fz > 1) {
                return false;
            }

            // Plain notation, no exponent
            if (x == z) {
                // At most one 0 starting the integer
                if (i > 1) {
                    return false;
                }

                // If the integer is 0, at most 2 zeroes start the fraction
                if (i == 1 && f - fz > 2) {
                    return false;
                }

                // The integer cannot have more than 7 digits
                if (p > 7) {
                    return false;
                }

                q = fz - x;

                // OK for plain notation
                return true;
            }

            // Computerized scientific notation

            // The integer has exactly one nonzero digit
            if (i != 0 || p != 1) {
                return false;
            }

            //
            // There must be an exponent indicator
            if (x == g) {
                return false;
            }

            // There must be an exponent
            if (ez == z) {
                return false;
            }

            // The exponent must not start with zeroes
            if (ez != e) {
                return false;
            }

            if (g != ez) {
                q = -q;
            }

            // The exponent must not lie in [-3, 7)
            if (-3 <= q && q < 7) {
                return false;
            }

            q += fz - x;

            // OK for computerized scientific notation
            return true;
        } catch (IOException ex) {
            // An IOException on a StringReader??? Please...
            return false;
        }
    }

    boolean isOK() {
        if (isNaN()) {
            return s.equals("NaN");
        }
        if (isNegative()) {
            if (s.isEmpty() || s.charAt(0) != '-') {
                return false;
            }
            invert();
            s = s.substring(1);
        }
        if (isInfinity()) {
            return s.equals("Infinity");
        }
        if (isZero()) {
            return s.equals("0.0");
        }
        if (!parse()) {
            return false;
        }
        if (len10 < 2) {
            c *= 10;
            q -= 1;
            len10 += 1;
        }
        if (2 > len10 || len10 > maxLen10()) {
            return false;
        }

        // The exponent is bounded
        if (minExp() > q + len10 || q + len10 > maxExp()) {
            return false;
        }

        // s must recover v
        try {
            if (!recovers(s)) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // Get rid of trailing zeroes, still ensuring at least 2 digits
        while (len10 > 2 && c % 10 == 0) {
            c /= 10;
            q += 1;
            len10 -= 1;
        }

        if (len10 > 2) {
            // Try with a shorter number less than v...
            if (recovers(BigDecimal.valueOf(c / 10, -q - 1))) {
                return false;
            }

            // ... and with a shorter number greater than v
            if (recovers(BigDecimal.valueOf(c / 10 + 1, -q - 1))) {
                return false;
            }
        }

        // Try with the decimal predecessor...
        BigDecimal dp = c == 10 ?
                BigDecimal.valueOf(99, -q + 1) :
                BigDecimal.valueOf(c - 1, -q);
        if (recovers(dp)) {
            BigDecimal bv = toBigDecimal();
            BigDecimal deltav = bv.subtract(BigDecimal.valueOf(c, -q));
            if (deltav.signum() >= 0) {
                return true;
            }
            BigDecimal delta = dp.subtract(bv);
            if (delta.signum() >= 0) {
                return false;
            }
            int cmp = deltav.compareTo(delta);
            return cmp > 0 || cmp == 0 && (c & 0x1) == 0;
        }

        // ... and with the decimal successor
        BigDecimal ds = BigDecimal.valueOf(c + 1, -q);
        if (recovers(ds)) {
            BigDecimal bv = toBigDecimal();
            BigDecimal deltav = bv.subtract(BigDecimal.valueOf(c, -q));
            if (deltav.signum() <= 0) {
                return true;
            }
            BigDecimal delta = ds.subtract(bv);
            if (delta.signum() <= 0) {
                return false;
            }
            int cmp = deltav.compareTo(delta);
            return cmp < 0 || cmp == 0 && (c & 0x1) == 0;
        }

        return true;
    }

    abstract BigDecimal toBigDecimal();

    abstract boolean recovers(BigDecimal b);

    abstract boolean recovers(String s);

    abstract int maxExp();

    abstract int minExp();

    abstract int maxLen10();

    abstract boolean isZero();

    abstract boolean isInfinity();

    abstract void invert();

    abstract boolean isNegative();

    abstract boolean isNaN();

}

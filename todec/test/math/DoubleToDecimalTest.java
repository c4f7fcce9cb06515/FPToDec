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

import org.junit.Test;

import java.util.Random;

import static java.lang.Math.scalb;
import static junit.framework.TestCase.*;
import static math.DecimalChecker.isCorrect;
import static math.DoubleToDecimal.Double.*;

public class DoubleToDecimalTest {

    private String toDecimal(double v) {
        String s = DoubleToDecimal.toString(v);
//        String s = Double.toString(v);
        assertTrue(isCorrect(v, s));
        return s;
    }

    @Test
    public void testExtremeValues() {
        toDecimal(Double.NEGATIVE_INFINITY);
        toDecimal(-Double.MAX_VALUE);
        toDecimal(-Double.MIN_NORMAL);
        toDecimal(-Double.MIN_VALUE);
        toDecimal(-0.0);
        toDecimal(0.0);
        toDecimal(Double.MIN_VALUE);
        toDecimal(Double.MIN_NORMAL);
        toDecimal(Double.MAX_VALUE);
        toDecimal(Double.POSITIVE_INFINITY);
        toDecimal(Double.NaN);
    }

    /*
    A few powers of 10 are incorrectly rendered by the JDK.
    The rendering is either too long or it is not the closest decimal.
     */
    @Test
    public void testPowersOf10() {
        for (int e = E_MIN_VALUE; e <= E_MAX_VALUE; ++e) {
            toDecimal(Double.parseDouble("1e" + e));
        }
    }

    /*
    Many powers of 2 are incorrectly rendered by the JDK.
    The rendering is either too long or it is not the closest decimal.
     */
    @Test
    public void testPowersOf2() {
        for (double v = Double.MIN_VALUE; v <= Double.MAX_VALUE; v *= 2.0) {
            toDecimal(v);
        }
    }

    /*
    There are tons of doubles that are rendered incorrectly by the JDK.
    While the renderings correctly round back to the original value,
    they are longer than needed or are not the closest decimal to the double.
    Here are just a very few examples.
     */
    private static final String[] Anomalies = {
            // JDK renders these, and others, with 18 digits!
            "2.82879384806159E17", "1.387364135037754E18",
            "1.45800632428665E17",

            // JDK renders these longer than needed.
            "1.6E-322", "6.3E-322",
            "7.3879E20", "2.0E23", "7.0E22", "9.2E22",
            "9.5E21", "3.1E22", "5.63E21", "8.41E21",

            // JDK does not render these, and many others, as the closest.
            "9.9E-324", "9.9E-323",
            "1.9400994884341945E25", "3.6131332396758635E25",
            "2.5138990223946153E25",
    };

    @Test
    public void testSomeAnomalies() {
        for (String dec : Anomalies) {
            toDecimal(Double.parseDouble(dec));
        }
    }

    /*
    Values are from
    Paxson V, "A Program for Testing IEEE Decimal–Binary Conversion"
     */
    private static final double[] PaxsonSignificands = {
            8_511_030_020_275_656L,
            5_201_988_407_066_741L,
            6_406_892_948_269_899L,
            8_431_154_198_732_492L,
            6_475_049_196_144_587L,
            8_274_307_542_972_842L,
            5_381_065_484_265_332L,
            6_761_728_585_499_734L,
            7_976_538_478_610_756L,
            5_982_403_858_958_067L,
            5_536_995_190_630_837L,
            7_225_450_889_282_194L,
            7_225_450_889_282_194L,
            8_703_372_741_147_379L,
            8_944_262_675_275_217L,
            7_459_803_696_087_692L,
            6_080_469_016_670_379L,
            8_385_515_147_034_757L,
            7_514_216_811_389_786L,
            8_397_297_803_260_511L,
            6_733_459_239_310_543L,
            8_091_450_587_292_794L,

            6_567_258_882_077_402L,
            6_712_731_423_444_934L,
            6_712_731_423_444_934L,
            5_298_405_411_573_037L,
            5_137_311_167_659_507L,
            6_722_280_709_661_868L,
            5_344_436_398_034_927L,
            8_369_123_604_277_281L,
            8_995_822_108_487_663L,
            8_942_832_835_564_782L,
            8_942_832_835_564_782L,
            8_942_832_835_564_782L,
            6_965_949_469_487_146L,
            6_965_949_469_487_146L,
            6_965_949_469_487_146L,
            7_487_252_720_986_826L,
            5_592_117_679_628_511L,
            8_887_055_249_355_788L,
            6_994_187_472_632_449L,
            8_797_576_579_012_143L,
            7_363_326_733_505_337L,
            8_549_497_411_294_502L,
    };

    private static final int[] PaxsonExponents = {
            -342,
            -824,
             237,
              72,
              99,
             726,
            -456,
             -57,
             376,
             377,
              93,
             710,
             709,
             117,
              -1,
            -707,
            -381,
             721,
            -828,
            -345,
             202,
            -473,

             952,
             535,
             534,
            -957,
            -144,
             363,
            -169,
            -853,
            -780,
            -383,
            -384,
            -385,
            -249,
            -250,
            -251,
             548,
             164,
             665,
             690,
             588,
             272,
            -448,
    };

    @Test
    public void testPaxson() {
        for (int i = 0; i < PaxsonSignificands.length; ++i) {
            toDecimal(scalb(PaxsonSignificands[i], PaxsonExponents[i]));
        }
    }

    /*
    Tests all integers of the form yx_xxx_000_000_000_000_000, y != 0.
    These are all exact doubles.
     */
    @Test
    public void testLongs() {
        for (int i = 10_000; i < 100_000; ++i) {
            String s = toDecimal(i * 1e15);
            String xp = Integer.toString(i);
            int j = 5;
            while (--j >= 2 && xp.charAt(j) == '0') ; // empty body intended
            xp = xp.substring(0, 1) + "." + xp.substring(1, j + 1) + "E19";
            assertEquals(xp, s);
        }
    }

    /*
    Tests all integers up to 100_000.
    These are all exact doubles.
     */
    @Test
    public void testInts() {
        for (int i = -100_000; i <= 100_000; ++i) {
            String s = toDecimal(i);
            String xp = Integer.toString(i) + ".0";
            assertEquals(xp, s);
        }
    }

    /*
    Random doubles over the whole range
     */
    @Test
    public void testRandom() {
        Random r = new Random();
        for (int i = 0; i < 100_000; ++i) {
            toDecimal(Double.longBitsToDouble(r.nextLong()));
        }
    }

    /*
    Random doubles over the integer range [0, 10^15).
    These integers are all exact doubles.
     */
    @Test
    public void testRandomUnit() {
        Random r = new Random();
        for (int i = 0; i < 100_000; ++i) {
            toDecimal(r.nextLong() % 1_000_000_000_000_000L);
        }
    }

    /*
    Random doubles over the range [0, 10^15) as "multiples" of 1e-3
     */
    @Test
    public void testRandomMilli() {
        Random r = new Random();
        for (int i = 0; i < 100_000; ++i) {
            toDecimal(r.nextLong() % 1_000_000_000_000_000_000L / 1e3);
        }
    }

    /*
    Random doubles over the range [0, 10^15) as "multiples" of 1e-6
     */
    @Test
    public void testRandomMicro() {
        Random r = new Random();
        for (int i = 0; i < 100_000; ++i) {
            toDecimal((r.nextLong() & 0x7FFF_FFFF_FFFF_FFFFL) / 1e6);
        }
    }

}

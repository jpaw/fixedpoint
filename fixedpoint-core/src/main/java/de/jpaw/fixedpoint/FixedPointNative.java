package de.jpaw.fixedpoint;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Support class which performs 64 * 64 bit to 128 bit multiplication, and subsequent scaling. */
public class FixedPointNative {
    static private final String LIBRARY_NAME = "jpawFP128";
    static private volatile boolean nativeAvailable = false;
    static {
        try {
            System.loadLibrary(LIBRARY_NAME); // Load native library at runtime
            System.out.println("Successfully loaded native library " + LIBRARY_NAME);
            nativeAvailable = true;
        } catch (Exception e) {
            System.out.println("Failed to load native library " + LIBRARY_NAME + ": " + e);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Failed to call native library " + LIBRARY_NAME + ": " + e);
        }
    }
    
    /** Computes a * b / c, with a 128 bit intermediate result. c is known to be a strictly positive number. */
    static private native long multdiv128(long a, long b, long c, int roundingMode);
    
    private final static long [] powersOfTen = {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L
    };
    
    /** decimalsScale is in range 1..18 */
    public static long multiply_and_scale(long mantissaA, long mantissaB, int decimalsScale, RoundingMode rounding) {
        if (nativeAvailable) {
            return multdiv128(mantissaA, mantissaB, powersOfTen[decimalsScale], rounding.ordinal());
        }
        // fallback implementation. Is there a better one with just BigInteger?
        BigDecimal product = BigDecimal.valueOf(mantissaA).multiply(BigDecimal.valueOf(mantissaB, decimalsScale));
        BigDecimal scaledProduct = product.setScale(0, rounding);
        return scaledProduct.longValue();
    }

    /** decimalsScale is in range 1..18 */
    public static long scale_and_divide(long mantissa, int decimalsScale, long divisor, RoundingMode rounding) {
        if (nativeAvailable) {
            return multdiv128(mantissa, powersOfTen[decimalsScale], divisor, rounding.ordinal());
        }
        // fallback implementation. Is there a better one with just BigInteger?
        BigDecimal quotient = BigDecimal.valueOf(mantissa).divide(BigDecimal.valueOf(divisor, decimalsScale), 0, rounding);
        return quotient.longValue();
    }

}

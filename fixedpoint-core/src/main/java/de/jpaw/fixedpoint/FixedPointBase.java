package de.jpaw.fixedpoint;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import de.jpaw.fixedpoint.types.VariableUnits;

/** Base class for fixed point arithmetic, using an implicitly scaled long value.
 * There are subclasses per number of decimals (from 0 to 9), and a variable scale
 * class, which stores the scale in a separate instance variable.
 *  
 * Instances of this class are immutable.
 * 
 * @author Michael Bischoff
 *
 */
public abstract class FixedPointBase<CLASS extends FixedPointBase<CLASS>> implements Serializable, Comparable<FixedPointBase<?>> {
    private static final long serialVersionUID = 8834214052987561284L;
    protected final static long [] powersOfTen = {
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
    private final long mantissa;
    
    protected FixedPointBase(long mantissa) {
        this.mantissa = mantissa;
    }
    
    /** Returns a fixed point value object which has the same number of decimals as this, with a given mantissa.
     * This implementation returns cached instances for 0 and 1. Otherwise, in case this has the same mantissa, this is returned. */
    public abstract CLASS newInstanceOf(long mantissa);
    
    /** Returns a fixed point value object which has a different number of decimals. Most implementations have a fixed scale and will not support this. */
    public CLASS newInstanceOf(long mantissa, int scale) {
        throw new ArithmeticException("Creating instances of different scale not supported for " + getClass().getCanonicalName());
    }
    
    /** Get the number of decimals. */
    public abstract int getScale();
    
    /** Get the number 0 in the same scale. */
    public abstract CLASS getZero(); 
    
    /** Get the number 1 in the same scale. */
    public abstract CLASS getUnit(); 
    
    /** Get a reference to myself (essentially "this", but avoids a type cast. */
    public abstract CLASS getMyself();
    
    /** Get the value representing the number 1. */
    public abstract long getUnitAsLong(); 
    
    /** Get the mantissa of this number as a primitive long. */
    public long getMantissa() {
        return mantissa;
    }
    
    /** Returns true if to instances of the same subclass will always have the same number of decimals. */
    public boolean isFixedScale() {
        return true;  // default implementations: most subtypes do.
    }
    
    /** Returns the value in a human readable form. */
    @Override
    public String toString() {
        return BigDecimal.valueOf(mantissa, getScale()).toPlainString();
    }
    
    @Override
    public int hashCode() {
        return 31 * getScale() + (int)(mantissa ^ mantissa >>> 32);
    }
    
    /** As with BigDecimal, equals returns true only of both objects are identical in all aspects. Use compareTo for numerical identity. */
    @Override
    public boolean equals(Object that) {
        if (that == null || !(that instanceof FixedPointBase))
            return false;
        if (that == this)
            return true;
        FixedPointBase<?> _that = (FixedPointBase<?>)that;
        return getScale() == _that.getScale() && mantissa == _that.mantissa && this.getClass() == that.getClass();
    }
    
    /** Returns the absolute value of this, using the same type and scale. */
    public CLASS abs() {
        if (mantissa >= 0)
            return getMyself();
        return newInstanceOf(-mantissa);
    }
    
    /** Returns a number with the opposite sign. */
    public CLASS negate() {
        return newInstanceOf(-mantissa);
    }
    /** Xtend syntax sugar. unary minus maps to the negate method. */
    public CLASS operator_minus() {
        return negate();
    }
    
    /** Returns the signum of this number, -1, 0, or +1. */
    public int signum() {
        return Long.signum(mantissa);
    }
    
    /** Returns true if this is numerically equivalent to 1. */
    public boolean isOne() {
        return mantissa == getUnitAsLong();
    }
    
    /** Returns true if this is numerically equivalent to -1. */
    public boolean isMinusOne() {
        return mantissa == -getUnitAsLong();
    }
    
    /** Returns true if this is not 0. */
    public boolean isNotZero() {
        return mantissa != 0;
    }
    
    /** Returns true if this is 0. */
    public boolean isZero() {
        return mantissa == 0;
    }
    /** Xtend syntax sugar. not maps to the isZero method. */
    public boolean operator_not() {
        return mantissa == 0;
    }
    
    /** Returns a unit in the last place. */
    public CLASS ulp() {
        return newInstanceOf(1);
    }
    
    /** Returns the number scaled by 0.01, by playing with the scale (if possible). */
    public VariableUnits percent() {
        switch (getScale()) {
        case 18:
            return VariableUnits.valueOf(mantissa / 100, 18);
        case 17:
            return VariableUnits.valueOf(mantissa / 10, 18);
        default:  // 0 .. 16 decimals
            return VariableUnits.valueOf(mantissa, getScale() + 2);
        }
    }
    
    /** Returns the signum of this number, -1, 0, or +1.
     * Special care is taken in this implementation to work around any kind of integral overflows. */
    @Override
    public int compareTo(FixedPointBase<?> that) {
        // first check is on signum only, to avoid incorrect responses due to integral overflow (MIN_VALUE must be < than MAX_VALUE)
        final int signumThis = Long.signum(this.mantissa);
        final int signumThat = Long.signum(that.mantissa);
        if (signumThis != signumThat) {
            // simple case, number differs by sign already
            return signumThis < signumThat ? -1 : 1;
        }
        if (signumThat == 0)
            return 0; // both are 0
        // here, both are either negative or positive
        // medium difficulty: they have the same scale
        int scaleDiff = this.getScale() - that.getScale();
        if (scaleDiff == 0) {
            // simple: compare the mantissas
            if (this.mantissa == that.mantissa)
                return 0;
            return this.mantissa < that.mantissa ? -1 : 1;
        }
        // both operands have the same sign, but differ in scaling. Scale down first, and only if the numbers then are the same, scale up
        if (scaleDiff < 0) {
            long diff = mantissa - that.mantissa / powersOfTen[-scaleDiff];
            if (diff != 0)
                return diff < 0 ? -1 : 1;
            // scaled difference is 0. In this case, scaling up cannot result in an overflow.
            diff = mantissa * powersOfTen[-scaleDiff] - that.mantissa;
            if (diff != 0)
                return diff < 0 ? -1 : 1;
            return 0;
        } else {
            long diff = mantissa  / powersOfTen[scaleDiff] - that.mantissa;
            if (diff != 0)
                return diff < 0 ? -1 : 1;
            // scaled difference is 0. In this case, scaling up cannot result in an overflow.
            diff = mantissa - that.mantissa * powersOfTen[scaleDiff];
            if (diff != 0)
                return diff < 0 ? -1 : 1;
            return 0;
        }
    }
    /** Xtend syntax sugar. spaceship maps to the compareTo method. */
    public int operator_spaceship(FixedPointBase<?> that) {
        return compareTo(that);
    }
    public boolean operator_equals(FixedPointBase<?> that) {
        return compareTo(that) == 0;
    }
    public boolean operator_notEquals(FixedPointBase<?> that) {
        return compareTo(that) != 0;
    }
    public boolean operator_lessThan(FixedPointBase<?> that) {
        return compareTo(that) < 0;
    }
    public boolean operator_lessEquals(FixedPointBase<?> that) {
        return compareTo(that) <= 0;
    }
    public boolean operator_greaterThan(FixedPointBase<?> that) {
        return compareTo(that) > 0;
    }
    public boolean operator_greaterEquals(FixedPointBase<?> that) {
        return compareTo(that) >= 0;
    }
    
    /** Returns the smaller of this and the parameter. */
    public CLASS min(CLASS that) {
        return this.compareTo(that) <= 0 ? getMyself() : that;
    }
    
    /** Returns the bigger of this and the parameter. */
    public CLASS max(CLASS that) {
        return this.compareTo(that) >= 0 ? getMyself() : that;
    }
    
    /** Returns the smaller of this and the parameter, allows different type parameters. */
    public FixedPointBase<?> gmin(FixedPointBase<?> that) {
        return this.compareTo(that) <= 0 ? this : that;
    }
    
    /** Returns the bigger of this and the parameter, allows different type parameters. */
    public FixedPointBase<?> gmax(FixedPointBase<?> that) {
        return this.compareTo(that) >= 0 ? this : that;
    }
    
    /** Multiplies a fixed point number by an integral factor. The scale (and type) of the product is the same as the one of this. */
    public CLASS multiply(int factor) {
        return newInstanceOf(mantissa * factor);
    }
    /** Xtend syntax sugar. multiply maps to the multiply method. */
    public CLASS operator_multiply(int factor) {
        return multiply(factor);
    }
    
    /** Returns this + 1. */
    public CLASS increment() {
        return newInstanceOf(mantissa + powersOfTen[getScale()]);
    }
    /** Xtend syntax sugar. ++ maps to the increment method. */
    public CLASS operator_plusplus() {
        return increment();
    }
    
    /** Returns this - 1. */
    public CLASS decrement() {
        return newInstanceOf(mantissa - powersOfTen[getScale()]);
    }
    /** Xtend syntax sugar. -- maps to the decrement method. */
    public CLASS operator_minusminus() {
        return decrement();
    }
    
    /** Subroutine to provide the mantissa of a multiplication. */
    /*
    public long mantissa_of_multiplication(FixedPointBase<?> that, int targetScale, RoundingMode rounding) {
        int digitsToScale = getDecimals() + that.getDecimals() - targetScale;
        long mantissaA = this.mantissa;
        long mantissaB = that.mantissa;
        if (digitsToScale <= 0) {
            // easy, no rounding
            return mantissaA * mantissaB * powersOfTen[-digitsToScale];
        }
        long sign = 1;
        if (mantissaA < 0) {
            mantissaA = -mantissaA;
            sign = -1;
        }
        if (mantissaB < 0) {
            mantissaB = -mantissaB;
            sign = -sign;
        }
            
        long unroundedProduct;
        // see if we can multiply first, then scale, without loosing precision
        if (Long.numberOfLeadingZeros(mantissaA) + Long.numberOfLeadingZeros(mantissaB) >= 65) {
            // both operands are positive and their product is as well
            unroundedProduct = mantissaA * mantissaB;
        } else {
            // as we do not have a true 128 bit multiplication, we first try to shave off any extra powers of ten
            // in chunks of 3, first A, then B
            while (digitsToScale >= 3) {
                if (mantissaA % 1000 == 0) {
                    mantissaA /= 1000;
                    digitsToScale -= 3;
                } else {
                    break;
                }
            }
            while (digitsToScale >= 3) {
                if (mantissaB % 1000 == 0) {
                    mantissaB /= 1000;
                    digitsToScale -= 3;
                } else {
                    break;
                }
            }
            while (digitsToScale > 0) {
                if (mantissaA % 10 == 0) {
                    mantissaA /= 10;
                    --digitsToScale;
                } else {
                    break;
                }
            }
            while (digitsToScale > 0) {
                if (mantissaB % 10 == 0) {
                    mantissaB /= 10;
                    --digitsToScale;
                } else {
                    break;
                }
            }
            if (digitsToScale == 0) {
                // easy, no rounding
                return sign * mantissaA * mantissaB;
            }
            // repeat the digits test
            if (Long.numberOfLeadingZeros(mantissaA) + Long.numberOfLeadingZeros(mantissaB) >= 65) {
                // both operands are positive and their product is as well
                unroundedProduct = mantissaA * mantissaB;
            } else {
                // FIXME
                return 0;
//                throw new ArithmeticException("internal fixable overflow");
            }
        }
        // the rounding, depending on the mode
        long work;
        switch (rounding) {
        case UNNECESSARY:
            if (unroundedProduct % powersOfTen[digitsToScale] != 0L)
                throw new ArithmeticException("Rounding required but forbidden, scaling " + unroundedProduct + " by " + digitsToScale + " digits");
            return sign * unroundedProduct % powersOfTen[digitsToScale];
        case DOWN:
            return sign * unroundedProduct / powersOfTen[digitsToScale];
        case UP:
            work = unroundedProduct % powersOfTen[digitsToScale];
            return sign * (unroundedProduct / powersOfTen[digitsToScale] + (work != 0 ? 1 : 0));
        case HALF_UP:
            work = unroundedProduct % powersOfTen[digitsToScale];
            return sign * (unroundedProduct / powersOfTen[digitsToScale] + (work >= (powersOfTen[digitsToScale] >> 1) ? 1 : 0));
        case HALF_DOWN:
            work = unroundedProduct % powersOfTen[digitsToScale];
            return sign * (unroundedProduct / powersOfTen[digitsToScale] + (work > (powersOfTen[digitsToScale] >> 1) ? 1 : 0));
        case CEILING:
            work = unroundedProduct % powersOfTen[digitsToScale];
            return sign * unroundedProduct / powersOfTen[digitsToScale] + (work != 0 ? 1 : 0);
        case FLOOR:
            work = unroundedProduct % powersOfTen[digitsToScale];
            return sign * unroundedProduct / powersOfTen[digitsToScale] - (sign < 0 && work != 0 ? 1 : 0);
        case HALF_EVEN:
            work = unroundedProduct % (powersOfTen[digitsToScale] << 1);
            // round as follows: [0, 0.5] down, (0.5, 1) up, [1, 1.5) down, [1.5, 2) up
            return sign * (unroundedProduct / powersOfTen[digitsToScale] + (work >= (powersOfTen[digitsToScale] >> 1) ? 1 : 0));
        default:
            return 0;   // FIXME
        }
    }
    */

    /** Short source, but high GC overhead version, as a testing reference. */
    public long mantissa_of_multiplication_using_BD(FixedPointBase<?> that, int targetScale, RoundingMode rounding) {
        BigDecimal product = BigDecimal.valueOf(this.mantissa, getScale()).multiply(BigDecimal.valueOf(that.mantissa, that.getScale()));
        BigDecimal scaledProduct = product.setScale(targetScale, rounding);
        return scaledProduct.scaleByPowerOfTen(targetScale).unscaledValue().longValue();
    }

    /** Use of native code for scaling and rounding, if required. */
    public long mantissa_of_multiplication(FixedPointBase<?> that, int targetScale, RoundingMode rounding) {
        int digitsToScale = getScale() + that.getScale() - targetScale;
        long mantissaA = this.mantissa;
        long mantissaB = that.mantissa;
        if (digitsToScale <= 0) {
            // easy, no rounding
            return mantissaA * mantissaB * powersOfTen[-digitsToScale];
        }
        return FixedPointNative.multiply_and_scale(mantissaA, mantissaB, digitsToScale, rounding);
    }
    
    /** Divide a / b and round according to specification. Does not need JNI, because we stay in range of a long here. */
    public static long divide_longs(long a, long b, RoundingMode rounding) {
        long tmp = a / b;
        long mod = a % b;
        if (mod == 0)
            return tmp;  // no rounding required: same for all modes...

        switch (rounding) {
        case UP:              // round towards bigger absolute value
            return tmp + (a >= 0 ? 1 : -1);
        case DOWN:            // // round towards smaller absolute value
            return tmp;
        case CEILING:         // round towards bigger numerical value
            return a >= 0 ? tmp + 1 : tmp;
        case FLOOR:           // round towards smaller numerical value
            return a < 0 ? tmp - 1 : tmp;
        case HALF_UP:
            if (a >= 0) {
                return mod >= (b >> 1) ? tmp + 1 : tmp;
            } else {
                return mod <= -(b >> 1) ? tmp - 1 : tmp;
            }
        case HALF_DOWN:
            if (a >= 0) {
                return mod > (b >> 1) ? tmp + 1 : tmp;
            } else {
                return mod < -(b >> 1) ? tmp - 1 : tmp;
            }
        case HALF_EVEN:
            if (a >= 0) {
                if (mod > (b >> 1)) {
                    return tmp + 1;
                } else if (mod < (b >> 1)) {
                    return tmp;
                } else {
                    // in this case, the rounding also depends on the last digit of the result. In case of equidistant numbers, it is rounded towards the nearest even number.
                    return tmp + (tmp & 1);
                }
            } else {
                if (mod < -(b >> 1)) {
                    return tmp - 1;
                } else if (mod > -(b >> 1)) {
                    return tmp;
                } else {
                    // in this case, the rounding also depends on the last digit of the result. In case of equidistant numbers, it is rounded towards the nearest even number.
                    return tmp - (tmp & 1);
                }
            }
        case UNNECESSARY:
            throw new ArithmeticException("Rounding required but forbidden by roundingMode parameter");
        default:
            return tmp;
        }
    }

    /** Multiplies a fixed point number by an another one. The type / scale of the result is undefined. */
    public FixedPointBase<?> gmultiply(FixedPointBase<?> that, RoundingMode rounding) {
        if (mantissa == 0)
            return this;                // 0 * x = 0
        if (that.mantissa == 0)
            return that;                // x * 0 = 0
        if (isOne())
            return that;                // 1 * x = x
        if (isMinusOne())
            return that.negate();       // -1 * x = -x
        if (that.isOne())
            return this;                // x * 1 = x
        if (that.isMinusOne())
            return this.negate();       // x * -1 = -x
        return newInstanceOf(mantissa_of_multiplication(that, this.getScale(), rounding));
    }
    /** Multiplies a fixed point number by an another one. The type / scale of the result is the same than that of the left operand. */
    public CLASS multiply(FixedPointBase<?> that, RoundingMode rounding) {
        if (mantissa == 0 || that.mantissa == 0)
            return getZero();           // x * 0 = 0 * x = 0
        if (that.isOne())
            return getMyself();         // x * 1 = x
        if (that.isMinusOne())
            return this.negate();       // x * -1 = -x
        return newInstanceOf(mantissa_of_multiplication(that, this.getScale(), rounding));
    }
    /** Xtend syntax sugar. multiply maps to the multiply method. */
    public CLASS operator_multiply(FixedPointBase<?> that) {
        return multiply(that, RoundingMode.HALF_EVEN);
    }
    
    /** Divides a fixed point number by an another one. The type / scale of the result is the same than that of the left operand. */
    public CLASS divide(FixedPointBase<?> that, RoundingMode rounding) {
        if (mantissa == 0)
            return getZero();           // x * 0 = 0 * x = 0
        if (that.isOne())
            return getMyself();         // x * 1 = x
        if (that.isMinusOne())
            return this.negate();       // x * -1 = -x
        return newInstanceOf(FixedPointNative.scale_and_divide(mantissa, that.getScale(), that.mantissa, rounding));
    }
    /** Xtend syntax sugar. multiply maps to the multiply method. */
    public CLASS operator_divide(FixedPointBase<?> that) {
        return divide(that, RoundingMode.HALF_EVEN);
    }
    
    
    
    
    /** Adds two fixed point numbers. The scale (and type) of the sum is the bigger of the operand scales. */
    public FixedPointBase<?> gadd(FixedPointBase<?> that) {
        // first checks, if we can void adding the numbers and return either operand.
        if (mantissa == 0)
            return that;
        if (that.mantissa == 0)
            return this;
        int diff = this.getScale() - that.getScale();
        if (diff >= 0)
            return this.newInstanceOf(this.mantissa + powersOfTen[diff] * that.mantissa);
        else
            return that.newInstanceOf(that.mantissa + powersOfTen[-diff] * this.mantissa);
    }
    /** Adds two fixed point numbers of exactly same type. For variable scale subtypes, the scale of the sum is the bigger of the operand scales. */
    public CLASS add(CLASS that) {
        int diff = this.getScale() - that.getScale();
        if (diff >= 0)
            return this.newInstanceOf(this.mantissa + powersOfTen[diff] * that.getMantissa());
        else
            return that.newInstanceOf(that.getMantissa() + powersOfTen[-diff] * this.mantissa);
    }
    /** Xtend syntax sugar. plus maps to the add method. */
    public CLASS operator_plus(CLASS that) {
        return add(that);
    }
    
    /** Subtracts two fixed point numbers. The scale (and type) of the sum is the bigger of the operand scales. */
    public FixedPointBase<?> gsubtract(FixedPointBase<?> that) {
        // first checks, if we can void adding the numbers and return either operand.
        if (that.mantissa == 0)
            return this;
        if (mantissa == 0)
            return that.negate();
        int diff = this.getScale() - that.getScale();
        if (diff >= 0)
            return this.newInstanceOf(this.mantissa - powersOfTen[diff] * that.mantissa);
        else
            return that.newInstanceOf(-that.mantissa + powersOfTen[-diff] * this.mantissa);
    }
    /** Subtracts two fixed point numbers of exactly same type. For variable scale subtypes, the scale of the sum is the bigger of the operand scales. */
    public CLASS subtract(CLASS that) {
        // first checks, if we can void adding the numbers and return either operand.
        int diff = this.getScale() - that.getScale();
        if (diff >= 0)
            return this.newInstanceOf(this.mantissa - powersOfTen[diff] * that.getMantissa());
        else
            return that.newInstanceOf(-that.getMantissa() + powersOfTen[-diff] * this.mantissa);
    }
    /** Xtend syntax sugar. minus maps to the subtract method. */
    public CLASS operator_minus(CLASS that) {
        return subtract(that);
    }

    /** Divides a number by an integer. */
    public CLASS divide(int divisor) {
        if (divisor == 0)
            throw new ArithmeticException("Division by 0");
        if (divisor == 1)
            return getMyself();
        if (divisor == -1)
            return this.negate();
        return newInstanceOf(mantissa / divisor); 
    }
    /** Xtend syntax sugar. divide maps to the divide method. */
    public CLASS operator_divide(int divisor) {
        return divide(divisor);
    }

    /** Computes the remainder of a division by an integer. */
    public CLASS remainder(int divisor) {
        if (divisor == 0)
            throw new ArithmeticException("Division by 0");
        if (divisor == 1 || divisor == -1)
            return this.getZero();
        if (divisor == -1)
            return this.negate();
        long quotient = mantissa / divisor;
        return newInstanceOf(mantissa - quotient * divisor); 
    }
    /** Xtend syntax sugar. modulo maps to the remainder method. */
    public CLASS operator_modulo(int divisor) {
        return remainder(divisor);
    }

}

package de.jpaw.fixedpoint.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.jpaw.fixedpoint.FixedPointBase;

public class Units extends FixedPointBase<Units> {
    private static final long serialVersionUID = -3073254135663195283L;
    public static final int DECIMALS = 0;
    public static final long UNIT_MANTISSA = 1;
    public static final Units ZERO = new Units(0);
    public static final Units ONE = new Units(UNIT_MANTISSA);
    
    public Units(long mantissa) {
        super(mantissa);
    }

    /** Constructs an instance with a specified mantissa. See also valueOf(long value), which constructs an integral instance. */ 
    public static Units of(long mantissa) {
        return ZERO.newInstanceOf(mantissa);
    }
    
    /** Constructs an instance with a specified integral value. See also of(long mantissa), which constructs an instance with a specified mantissa. */ 
    public static Units valueOf(long value) {
        return ZERO.newInstanceOf(value);
    }
    
    /** Returns a re-typed instance of that. Loosing precision is not supported. */
    public static Units of(FixedPointBase<?> that) {
        if (that.getScale() == 0)
            return Units.of(that.getMantissa());
        throw new ArithmeticException("Retyping with reduction of scale requires specfication of a rounding mode");
    }
    
    /** Returns a re-typed instance of that. */
    public static Units of(FixedPointBase<?> that, RoundingMode rounding) {
        if (that.getScale() == 0)
            return Units.of(that.getMantissa());
        // rescale
        return  Units.of(divide_longs(that.getMantissa(), powersOfTen[that.getScale()], rounding));
    }

    // This is certainly not be the most efficient implementation, as it involves the construction of up to 2 new BigDecimals
    // TODO: replace it by a zero GC version
    public static Units of(BigDecimal number) {
        return of(number.setScale(DECIMALS, RoundingMode.UNNECESSARY).longValue());
    }

    @Override
    public Units newInstanceOf(long mantissa) {
        // caching checks...
        if (mantissa == 0)
            return ZERO;
        if (mantissa == UNIT_MANTISSA)
            return ONE;
        if (mantissa == getMantissa())
            return this;
        return new Units(mantissa);
    }

    @Override
    public int getScale() {
        return DECIMALS;
    }

    @Override
    public Units getZero() {
        return ZERO;
    }

    @Override
    public Units getUnit() {
        return ONE;
    }

    @Override
    public long getUnitAsLong() {
        return UNIT_MANTISSA;
    }
    
    @Override
    public Units getMyself() {
        return this;
    }
}

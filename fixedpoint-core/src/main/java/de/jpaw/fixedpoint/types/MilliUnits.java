package de.jpaw.fixedpoint.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.jpaw.fixedpoint.FixedPointBase;

public class MilliUnits extends FixedPointBase<MilliUnits> {
    private static final long serialVersionUID = -466464673376366003L;
    public static final int DECIMALS = 3;
    public static final long UNIT_MANTISSA = 1000L;
    public static final MilliUnits ZERO = new MilliUnits(0);
    public static final MilliUnits ONE = new MilliUnits(UNIT_MANTISSA);
    
    public MilliUnits(long mantissa) {
        super(mantissa);
    }

    /** Constructs an instance with a specified mantissa. See also valueOf(long value), which constructs an integral instance. */ 
    public static MilliUnits of(long mantissa) {
        return ZERO.newInstanceOf(mantissa);
    }
    
    /** Constructs an instance with a specified integral value. See also of(long mantissa), which constructs an instance with a specified mantissa. */ 
    public static MilliUnits valueOf(long value) {
        return ZERO.newInstanceOf(value * UNIT_MANTISSA);
    }
    
    /** Returns a re-typed instance of that. Loosing precision is not supported. */
    public static MilliUnits of(FixedPointBase<?> that) {
        int scaleDiff = DECIMALS - that.getScale();
        if (scaleDiff >= 0)
            return MilliUnits.of(that.getMantissa() * powersOfTen[scaleDiff]);
        throw new ArithmeticException("Retyping with reduction of scale requires specfication of a rounding mode");
    }
    
    /** Returns a re-typed instance of that. */
    public static MilliUnits of(FixedPointBase<?> that, RoundingMode rounding) {
        int scaleDiff = DECIMALS - that.getScale();
        if (scaleDiff >= 0)
            return MilliUnits.of(that.getMantissa() * powersOfTen[scaleDiff]);
        // rescale
        return  MilliUnits.of(divide_longs(that.getMantissa(), powersOfTen[-scaleDiff], rounding));
    }
    
    // This is certainly not be the most efficient implementation, as it involves the construction of up to 2 new BigDecimals
    // TODO: replace it by a zero GC version
    public static MilliUnits of(BigDecimal number) {
        return of(number.setScale(DECIMALS, RoundingMode.UNNECESSARY).scaleByPowerOfTen(DECIMALS).longValue());
    }
    
    @Override
    public MilliUnits newInstanceOf(long mantissa) {
        // caching checks...
        if (mantissa == 0)
            return ZERO;
        if (mantissa == UNIT_MANTISSA)
            return ONE;
        if (mantissa == getMantissa())
            return this;
        return new MilliUnits(mantissa);
    }

    @Override
    public int getScale() {
        return DECIMALS;
    }

    @Override
    public MilliUnits getZero() {
        return ZERO;
    }

    @Override
    public MilliUnits getUnit() {
        return ONE;
    }

    @Override
    public long getUnitAsLong() {
        return UNIT_MANTISSA;
    }
    
    @Override
    public MilliUnits getMyself() {
        return this;
    }
}

package de.jpaw.fixedpoint.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.jpaw.fixedpoint.FixedPointBase;

public class PicoUnits extends FixedPointBase<PicoUnits> {
    private static final long serialVersionUID = -4664646733763660012L;
    public static final int DECIMALS = 12;
    public static final long UNIT_MANTISSA = 1000000000000L;
    public static final PicoUnits ZERO = new PicoUnits(0);
    public static final PicoUnits ONE = new PicoUnits(UNIT_MANTISSA);
    
    public PicoUnits(long mantissa) {
        super(mantissa);
    }

    /** Constructs an instance with a specified mantissa. See also valueOf(long value), which constructs an integral instance. */ 
    public static PicoUnits of(long mantissa) {
        return ZERO.newInstanceOf(mantissa);
    }
    
    /** Constructs an instance with a specified integral value. See also of(long mantissa), which constructs an instance with a specified mantissa. */ 
    public static PicoUnits valueOf(long value) {
        return ZERO.newInstanceOf(value * UNIT_MANTISSA);
    }
    
    /** Returns a re-typed instance of that. Loosing precision is not supported. */
    public static PicoUnits of(FixedPointBase<?> that) {
        int scaleDiff = DECIMALS - that.getScale();
        if (scaleDiff >= 0)
            return PicoUnits.of(that.getMantissa() * powersOfTen[scaleDiff]);
        throw new ArithmeticException("Retyping with reduction of scale requires specfication of a rounding mode");
    }
    
    /** Returns a re-typed instance of that. */
    public static PicoUnits of(FixedPointBase<?> that, RoundingMode rounding) {
        int scaleDiff = DECIMALS - that.getScale();
        if (scaleDiff >= 0)
            return PicoUnits.of(that.getMantissa() * powersOfTen[scaleDiff]);
        // rescale
        return  PicoUnits.of(divide_longs(that.getMantissa(), powersOfTen[-scaleDiff], rounding));
    }
    
    // This is certainly not be the most efficient implementation, as it involves the construction of up to 2 new BigDecimals
    // TODO: replace it by a zero GC version
    public static PicoUnits of(BigDecimal number) {
        return of(number.setScale(DECIMALS, RoundingMode.UNNECESSARY).scaleByPowerOfTen(DECIMALS).longValue());
    }
    
    @Override
    public PicoUnits newInstanceOf(long mantissa) {
        // caching checks...
        if (mantissa == 0)
            return ZERO;
        if (mantissa == UNIT_MANTISSA)
            return ONE;
        if (mantissa == getMantissa())
            return this;
        return new PicoUnits(mantissa);
    }

    @Override
    public int getScale() {
        return DECIMALS;
    }

    @Override
    public PicoUnits getZero() {
        return ZERO;
    }

    @Override
    public PicoUnits getUnit() {
        return ONE;
    }

    @Override
    public long getUnitAsLong() {
        return UNIT_MANTISSA;
    }
    
    @Override
    public PicoUnits getMyself() {
        return this;
    }
}

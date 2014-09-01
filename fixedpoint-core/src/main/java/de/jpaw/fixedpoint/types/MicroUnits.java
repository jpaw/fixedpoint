package de.jpaw.fixedpoint.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.jpaw.fixedpoint.FixedPointBase;

public class MicroUnits extends FixedPointBase<MicroUnits> {
    private static final long serialVersionUID = -466464673376366006L;
    public static final int DECIMALS = 6;
    public static final long UNIT_MANTISSA = 1000000L;
    public static final double UNIT_SCALE = UNIT_MANTISSA;       // casted to double at class initialisation time
    public static final MicroUnits ZERO = new MicroUnits(0);
    public static final MicroUnits ONE = new MicroUnits(UNIT_MANTISSA);
    
    public MicroUnits(long mantissa) {
        super(mantissa);
    }

    public MicroUnits(double value) {
        super(Math.round(value * UNIT_SCALE));
    }

    public MicroUnits(String value) {
        super(parseMantissa(value, DECIMALS));
    }

    /** Constructs an instance with a specified mantissa. See also valueOf(long value), which constructs an integral instance. */ 
    public static MicroUnits of(long mantissa) {
        return ZERO.newInstanceOf(mantissa);
    }
    
    /** Constructs an instance with a specified integral value. See also of(long mantissa), which constructs an instance with a specified mantissa. */ 
    public static MicroUnits valueOf(long value) {
        return ZERO.newInstanceOf(value * UNIT_MANTISSA);
    }
    
    /** Constructs an instance with a specified value specified via floating point. Take care for rounding issues! */ 
    public static MicroUnits valueOf(double value) {
        return ZERO.newInstanceOf(Math.round(value * UNIT_SCALE));
    }
    
    /** Constructs an instance with a specified value specified via string representation. */ 
    public static MicroUnits valueOf(String value) {
        return ZERO.newInstanceOf(parseMantissa(value, DECIMALS));
    }
    
    /** Returns a re-typed instance of that. Loosing precision is not supported. */
    public static MicroUnits of(FixedPointBase<?> that) {
        int scaleDiff = DECIMALS - that.getScale();
        if (scaleDiff >= 0)
            return MicroUnits.of(that.getMantissa() * powersOfTen[scaleDiff]);
        throw new ArithmeticException("Retyping with reduction of scale requires specfication of a rounding mode");
    }
    
    /** Returns a re-typed instance of that. */
    public static MicroUnits of(FixedPointBase<?> that, RoundingMode rounding) {
        int scaleDiff = DECIMALS - that.getScale();
        if (scaleDiff >= 0)
            return MicroUnits.of(that.getMantissa() * powersOfTen[scaleDiff]);
        // rescale
        return  MicroUnits.of(divide_longs(that.getMantissa(), powersOfTen[-scaleDiff], rounding));
    }
    
    // This is certainly not be the most efficient implementation, as it involves the construction of up to 2 new BigDecimals
    // TODO: replace it by a zero GC version
    public static MicroUnits of(BigDecimal number) {
        return of(number.setScale(DECIMALS, RoundingMode.UNNECESSARY).scaleByPowerOfTen(DECIMALS).longValue());
    }
    
    @Override
    public MicroUnits newInstanceOf(long mantissa) {
        // caching checks...
        if (mantissa == 0)
            return ZERO;
        if (mantissa == UNIT_MANTISSA)
            return ONE;
        if (mantissa == getMantissa())
            return this;
        return new MicroUnits(mantissa);
    }

    @Override
    public int getScale() {
        return DECIMALS;
    }

    @Override
    public MicroUnits getZero() {
        return ZERO;
    }

    @Override
    public MicroUnits getUnit() {
        return ONE;
    }

    @Override
    public long getUnitAsLong() {
        return UNIT_MANTISSA;
    }
    
    @Override
    public MicroUnits getMyself() {
        return this;
    }
}

/*
 * Units of Measurement Reference Implementation
 * Copyright (c) 2005-2020, Jean-Marie Dautelle, Werner Keil, Otavio Santana.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *    and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of JSR-385, Indriya nor the names of their contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.raynigon.unit_api.core.function.unitconverter;

import com.raynigon.unit_api.core.function.numbersystem.DefaultNumberSystem;
import com.raynigon.unit_api.core.function.Converter;
import com.raynigon.unit_api.core.function.ValueSupplier;
import com.raynigon.unit_api.core.function.FactorSupplier;
import com.raynigon.unit_api.core.function.RationalNumber;
import com.raynigon.unit_api.core.function.CalculusUtils;
import com.raynigon.unit_api.core.function.NumberSystem;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.measure.Prefix;
import javax.measure.UnitConverter;

/**
 * This class represents a converter multiplying numeric values by a constant scaling factor
 * represented by the {@link Number} type.
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @author <a href="mailto:werner@units.tech">Werner Keil</a>
 * @author Andi Huber
 * @version 2.6, September 10, 2019
 * @since 1.0
 */
public interface MultiplyConverter
        extends UnitConverter,
        Converter<Number, Number>,
        ValueSupplier<Number>,
        FactorSupplier<Number>,
        Comparable<UnitConverter> {

    // -- FACTORIES

    public static MultiplyConverter ofRational(RationalNumber factor) {
        if (factor.equals(RationalNumber.ONE)) {
            return identity();
        }
        return RationalConverter.of(factor);
    }

    /**
     * Creates a MultiplyConverter with the specified rational factor made up of {@code dividend} and
     * {@code divisor}
     *
     * @param dividend
     * @param divisor
     */
    public static MultiplyConverter ofRational(long dividend, long divisor) {
        RationalNumber rational = RationalNumber.of(dividend, divisor);
        return ofRational(rational);
    }

    /**
     * Creates a MultiplyConverter with the specified rational factor made up of {@code dividend} and
     * {@code divisor}
     *
     * @param dividend
     * @param divisor
     */
    public static MultiplyConverter ofRational(BigInteger dividend, BigInteger divisor) {
        RationalNumber rational = RationalNumber.of(dividend, divisor);
        return ofRational(rational);
    }

    /**
     * Creates a MultiplyConverter with the specified constant factor.
     *
     * @param factor
     * @return
     */
    public static MultiplyConverter of(Number factor) {

        NumberSystem ns = CalculusUtils.currentNumberSystem();

        if (ns.isOne(factor)) {
            return identity();
        }

        Number narrowedFactor = ns.narrow(factor);

        if (narrowedFactor instanceof RationalNumber) {
            return ofRational((RationalNumber) narrowedFactor);
        }

        if (ns.isInteger(narrowedFactor)) {
            if (narrowedFactor instanceof BigInteger) {
                return ofRational(RationalNumber.ofInteger((BigInteger) narrowedFactor));
            }

            // TODO[220] yet only implemented for the default number system,
            // any other implementation might behave differently;
            // could fall back to long, but instead fail early
            if (!(ns instanceof DefaultNumberSystem)) {
                throw new UnsupportedOperationException("not yet supported");
            }

            return ofRational(RationalNumber.ofInteger(narrowedFactor.longValue()));
        }

        if (narrowedFactor instanceof Double || narrowedFactor instanceof Float) {
            return of(narrowedFactor.doubleValue());
        }

        if (narrowedFactor instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) narrowedFactor;
            RationalNumber rational = RationalNumber.of(decimal);
            return ofRational(rational);
        }

        // TODO[220] any other case not supported yet, could fall back to double, but
        // instead fail early
        throw new UnsupportedOperationException("not yet supported");
    }

    /**
     * Creates a MultiplyConverter with the specified constant factor.
     *
     * @param factor the double factor.
     * @return a new MultiplyConverter.
     */
    public static MultiplyConverter of(double factor) {
        if (factor == 1.d) {
            return identity();
        }
        RationalNumber rational = RationalNumber.of(factor);
        return ofRational(rational);
    }

    /**
     * Creates a MultiplyConverter with the specified Prefix.
     *
     * @param prefix the prefix for the factor.
     * @return a new MultiplyConverter.
     */
    public static MultiplyConverter ofPrefix(Prefix prefix) {
        if (prefix == null) {
            return identity();
        }

        // this is an optimization for the special case of exponent == 1, where we simply use
        // Prefix.getValue() as the factor
        if (prefix.getExponent() == 1) {
            return of(prefix.getValue());
        }

        // as the spec allows for Prefix.getValue() to also return non integer numbers,
        // we do have to account for these (rare) cases
        NumberSystem ns = CalculusUtils.currentNumberSystem();
        if (!ns.isInteger(prefix.getValue())) {
            Number factor = ns.power(prefix.getValue(), prefix.getExponent());
            return of(factor);
        }

        return PowerOfIntConverter.of(prefix);
    }

    /**
     * Creates a MultiplyConverter with the specified exponent of Pi.
     *
     * @param exponent the exponent for the factor π^exponent.
     * @return a new MultiplyConverter.
     */
    public static MultiplyConverter ofPiExponent(int exponent) {
        if (exponent == 0) {
            return identity();
        }
        return PowerOfPiConverter.of(exponent);
    }

    /**
     * Creates a MultiplyConverter with the specified base and exponent.
     *
     * @param base     the base.
     * @param exponent the exponent.
     * @return a new MultiplyConverter.
     */
    public static MultiplyConverter ofExponent(int base, int exponent) {
        if (exponent == 0) {
            return identity();
        }
        return PowerOfIntConverter.of(base, exponent);
    }

    /**
     * Creates a MultiplyConverter with base 10 and an exponent.
     *
     * @param exponent the exponent for the factor 10^exponent.
     */
    public static MultiplyConverter ofTenExponent(int exponent) {
        if (exponent == 0) {
            return identity();
        }
        return PowerOfIntConverter.of(10, exponent);
    }

    /**
     * Returns a MultiplyConverter that acts as a 'pass-through'.
     */
    public static MultiplyConverter identity() {
        return IdentityMultiplyConverter.INSTANCE;
    }

    // -- DEFAULTS

    @Override
    default boolean isLinear() {
        return true;
    }

    /**
     * Returns the scale factor of this converter.
     *
     * @return the scale factor.
     */
    default Number getFactor() {
        return getValue();
    }
}

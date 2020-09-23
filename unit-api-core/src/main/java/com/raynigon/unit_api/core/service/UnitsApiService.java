package com.raynigon.unit_api.core.service;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.spi.SystemOfUnits;

public interface UnitsApiService {

    static UnitsApiService getInstance() {
        return DefaultUnitsApiService.getInstance();
    }

    static <T extends Quantity<T>> Unit<T> unit(Class<T> quantityType) {
        return getInstance().getUnit(quantityType);
    }

    static Unit<?> unit(String symbol) {
        return getInstance().getUnit(symbol);
    }

    static Quantity<?> quantity(String quantity) {
        return getInstance().parseQuantity(quantity);
    }

    static <Q extends Quantity<Q>> Quantity<Q> quantity(Number number, Unit<Q> unit) {
        return getInstance().createQuantity(number, unit);
    }

    /**
     * Adds an additional {@link SystemOfUnits} to the UnitsApiService.
     * The Name provided by the {@link SystemOfUnits} is used as an id
     * and therefore has to be unique.
     * @param system    the additional {@link SystemOfUnits}
     */
    void addSystemOfUnits(SystemOfUnits system);

    <T extends Quantity<T>> Unit<T> getUnit(Class<T> quantityType);

    Unit<?> getUnit(String symbol);

    String format(Quantity<?> quantity);

    Quantity<?> parseQuantity(String quantity);

    <Q extends Quantity<Q>> Quantity<Q> createQuantity(Number number, Unit<Q> unit);
}

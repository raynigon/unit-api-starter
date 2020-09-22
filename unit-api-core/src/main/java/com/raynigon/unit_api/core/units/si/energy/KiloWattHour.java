package com.raynigon.unit_api.core.units.si.energy;

import com.raynigon.unit_api.core.units.general.ScaledUnit;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Energy;

public class KiloWattHour extends ScaledUnit<Energy> {

    public KiloWattHour() {
        super(MetricPrefix.KILO, new WattHour(), "KiloWattHour");
    }
}

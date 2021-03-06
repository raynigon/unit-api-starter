package com.raynigon.unit_api.core.units.si.electrical.charge;

import com.raynigon.unit_api.core.function.unitconverter.MultiplyConverter;
import com.raynigon.unit_api.core.units.general.IUnit;
import com.raynigon.unit_api.core.units.general.TransformedUnit;
import com.raynigon.unit_api.core.units.si.SISystem;
import com.raynigon.unit_api.core.units.si.electrical.current.Ampere;
import com.raynigon.unit_api.core.units.si.time.Hour;

import javax.measure.Unit;
import javax.measure.quantity.ElectricCharge;

public class AmpereHour extends TransformedUnit<ElectricCharge> implements IUnit<ElectricCharge> {

    @SuppressWarnings("unchecked")
    public AmpereHour() {
        super(
                "Ah",
                "Ampere hour",
                (Unit<ElectricCharge>) new Hour().multiply(new Ampere()),
                new Coulomb(),
                MultiplyConverter.of(1)
        );
    }

    @Override
    public String getSystemId() {
        return SISystem.ID;
    }

    @Override
    public Class<ElectricCharge> getQuantityType() {
        return ElectricCharge.class;
    }
}

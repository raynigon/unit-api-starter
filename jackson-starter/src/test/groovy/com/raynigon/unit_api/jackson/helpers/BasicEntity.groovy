package com.raynigon.unit_api.jackson.helpers

import com.raynigon.unit_api.core.units.si.length.Kilometre
import com.raynigon.unit_api.core.units.si.speed.KilometrePerHour
import com.raynigon.unit_api.core.units.si.temperature.Celsius
import com.raynigon.unit_api.jackson.annotation.JsonUnit
import com.raynigon.unit_api.core.annotation.QuantityShape

import javax.measure.Quantity
import javax.measure.quantity.Length
import javax.measure.quantity.Speed
import javax.measure.quantity.Temperature

class BasicEntity {

    public String id

    public Quantity<Speed> speed

    @JsonUnit(unit = Celsius, shape = QuantityShape.STRING)
    public Quantity<Temperature> temperature

    @JsonUnit(unit = Kilometre)
    public Quantity<Length> distance

    /*@JsonUnit(unit="mWh")
    public Quantity<Energy> energy*/
}

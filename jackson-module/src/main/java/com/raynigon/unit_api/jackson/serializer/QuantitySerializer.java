package com.raynigon.unit_api.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.raynigon.unit_api.core.annotation.QuantityShape;
import com.raynigon.unit_api.core.io.QuantityWriter;
import com.raynigon.unit_api.core.service.UnitsApiService;
import com.raynigon.unit_api.core.units.general.IUnit;
import com.raynigon.unit_api.jackson.annotation.JsonQuantityHelper;
import com.raynigon.unit_api.jackson.annotation.JsonQuantityWriter;
import com.raynigon.unit_api.jackson.config.UnitApiConfig;
import com.raynigon.unit_api.jackson.annotation.JsonUnit;
import com.raynigon.unit_api.jackson.annotation.JsonUnitHelper;
import com.raynigon.unit_api.jackson.exception.UnknownUnitException;

import java.io.IOException;
import java.util.Objects;
import javax.measure.Quantity;
import javax.measure.Unit;

@SuppressWarnings("rawtypes")
public class QuantitySerializer extends JsonSerializer<Quantity> implements ContextualSerializer {

    private final UnitApiConfig config;
    private Unit<?> unit;
    private QuantityShape shape;
    private QuantityWriter writer;

    public QuantitySerializer(UnitApiConfig config) {
        this(config, null, QuantityShape.NUMBER, UnitsApiService.writer());
    }

    public QuantitySerializer(UnitApiConfig config, Unit<?> unit, QuantityShape shape, QuantityWriter writer) {
        Objects.requireNonNull(config);
        this.config = config;
        this.unit = unit;
        this.shape = shape;
        this.writer = writer;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        Class<Quantity> quantityType =
                (Class<Quantity>) property.getType().getBindings().getBoundType(0).getRawClass();
        unit = UnitsApiService.getInstance().getUnit(quantityType);

        JsonQuantityWriter writerWrapper = property.getAnnotation(JsonQuantityWriter.class);
        if (writerWrapper != null) {
            writer = JsonQuantityHelper.getWriterInstance(writerWrapper);
        }

        JsonUnit unitWrapper = property.getAnnotation(JsonUnit.class);
        if (unitWrapper == null) return new QuantitySerializer(config, unit, shape, writer);
        shape = JsonUnitHelper.getShape(unitWrapper);
        IUnit<?> unitInstance = JsonUnitHelper.getUnitInstance(unitWrapper);

        if (unitInstance != null) {
            unit = unitInstance;
        }
        if (unit == null) {
            throw new UnknownUnitException(prov.getGenerator(), quantityType);
        }

        return new QuantitySerializer(config, unit, shape, writer);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void serialize(Quantity quantity, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        Quantity convertedQuantity = quantity;
        if (this.unit != null) {
            convertedQuantity = quantity.to(unit);
        }
        switch (shape) {
            case NUMBER:
                gen.writeNumber(convertedQuantity.getValue().doubleValue());
                break;
            case NUMERIC_STRING:
                gen.writeString("" + convertedQuantity.getValue().doubleValue());
                break;
            case STRING:
                gen.writeString(writer.write(convertedQuantity));
                break;
            case OBJECT:
                gen.writeStartObject();
                gen.writeFieldName("value");
                gen.writeNumber(convertedQuantity.getValue().doubleValue());
                gen.writeFieldName("unit");
                gen.writeString(convertedQuantity.getUnit().getSymbol());
                gen.writeEndObject();
                break;
            default:
                throw new IllegalArgumentException("Unknown Shape: " + shape);
        }
    }
}

package com.skkuse.team1.socialhub.custom_serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    public LocalDateTimeSerializer() {
        this(null);
    }
    public LocalDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("day", value.getDayOfMonth());
        gen.writeNumberField("month", value.getMonthValue());
        gen.writeNumberField("year", value.getYear());
        gen.writeNumberField("hour", value.getHour());
        gen.writeNumberField("minute", value.getMinute());
        gen.writeEndObject();
    }
}

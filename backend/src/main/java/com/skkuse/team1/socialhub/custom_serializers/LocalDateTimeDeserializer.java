package com.skkuse.team1.socialhub.custom_serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    public LocalDateTimeDeserializer() {
        this(null);
    }
    protected LocalDateTimeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        return LocalDateTime.of(node.get("year").asInt(), node.get("month").asInt(), node.get("day").asInt(), node.get("hour").asInt(), node.get("minute").asInt());
    }
}

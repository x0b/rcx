package ca.pkay.rcloneexplorer.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.ParseException;

import ca.pkay.rcloneexplorer.RcloneRcd;
import io.github.x0b.rfc3339parser.Rfc3339Parser;
import io.github.x0b.rfc3339parser.Rfc3339Strict;

public class Rfc3339Deserializer extends StdDeserializer<Long> {

    private Rfc3339Parser rfc3339Parser;

    protected Rfc3339Deserializer() {
        super(Rfc3339Deserializer.class);
        rfc3339Parser = new Rfc3339Strict();
    }

    @Override
    public Long deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode timeNode = parser.getCodec().readTree(parser);
        try {
            return rfc3339Parser.parseCalendar(timeNode.asText()).getTimeInMillis();
        } catch (ParseException e) {
            return 0L;
        }
    }
}

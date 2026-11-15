package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class ConversionRuleTest {
    private static com.mojang.serialization.DataResult<ConversionRule> parse(String json) {
        return ConversionRule.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    @Test
    void readsPriority() {
        assertEquals(100, parse("{\"priority\": 100}").getOrThrow().priority());
    }

    @Test
    void rejectsMissingPriority() {
        assertTrue(parse("{}").isError());
    }

    @Test
    void rejectsNonIntegerPriority() {
        assertTrue(parse("{\"priority\": \"high\"}").isError());
    }
}

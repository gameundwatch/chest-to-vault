package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.Test;

/** Mod が上書きする Vault の見た目の定義（plan §4.8）。どの状態にも、ちょうど1つの見た目が当てはまること。 */
class VaultBlockStatesTest {
    private static JsonObject variants() throws Exception {
        try (Reader reader = new InputStreamReader(
                VaultBlockStatesTest.class.getResourceAsStream("/assets/minecraft/blockstates/vault.json"),
                StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("variants");
        }
    }

    /** "a=b,c=d" の形の条件に、状態が当てはまるか。書いていない項目は何でもよい。 */
    private static boolean matches(String key, BlockState state) {
        Map<String, String> values = state.getValues().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getName(), e -> name(e.getKey(), e.getValue())));
        return Arrays.stream(key.split(",")).map(c -> c.split("=")).allMatch(c -> c[1].equals(values.get(c[0])));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String name(Property property, Comparable value) {
        return property.getName(value);
    }

    @Test
    void everyVaultStateHasExactlyOneVariant() throws Exception {
        JsonObject variants = variants();
        for (BlockState state : Blocks.VAULT.getStateDefinition().getPossibleStates()) {
            long count = variants.keySet().stream()
                    .filter(key -> matches(key, state))
                    .count();
            assertEquals(1, count, "variants matching " + state);
        }
    }

    @Test
    void barrelVaultOpensWhileUnlockingOrEjecting() throws Exception {
        JsonObject variants = variants();
        assertEquals(
                "minecraft:block/barrel_open",
                variants.getAsJsonObject("appearance=barrel,vault_state=unlocking")
                        .get("model")
                        .getAsString());
        assertEquals(
                "minecraft:block/barrel_open",
                variants.getAsJsonObject("appearance=barrel,vault_state=ejecting")
                        .get("model")
                        .getAsString());
        assertEquals(
                "minecraft:block/barrel",
                variants.getAsJsonObject("appearance=barrel,vault_state=active")
                        .get("model")
                        .getAsString());
    }
}

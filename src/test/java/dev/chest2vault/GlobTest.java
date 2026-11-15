package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GlobTest {
    @Test
    void matchesExactIdWithoutWildcard() {
        assertTrue(Glob.matches("minecraft:chests/igloo_chest", "minecraft:chests/igloo_chest"));
    }

    @Test
    void matchesWholeIdNotPart() {
        assertFalse(Glob.matches("minecraft:chests/igloo", "minecraft:chests/igloo_chest"));
        assertFalse(Glob.matches("chests/igloo_chest", "minecraft:chests/igloo_chest"));
    }

    @Test
    void starMatchesWithinPath() {
        assertTrue(Glob.matches("minecraft:chests/village/*", "minecraft:chests/village/village_armorer"));
        assertFalse(Glob.matches("minecraft:chests/village/*", "minecraft:chests/simple_dungeon"));
    }

    @Test
    void starMatchesAcrossSlashes() {
        assertTrue(Glob.matches("othermod:*", "othermod:chests/deep/nested"));
        assertFalse(Glob.matches("othermod:*", "minecraft:chests/simple_dungeon"));
    }

    @Test
    void starMatchesEmpty() {
        assertTrue(Glob.matches("minecraft:chests/*_chest", "minecraft:chests/_chest"));
    }

    @Test
    void questionMarkMatchesOneCharacter() {
        assertTrue(Glob.matches("minecraft:chests/ancient_city?ice_box", "minecraft:chests/ancient_city_ice_box"));
        assertFalse(Glob.matches("minecraft:chests/ancient_city?", "minecraft:chests/ancient_city"));
    }

    @Test
    void isCaseSensitive() {
        assertFalse(Glob.matches("Minecraft:*", "minecraft:chests/simple_dungeon"));
    }

    @Test
    void treatsRegexCharactersLiterally() {
        assertTrue(Glob.matches("my.mod:chests/a+b", "my.mod:chests/a+b"));
        assertFalse(Glob.matches("my.mod:chests/a+b", "myxmod:chests/aab"));
    }
}

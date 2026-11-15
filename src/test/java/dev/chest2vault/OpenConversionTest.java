package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class OpenConversionTest {
    @Test
    void messageNamesTheKeyWithEnglishFallback() {
        ItemStack key = new ItemStack(Items.OMINOUS_TRIAL_KEY);
        Component message = OpenConversion.message(key);

        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, message.getContents());
        assertEquals("chest2vault.message.converted", contents.getKey());
        assertEquals("This chest turned into a Vault. Key: %s", contents.getFallback());
        assertArrayEquals(new Object[] {key.getHoverName()}, contents.getArgs());
    }
}

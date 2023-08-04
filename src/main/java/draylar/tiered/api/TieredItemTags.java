package draylar.tiered.api;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

//Since v1.0.3 unused cause of AutoTag usage
public class TieredItemTags {

    public static final TagKey<Item> REFORGE_ADDITION = register("reforge_addition");
    public static final TagKey<Item> REFORGE_BASE_ITEM = register("reforge_base_item");

    private TieredItemTags() {
    }

    public static void init() {
    }

    private static TagKey<Item> register(String id) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier("tiered", id));
    }
}

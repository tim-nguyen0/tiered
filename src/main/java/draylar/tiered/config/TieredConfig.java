package draylar.tiered.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "tiered")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class TieredConfig implements ConfigData {

    @Comment("Items in for example mineshaft chests get modifiers")
    public boolean lootContainerModifier = true;
    @Comment("Decreases the biggest weights")
    public float reforge_modifier = 0.9F;

    public int xIconPosition = 0;
    public int yIconPosition = 0;

}

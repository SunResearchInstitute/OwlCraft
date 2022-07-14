package net.owlcraft.core;

import lombok.Getter;
import net.owlcraft.core.commands.glyphs.GlyphCommand;
import net.owlcraft.core.spells.SpellManager;
import net.owlcraft.core.spells.implementation.fire.Fireball;
import net.owlcraft.core.spells.implementation.ice.Ice;
import net.owlcraft.core.spells.implementation.space.TeleportAnchor;
import org.bukkit.plugin.java.JavaPlugin;

public final class OwlCraft extends JavaPlugin {
    @Getter
    private static OwlCraft instance;
    @Getter
    private SpellManager spellManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        spellManager = new SpellManager();
        loadSpells(spellManager);
        loadCommands();
    }

    private void loadCommands() {
        new GlyphCommand().registerCommand(getCommand("glyph"));
    }

    private static void loadSpells(SpellManager spellManager) {
        spellManager.add(new Fireball());
        spellManager.add(new Ice());
        spellManager.add(new TeleportAnchor());
    }
}

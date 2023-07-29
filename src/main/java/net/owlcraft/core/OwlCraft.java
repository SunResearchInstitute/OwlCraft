package net.owlcraft.core;

import dev.sunresearch.spigotextensions.utils.ChatUtils;
import lombok.Getter;
import net.owlcraft.core.commands.glyphs.GlyphCommand;
import net.owlcraft.core.spells.SpellManager;
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
        loadCommands();
    }

    private void loadCommands() {
        new GlyphCommand().registerCommand(getCommand("glyph"));
    }
}

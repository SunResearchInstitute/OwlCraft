package net.owlcraft;

import lombok.Getter;
import net.owlcraft.commands.glyphs.GiveGlyphCommand;
import net.owlcraft.spells.SpellManager;
import net.owlcraft.spells.implementation.fire.Fireball;
import net.owlcraft.spells.implementation.ice.Ice;
import net.owlcraft.spells.implementation.space.TeleportAnchor;
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
        new GiveGlyphCommand().registerCommand(getCommand("giveglyph"));
    }

    private static void loadSpells(SpellManager spellManager) {
        spellManager.add(new Fireball());
        spellManager.add(new Ice());
        spellManager.add(new TeleportAnchor());
    }
}

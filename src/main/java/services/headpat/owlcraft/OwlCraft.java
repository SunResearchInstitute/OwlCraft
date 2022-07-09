package services.headpat.owlcraft;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import services.headpat.owlcraft.commands.glyphs.GiveGlyphCommand;
import services.headpat.owlcraft.spells.SpellManager;
import services.headpat.owlcraft.spells.implementation.fire.Fireball;
import services.headpat.owlcraft.spells.implementation.ice.Ice;
import services.headpat.owlcraft.spells.implementation.space.TeleportAnchor;

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

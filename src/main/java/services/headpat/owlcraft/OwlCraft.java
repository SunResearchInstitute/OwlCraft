package services.headpat.owlcraft;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import services.headpat.owlcraft.spells.SpellManager;
import services.headpat.owlcraft.spells.implementation.fire.FireballSpell;
import services.headpat.owlcraft.spells.implementation.ice.IceSpell;
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

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        spellManager = new SpellManager();
        loadSpells(spellManager);
    }

    private static void loadSpells(SpellManager spellManager) {
        spellManager.add(new FireballSpell());
        spellManager.add(new IceSpell());
        spellManager.add(new TeleportAnchor());
    }
}

package services.headpat.owlcraft;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import services.headpat.owlcraft.external.GlowAPIController;
import services.headpat.owlcraft.external.MetricsListener;
import services.headpat.owlcraft.spells.SpellManager;
import services.headpat.owlcraft.spells.implementation.fire.FireballSpell;
import services.headpat.owlcraft.spells.implementation.ice.IceSpell;

public final class OwlCraft extends JavaPlugin {
	@Getter
	private static OwlCraft instance;
	@Getter
	private SpellManager spellManager;
	@Getter
	private GlowAPIController glowAPIController;
	@Getter
	private Metrics metrics;
	@Getter
	private MetricsListener metricsListener;

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		metrics = new Metrics(this, 9241);
		metricsListener = new MetricsListener();
		metrics.addCustomChart(new Metrics.AdvancedPie("spells_crafted", () -> metricsListener.getSpellsCrafted()));
		metrics.addCustomChart(new Metrics.AdvancedPie("spells_casted", () -> metricsListener.getSpellsCasted()));
		spellManager = new SpellManager();
		glowAPIController = new GlowAPIController();
		loadSpells(spellManager);
	}

	private static void loadSpells(@NotNull SpellManager spellManager) {
		spellManager.add(new FireballSpell());
		spellManager.add(new IceSpell());
	}
}

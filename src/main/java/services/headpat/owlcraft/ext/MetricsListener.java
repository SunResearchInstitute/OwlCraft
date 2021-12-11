package services.headpat.owlcraft.ext;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.Recipe;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.Spell;
import services.headpat.owlcraft.spells.events.SpellCastEvent;

import java.util.HashMap;
import java.util.List;

public class MetricsListener implements Listener {
	@Getter
	private final HashMap<String, Integer> spellsCrafted;
	@Getter
	private final HashMap<String, Integer> spellsCasted;

	public MetricsListener() {
		spellsCrafted = new HashMap<>();
		spellsCasted = new HashMap<>();
		Bukkit.getPluginManager().registerEvents(this, OwlCraft.getInstance());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onCraftItemEvent(CraftItemEvent event) {
		for (Spell spell : OwlCraft.getInstance().getSpellManager().getSpells()) {
			List<Recipe> recipes = spell.getRecipes();
			if (recipes != null) {
				for (Recipe recipe : recipes) {
					if (recipe.equals(event.getRecipe())) {
						this.incrementSpellOnMap(spell.getName(), spellsCrafted);
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onSpellCastEvent(SpellCastEvent event) {
		this.incrementSpellOnMap(event.getSpell().getName(), spellsCasted);
	}

	private void incrementSpellOnMap(String spellName, HashMap<String, Integer> map) {
		if (map.containsKey(spellName))
			map.put(spellName, map.get(spellName) + 1);
		else
			map.put(spellName, 1);
	}
}

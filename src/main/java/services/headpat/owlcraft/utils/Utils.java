package services.headpat.owlcraft.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import services.headpat.spigotextensions.utils.ChatUtils;

public class Utils {
	public static final PotionEffectType[] POSITIVE_EFFECTS = {
			PotionEffectType.ABSORPTION,
			PotionEffectType.DAMAGE_RESISTANCE,
			PotionEffectType.DOLPHINS_GRACE,
			PotionEffectType.FAST_DIGGING,
			PotionEffectType.FIRE_RESISTANCE,
			PotionEffectType.HEAL,
			PotionEffectType.HEALTH_BOOST,
			PotionEffectType.WATER_BREATHING,
			PotionEffectType.REGENERATION,
			PotionEffectType.INVISIBILITY,
			PotionEffectType.JUMP,
			PotionEffectType.INCREASE_DAMAGE,
			PotionEffectType.LUCK,
			PotionEffectType.NIGHT_VISION,
			PotionEffectType.SLOW_FALLING,
			PotionEffectType.SPEED,
	};

	public static void addBaseFlags(@NotNull ItemMeta meta) {
		meta.addEnchant(Enchantment.DURABILITY, 5, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
	}

	@Contract("_, _, _ -> param1")
	public static @NotNull ItemStack appendNameAndLore(@NotNull ItemStack stack, String name, String lore) {
		if (stack.hasItemMeta()) {
			ItemMeta meta = stack.getItemMeta();
			if (!name.equals(""))
				meta.setDisplayName(name);
			if (!lore.equals(""))
				meta.setLore(ChatUtils.wrapLore(lore));
			stack.setItemMeta(meta);
		}
		return stack;
	}
}

package services.headpat.owlcraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import services.headpat.owlcraft.OwlCraft;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static @NotNull File getPluginFolder() {
		return OwlCraft.getInstance().getDataFolder();
	}

	public static boolean createFolder(@NotNull File folder) {
		if (!folder.exists())
			return folder.mkdir();
		else
			return true;
	}

	public static int timeToTicks(int minutes, int seconds) {
		return ((minutes * 60) + seconds) * 20;
	}

	/**
	 * Wraps text that will displayed in a lore to the best of its ability.
	 *
	 * @param lore       The full length lore with no breaks.
	 * @param lineLength The maximum length for a single line.
	 * @return The text-wrapped lore.
	 */
	public static @NotNull List<String> wrapLore(@NotNull String lore, int lineLength, ChatColor loreChatColor) {
		String[] words = lore.split(" ");

		List<String> result = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (String word : words) {
			if (builder.length() == 0 || ((builder.length() + 1 + word.length()) <= lineLength)) {
				if (builder.length() > 0) {
					builder.append(' ');
				}
			} else {
				result.add(loreChatColor + "" + ChatColor.ITALIC + builder.toString());
				builder.setLength(0);
			}
			builder.append(word);
		}
		if (builder.length() != 0) {
			result.add(loreChatColor + "" + ChatColor.ITALIC + builder.toString());
		}

		return (result);
	}

	/**
	 * Wraps text that will displayed in a lore to the best of its ability.
	 *
	 * @param lore The full length lore with no breaks.
	 * @return The text-wrapped lore.
	 */
	public static @NotNull List<String> wrapLore(String lore) {
		return (Utils.wrapLore(lore, 25, ChatColor.DARK_PURPLE));
	}

	public static @NotNull List<String> wrapLore(String lore, ChatColor loreChatColor) {
		return (Utils.wrapLore(lore, 25, loreChatColor));
	}

	public static void addBaseFlags(@NotNull ItemMeta meta) {
		meta.addEnchant(Enchantment.DURABILITY, 5, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
	}

	public static Set<Player> getNearbyPlayers(Location location, double radius, boolean sortByClosest) {
		Stream<? extends Player> stream = Bukkit.getOnlinePlayers().stream().filter((player) -> {
			if (location.getWorld() != player.getWorld()) {
				return (false);
			}
			return (location.distanceSquared(player.getLocation()) <= (radius * radius));
		});
		if (sortByClosest) {
			stream = stream.sorted(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(location)));
		}
		return (stream.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	@Contract("_, _, _ -> param1")
	public static @NotNull ItemStack appendNameAndLore(@NotNull ItemStack stack, String name, String lore) {
		if (stack.hasItemMeta()) {
			ItemMeta meta = stack.getItemMeta();
			if (!name.equals(""))
				meta.setDisplayName(name);
			if (!lore.equals(""))
				meta.setLore(Utils.wrapLore(lore));
			stack.setItemMeta(meta);
		}
		return stack;
	}

	@Contract(pure = true)
	public static String @NotNull [] addBeginningString(String str, String @NotNull ... strings) {
		String[] newStrings = new String[strings.length + 1];
		newStrings[0] = str;
		if (strings.length - 1 >= 0)
			System.arraycopy(strings, 0, newStrings, 1, strings.length - 1);
		return newStrings;
	}
}

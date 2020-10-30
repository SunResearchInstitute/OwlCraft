package services.headpat.owlcraft.external;

import net.ess3.api.events.NickChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.inventivetalent.nicknamer.api.NickNamerAPI;
import org.jetbrains.annotations.NotNull;

public class NickNameListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoinEvent(@NotNull NickChangeEvent event) {
		try {
			NickNamerAPI.getNickManager().setNick(event.getController().getBase().getUniqueId(), event.getValue());
		} catch (Exception e) {
			event.getController().getBase().sendMessage(ChatColor.RED + "Unable to set name tag, your nickname may be too long! You can also manually set your name tag with /nickname <name>.");
		}
	}
}

package fr.tynitv.actionboard.display;

import fr.tynitv.actionboard.ActionBoard;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DisplayTask extends BukkitRunnable {

    private final ActionBoard plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public DisplayTask(ActionBoard plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("actionbar.enabled", true)) return;

        String template = plugin.getConfig().getString("actionbar.text", "");
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();

        for (Player p : Bukkit.getOnlinePlayers()) {
            String text = template
                    .replace("{online}", String.valueOf(online))
                    .replace("{max}", String.valueOf(max))
                    .replace("{ping}", String.valueOf(p.getPing()));
            p.sendActionBar(mm.deserialize(text));
        }
    }
}

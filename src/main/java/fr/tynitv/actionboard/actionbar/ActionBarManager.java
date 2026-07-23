package fr.tynitv.actionboard.actionbar;

import fr.tynitv.actionboard.ActionBoard;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarManager {

    private final ActionBoard plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, TimedActionBar> tempActionBars = new ConcurrentHashMap<>();

    public ActionBarManager(ActionBoard plugin) {
        this.plugin = plugin;
        startGlobalTask();
    }

    public static class TimedActionBar {
        private final String message;
        private int remainingSeconds;

        public TimedActionBar(String message, int seconds) {
            this.message = message;
            this.remainingSeconds = seconds;
        }

        public String getMessage() { return message; }
        public int getRemainingSeconds() { return remainingSeconds; }
        public void decrement() { this.remainingSeconds--; }
    }

    private void startGlobalTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int online = Bukkit.getOnlinePlayers().size();
                int max = Bukkit.getMaxPlayers();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (disabledPlayers.contains(p.getUniqueId())) continue;

                    TimedActionBar temp = tempActionBars.get(p.getUniqueId());
                    String template;

                    if (temp != null) {
                        template = temp.getMessage().replace("{time}", String.valueOf(temp.getRemainingSeconds()));
                        temp.decrement();
                        if (temp.getRemainingSeconds() <= 0) {
                            tempActionBars.remove(p.getUniqueId());
                        }
                    } else if (plugin.getConfig().getBoolean("actionbar.enabled", true)) {
                        template = plugin.getConfig().getString("actionbar.text", "");
                    } else {
                        continue;
                    }

                    if (template.isEmpty()) continue;

                    String formatted = template
                            .replace("{online}", String.valueOf(online))
                            .replace("{max}", String.valueOf(max))
                            .replace("{ping}", String.valueOf(p.getPing()))
                            .replace("{player}", p.getName())
                            .replace("{health}", String.valueOf((int) p.getHealth()))
                            .replace("{world}", p.getWorld().getName());

                    p.sendActionBar(mm.deserialize(formatted));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void sendTemporaryActionBar(Player player, int durationSeconds, String rawMessage) {
        if (player == null || !player.isOnline()) return;
        tempActionBars.put(player.getUniqueId(), new TimedActionBar(rawMessage, durationSeconds));
    }

    public void broadcastTemporaryActionBar(int durationSeconds, String rawMessage) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendTemporaryActionBar(p, durationSeconds, rawMessage);
        }
    }

    public boolean toggleActionBar(Player player) {
        UUID uid = player.getUniqueId();
        if (disabledPlayers.contains(uid)) {
            disabledPlayers.remove(uid);
            return true; // Enabled
        } else {
            disabledPlayers.add(uid);
            return false; // Disabled
        }
    }

    public boolean isEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId());
    }
}

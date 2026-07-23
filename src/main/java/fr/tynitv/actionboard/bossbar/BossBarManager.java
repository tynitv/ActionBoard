package fr.tynitv.actionboard.bossbar;

import fr.tynitv.actionboard.ActionBoard;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final ActionBoard plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, TimedBossBar> activeBars = new ConcurrentHashMap<>();

    public BossBarManager(ActionBoard plugin) {
        this.plugin = plugin;
    }

    public static class TimedBossBar {
        private final String id;
        private final BossBar bossBar;
        private final int totalSeconds;
        private int remainingSeconds;
        private final BukkitTask task;

        public TimedBossBar(String id, BossBar bossBar, int totalSeconds, BukkitTask task) {
            this.id = id;
            this.bossBar = bossBar;
            this.totalSeconds = totalSeconds;
            this.remainingSeconds = totalSeconds;
            this.task = task;
        }

        public String getId() { return id; }
        public BossBar getBossBar() { return bossBar; }
        public int getTotalSeconds() { return totalSeconds; }
        public int getRemainingSeconds() { return remainingSeconds; }
        public void decrement() { this.remainingSeconds--; }
        public void cancelTask() { if (task != null) task.cancel(); }
    }

    public boolean createBossBar(String id, int durationSeconds, BossBar.Color color, BossBar.Overlay overlay, String rawTitle) {
        if (activeBars.containsKey(id.toLowerCase())) {
            removeBossBar(id);
        }

        Component title = mm.deserialize(rawTitle.replace("{time}", String.valueOf(durationSeconds)));
        BossBar bossBar = BossBar.bossBar(title, 1.0f, color, overlay);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(bossBar);
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                TimedBossBar bar = activeBars.get(id.toLowerCase());
                if (bar == null) {
                    cancel();
                    return;
                }

                bar.decrement();
                int remaining = bar.getRemainingSeconds();

                if (remaining <= 0) {
                    removeBossBar(id);
                    cancel();
                    return;
                }

                float progress = Math.max(0.0f, Math.min(1.0f, (float) remaining / bar.getTotalSeconds()));
                bossBar.progress(progress);
                bossBar.name(mm.deserialize(rawTitle.replace("{time}", String.valueOf(remaining))));
            }
        }.runTaskTimer(plugin, 20L, 20L);

        activeBars.put(id.toLowerCase(), new TimedBossBar(id, bossBar, durationSeconds, task));
        return true;
    }

    public boolean removeBossBar(String id) {
        TimedBossBar bar = activeBars.remove(id.toLowerCase());
        if (bar != null) {
            bar.cancelTask();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(bar.getBossBar());
            }
            return true;
        }
        return false;
    }

    public void clearAll() {
        for (String id : activeBars.keySet()) {
            removeBossBar(id);
        }
    }

    public Collection<TimedBossBar> getActiveBars() {
        return activeBars.values();
    }

    public void showAllToPlayer(Player player) {
        for (TimedBossBar bar : activeBars.values()) {
            player.showBossBar(bar.getBossBar());
        }
    }
}

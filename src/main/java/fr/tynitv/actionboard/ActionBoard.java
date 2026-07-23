package fr.tynitv.actionboard;

import fr.tynitv.actionboard.actionbar.ActionBarManager;
import fr.tynitv.actionboard.bossbar.BossBarManager;
import fr.tynitv.actionboard.commands.ActionBoardCommand;
import fr.tynitv.actionboard.commands.ActionBoardTabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionBoard extends JavaPlugin implements Listener {

    private static ActionBoard instance;
    private BossBarManager bossBarManager;
    private ActionBarManager actionBarManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.bossBarManager = new BossBarManager(this);
        this.actionBarManager = new ActionBarManager(this);

        if (getCommand("actionboard") != null) {
            getCommand("actionboard").setExecutor(new ActionBoardCommand(this));
            getCommand("actionboard").setTabCompleter(new ActionBoardTabCompleter(this));
        }

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("ActionBoard v1.1.0 (ActionBar & BossBar Engine) enabled!");
    }

    @Override
    public void onDisable() {
        if (bossBarManager != null) {
            bossBarManager.clearAll();
        }
        getLogger().info("ActionBoard disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (bossBarManager != null) {
            bossBarManager.showAllToPlayer(event.getPlayer());
        }
    }

    public static ActionBoard getInstance() {
        return instance;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }
}

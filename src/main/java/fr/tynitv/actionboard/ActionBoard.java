package fr.tynitv.actionboard;

import fr.tynitv.actionboard.display.DisplayTask;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionBoard extends JavaPlugin {

    private static ActionBoard instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        new DisplayTask(this).runTaskTimer(this, 0L, 20L);

        getLogger().info("ActionBoard v1.0.0 enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ActionBoard disabled!");
    }

    public static ActionBoard getInstance() {
        return instance;
    }
}

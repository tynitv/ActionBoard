package fr.tynitv.actionboard.commands;

import fr.tynitv.actionboard.ActionBoard;
import fr.tynitv.actionboard.bossbar.BossBarManager;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionBoardTabCompleter implements TabCompleter {

    private final ActionBoard plugin;

    public ActionBoardTabCompleter(ActionBoard plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("toggle", "help"));
            if (sender.hasPermission("actionboard.admin")) {
                subs.addAll(Arrays.asList("bossbar", "actionbar", "reload"));
            }
            return filter(subs, args[0]);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("bossbar")) {
            if (!sender.hasPermission("actionboard.admin")) return completions;

            if (args.length == 2) {
                return filter(Arrays.asList("create", "remove", "list", "clear"), args[1]);
            }

            String action = args[1].toLowerCase();

            if (action.equals("remove") && args.length == 3) {
                List<String> ids = plugin.getBossBarManager().getActiveBars().stream()
                        .map(BossBarManager.TimedBossBar::getId)
                        .collect(Collectors.toList());
                return filter(ids, args[2]);
            }

            if (action.equals("create")) {
                if (args.length == 3) return filter(Arrays.asList("event_bar", "boss_timer", "server_announce"), args[2]);
                if (args.length == 4) return filter(Arrays.asList("10", "30", "60", "300"), args[3]);
                if (args.length == 5) {
                    List<String> colors = Arrays.stream(BossBar.Color.values()).map(Enum::name).collect(Collectors.toList());
                    return filter(colors, args[4]);
                }
                if (args.length == 6) {
                    List<String> overlays = Arrays.stream(BossBar.Overlay.values()).map(Enum::name).collect(Collectors.toList());
                    return filter(overlays, args[5]);
                }
                if (args.length == 7) return filter(Arrays.asList("<gradient:#FF0033:#FFD700><bold>Événement en cours : {time}s</bold></gradient>"), args[6]);
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("actionbar")) {
            if (!sender.hasPermission("actionboard.admin")) return completions;

            if (args.length == 2) return filter(Arrays.asList("send"), args[1]);

            if (args[1].equalsIgnoreCase("send")) {
                if (args.length == 3) {
                    List<String> targets = new ArrayList<>(Arrays.asList("all"));
                    targets.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                    return filter(targets, args[2]);
                }
                if (args.length == 4) return filter(Arrays.asList("5", "10", "30", "60"), args[3]);
                if (args.length == 5) return filter(Arrays.asList("<gold><bold>Attention !</bold></gold> <gray>Message important</gray>"), args[4]);
            }
        }

        return completions;
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}

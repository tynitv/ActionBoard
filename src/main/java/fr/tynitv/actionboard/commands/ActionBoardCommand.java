package fr.tynitv.actionboard.commands;

import fr.tynitv.actionboard.ActionBoard;
import fr.tynitv.actionboard.bossbar.BossBarManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionBoardCommand implements CommandExecutor {

    private final ActionBoard plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ActionBoardCommand(ActionBoard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("toggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Seul un joueur peut exécuter cette commande.</red>"));
                return true;
            }
            boolean enabled = plugin.getActionBarManager().toggleActionBar(player);
            if (enabled) {
                player.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>Affichage réactivé.</green>"));
            } else {
                player.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <red>Affichage désactivé.</red>"));
            }
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("actionboard.admin")) {
                sender.sendMessage(mm.deserialize("<red>Vous n'avez pas la permission.</red>"));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>Configuration rechargée avec succès !</green>"));
            return true;
        }

        if (sub.equals("bossbar")) {
            if (!sender.hasPermission("actionboard.admin")) {
                sender.sendMessage(mm.deserialize("<red>Vous n'avez pas la permission.</red>"));
                return true;
            }
            return handleBossBar(sender, args);
        }

        if (sub.equals("actionbar")) {
            if (!sender.hasPermission("actionboard.admin")) {
                sender.sendMessage(mm.deserialize("<red>Vous n'avez pas la permission.</red>"));
                return true;
            }
            return handleActionBar(sender, args);
        }

        sendHelp(sender);
        return true;
    }

    private boolean handleBossBar(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(mm.deserialize("<yellow>Utilisation : /actionboard bossbar <create|remove|list|clear></yellow>"));
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("list")) {
            sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>BossBars Actives :</bold></gradient>"));
            for (BossBarManager.TimedBossBar bar : plugin.getBossBarManager().getActiveBars()) {
                sender.sendMessage(mm.deserialize("<gray>- ID: <white>" + bar.getId() + "</white> | Temps restant: <gold>" + bar.getRemainingSeconds() + "s</gold></gray>"));
            }
            return true;
        }

        if (action.equals("clear")) {
            plugin.getBossBarManager().clearAll();
            sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>Toutes les BossBars ont été supprimées.</green>"));
            return true;
        }

        if (action.equals("remove")) {
            if (args.length < 3) {
                sender.sendMessage(mm.deserialize("<red>Utilisation : /actionboard bossbar remove <id></red>"));
                return true;
            }
            String id = args[2];
            if (plugin.getBossBarManager().removeBossBar(id)) {
                sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>BossBar '" + id + "' supprimée.</green>"));
            } else {
                sender.sendMessage(mm.deserialize("<red>Aucune BossBar active avec l'ID '" + id + "'.</red>"));
            }
            return true;
        }

        if (action.equals("create")) {
            // /actionboard bossbar create <id> <seconds> <color> <style> <title...>
            if (args.length < 6) {
                sender.sendMessage(mm.deserialize("<red>Utilisation : /actionboard bossbar create <id> <secondes> <couleur> <style> <titre...></red>"));
                return true;
            }

            String id = args[2];
            int seconds;
            try {
                seconds = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(mm.deserialize("<red>Le temps doit être un nombre valide en secondes.</red>"));
                return true;
            }

            BossBar.Color color;
            try {
                color = BossBar.Color.valueOf(args[4].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(mm.deserialize("<red>Couleur invalide. (RED, BLUE, GREEN, PINK, PURPLE, WHITE, YELLOW)</red>"));
                return true;
            }

            BossBar.Overlay overlay;
            try {
                overlay = BossBar.Overlay.valueOf(args[5].toUpperCase());
            } catch (IllegalArgumentException e) {
                overlay = BossBar.Overlay.PROGRESS;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 6; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            String title = sb.toString().trim();

            plugin.getBossBarManager().createBossBar(id, seconds, color, overlay, title);
            sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>BossBar '" + id + "' créée pour " + seconds + "s !</green>"));
            return true;
        }

        return true;
    }

    private boolean handleActionBar(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(mm.deserialize("<yellow>Utilisation : /actionboard actionbar send <joueur|all> <secondes> <message...></yellow>"));
            return true;
        }

        String target = args[2];
        int seconds;
        try {
            seconds = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(mm.deserialize("<red>La durée doit être un nombre en secondes.</red>"));
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String msg = sb.toString().trim();

        if (target.equalsIgnoreCase("all") || target.equals("*")) {
            plugin.getActionBarManager().broadcastTemporaryActionBar(seconds, msg);
            sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>ActionBar envoyée à tous les joueurs pour " + seconds + "s !</green>"));
        } else {
            Player p = Bukkit.getPlayer(target);
            if (p == null) {
                sender.sendMessage(mm.deserialize("<red>Joueur '" + target + "' introuvable.</red>"));
                return true;
            }
            plugin.getActionBarManager().sendTemporaryActionBar(p, seconds, msg);
            sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>ActionBoard</bold></gradient> <gray>»</gray> <green>ActionBar envoyée à " + p.getName() + " pour " + seconds + "s !</green>"));
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gradient:#00F2FE:#4FACFE><bold>=== ActionBoard v1.1.0 Commandes ===</bold></gradient>"));
        sender.sendMessage(mm.deserialize("<gray>/actionboard toggle</gray> - <white>Activer/désactiver votre ActionBar</white>"));
        if (sender.hasPermission("actionboard.admin")) {
            sender.sendMessage(mm.deserialize("<gray>/actionboard bossbar create <id> <sec> <couleur> <style> <titre></gray>"));
            sender.sendMessage(mm.deserialize("<gray>/actionboard bossbar remove <id></gray>"));
            sender.sendMessage(mm.deserialize("<gray>/actionboard bossbar list</gray>"));
            sender.sendMessage(mm.deserialize("<gray>/actionboard actionbar send <joueur|all> <sec> <msg></gray>"));
            sender.sendMessage(mm.deserialize("<gray>/actionboard reload</gray> - <white>Recharger la configuration</white>"));
        }
    }
}

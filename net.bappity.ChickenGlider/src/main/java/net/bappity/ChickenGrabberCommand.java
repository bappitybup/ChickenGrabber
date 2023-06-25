package net.bappity;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class ChickenGrabberCommand implements CommandExecutor {
    private final ChickenGrabber _plugin;

    public ChickenGrabberCommand(ChickenGrabber plugin) {
        _plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command sender is a player and has permission, or if it is the console
        if (sender == null || (sender instanceof Player && sender.hasPermission("ChickenGrabber.use"))) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "help":
                        // Execute the reload command
                        sendHelpMessage(sender);
                        break;
                    case "reload":
                        // Execute the reload command
                        reloadAndNotify(sender);
                        break;
                    default:
                        // Display usage help for invalid subcommands
                        sendMessage(sender, "§cInvalid command. Usage: /chickengrabber <command>");
                        break;
                }
            } else {
                // Display usage help if no subcommands are provided
                sendHelpMessage(sender);
            }
        } else {
            // Notify the player that they do not have permission to use the command
            sendMessage(sender, "§cYou do not have permission to use this command.");
        }

        return true;
    }

    // Add this helper method below to handle sending messages to players and the console
    private void sendMessage(CommandSender commandSender, String message) {
        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(message);
        } else {
            _plugin.getLogger().info(ChatColor.stripColor(message));
        }
    }

    private void reloadAndNotify(CommandSender commandSender) {
        _plugin.reloadConfig();
        _plugin.reloadChickenGrabEvents();
        _plugin.reloadAllEventClasses(_plugin.getConfig());
        String reloadMessage = "§aConfiguration reloaded successfully!";

        sendMessage(commandSender, reloadMessage);
    }

    public void sendHelpMessage(CommandSender sender) {
        String headerFooter = ChatColor.AQUA + "==========" + ChatColor.RED + " ChickenGrabber " + ChatColor.AQUA + "==========";
        String helpCommand = ChatColor.YELLOW + "/chickengrabber help" + ChatColor.WHITE + " - Display the plugin command list";
        String reloadCommand = ChatColor.YELLOW + "/chickengrabber reload" + ChatColor.WHITE + " - Reloads the plugin configuration";

        sender.sendMessage(headerFooter);
        sender.sendMessage(helpCommand);
        sender.sendMessage(reloadCommand);
        sender.sendMessage(headerFooter);
    }
}
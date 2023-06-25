package net.bappity;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MyTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("chickengrabber")) {
                    switch (args.length) {
                        case 1: // first argument possibilities
                            completions.add("help");
                            completions.add("reload");
                            break;
                        default:
                            break;
                    }
                }

        return completions;
    }
}
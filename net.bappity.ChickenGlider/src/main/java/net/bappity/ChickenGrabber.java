package net.bappity;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChickenGrabber extends JavaPlugin {

    private ChickenGrabEvents _ChickenGrabEventsClass;

    @Override
    public void onEnable() {
        // Save the default config.yml if it doesn't exist in the plugin's data folder
        this.saveDefaultConfig();

        // Load the merged config (user's config with missing values filled from default
        // config)
        FileConfiguration config = this.getConfig();

        _ChickenGrabEventsClass = new ChickenGrabEvents(this, config);

        getCommand("chickengrabber").setExecutor(new ChickenGrabberCommand(this));
        getCommand("chickengrabber").setTabCompleter(new MyTabCompleter());

        // Register the event listener in the plugin manager
        getServer().getPluginManager().registerEvents(_ChickenGrabEventsClass, this);

        // This method is called when the plugin is enabled
        getLogger().info("ChickenGrabber is enabled!");
    }

    @Override
    public void onDisable() {
        // This method is called when the plugin is disabled
        getLogger().info("ChickenGrabber is disabled!");
    }

    public void reloadAllEventClasses(FileConfiguration config) {
        reloadChickenGrabEvents();
    }

    public void reloadChickenGrabEvents() {
        if (_ChickenGrabEventsClass != null) {
            _ChickenGrabEventsClass.reload();
        }
    }

    public ChickenGrabEvents getChickenGrabEventsClass() {
        return _ChickenGrabEventsClass;
    }
}
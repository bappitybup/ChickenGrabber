package net.bappity;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigValueGrabber<T> {
    private final FileConfiguration _config;

    public ConfigValueGrabber(FileConfiguration config) {
        _config = config;
    }

    public <T> T getCustomConfigValue(Class<T> clazz, String configEnabledPath, String configValuePath,
            T valueIfDisabledOrDefaultValue) {
        if (_config.getBoolean(configEnabledPath)) {
            Object configValue = null;
            if (clazz == Integer.class) {
                configValue = _config.getInt(configValuePath);
            } else if (clazz == Double.class) {
                configValue = _config.getDouble(configValuePath);
            } else if (clazz == Boolean.class) {
                configValue = _config.getBoolean(configValuePath);
            } else if (clazz == String.class) {
                configValue = _config.getString(configValuePath);
            } else {
                throw new IllegalArgumentException("Unsupported configuration value type " + clazz.getName());
            }
            return clazz.cast(configValue);
        } else {
            return valueIfDisabledOrDefaultValue;
        }
    }
}
package de.alphahelix.alphalibary.file;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SimpleFolder extends File {

    public SimpleFolder(String parent, String child) {
        super(parent, child);

        if (!this.exists())
            this.mkdirs();
    }

    public SimpleFolder(JavaPlugin plugin, String child) {
        this(plugin.getDataFolder().getAbsolutePath(), child);
    }
}

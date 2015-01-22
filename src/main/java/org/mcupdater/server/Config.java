package org.mcupdater.server;

import org.mcupdater.MCUServer;
import org.mcupdater.api.Version;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by alauritzen on 1/22/15.
 */
public class Config {
    public static String serverPackURL = "https://files.mcupdater.com/example/SamplePack.xml";
    public static String forgeJarPath = "";
    public static boolean autoStart = false;

    public static final String CONFIG_FILENAME = "mcuserver.properties";

    private Properties props;
    private File file;

    private static Config INSTANCE;

    private Config() {
        props = new Properties();
        file = new File(CONFIG_FILENAME);
    }

    public static void load() {
        if (INSTANCE == null)
            INSTANCE = new Config();
        INSTANCE._load();
    }

    private void _load() {
        if( !file.exists() ) {
            try {
                file.createNewFile();
                props.load(new FileReader(file));
            } catch (IOException e) {
                MCUServer.writeError("Unable to create new config file.");
            }
        }

        serverPackURL = props.getProperty("serverPackURL", serverPackURL);
        forgeJarPath = props.getProperty("forgeJarPath", forgeJarPath);
        autoStart = Boolean.parseBoolean(props.getProperty("autoStart", Boolean.toString(autoStart)));
    }

    public static void save() {
        INSTANCE._save();
    }
    private void _save() {
        props.setProperty("serverPackURL", serverPackURL);
        props.setProperty("forgeJarPath", forgeJarPath);
        props.setProperty("autoStart", Boolean.toString(autoStart));

        try {
            props.store(new FileWriter(file), "MCUServer settings, "+ Version.VERSION);
        } catch (IOException e) {
            MCUServer.writeError("Unable to save config settings.");
        }
    }
}

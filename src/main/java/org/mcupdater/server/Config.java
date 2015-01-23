package org.mcupdater.server;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.mcupdater.MCUServer;
import org.mcupdater.api.Version;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by alauritzen on 1/22/15.
 */
public class Config {
    private static final Map<String, Object> defaults;
    static {
        defaults = Maps.newHashMap();

        defaults.put("serverPackURL", "https://files.mcupdater.com/example/SamplePack.xml");
        defaults.put("forgeJarPath", "");
        defaults.put("autoStart", Boolean.valueOf(false));

        defaults.put("debug", Boolean.valueOf(false));
    }

    public static boolean debug = false;

    public static final String CONFIG_FILENAME = "mcuserver.properties";

    private Properties props;
    private File file;

    private static Config INSTANCE;

    private Config() {
        props = new Properties();
        file = new File(CONFIG_FILENAME);
    }

    public static void set(String key, String value) {
        INSTANCE.props.setProperty(key, value);
    }
    public static String get(String key) {
        return INSTANCE.props.getProperty(key);
    }

    public static void setBool(String key, boolean value) {
        set(key, Boolean.toString(value));
    }
    public static boolean getBool(String key) {
        return Boolean.parseBoolean(get(key));
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
            } catch (IOException e) {
                MCUServer.writeError("Unable to create new config file.");
            }
        }
        try {
            props.load(new FileReader(file));
            for( Map.Entry<String,Object> key : defaults.entrySet() ) {
                if( props.containsKey(key.getKey()) ) {
                    continue;
                }
                if( key.getValue() instanceof Boolean ) {
                    setBool(key.getKey(), (Boolean)key.getValue());
                } else {
                    set(key.getKey(), (String)key.getValue());
                }
            }
        } catch (IOException e) {
            MCUServer.writeError("Unable to read config file.");
        }

        debug = getBool("debug");
        if( debug ) {
            for( String key : defaults.keySet() ) {
                final String val = get(key);
                final boolean isDefault = val.equals(defaults.get(key).toString());
                MCUServer.write("["+(isDefault?"::":"++")+"] "+key+" = "+val);
            }
        }
    }

    public static void save() {
        INSTANCE._save();
    }
    private void _save() {
        try {
            props.store(new FileWriter(file), "MCUServer settings, "+ Version.VERSION);
        } catch (IOException e) {
            MCUServer.writeError("Unable to save config settings.");
        }
    }
}

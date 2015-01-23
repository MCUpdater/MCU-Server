package org.mcupdater.commands;

import org.apache.commons.lang3.StringUtils;
import org.mcupdater.MCUServer;
import org.mcupdater.server.Config;

/**
 * Created by alauritzen on 1/23/15.
 */
public class SetCommand extends AbstractCommandWrapper {

    @Override
    public boolean run(String args) {
        final String[] words = StringUtils.split(args, " ", 2);
        final String key;
        if( words.length != 2 ) {
            MCUServer.writeError("Syntax: set <variable> <value>");
        } else if( !Config.isOption(key = words[0]) ) {
            MCUServer.writeError("Invalid option '"+key+"'");
        } else {
            final String oldVal = Config.get(key);
            final String newVal = words[1];
            if( oldVal.equals(newVal) ) {
                MCUServer.write("Value unchanged.");
            } else {
                if (Config.isBoolean(key)) {
                    // TODO: complain if value input is not valid boolean
                    boolean boolVal = Boolean.parseBoolean(newVal);
                    Config.setBool(key, boolVal);
                } else {
                    Config.set(key, newVal);
                }
                MCUServer.write("Set '"+key+"' = "+Config.get(key)+" (was: "+oldVal+")");
                Config.save();
            }
        }
        return true;
    }

    @Override
    public String help() {
        return "Sets an option and saves the result to the config file.\n"+
                "  set <variable> <value>";
    }

    @Override
    public String shortHelp() {
        return "Set a config option.";
    }
}

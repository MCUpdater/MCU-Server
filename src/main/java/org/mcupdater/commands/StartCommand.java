package org.mcupdater.commands;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.mcupdater.MCUServer;
import org.mcupdater.server.Config;

import java.io.File;

public class StartCommand extends AbstractCommandWrapper {
    @Override
    public boolean run(String args) {
        final String jarPath;
        if( Strings.isNullOrEmpty(args) ) {
            // use the default
            jarPath = Config.get("forgeJarPath");
        } else {
            // we're attempting to override our default jar path
            jarPath = args;
        }

        // verify that the jar in question actually exists
        File jarFile = new File(jarPath);
        if( !jarFile.exists() ) {
            MCUServer.writeError("Unable to find jar to execute at '"+jarPath+"'");
            return false;
        }

        // TODO: finish implementing this :)

        MCUServer.writeError("Found jar but start functionality not yet implemented.");
        return false;
    }
}

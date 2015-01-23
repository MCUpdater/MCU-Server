package org.mcupdater.commands;

/**
 * Created by alauritzen on 1/23/15.
 */
public class SetCommand extends AbstractCommandWrapper {
    /*
    @Override
    public boolean run(String args) {

    }
    */
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

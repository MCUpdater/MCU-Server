package org.mcupdater.commands;

import org.mcupdater.MCUServer;

public abstract class AbstractCommandWrapper implements ICommandWrapper {

	@Override
	public boolean run(String args) {
		MCUServer.writeError("This command not yet implemented.");
		return true;
	}

	@Override
	public String help() {
		return "This command provides no help yet.";
	}

	@Override
	public String shortHelp() {
		return "--";
	}

}

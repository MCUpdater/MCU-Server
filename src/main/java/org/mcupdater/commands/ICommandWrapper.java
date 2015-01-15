package org.mcupdater.commands;

public interface ICommandWrapper {
	public boolean run(String args); 
	public String help();
	public String shortHelp();
}

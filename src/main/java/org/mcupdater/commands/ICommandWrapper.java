package org.mcupdater.commands;

public interface ICommandWrapper {
	boolean run(String args);
	String help();
	String shortHelp();
}

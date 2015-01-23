package org.mcupdater;

import java.io.Console;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.mcupdater.api.Version;
import org.mcupdater.commands.*;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.mcupdater.server.Config;

public class MCUServer {
	private static final int NAG_RATE = 3;	// how many errors do we remind them about 'help'?
	private static final int CONSOLE_WIDTH = 80;	// how wide are we assuming their console is?
	
	private static MCUServer _server;
	
	private boolean running;
	private Console console;
	private SortedMap<String,ICommandWrapper> commands; 

	public static void main(String[] args) {
		// NB: Once we are working, we can add cli args to allow non-interactive execution
		Console c = System.console();
		if( c == null ) {
			System.err.println("Non-interactive session, unable to attach to console. Aborting.");
			System.exit(1);
		}

		_server = new MCUServer(args, c);
		_server.init();
		_server.start();
	}
	
	protected MCUServer(String[] args, Console c) {
		// TODO: parse arguments
		console = c;
		commands = Maps.newTreeMap();
	}
	
	protected void init() {
        // read config file
        Config.load();
        Config.save();

		// register all commands
		registerCommand("help", new HelpCommand());
		registerCommand("quit", new QuitCommand());
		
		registerCommand("start", new StartCommand());
		registerCommand("stop", new StopCommand());

        registerCommand("set", new SetCommand());
	}
	
	public static MCUServer getServer() {
		return _server;
	}
	
	public static void printf(String fmt, Object ... params) {
		_server.console.writer().printf(fmt, params);
	}
	public static void write(String str) {
		_server.console.writer().println(WordUtils.wrap(str, CONSOLE_WIDTH));
	}
	public static void writeError(String str) {
		write("[--] "+str);
	}
	public static void writeCritical(String str) {
		write("[!!] "+str);
	}
	
	public void registerCommand(String cmd, ICommandWrapper wrapper) {
		commands.put(cmd, wrapper);
	}
	
	protected void start() {
		write("MCUpdater Server "+ Version.VERSION);

        // autostart support
        if( Config.getBool("autoStart") ) {
            write("Autostarting server...");
            // parseCommand("update");
            parseCommand("start");
        }

        running = true;
        int err_count = 0;
		while( running ) {
			String cmd = console.readLine("> ");
            // TODO: allow parseCommand to return false for errors
			if( !Strings.isNullOrEmpty(cmd) && !parseCommand(cmd) ) {
				writeError("Unknown command.");
				// nag
				++err_count;
				if( err_count % NAG_RATE == 0 ) {
					write("[::] Type 'help' for a list of valid commands.");
				}
			}
		}
		
		// TODO: cleanly shutdown child process
		write("Goodbye.");
	}
	
	public void stop() {
		//write("Got stop signal, attempting to stop.");
		running = false;
	}
	
	protected boolean parseCommand(final String cmd) {
		String command = cmd;
		String params = "";
		// split out params
		final int sep_idx = cmd.indexOf(' ');
		if( sep_idx >= 0 ) {
			command = cmd.substring(0,sep_idx);
			params = cmd.substring(sep_idx+1);
		}
		// clean up and feed into the mill
		command = command.toLowerCase();
		params = Strings.nullToEmpty(params).trim();
		
		return commands.containsKey(command) && commands.get(command).run(params);
	}

	public class HelpCommand implements ICommandWrapper {
		public HelpCommand() {}
		
		public boolean run(String args) {
			if( !args.isEmpty() ) {
				ICommandWrapper command = commands.get(args);
				if( command != null ) {
                    final String hr = "+" + StringUtils.leftPad("", args.length()+2, "-") + "+";
                    write(hr + "\n| "+args+" |\n" + hr);
					write(command.help());
				} else {
					writeError("No such command '"+args+"'");
					return false;
				}
			} else {
				write("The following commands are available:\n");
				// generate our list of registered commands to display help for
				for( Map.Entry<String, ICommandWrapper> entry : commands.entrySet()) {
					printf("  %-10s  %s\n",entry.getKey(),entry.getValue().shortHelp());
				}
				write("\nAdditionally, you can type 'help <command>' for more information on a given command.");
			}
			return true;
		}
		
		public String shortHelp() {
			return "This command";
		}
		
		public String help() {
			return "Type 'help' for a list of commands available, or 'help <command>' for more information on a given command.";
		}
	}
	
	public class QuitCommand implements ICommandWrapper {
		public QuitCommand() {}
		
		public boolean run(String args) {
			stop();
			return true;
		}

		public String help() {
			return "Quit MCU-Server. NB: This will shut down any running Minecraft server instance.";
		}

		public String shortHelp() {
			return "Quit";
		}		
	}
}

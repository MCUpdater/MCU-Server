package org.mcupdater.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;

public class ServerWrapper extends Thread {
	private static final int POLL_DELAY = 1000;	// one second
	
	private Path jarPath;
	private Process proc;
	private List<String> commandBuffer;
	private StringBuilder outBuffer;
	
	public ServerWrapper(String serverJar) {
		Path path = Paths.get(serverJar);
		if( Files.exists(path) && Files.isReadable(path) && !Files.isDirectory(path) ) {
			this.jarPath = path;
			forkServer();
		}
	}
	
	public Process getProcess() {
		return proc;
	}
	
	private void forkServer() {
		final String separator = System.getProperty("file.separator");
		final String classpath = System.getProperty("java.class.path");
		final String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		
		final ProcessBuilder pb;
		try {
			pb = new ProcessBuilder(path, "-cp", classpath, "-jar", jarPath.toString());
			pb.directory(jarPath.getParent().toFile());
			proc = pb.start();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			proc = null;
		}
		
		// if we forked something, prep the buffer and spin up the monitor thread
		if( proc != null ) {
			commandBuffer = Lists.newArrayList();
			start();
		}
	}
	
	public void write(String str) {
		if( commandBuffer != null ) {
			synchronized(commandBuffer) {
				commandBuffer.add(str+"\n");
			}
		}
	}
	
	public String read() {
		if( outBuffer != null ) {
			synchronized(outBuffer) {
				final String out = outBuffer.toString();
				outBuffer.setLength(0);
				return out;
			}
		} else {
			return "<EOF>";
		}
	}
	
	@Override
	public void run() {
		final InputStreamReader isr = new InputStreamReader(proc.getInputStream());
		final BufferedReader _in = new BufferedReader(isr);
		
		final InputStreamReader esr = new InputStreamReader(proc.getErrorStream());
		final BufferedReader _err = new BufferedReader(esr);
		
		final OutputStreamWriter osw = new OutputStreamWriter(proc.getOutputStream());
		final BufferedWriter _out = new BufferedWriter(osw);
		
		try {
			boolean running = true;
			String line = null;
			while( running ) {
				if( _in.ready() ) {
					synchronized(outBuffer) {
						while( (line = _in.readLine()) != null ) {
							outBuffer.append(line+"\n");
						}
					}
				}
				if( _err.ready() ) {
					synchronized(outBuffer) {
						while( (line = _err.readLine()) != null ) {
							outBuffer.append(line+"\n");
						}
					}
				}
				if( !commandBuffer.isEmpty() ) {
					synchronized(commandBuffer) {
						for( String command : commandBuffer ) {
							_out.write(command);
						}
					}
				}
				/*
				 * This feels stupid, but how else am I supposed to accomplish
				 * something like this at 2am? - al [2015.01.16]
				 */
				try {
					@SuppressWarnings("unused")
					int exit = proc.exitValue();
					running = false;
					// TODO: echo Server exited with status <exit>
				} catch (IllegalThreadStateException e) {
					// ignore me, we'll be seeing a lot of each other
				}
				// and sleep the pain away
				Thread.sleep(POLL_DELAY);
			}
			
			// clean up after ourselves just a bit
			_in.close();
			_err.close();
			_out.close();
		} catch( IOException ioe ) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			// some jerk woke us up
			e.printStackTrace();
		}		
	}
}

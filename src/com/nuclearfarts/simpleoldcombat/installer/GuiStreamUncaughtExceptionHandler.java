package com.nuclearfarts.simpleoldcombat.installer;

import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

public class GuiStreamUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private final GuiOutputStream stream;
	
	public GuiStreamUncaughtExceptionHandler(GuiOutputStream stream) {
		this.stream = stream;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		PrintStream s = new PrintStream(stream);
		s.println("A fatal error has occured and the OldCombat installer will now exit.");
		s.println("Error details: ");
		s.println("Exception on thread " + t.getName() + ":");
		e.printStackTrace(s);
		try {
			SwingUtilities.invokeAndWait(stream::display);
		} catch (InvocationTargetException | InterruptedException e1) {}
		System.exit(1);
	}

}

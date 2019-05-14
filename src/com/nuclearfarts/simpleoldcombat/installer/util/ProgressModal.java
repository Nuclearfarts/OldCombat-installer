package com.nuclearfarts.simpleoldcombat.installer.util;

import java.awt.Container;
import java.awt.Label;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class ProgressModal {
	
	private final JDialog window;
	private final Label label;
	private final JProgressBar progressBar;
	private long currentProgress = 0;
	private final long maxProgress;
	
	public ProgressModal(JFrame parent, String title, long maxProgress) {
		this.maxProgress = maxProgress;
		window = new JDialog(parent, title, true);
		window.setLocationByPlatform(true);
		Container pane = window.getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		label = new Label();
		pane.add(label);
		progressBar = new JProgressBar();
		pane.add(progressBar);
		window.pack();
	}
	
	public void setProgressText(String text) {
		SwingUtilities.invokeLater(() -> {label.setText(text);});
	}
	
	public void progressBy(long howMuch) {
		currentProgress += howMuch;
		int percent =  (int)(((double)currentProgress / (double)maxProgress) * 100);
		SwingUtilities.invokeLater(() -> progressBar.setValue(percent));
		if(currentProgress >= maxProgress) {
			try {
				SwingUtilities.invokeAndWait(() -> window.setVisible(false));
			} catch (InvocationTargetException | InterruptedException e) {}
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	public void display() {
		window.setVisible(true);
	}
}

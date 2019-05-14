package com.nuclearfarts.simpleoldcombat.installer;

import java.awt.Container;
import java.io.OutputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GuiOutputStream extends OutputStream {

	private final JFrame parent;
	private final String title;
	
	private StringBuilder builder = new StringBuilder();
	
	public GuiOutputStream(JFrame dialogParent, String dialogTitle) {
		parent = dialogParent;
		title = dialogTitle;
	}
	
	@Override
	public void write(int b) {
		builder.append((char)b);
	}
	
	public void display() {
		JDialog window = new JDialog(parent, title);
		window.setLocationByPlatform(true);
		window.setModal(true);
		JScrollPane scrollPane = new JScrollPane();
		Container panel = scrollPane.getViewport();
		window.getContentPane().add(scrollPane);
		JTextArea text = new JTextArea();
		panel.add(text);
		text.setText(builder.toString());
		text.setEditable(false);
		System.out.println(text.getText());
		builder = new StringBuilder();
		window.pack();
		window.setVisible(true);
	}
}

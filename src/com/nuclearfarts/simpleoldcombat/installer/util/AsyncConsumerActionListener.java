package com.nuclearfarts.simpleoldcombat.installer.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * An AWT ActionListener that delegates the call to a Consumer<ActionListener>.
 * This is just so you can use lambdas and :: notation as ActionListeners
 * easily.
 */
public class AsyncConsumerActionListener implements ActionListener {

	private Consumer<ActionEvent> consumer;

	public AsyncConsumerActionListener(Consumer<ActionEvent> c) {
		consumer = c;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(() -> consumer.accept(e)).start();
	}

}

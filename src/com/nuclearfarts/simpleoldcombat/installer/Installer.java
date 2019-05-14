package com.nuclearfarts.simpleoldcombat.installer;

import java.awt.Container;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nuclearfarts.simpleoldcombat.installer.util.AsyncConsumerActionListener;
import com.nuclearfarts.simpleoldcombat.installer.util.DigestUtil;
import com.nuclearfarts.simpleoldcombat.installer.util.DownloadData;
import com.nuclearfarts.simpleoldcombat.installer.util.DownloadVerificationException;
import com.nuclearfarts.simpleoldcombat.installer.util.ProgressModal;

public class Installer {

	/** If true, the installer will use a local index file. For testing. */
	private static final boolean USE_LOCAL = false;
	public static final URL VERSION_INDEX_LOCATION;

	static {
		try {
			VERSION_INDEX_LOCATION = new URL("https://github.com/Nuclearfarts/OldCombat_simple/raw/master/releases/index.json");
		} catch (MalformedURLException e) {
			throw new RuntimeException("someone mistyped a hardcoded URL", e);
		}
	}

	private final JFrame window;
	private final JComboBox<OldCombatVersion> versionSelect;
	private final JButton installClient;
	private final JButton installServer;
	private static GuiOutputStream errStream;
	private OldCombatVersion[] versions = new OldCombatVersion[] { OldCombatVersion.LOADING_VERSION };

	public Installer() {
		//set up frame with BoxLayout
		window = new JFrame("OldCombat Installer");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationByPlatform(true);
		Container pane = window.getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		
		//set up a GuiOutputStream for System.err and the UncaughtExceptionHandler.
		errStream = new GuiOutputStream(window, "An error occurred:");
		System.setErr(new PrintStream(errStream));
		Thread.setDefaultUncaughtExceptionHandler(new GuiStreamUncaughtExceptionHandler(errStream));
		//Set up the label and selection box for the version.
		Container versionSelectContainer = new Container();
		versionSelectContainer.setLayout(new BoxLayout(versionSelectContainer, BoxLayout.LINE_AXIS));
		versionSelectContainer.add(new Label("Select Minecraft version: "));
		versionSelect = new JComboBox<OldCombatVersion>(versions);
		versionSelect.setEnabled(false);
		versionSelect.setSelectedIndex(0);
		versionSelectContainer.add(versionSelect);
		
		//Set up the "install client" and "install server" buttons.
		Container installButtonContainer = new Container();
		installButtonContainer.setLayout(new BoxLayout(installButtonContainer, BoxLayout.LINE_AXIS));
		installClient = new JButton("Install Client");
		installClient.setEnabled(false);
		installClient.addActionListener(new AsyncConsumerActionListener(this::installClient));
		installServer = new JButton("Install Server...");
		installServer.setEnabled(false);
		installServer.addActionListener(new AsyncConsumerActionListener(this::installServer));
		installButtonContainer.add(installClient);
		installButtonContainer.add(installServer);
		
		//add all of them to the frame and make it visible.
		pane.add(versionSelectContainer);
		pane.add(installButtonContainer);
		window.pack();
		window.setVisible(true);
		loadVersions();
	}

	public void loadVersions() {
		BufferedReader reader;
		//if we're debugging, use a local file.
		//otherwise, use the one from the index location variable.
		//index.json is a file containing a json array of json objects which can be converted into OldCombatVersion objects.
		if (USE_LOCAL) {
			try {
				reader = new BufferedReader(new FileReader(new File("index.json")));
			} catch (FileNotFoundException e) {
				System.err.println("Sal enabled local index for debugging but didn't create a local index file, making him tonight's biggest loser.");
				e.printStackTrace();
				errStream.display();
				return;
			}
		} else {
			try {
				reader = new BufferedReader(new InputStreamReader(VERSION_INDEX_LOCATION.openStream()));
			} catch (IOException e) {
				System.err.println("Could not fetch index file. Github is down, your internet is down, or I moved the file. If it's option 3, redownload the installer.");
				e.printStackTrace();
				errStream.display();
				return;
			}
		}
		
		//set up GSON.
		//custom deserializers because immutable types need them.
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(OldCombatVersion.class, new OldCombatVersion.Deserializer())
				.registerTypeAdapter(DownloadData.class, new DownloadData.Deserializer()).create();
		versions = gson.fromJson(reader, OldCombatVersion[].class);
		//close the reader
		try {
			reader.close();
		} catch (IOException e) {
			System.err.println("Index file reader could not be closed!?");
			e.printStackTrace();
			errStream.display();
		}
		//wipe the version list, which is currently just the LOADING_VERSION...
		versionSelect.removeAllItems();
		//...and add the actual versions
		for(OldCombatVersion version : versions) {
			versionSelect.addItem(version);
		}
		
		//enable the selector and buttons
		setControlsEnabled(true);
		//re-pack the window so everything fits again
		window.pack();
	}
	
	public void installClient(ActionEvent a) {
		//disable the buttons so people don't do things twice by accident.
		try {
			SwingUtilities.invokeAndWait(() -> setControlsEnabled(false));
		} catch (InvocationTargetException | InterruptedException e1) {e1.printStackTrace();}
		//get selected version
		OldCombatVersion version = (OldCombatVersion) versionSelect.getSelectedItem();
		//progress modal
		ProgressModal progressModal = new ProgressModal(window, "Installing client...", version.clientJson.size);
		SwingUtilities.invokeLater(progressModal::display);
		progressModal.setProgressText("Downloading client json file");
		//create the install folder and json file
		String launcherName = version.mcVersion + "-oldcombat";
		Path versions = MCLocator.getMinecraftDirectory().toPath().resolve("versions");
		Path installFolder = versions.resolve(launcherName);
		installFolder.toFile().mkdir();
		File json = installFolder.resolve(launcherName + ".json").toFile();
		//download the file.
		try {
			json.createNewFile();
			version.clientJson.download(json, progressModal::progressBy);
		} catch (IOException e) {
			System.err.println("Error downloading .json file.");
			System.err.println(json.getAbsolutePath());
			e.printStackTrace();
			try {
				SwingUtilities.invokeAndWait(errStream::display);
			} catch (InvocationTargetException | InterruptedException e1) {}
		} catch (DownloadVerificationException e) {
			logDownloadError(e.getDownload(), e.getLocation(), e.getMessage());
		}
		//re-enable the controls
		try {
			SwingUtilities.invokeAndWait(() -> setControlsEnabled(true));
		} catch (InvocationTargetException | InterruptedException e) {}
	}

	public void installServer(ActionEvent event) {
		//disable the buttons so people don't do things twice by accident.
		try {
			SwingUtilities.invokeAndWait(() -> setControlsEnabled(false));
		} catch (InvocationTargetException | InterruptedException e1) {e1.printStackTrace();}
		//get selected version
		OldCombatVersion version = (OldCombatVersion) versionSelect.getSelectedItem();
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select server folder");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(fileChooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			//progress modal
			ProgressModal progressModal = new ProgressModal(window, "Installing server...", version.mcServer.size + version.serverJar.size);
			SwingUtilities.invokeLater(progressModal::display);
			progressModal.setProgressText("Downloading Minecraft server");
			//get file locations
			Path serverDir = fileChooser.getSelectedFile().toPath();
			File mcServer = serverDir.resolve("server.jar").toFile();
			File oldcombat = serverDir.resolve("oldcombat-" + version.oldCombatVersion + "-server.jar").toFile();
			try {
				mcServer.createNewFile();
				version.mcServer.download(mcServer, progressModal::progressBy);
			} catch (IOException e) {
				System.err.println("Error downloading Minecraft server.");
				System.err.println(mcServer.getAbsolutePath());
				e.printStackTrace();
			} catch (DownloadVerificationException e) {
				logDownloadError(e.getDownload(), e.getLocation(), e.getMessage());
			}
			progressModal.setProgressText("Downloading OldCombat...");
			try {
				oldcombat.createNewFile();
				version.serverJar.download(oldcombat, progressModal::progressBy);
			} catch (IOException e) {
				System.err.println("Error downloading OldCombat.");
				System.err.println(oldcombat.getAbsolutePath());
				e.printStackTrace();
			} catch (DownloadVerificationException e) {
				logDownloadError(e.getDownload(), e.getLocation(), e.getMessage());
			}
		}
		//re-enable the controls
		try {
			SwingUtilities.invokeAndWait(() -> setControlsEnabled(true));
		} catch (InvocationTargetException | InterruptedException e) {}
	}
	
	private void setControlsEnabled(boolean enabled) {
		versionSelect.setEnabled(enabled);
		installClient.setEnabled(enabled);
		installServer.setEnabled(enabled);
	}
	
	public static void logDownloadError(DownloadData d, File f, String msg) {
		System.err.println("Download failed verification with error: " + msg);
		System.err.println("Attempted to download from URL: " + d.url);
		System.err.println("To file: " + f.getAbsolutePath());
		try {
			System.err.println("SHA1 downloaded: " + DigestUtil.sha1(f));
		} catch (IOException e1) {}
		System.err.println("(expected " + d.sha1 + ")");
		try {
			System.err.println("Remote size status: " + d.verifyRemoteSize());
		} catch (IOException e) {}
		try {
			SwingUtilities.invokeAndWait(errStream::display);
		} catch (InvocationTargetException | InterruptedException e) {}
	}

	public static void main(String[] args) {
		//default Swing is horrible, switch to system version.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new Installer();
	}
}

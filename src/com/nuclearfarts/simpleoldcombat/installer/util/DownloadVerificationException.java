package com.nuclearfarts.simpleoldcombat.installer.util;

import java.io.File;

@SuppressWarnings("serial")
public class DownloadVerificationException extends Exception {
	
	private DownloadData download = null;
	private File location;
	
	public DownloadVerificationException() {
	}

	public DownloadVerificationException(String message) {
		super(message);
	}

	public DownloadVerificationException(Throwable cause) {
		super(cause);
	}

	public DownloadVerificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DownloadVerificationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public DownloadVerificationException setDownload(DownloadData download) {
		this.download = download;
		return this;
	}
	
	public DownloadData getDownload() {
		return download;
	}

	public File getLocation() {
		return location;
	}

	public DownloadVerificationException setLocation(File location) {
		this.location = location;
		return this;
	}
}

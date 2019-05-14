package com.nuclearfarts.simpleoldcombat.installer.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class DownloadData {
	public final URL url;
	public final String sha1;
	public final long size;

	public DownloadData(URL url, String sha1, long size) {
		this.url = url;
		this.sha1 = sha1;
		this.size = size;
	}

	public DownloadData(String url, String sha1, long size) throws MalformedURLException {
		this(new URL(url), sha1, size);
	}

	public boolean verify(File file) throws IOException {
		if (file.length() != size) {
			return false;
		}
		return DigestUtil.sha1(file).equalsIgnoreCase(sha1);
	}

	public void download(File file) throws IOException, DownloadVerificationException {
		download(file, null);
	}

	public void download(File file, Consumer<Integer> progressCallback) throws IOException, DownloadVerificationException {
		if(!verifyRemoteSize()) {
			throw new DownloadVerificationException("Incorrect remote size!").setDownload(this).setLocation(file);
		}
		try (BufferedInputStream in = new BufferedInputStream(url.openStream());
			FileOutputStream out = new FileOutputStream(file)) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				out.write(dataBuffer, 0, bytesRead);
				progressCallback.accept(bytesRead);
			}
		}
		if(!verify(file)) {
			throw new DownloadVerificationException("Incorrect checksum!").setDownload(this).setLocation(file);
		}
	}

	public boolean verifyRemoteSize() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("HEAD");
		long remoteSize = connection.getContentLengthLong();
		return remoteSize == size;
	}

	public static final class Deserializer implements JsonDeserializer<DownloadData> {

		@Override
		public DownloadData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			try {
				return new DownloadData(obj.get("url").getAsString(), obj.get("sha1").getAsString(),
						obj.get("size").getAsLong());
			} catch (MalformedURLException e) {
				throw new JsonParseException("Malformed download URL", e);
			}
		}

	}
}

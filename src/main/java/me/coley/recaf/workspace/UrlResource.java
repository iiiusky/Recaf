package me.coley.recaf.workspace;

import me.coley.recaf.util.NetworkUtil;
import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Importable online resource.
 *
 * @author Matt
 */
public class UrlResource extends JavaResource {
	/**
	 * URL pointing to file.
	 */
	private final URL url;
	/**
	 * Backing jar resource pointing to the local copy of the resource.
	 */
	private JavaResource backing;

	/**
	 * Constructs a URL resource.
	 *
	 * @param url
	 * 		The URL to pull content from. Should reference a class or jar file.
	 */
	public UrlResource(URL url) {
		super(ResourceKind.URL);
		this.url = url;
		verify();
		detectUrlKind();
	}

	/**
	 * @return The URL imported from.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Verify that the URL points to a valid location.
	 */
	private void verify() {
		NetworkUtil.verifyUrlContent(url);
	}

	/**
	 * Analyze the URL to determine which backing JavaResource implmentation to use.
	 */
	private void detectUrlKind() {
		// TODO: These temporary files should be deleted at some point
		String name = url.toString().toLowerCase();
		File file;
		if (name.endsWith(".class")) {
			try {
				if (name.startsWith("file:"))
					file = new File(url.getFile());
				else  {
					file = File.createTempFile("recaf", "temp.class");
					FileUtils.copyURLToFile(url, file);
				}
				backing = new ClassResource(file);
			} catch(IOException ex) {
				Logger.error(ex, "Failed to import class from URL \"{}\"", name);
			}
		} else if (name.endsWith(".jar")) {
			try {
				if (name.startsWith("file:"))
					file = new File(url.getFile());
				else  {
					file = File.createTempFile("recaf", "temp.jar");
					FileUtils.copyURLToFile(url, file);
				}
				backing = new JarResource(file);
			} catch(IOException ex) {
				Logger.error(ex, "Failed to import class from URL \"{}\"", name);
			}
		} else {
			// Invalid URL
			throw new IllegalArgumentException("URLs must end in a \".class\" or \".jar\", found \"" + name + "\"");
		}
	}

	@Override
	protected Map<String, byte[]> loadClasses() throws IOException {
		return backing.loadClasses();
	}

	@Override
	protected Map<String, byte[]> loadResources() throws IOException {
		return backing.loadResources();
	}

	@Override
	protected Map<String, SourceCode> loadSources(File file) throws IOException {
		return backing.loadSources(file);
	}

	@Override
	public String toString() {
		return url.toString();
	}
}
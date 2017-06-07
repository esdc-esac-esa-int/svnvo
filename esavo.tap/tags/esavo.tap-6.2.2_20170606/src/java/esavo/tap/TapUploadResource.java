package esavo.tap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import com.oreilly.servlet.MultipartRequest;

import esavo.uws.actions.UwsUploadResource;

public class TapUploadResource implements UwsUploadResource {
	private static final String URL_REGEXP = "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static final String PARAM_PREFIX = "param:";

	private final String name;
	private final URL url;
	private final String param;

	private final File file;

//	public TapUploadResource(final String name, final String value) {
//		this(name, value, (MultipartRequest) null);
//	}

	@SuppressWarnings("unchecked")
	public TapUploadResource(final String name, final String uri,	final MultipartRequest multipart) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("A resource name can not be NULL !");
		}
		this.name = name.trim();

		if (uri == null || uri.trim().isEmpty()){
			throw new IllegalArgumentException("The resource URI can not be NULL !");
		}
		
		String URI = uri.trim();
		if (URI.startsWith(PARAM_PREFIX)) {
			if (multipart == null){
				throw new IllegalArgumentException(
						"The URI scheme \"param\" can be used ONLY IF the VOTable is provided inside the HTTP request (multipart/form-data) !");
			} else if (URI.length() <= PARAM_PREFIX.length()) {
				throw new IllegalArgumentException("Incomplete URI (" + URI
						+ "): empty parameter name !");
			}
			
			url = null;
			param = URI.substring(PARAM_PREFIX.length()).trim();

			Enumeration<String> enumeration = multipart.getFileNames();
			File foundFile = null;
			while (foundFile == null && enumeration.hasMoreElements()) {
				String fileName = enumeration.nextElement();
				if (fileName.equals(param)) {
					foundFile = multipart.getFile(fileName);
				}
			}

			if (foundFile == null){
				throw new IllegalArgumentException(
						"Incorrect file reference (" + URI
								+ "): the parameter \"" + param
								+ "\" does not exist !");
			} else {
				file = foundFile;
			}
		} else if (URI.matches(URL_REGEXP)) {
			try {
				url = new URL(URI);
				param = null;
				file = null;
			} catch (MalformedURLException mue) {
				throw new IllegalArgumentException(mue.getMessage());
			}
		} else {
			throw new IllegalArgumentException("Invalid table URI: \"" + URI
					+ "\" !");
		}
	}

	@Override
	public InputStream openStream() throws IOException {
		if (url != null) {
			return url.openStream();
		} else {
			return new FileInputStream(file);
		}
	}

	@Override
	public boolean deleteFile() {
		if (file != null && file.exists()) {
			return file.delete();
		} else {
			return false;
		}
	}
	
	@Override
	public String getName(){
		return name;
	}

}

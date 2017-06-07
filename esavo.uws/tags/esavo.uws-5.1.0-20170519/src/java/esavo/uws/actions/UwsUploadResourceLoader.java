package esavo.uws.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UwsUploadResourceLoader implements UwsUploadResource {
	
	private final String name;
	private final File file;

	public UwsUploadResourceLoader(final String name, final File file) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Resource name can not be NULL !");
		}
		this.name = name.trim();
		
		if (file == null) {
			throw new IllegalArgumentException("Resource file can not be NULL !");
		}
		this.file = file;
	}

	@Override
	public InputStream openStream() throws IOException {
		return new FileInputStream(file);
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

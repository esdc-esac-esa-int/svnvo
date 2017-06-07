package esavo.uws.utils.test.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class DummyServletOutputStream extends ServletOutputStream {
	
	private OutputStream output;
	
	public DummyServletOutputStream(OutputStream output){
		this.output = output;
	}

	@Override
	public void write(int b) throws IOException {
		this.output.write(b);
	}

}

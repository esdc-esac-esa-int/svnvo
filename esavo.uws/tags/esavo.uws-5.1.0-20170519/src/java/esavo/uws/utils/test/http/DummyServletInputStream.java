package esavo.uws.utils.test.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

public class DummyServletInputStream extends ServletInputStream{
	
	private InputStream source;
	
	public DummyServletInputStream(InputStream source){
		this.source = source;
//		if(source == null){
//			System.out.println("\n\n--------------------------\nnull\n\n");
//			DummyUwsLog.doDump("ERROR", Thread.currentThread().getStackTrace());
//			System.out.println("\n---------------------------\n\n");
//		}
	}

	@Override
	public int read() throws IOException {
		return source.read();
	}

}

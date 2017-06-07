package esavo.uws.utils.test.http;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class DummyServletConfig implements ServletConfig {
	
	private DummyServletContext servletContext;
	private String servletName;
	
	public DummyServletConfig(DummyServletContext servletContext){
		this.servletContext = servletContext;
	}
	
	public void setServletName(String servletName){
		this.servletName = servletName;
	}

	@Override
	public String getInitParameter(String key) {
		return servletContext.getInitParameter(key);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return servletContext.getInitParameterNames();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public String getServletName() {
		return servletName;
	}

}

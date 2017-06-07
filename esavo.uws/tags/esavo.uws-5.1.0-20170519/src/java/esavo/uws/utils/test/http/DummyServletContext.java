/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.utils.test.http;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import esavo.uws.utils.test.utils.IteratorEnumeration;

public class DummyServletContext implements ServletContext {
	
	private Map<String, Object> attributes;
	private Map<String, String> initParameters;
	
	public DummyServletContext(){
		attributes = new HashMap<String, Object>();
		initParameters = new HashMap<String, String>();
	}
	
	public void setInitParameter(String key, String value){
		initParameters.put(key, value);
	}

	@Override
	public Object getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	@Override
	public Enumeration getAttributeNames() {
		Iterator<String> it = attributes.keySet().iterator();
		return new IteratorEnumeration<String>(it);
	}

	@Override
	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String arg0) {
		return initParameters.get(arg0);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return new IteratorEnumeration(initParameters.keySet().iterator());
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMimeType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set getResourcePaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String arg0) {
		System.out.println(arg0);
	}

	@Override
	public void log(Exception arg0, String arg1) {
		System.out.println(arg1 + ": " + (arg0 == null ? "" : arg0.getMessage()));
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		System.out.println(arg0 + ": " + (arg1 == null ? "" : arg1.getMessage()));
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

}

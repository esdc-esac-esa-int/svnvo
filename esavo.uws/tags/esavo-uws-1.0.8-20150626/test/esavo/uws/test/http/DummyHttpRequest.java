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
package esavo.uws.test.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class DummyHttpRequest implements HttpServletRequest {
	
	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	
	private String contentType;
	private int contentLength;
	private Map<String,Object> attributes;
	private String characterEncoding;
	private ServletInputStream is;
	private String localAddr;
	private String localName;
	private int localPort;
	private Locale locale;
	private Map<String,String> parameters;
	private String protocol;
	private Map<String,String> headers;
	private String method;
	private String pathInfo;
	private Cookie[] cookies;
	private String remoteAddr;
	private String remoteHost;
	private int remotePort;
	private String serverName;
	private int serverPort;
	private boolean isSecure;
	private String authType;
	private String contextPath;
	private String pathTranslated;
	private String queryString;
	private String remoteUser;
	private String requestUri;
	private StringBuffer requestUrl;
	private String requestedSessionId;
	private String servletPath;
	private HttpSession session;
	private String scheme;
	private String servletName;
	
	public DummyHttpRequest(){
		attributes = new HashMap<String, Object>();
		parameters = new HashMap<String, String>();
		headers = new HashMap<String, String>();
		//Default values:
		protocol = "HTTP/1.1";
		remoteHost = "127.0.0.1";
		remotePort = 32627;
		remoteAddr = "127.0.0.1";
		pathInfo = null;
		pathTranslated = null;
		servletName = null;
		method = "GET";
	}
	
	public DummyHttpRequest(String url, String servletName){
		this();
		parseAndSetFromUrl(url, servletName);
	}

	public DummyHttpRequest(String url, String servletName, Map<String,String> headers){
		this(url, servletName);
		this.headers = headers;
		setContentType(headers.get(HTTP_HEADER_CONTENT_TYPE));
		String contentLength = headers.get(HTTP_HEADER_CONTENT_LENGTH);
		if(contentLength != null){
			try{
				setContentLength(Integer.parseInt(contentLength));
			}catch(NumberFormatException nfe){
				System.out.println("Invalid content length: " + headers.get(HTTP_HEADER_CONTENT_LENGTH) + ". Url: " + url);
			}
		}else{
			System.out.println("Content length not provided for url: " + url);
		}
	}

	@Override
	public Object getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}
	
	@Override
	public int getContentLength() {
		return contentLength;
	}
	
	public void setContentLength(int contentLength){
		this.contentLength = contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType){
		this.contentType = contentType; 
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return is;
	}
	
	public void setInputStream(ServletInputStream is){
		this.is = is;
	}
	
	public void closeInputStream(){
		if(this.is != null){
			try{
				this.is.close();
			}catch(Exception e){
				
			}
		}
	}

	@Override
	public String getLocalAddr() {
		return localAddr;
	}
	
	public void setLocalAddr(String localAddr){
		this.localAddr = localAddr;
	}

	@Override
	public String getLocalName() {
		return localName;
	}
	
	public void setLocalName(String localName) {
		this.localName = localName;
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}
	
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		return parameters.get(arg0);
	}

	@Override
	public Map getParameterMap() {
		return parameters;
	}
	
	public void setParameter(String key, String value){
		parameters.put(key, value);
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return (String[])parameters.values().toArray();
	}

	@Override
	public String getProtocol() {
		return protocol;
	}
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(is));
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}
	
	public void setRemoteAddr(String remoteAddr){
		this.remoteAddr = remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}
	
	public void setRemoteHost(String remoteHost){
		this.remoteHost = remoteHost;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}
	
	public void setRemotePort(int remotePort){
		this.remotePort = remotePort;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		return scheme;
	}
	
	public void setScheme(String scheme){
		this.scheme = scheme;
	}

	@Override
	public String getServerName() {
		return serverName;
	}
	
	public void setServerName(String serverName){
		this.serverName = serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(int serverPort){
		this.serverPort = serverPort;
	}

	@Override
	public boolean isSecure() {
		return isSecure;
	}
	
	public void setIsSecure(boolean isSecure){
		this.isSecure = isSecure;
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		characterEncoding = arg0;
	}

	@Override
	public String getAuthType() {
		return authType;
	}
	
	public void setAuthType(String authType){
		this.authType = authType;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}
	
	public void setContextPath(String contextPath){
		this.contextPath = contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}
	
	public void setCookies(Cookie[] cookies){
		this.cookies = cookies;
	}

	@Override
	public long getDateHeader(String arg0) {
		return Long.parseLong(headers.get(arg0));
	}

	@Override
	public String getHeader(String arg0) {
		return headers.get(arg0);
	}

	@Override
	public Enumeration getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public Enumeration getHeaders(String arg0) {
		return Collections.enumeration(headers.values());
	}

	@Override
	public int getIntHeader(String arg0) {
		return Integer.parseInt(headers.get(arg0));
	}
	
	public void putHeader(String key, String value){
		headers.put(key,value);
	}
	
	public void putHeaders(Map<String,String> headers){
		this.headers = headers;
	}

	@Override
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method){
		this.method = method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}
	
	public void setPathInfo(String pathInfo){
		this.pathInfo = pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return pathTranslated;
	}
	
	public void setPathTranslated(String pathTranslated){
		this.pathTranslated = pathTranslated;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}
	
	public void setQueryString(String queryString){
		this.queryString = queryString;
	}

	@Override
	public String getRemoteUser() {
		return remoteUser;
	}
	
	public void setRemoteUser(String remoteUser){
		this.remoteUser = remoteUser;
	}

	@Override
	public String getRequestURI() {
		return requestUri;
	}
	
	public void setRequestURI(String requestUri){
		this.requestUri = requestUri;
	}

	@Override
	public StringBuffer getRequestURL() {
		return requestUrl;
	}
	
	public void setRequestURL(StringBuffer requestUrl){
		this.requestUrl = requestUrl;
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId;
	}
	
	public void setRequestedSessionId(String requestedSessionId){
		this.requestedSessionId = requestedSessionId;
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}
	
	public void setServletPath(String servletPath){
		this.servletPath = servletPath;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}
	
	public void setHttpSession(HttpSession session){
		this.session = session;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void parseAndSetFromUrl(String url, String servletName){
		this.servletName = servletName;
		int pos = url.indexOf('?');
		String urlWithoutParameters;
		if(pos >= 0){
			urlWithoutParameters = url.substring(0, pos);
			setQueryString(url.substring(pos+1));
			//Set parameters;
			String[] parameters = url.substring(pos+1).split("&");
			String[] parameter;
			this.parameters.clear();
			for(String p: parameters){
				parameter = p.split("=");
				this.parameters.put(parameter[0], parameter[1]);
			}
		}else{
			urlWithoutParameters = url;
		}
		setRequestURL(new StringBuffer(urlWithoutParameters));
		//Scheme
		pos = urlWithoutParameters.indexOf("://");
		if(pos >= 0){
			setScheme(urlWithoutParameters.substring(0, pos));
		}else{
			//no scheme!!!
			throw new IllegalArgumentException("Url without protocol is not allowed. Url: " + url);
		}
		int start = pos+3;
		int end;
		end = urlWithoutParameters.indexOf(':', start);
		String host;
		int port;
		if(end > 0){
			//contains port
			host = urlWithoutParameters.substring(start, end);
			start = end+1;
			end = urlWithoutParameters.indexOf('/', start);
			if(end >= 0){
				port = Integer.parseInt(urlWithoutParameters.substring(start, end));
				start = end+1;
			}else{
				//no more inputs, only port
				port = Integer.parseInt(urlWithoutParameters.substring(start));
				start = urlWithoutParameters.length();
			}
		}else{
			//does not contain port: use standard port
			port = 80;
			end = urlWithoutParameters.indexOf('/', start);
			if(end >= 0){
				//more parameters
				host = urlWithoutParameters.substring(start, end);
				start = end+1;
			} else {
				//no more parameters
				host = urlWithoutParameters.substring(start);
				start = urlWithoutParameters.length();
			}
		}
		setServerName(host);
		setServerPort(port);
		if(start < urlWithoutParameters.length()){
			//get URI, context path & context path
			String uri = urlWithoutParameters.substring(start);
			setRequestURI("/"+uri);
			String[] path = uri.split("/");
//			if(path != null){
//				setServletPath("/"+path[path.length-1]);
//				StringBuilder sb = new StringBuilder("/");
//				for(int i = 0; i < path.length-1; i++){
//					if(i != 0){
//						sb.append("/");
//					}
//					sb.append(path[i]);
//				}
//				setContextPath(sb.toString());
//			}
			if (path != null) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < path.length; i++) {
					if (path[i].equals(servletName)) {
						setServletPath("/" + path[i]);
						setContextPath(sb.toString());
						sb.setLength(0);
					} else {
						sb.append('/').append(path[i]);
					}
				}
				String pathInfo = sb.toString();
				if("".equals(pathInfo) || "/".equals(pathInfo)){
					setPathInfo(null);
				}else{
					setPathInfo(pathInfo);
				}
			}

		}
	}
	
	public static void main(String[] args){
		//DummyHttpRequest r = new DummyHttpRequest("http://localhost:8080/tap-local/tap/Upload?param=value&param2=value2");
		DummyHttpRequest r = new DummyHttpRequest("http://localhost:8080/tap-local/tap/async/134525/phase?PHASE=RUN", "tap");
		//Expected:
//		URL: http://localhost:8080/tap-local/tap/async/12342352/phase
//		URI: /tap-local/tap/async/12342352/phase  (all without host:port)
//		Scheme: http
//		Server name: localhost
//		Server port: 8080
//		Context path: /tap-local                  (from host/port up to servletName)
//		Servlet path: /tap                        (servletName)
//		Path info: /async/12342352/phase          (from servletName up to queryString)
//		Query string: PHASE=RUN
		System.out.println("Protocol: " + r.getProtocol());
		System.out.println("URL: " + r.getRequestURL());
		System.out.println("URI: " + r.getRequestURI());
		System.out.println("Scheme: " + r.getScheme());
		System.out.println("Server name: " + r.getServerName());
		System.out.println("Server port: " + r.getServerPort());
		System.out.println("Context path: " + r.getContextPath());
		System.out.println("Servlet path: " + r.getServletPath());
		System.out.println("Path info: " + r.getPathInfo());
		System.out.println("Query string: " + r.getQueryString());
	}

}

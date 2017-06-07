package esavo.uws.utils.test.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class DummyHttpResponse implements HttpServletResponse {
	
	public static final String HTTP_HEADER_USER_AGENT = "User-Agent";
	public static final String HTTP_HEADER_HOST = "Host";
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	
	private ServletOutputStream output;
	private int contentLength;
	private String contentType;
	private Locale locale;
	private String characterEncoding;
	private Map<String,String> headers;
	private int status;
	private String message;
	private String redirect;
	private boolean commited;
	private int bufferSize;
	private List<Cookie> cookies;
	private PrintWriter writer;
	
	private ByteArrayOutputStream baos;
	private DummyServletOutputStream os;
	
	public DummyHttpResponse(){
		headers = new HashMap<String, String>();
		cookies = new ArrayList<Cookie>();
		bufferSize = 0;
		commited = false;
		//Default output:
		clearOutput();
	}
	
	public void clearOutput(){
		closeOutputStream();
		commited = false;
		baos = new ByteArrayOutputStream();
		os = new DummyServletOutputStream(baos);
		output = os;
		writer = new PrintWriter(output);
	}
	
	public void closeOutputStream(){
		if(baos!=null){
			try{
				baos.close();
			}catch(Exception e){
				
			}
		}
		if(os!=null){
			try{
				os.close();
			}catch(Exception e){
				
			}
		}
		if(writer != null){
			try{
				writer.close();
			}catch(Exception e){
				
			}
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		commited = true;
		if(writer != null){
			writer.flush();
		}
		if(baos != null){
			baos.flush();
		}
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return output;
	}
	
	public void setOutputStream(ServletOutputStream output){
		this.output = output;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if(writer == null){
			writer = new PrintWriter(output);
		}
		return writer;
	}

	@Override
	public boolean isCommitted() {
		return commited;
	}

	@Override
	public void reset() {
		resetBuffer();
		status = -1;
		message = null;
		headers.clear();
	}

	@Override
	public void resetBuffer() {
		//TODO something to do?
	}

	@Override
	public void setBufferSize(int arg0) {
		bufferSize = arg0;
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		this.characterEncoding = arg0;
	}

	@Override
	public void setContentLength(int arg0) {
		this.contentLength = arg0;
	}
	
	public int getContentLength(){
		return this.contentLength;
	}

	@Override
	public void setContentType(String arg0) {
		this.contentType = arg0;
	}
	
	@Override
	public void setLocale(Locale arg0) {
		this.locale = arg0;
	}

	@Override
	public void addCookie(Cookie arg0) {
		cookies.add(arg0);
	}
	
	public List<Cookie> getCookies(){
		return cookies;
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		headers.put(arg0, ""+arg1);
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		headers.put(arg0, arg1);
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		headers.put(arg0, ""+arg1);
	}
	
	public void setHeaders(Map<String,String> headers){
		this.headers = headers;
	}

	@Override
	public boolean containsHeader(String arg0) {
		return headers.containsKey(arg0);
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		return encodeURL(arg0);
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		return encodeRedirectURL(arg0);
	}

	@Override
	public String encodeURL(String arg0) {
		// TODO add session ID?
		return arg0;
	}

	@Override
	public String encodeUrl(String arg0) {
		return encodeURL(arg0);
	}

	@Override
	public void sendError(int arg0) throws IOException {
		setStatus(arg0);
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		setStatus(arg0, arg1);
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		redirect = arg0;
	}
	
	public String getRedirect(){
		return redirect;
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		headers.put(arg0, ""+arg1);
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		headers.put(arg0, arg1);
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		headers.put(arg0, ""+arg1);
	}
	
	public String getHeader(String header){
		return headers.get(header);
	}
	
	public Map<String,String> getHeaders(){
		return headers;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus(){
		return this.status;
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		setStatus(arg0);
		message = arg1;
	}
	
	public String getOutputAsString(){
		try {
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

}

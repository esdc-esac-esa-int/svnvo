package esavo.sl.services.vospace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import esavo.uws.config.UwsConfigurationManager;


public class ConnectionHandler {
	
	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
	public static final String CONTENT_TYPE_APP_FORM_URLENCODED = "application/x-www-form-urlencoded";
	
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_USER_AGENT = "User-Agent";
	
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_PUT = "PUT";
	
	public static final String LINE_FEED = "\r\n";
	
	private String userAgent;
	private URL url;
	private boolean isHttps;
	private Object connection;
	private InputStream is;
	private OutputStream os;
	private String charset;
	
	public ConnectionHandler(URL url, String userAgent){
		this(url, userAgent, "UTF-8");
	}
	
	public ConnectionHandler(URL url, String userAgent, String charset){
		this.url = url;
		this.userAgent = userAgent;
		this.charset = charset;
		this.isHttps = isHttps();
	}
	
	public int getResponseCode() throws IOException{
		if(isHttps){
			return ((HttpsURLConnection)connection).getResponseCode();
		}else{
			return ((HttpURLConnection)connection).getResponseCode();
		}
	}
	
	public String getResponseMessage() throws IOException{
		if(isHttps){
			return ((HttpsURLConnection)connection).getResponseMessage();
		}else{
			return ((HttpURLConnection)connection).getResponseMessage();
		}
	}
	
	public String getResponseHeader(String header){
		if(isHttps){
			return ((HttpsURLConnection)connection).getHeaderField(header);
		}else{
			return ((HttpURLConnection)connection).getHeaderField(header);
		}
	}
	
	public String sendGet() throws IOException{
		open(METHOD_GET, null);
		try{
			return readResponse();
		}catch(IOException ioe){
			close();
			throw ioe;
		}
	}
	
	public String sendPost(InputStream data, boolean autoredirect, String method) throws IOException{
		open(method, CONTENT_TYPE_APP_FORM_URLENCODED);
		setDoOutput();
		setRedirect(autoredirect);
		try{
			os = getOutputStream();
			dumpData(data);
			return readResponse();
		}catch(IOException ioe){
			close();
			throw ioe;
		}
	}

	public String sendMultipart(InputStream data, String fileName, String method) throws IOException{
		String boundary = "===" + System.currentTimeMillis() + "===";
		String contentType = this.CONTENT_TYPE_MULTIPART + "; boundary=" + boundary;
		open(method, contentType);
		setDoOutput();
		setRedirect(false);
		try{
			os = getOutputStream();
			writeMultipartData(boundary, fileName, data);
			return readResponse();
			//return null;
		}catch(IOException ioe){
			close();
			throw ioe;
		}
	}
	
	private void writeMultipartData(String boundary, String fileName, InputStream data) throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os), true);
		addFilePart(pw, boundary, "textFile", fileName, data);
		endMultipart(pw, boundary);
	}
	
	private String readResponse() throws IOException {
//		if (getResponseCode() < 400) {
//			is = getInputStream();
//		} else {
//			is = getErrorInputStream();
//		}
		try{
			is = getInputStream();
		}catch(IOException ioe){
		}
		if(is == null){
			try{
				is = getErrorInputStream();
			}catch(IOException ioe){
			}
		}
		if(is == null){
			return null;
		}
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = br.readLine()) != null){
				sb.append(line).append("\n");
			}
			return sb.toString();
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(Exception e){
				}
			}
			if(is != null){
				try{
					is.close();
				}catch(Exception e){
					
				}
			}
		}
	}
	
	private void open(String method, String contentType) throws IOException{
		if(isHttps){
			connection = (HttpsURLConnection)url.openConnection();
			((HttpsURLConnection)connection).setRequestMethod(method);
			((HttpsURLConnection)connection).setDoInput(true);
			((HttpsURLConnection)connection).setUseCaches(false);
			if(contentType != null){
				((HttpsURLConnection)connection).setRequestProperty(HEADER_CONTENT_TYPE, contentType);
			}
			if(userAgent != null){
				((HttpsURLConnection)connection).setRequestProperty(HEADER_USER_AGENT, userAgent);
			}
		}else{
			connection = (HttpURLConnection)url.openConnection();
			((HttpURLConnection)connection).setRequestMethod(method);
			((HttpURLConnection)connection).setDoInput(true);
			((HttpURLConnection)connection).setUseCaches(false);
			if(contentType != null){
				((HttpURLConnection)connection).setRequestProperty(HEADER_CONTENT_TYPE, contentType);
			}
			if(userAgent != null){
				((HttpsURLConnection)connection).setRequestProperty(HEADER_USER_AGENT, userAgent);
			}
		}
	}
	
	private void setDoOutput(){
		if(isHttps){
			((HttpsURLConnection)connection).setDoOutput(true);
		}else{
			((HttpURLConnection)connection).setDoOutput(true);
		}
	}
	
	private void setRedirect(boolean redirect){
		if(isHttps){
			((HttpsURLConnection)connection).setInstanceFollowRedirects(redirect);
		}else{
			((HttpURLConnection)connection).setInstanceFollowRedirects(redirect);
		}
	}
	
	private InputStream getInputStream() throws IOException{
		if(isHttps){
			return ((HttpsURLConnection)connection).getInputStream();
		}else{
			return ((HttpURLConnection)connection).getInputStream();
		}
	}
	
	private InputStream getErrorInputStream() throws IOException {
		if (isHttps) {
			return ((HttpsURLConnection) connection).getErrorStream();
		} else {
			return ((HttpURLConnection) connection).getErrorStream();
		}
	}
	
	private OutputStream getOutputStream() throws IOException{
		if(isHttps){
			return ((HttpsURLConnection)connection).getOutputStream();
		}else{
			return ((HttpURLConnection)connection).getOutputStream();
		}
	}
	
	public String dumpHeaders(){
		Map<String,List<String>> headers = new HashMap<String, List<String>>();
		if(isHttps){
			headers = ((HttpsURLConnection)connection).getHeaderFields();
		}else{
			headers = ((HttpURLConnection)connection).getHeaderFields();
		}
		if(headers != null){
			StringBuilder sb = new StringBuilder();
			for(Entry<String,List<String>> e: headers.entrySet()){
				sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
			}
			return sb.toString();
		} else {
			return null;
		}
	}
	
	public void close(){
		if(os != null){
			try{
				os.close();
			}catch(Exception e){
				
			}
		}
		if(is != null){
			try{
				is.close();
			}catch(Exception e){
				
			}
		}
		connection = null;
	}
	
	private boolean isHttps(){
		String protocol = url.getProtocol();
		return "https".equalsIgnoreCase(protocol);
	}
	
	private void dumpData(InputStream data) throws IOException {
		if(data == null){
			return;
		}
		byte[] buffer = new byte[65536];
		int read;
		while((read = data.read(buffer)) != -1){
			os.write(buffer, 0, read);
		}
		os.flush();
	}

    public void addFilePart(PrintWriter pw, String boundary, String fieldName, String fileName, InputStream data) throws IOException {
        String tmp;
        pw.append("--" + boundary).append(LINE_FEED);
        tmp = "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"";
        pw.append(tmp).append(LINE_FEED);
        //tmp = "Content-Type: " + URLConnection.guessContentTypeFromName(fileName);
        tmp = "Content-Type: " + "application/x-votable+xml";
        pw.append(tmp).append(LINE_FEED);
        pw.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        pw.append(LINE_FEED);
        pw.flush();
        
        dumpData(data);
                
        pw.append(LINE_FEED);
        pw.flush();    
    }
    
    public void endMultipart(PrintWriter pw, String boundary){
    	pw.append(LINE_FEED).flush();
    	pw.append("--" + boundary + "--").append(LINE_FEED);
    	pw.flush();
    }


}

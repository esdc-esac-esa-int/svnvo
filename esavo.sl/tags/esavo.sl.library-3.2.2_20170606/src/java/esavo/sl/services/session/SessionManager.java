package esavo.sl.services.session;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionManager {
	
	public SessionManager(){
		
	}
	
	public static synchronized long getUniqueId(){
		long time = System.currentTimeMillis();
		long t;
		while((t = System.currentTimeMillis()) == time);
		return t;
	}
	
	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String jsonOutput= "{ 'id': '"+getUniqueId()+"' }";
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(jsonOutput);
		out.flush();
	}

}

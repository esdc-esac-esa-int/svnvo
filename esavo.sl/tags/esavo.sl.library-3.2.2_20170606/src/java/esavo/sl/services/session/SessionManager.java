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

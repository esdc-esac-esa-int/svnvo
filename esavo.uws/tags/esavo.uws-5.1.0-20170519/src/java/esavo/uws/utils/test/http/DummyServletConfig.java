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

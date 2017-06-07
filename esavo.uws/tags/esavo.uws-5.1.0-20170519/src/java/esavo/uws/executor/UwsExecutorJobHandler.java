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
package esavo.uws.executor;

public class UwsExecutorJobHandler {
	
	private Object handler;
	private UwsExecutor executor;
	
//	public UwsExecutorJobHandler(Object handler){
//		this.handler = handler;
//	}
//	
//	public UwsExecutorJobHandler(UwsExecutor executor, Object handler){
//		this.executor = executor;
//		this.handler = handler;
//	}
	
	public Object getHandler(){
		return handler;
	}
	public void setHandler(Object handler){
		this.handler = handler;
	}

	/**
	 * @return the executor
	 */
	public UwsExecutor getExecutor() {
		return executor;
	}

	/**
	 * @param executor the executor to set
	 */
	public void setExecutor(UwsExecutor executor) {
		this.executor = executor;
	}

}

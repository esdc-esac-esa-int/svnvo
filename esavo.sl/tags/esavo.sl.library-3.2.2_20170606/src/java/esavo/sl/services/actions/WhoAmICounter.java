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
package esavo.sl.services.actions;

public class WhoAmICounter {
	
	private static WhoAmICounter whoAmICounter;
	
	private int counter;
	
	private WhoAmICounter(){
		counter = 0;
	}
	
	public static synchronized WhoAmICounter getInstance(){
		if(whoAmICounter == null){
			whoAmICounter = new WhoAmICounter();
		}
		return whoAmICounter;
	}
	
	public synchronized int getCounter(){
		return counter++;
	}

}

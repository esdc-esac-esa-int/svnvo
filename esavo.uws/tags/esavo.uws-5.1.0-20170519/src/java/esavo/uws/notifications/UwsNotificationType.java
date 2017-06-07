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
package esavo.uws.notifications;

/**
 * For UWS, reserved from 0 to 999
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsNotificationType {
	
	//Notifications with NEGATIVE CODES are shown to ALL USERS
	
	//System warnings notifications: -1xx
	public static final int SYSTEM_WARNING_NOTIFICATION = -100;
	
}

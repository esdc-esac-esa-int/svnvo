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
package esavo.uws.jobs.utils;

public class UwsJobDetailsComparison extends UwsJobDetails {

	private long startTimeLimit;
	private long endTimeLimit;

	/**
	 * @return the startTimeLimit
	 */
	public long getStartTimeLimit() {
		return startTimeLimit;
	}

	/**
	 * @param startTimeLimit the startTimeLimit to set
	 */
	public void setStartTimeLimit(long startTimeLimit) {
		this.startTimeLimit = startTimeLimit;
	}

	/**
	 * @return the endTimeLimit
	 */
	public long getEndTimeLimit() {
		return endTimeLimit;
	}

	/**
	 * @param endTimeLimit the endTimeLimit to set
	 */
	public void setEndTimeLimit(long endTimeLimit) {
		this.endTimeLimit = endTimeLimit;
	}

	@Override
	public String toString() {
		return super.toString() + ",\nStart time limit: " + startTimeLimit + ", End time limit: " + endTimeLimit;
	}

}

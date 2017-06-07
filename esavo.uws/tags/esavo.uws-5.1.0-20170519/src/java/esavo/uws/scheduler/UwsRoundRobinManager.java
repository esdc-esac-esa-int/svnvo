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
package esavo.uws.scheduler;

public class UwsRoundRobinManager {

	private int size = 1;
	private int index = 0;
	private int checkPoint = 0;

	public UwsRoundRobinManager(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public int getIndex() {
		return index;
	}

	public boolean isCheckPointReached() {
		if (size < 1) {
			return true;
		}
		return index == checkPoint;
	}

	public void addOneSlot() {
		this.size++;
	}

	public void markCheckPoint() {
		this.checkPoint = index;
	}

	public int incrementIndex() {
		index = getSuitableIndex(index + 1);
		return index;
	}

	private int getSuitableIndex(int indexValue) {
		if (indexValue < size) {
			return indexValue;
		} else {
			return 0;
		}
	}

}

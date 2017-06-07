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

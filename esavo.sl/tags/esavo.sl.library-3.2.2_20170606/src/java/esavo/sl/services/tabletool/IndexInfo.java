package esavo.sl.services.tabletool;

public class IndexInfo {
	private boolean removeOldIndex = false;
	private boolean createNewIndex = false;
	
	public boolean isNewIndexRequested(){
		return createNewIndex;
	}
	public boolean isOldIndexRemovalRequested(){
		return removeOldIndex;
	}
	public void setRemoveOldIndex(boolean b){
		removeOldIndex = b;
	}
	public void setCreateNewIndex(boolean b){
		createNewIndex = b;
	}

}

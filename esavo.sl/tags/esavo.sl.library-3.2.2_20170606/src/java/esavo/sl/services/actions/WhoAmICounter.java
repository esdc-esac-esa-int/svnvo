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

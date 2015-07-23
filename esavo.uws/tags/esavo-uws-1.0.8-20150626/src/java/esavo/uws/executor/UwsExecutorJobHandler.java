package esavo.uws.executor;

public class UwsExecutorJobHandler {
	
	private Object handler;
	
	public UwsExecutorJobHandler(){
		
	}
	
	public UwsExecutorJobHandler(Object handler){
		this.handler = handler;
	}
	
	public Object getHandler(){
		return handler;
	}
	public void setHandler(Object handler){
		this.handler = handler;
	}

}

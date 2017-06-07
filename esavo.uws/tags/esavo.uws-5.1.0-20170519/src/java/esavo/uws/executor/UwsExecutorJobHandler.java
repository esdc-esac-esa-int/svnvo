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

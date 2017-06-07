package esavo.sl.dd.requests;

import java.util.List;
import java.util.Map;


import esavo.sl.dd.util.DDFilePath;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;

public interface DDFunctions {
	
	public DDRequestResult process(DDRetrievalRequest retrievalRequest) throws Exception;
	public UwsConfiguration getConfiguration();
	
	public int getProprietary(Map<String,String> retrievalElement, UwsJobOwner user) throws Exception;
	public List<DDFilePath> getPaths(DDRetrievalRequest retrievalRequest) throws Exception;
	
	public void insertLog(DDRetrievalRequest retrievalRequest, int statusOid) throws Exception;
	public void updateLog(DDRetrievalRequest retrievalRequest, double size, int statusOid, String propStatus) throws Exception;

}

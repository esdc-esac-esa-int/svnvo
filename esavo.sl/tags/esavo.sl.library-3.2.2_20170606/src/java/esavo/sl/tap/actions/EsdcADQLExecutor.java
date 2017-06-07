package esavo.sl.tap.actions;


import java.sql.ResultSet;

import esavo.uws.UwsException;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;
import esavo.tap.ADQLExecutor;
import esavo.tap.log.TAPLog;
import esavo.tap.parameters.TAPParameters;

public class EsdcADQLExecutor extends ADQLExecutor implements UwsExecutor {
	
	public EsdcADQLExecutor(final EsacTapService service, String appid, TAPLog logger){
		super(service, appid, logger);
	}
	
	protected boolean isQuotaUpdateNeeded(TAPParameters tapParams){
		return isTableCreated(tapParams);
	}
	
	protected boolean isTableCreated(TAPParameters tapParams){
		if(isXMatch(tapParams)){
			return true;
		}
		return false;
	}
	
	private boolean isXMatch(TAPParameters tapParams) {
		if(tapParams.getQuery().indexOf("crossmatch_positional")>=0){
			return true;
		}
		return false;
	}

	protected long getSuitableNumberOfRows(long numberOfRows, TAPParameters tapParams, ResultSet queryResult){
		return numberOfRows;
//		if (isXMatch(tapParams)) {
//			try {
//				result.setRows(queryResult.getLong(2));
//			} catch (SQLException e) {
//				result.setRows(0);
//				logger.error("Cannot set the number of rows for XMATCH job: "
//						+ job.getJobId(), e);
//			}
//		} else {
//			result.setRows(numberOfRows);
//		}
	}
	
	@Override
	public Object execute(UwsJob job) throws InterruptedException, UwsException {
		job.getParameters().setParameter(EsacTapService.PARAM_SL_LIB_VERSION, ((EsacTapService)service).getSlVersion() );
		return super.execute(job);
	}


}

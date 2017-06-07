package esavo.sl.tap.actions;

import esavo.sl.dd.requests.DDFunctions;
import esavo.tap.TAPService;

public interface EsacTapService extends TAPService {
	
	public static final String PARAM_SL_LIB_VERSION = "lib_sl_version";

	
	public String getCasServerUrlBase();
	public DDFunctions getDataDistribution();
	public String getSlVersion();

}

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
package esavo.sl.tap.actions;

import esavo.sl.dd.requests.DDFunctions;
import esavo.tap.TAPService;

public interface EsacTapService extends TAPService {
	
	public static final String PARAM_SL_LIB_VERSION = "lib_sl_version";

	
	public String getCasServerUrlBase();
	public DDFunctions getDataDistribution();
	public String getSlVersion();

}

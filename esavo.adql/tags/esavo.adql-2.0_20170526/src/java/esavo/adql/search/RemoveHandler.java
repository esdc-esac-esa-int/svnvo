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
package esavo.adql.search;

/*
 * This file is part of ADQLLibrary.
 * 
 * ADQLLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ADQLLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ADQLLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import esavo.adql.query.ADQLObject;

/**
 * <p>In this handler the replacement ADQLObject is always <i>null</i>.
 * It may be interpreted as a removal of the matched ADQL item from its ADQL parent.</p>
 * 
 * <p><b><u>IMPORTANT:</u> It is the responsibility of the object which calls this method to apply the removal !</b></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 11/2010
 */
public abstract class RemoveHandler extends SimpleReplaceHandler {

	public RemoveHandler(){
		super();
	}

	public RemoveHandler(boolean recursive, boolean onlyFirstMatch){
		super(recursive, onlyFirstMatch);
	}

	public RemoveHandler(boolean recursive){
		super(recursive);
	}

	/**
	 * Always returns <i>null</i>. It may be interpreted as a removal of the matched ADQL item from its ADQL parent.
	 * 
	 * <p><b><u>IMPORTANT:</u> It is the responsibility of the object which calls this method to apply the removal !</b></p>
	 * 
	 * @see esavo.adql.search.SimpleReplaceHandler#getReplacer(esavo.adql.query.ADQLObject)
	 */
	@Override
	public ADQLObject getReplacer(ADQLObject objToReplace){
		return null;
	}

}

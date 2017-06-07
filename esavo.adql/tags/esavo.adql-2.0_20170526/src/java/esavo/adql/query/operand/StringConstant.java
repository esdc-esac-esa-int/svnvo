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
package esavo.adql.query.operand;

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

import esavo.adql.query.ADQLIterator;
import esavo.adql.query.ADQLObject;
import esavo.adql.query.NullADQLIterator;

/**
 * A string constant.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2011
 */
public final class StringConstant implements ADQLOperand {

	private String value;

	public StringConstant(String value){
		this.value = value;
	}

	public StringConstant(StringConstant toCopy){
		this.value = toCopy.value;
	}

	public final String getValue(){
		return value;
	}

	public final void setValue(String value){
		this.value = value;
	}

	public final boolean isNumeric(){
		return false;
	}

	public final boolean isString(){
		return true;
	}

	public ADQLObject getCopy(){
		return new StringConstant(this);
	}

	public String getName(){
		return "'" + value + "'";
	}

	public ADQLIterator adqlIterator(){
		return new NullADQLIterator();
	}

	public String toADQL(){
		return "'" + value + "'";
	}

}

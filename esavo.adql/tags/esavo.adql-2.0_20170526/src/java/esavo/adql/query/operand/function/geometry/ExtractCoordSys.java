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
package esavo.adql.query.operand.function.geometry;

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
import esavo.adql.query.operand.ADQLColumn;
import esavo.adql.query.operand.ADQLOperand;


/**
 * <p>It represents the COORDSYS function the ADQL language.</p>
 * 
 * <p>This function extracts the coordinate system string value from a given geometry.</p>
 * 
 * <p><i><u>Example:</u>
 * <p>COORDSYS(POINT('ICRS GEOCENTER', 25.0, -19.5))
 * <p>In this example the function extracts the coordinate system of a point with position (25, -19.5) in degrees according to the ICRS coordinate
 * system with GEOCENTER reference position.
 * </i>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2011
 */
public class ExtractCoordSys extends GeometryFunction {

	/** The geometry from which the coordinate system string must be extracted. */
	protected GeometryValue<GeometryFunction> geomExpr;

	/**
	 * Builds a COORDSYS function.
	 * 
	 * @param param	The geometry from which the coordinate system string must be extracted.
	 */
	public ExtractCoordSys(GeometryValue<GeometryFunction> param){
		super();
		geomExpr = param;
	}

	/**
	 * Builds a COORDSYS function by copying the given one.
	 * 
	 * @param toCopy		The COORDSYS function to copy.
	 * @throws Exception	If there is an error during the copy.
	 */
	@SuppressWarnings("unchecked")
	public ExtractCoordSys(ExtractCoordSys toCopy) throws Exception{
		super();
		geomExpr = (GeometryValue<GeometryFunction>)(toCopy.geomExpr.getCopy());
	}

	public ADQLObject getCopy() throws Exception{
		return new ExtractCoordSys(this);
	}

	public String getName(){
		return "COORDSYS";
	}

	public boolean isNumeric(){
		return false;
	}

	public boolean isString(){
		return true;
	}

	@Override
	public ADQLOperand[] getParameters(){
		return new ADQLOperand[]{geomExpr.getValue()};
	}

	@Override
	public int getNbParameters(){
		return 1;
	}

	@Override
	public ADQLOperand getParameter(int index) throws ArrayIndexOutOfBoundsException{
		if (index == 0)
			return geomExpr.getValue();
		else
			throw new ArrayIndexOutOfBoundsException("No " + index + "-th parameter for the function " + getName() + " !");
	}

	@SuppressWarnings("unchecked")
	@Override
	public ADQLOperand setParameter(int index, ADQLOperand replacer) throws ArrayIndexOutOfBoundsException, NullPointerException, Exception{
		if (index == 0){
			ADQLOperand replaced = geomExpr.getValue();
			if (replacer == null)
				throw new NullPointerException("Impossible to remove the only required parameter of the " + getName() + " function !");
			else if (replacer instanceof GeometryValue)
				geomExpr = (GeometryValue<GeometryFunction>)replacer;
			else if (replacer instanceof ADQLColumn)
				geomExpr.setColumn((ADQLColumn)replacer);
			else if (replacer instanceof GeometryFunction)
				geomExpr.setGeometry((GeometryFunction)replacer);
			else
				throw new Exception("Impossible to replace GeometryValue/Column/GeometryFunction by a " + replacer.getClass().getName() + " (" + replacer.toADQL() + ") !");
			return replaced;
		}else
			throw new ArrayIndexOutOfBoundsException("No " + index + "-th parameter for the function " + getName() + " !");
	}

}

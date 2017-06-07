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
 * Copyright 2016 - ESAC Science Data Center (ESDC, ESA)
 *
 */

import esavo.adql.query.ADQLObject;
import esavo.adql.query.operand.ADQLOperand;


/**
 * <p>It represents the ResolveTargetFunction function of an extended ADQL language.</p>
 * 
 * <p>This function produces a circular region on the sky (a cone in space) centered on the resolved postion
 in Simbad</p>
 * 
 * <p><i><u>Example:</u>
 * <p>RESOLVETARGET('M31')
 * </i></p>
 * 
 * @author Jesus Salgado (ESDC)
 * @version 01/2016
 */
public class ResolveTargetFunction extends GeometryFunction {

	/** The astronomical target of the center position. */
	private ADQLOperand target;

	/**
	 * Builds a ResolveTarget function.
	 * 
	 * @param thisTarget			The astronomical target to be resolved
	 * @throws NullPointerException	If at least one parameter is incorrect or if the coordinate system is unknown.
	 * @throws Exception 			If there is another error.
	 */
	public ResolveTargetFunction(ADQLOperand thisTarget) throws NullPointerException, Exception{

		if (thisTarget == null)
			throw new NullPointerException("The target name must be different from NULL !");
		if (!thisTarget.isString())
			throw new NullPointerException("The target name must be a string !");

		this.target = thisTarget;
	}

	/**
	 * Builds a ResolveTarget function by copying the given one.
	 * 
	 * @param toCopy		The ResolveTarget function to copy.
	 * @throws Exception	If there is an error during the copy.
	 */
	public ResolveTargetFunction(ResolveTargetFunction toCopy) throws Exception{
		this.target = (ADQLOperand) (toCopy.target.getCopy());
	}

	public ADQLObject getCopy() throws Exception{
		return new ResolveTargetFunction(this);
	}

	public String getName(){
		return "RESOLVETARGET";
	}

	public boolean isNumeric(){
		return false;
	}

	public boolean isString(){
		return true;
	}

	/**
	 * Gets the astronomical target of the center.
	 * 
	 * @return The astronomical target
	 */
	public final ADQLOperand getTarget(){
		return target;
	}

	/**
	 * Sets the astronomical target of the center.
	 * 
	 * @param target Target.
	 */
	public final void setTarget(ADQLOperand target){
		this.target = target;
	}

	@Override
	public ADQLOperand[] getParameters(){
		return new ADQLOperand[]{target};
	}

	@Override
	public int getNbParameters(){
		return 1;
	}

	@Override
	public ADQLOperand getParameter(int index) throws ArrayIndexOutOfBoundsException{
		switch(index){
			case 0:
				return target;
			default:
				throw new ArrayIndexOutOfBoundsException("No " + index + "-th parameter for the function \"" + getName() + "\" !");
		}
	}

	@Override
	public ADQLOperand setParameter(int index, ADQLOperand replacer) throws ArrayIndexOutOfBoundsException, NullPointerException, Exception{
		if (replacer == null)
			throw new NullPointerException("Impossible to remove one parameter of a " + getName() + " function !");

		ADQLOperand replaced = null;
		switch(index){
			case 0:
				replaced = target;
				break;
			default:
				throw new ArrayIndexOutOfBoundsException("No " + index + "-th parameter for the function \"" + getName() + "\" !");
		}
		return replaced;
	}

}

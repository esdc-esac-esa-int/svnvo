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
package esavo.adql.query.constraint;

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

import java.util.NoSuchElementException;

import esavo.adql.query.ADQLIterator;
import esavo.adql.query.ADQLObject;
import esavo.adql.query.operand.ADQLOperand;


/**
 * Represents a comparison (numeric or not) between two operands.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2011
 * 
 * @see ComparisonOperator
 */
public class Comparison implements ADQLConstraint {

	/** The left part of the comparison. */
	private ADQLOperand leftOperand;

	/** The comparison symbol. */
	private ComparisonOperator compOperator;

	/** The right part of the comparison. */
	private ADQLOperand rightOperand;

	/**
	 * Creates a comparison between two operands.
	 * 
	 * @param left								The left part.
	 * @param comp								The comparison symbol.
	 * @param right								The right part.
	 * 
	 * @throws NullPointerException				If one of the given parameter is <i>null</i>.
	 * @throws UnsupportedOperationException	If the type of operands and of the operator are incompatibles.
	 * 
	 * @see Comparison#setLeftOperand(ADQLOperand)
	 * @see Comparison#setRightOperand(ADQLOperand)
	 */
	public Comparison(ADQLOperand left, ComparisonOperator comp, ADQLOperand right) throws NullPointerException, UnsupportedOperationException{
		setLeftOperand(left);
		setRightOperand(right);
		setOperation(comp);
	}

	/**
	 * Builds a comparison by copying the given one.
	 * 
	 * @param toCopy		The comparison to copy.
	 * @throws Exception	If there is an error during the copy.
	 */
	public Comparison(Comparison toCopy) throws Exception{
		leftOperand = (ADQLOperand)toCopy.leftOperand.getCopy();
		compOperator = toCopy.compOperator;
		rightOperand = (ADQLOperand)toCopy.rightOperand.getCopy();
	}

	/**
	 * Gets the left part of the comparison.
	 * 
	 * @return The left operand.
	 */
	public final ADQLOperand getLeftOperand(){
		return leftOperand;
	}

	/**
	 * Changes the left operand of this comparison.
	 * 
	 * @param newLeftOperand					The new left operand.
	 * @throws NullPointerException				If the given operand is <i>null</i>.
	 * @throws UnsupportedOperationException	If the type of the given operand is incompatible with the right operand and/or with the comparison operator.
	 */
	public void setLeftOperand(ADQLOperand newLeftOperand) throws NullPointerException, UnsupportedOperationException{
		if (newLeftOperand == null)
			throw new NullPointerException("Impossible to update the left operand of the comparison (" + toADQL() + ") with a NULL operand !");
		if (rightOperand != null && newLeftOperand.isNumeric() != rightOperand.isNumeric() && newLeftOperand.isString() != rightOperand.isString())
			throw new UnsupportedOperationException("Impossible to update the left operand of the comparison (" + toADQL() + ") with \"" + newLeftOperand.toADQL() + "\" because its type is not compatible with the type of the right operand !");
		if (compOperator != null && newLeftOperand.isNumeric() && (compOperator == ComparisonOperator.LIKE || compOperator == ComparisonOperator.NOTLIKE))
			throw new UnsupportedOperationException("Impossible to update the left operand of the comparison (" + toADQL() + ") with \"" + newLeftOperand.toADQL() + "\" because the comparison operator " + compOperator.toADQL() + " is not applicable on numeric operands !");

		leftOperand = newLeftOperand;
	}

	/**
	 * Gets the comparison symbol.
	 * 
	 * @return The comparison operator.
	 */
	public ComparisonOperator getOperator(){
		return compOperator;
	}

	/**
	 * Changes the type of this operation.
	 * 
	 * @param newOperation						The new operation type.
	 * @throws NullPointerException				If the given type is <i>null</i>.
	 * @throws UnsupportedOperationException	If the given type is incompatible with the two operands.
	 */
	public void setOperation(ComparisonOperator newOperation) throws NullPointerException, UnsupportedOperationException{
		if (newOperation == null)
			throw new NullPointerException("Impossible to update the comparison operator (" + compOperator.toADQL() + ") with a NULL operand !");
		if ((!leftOperand.isString() || !rightOperand.isString()) && (newOperation == ComparisonOperator.LIKE || newOperation == ComparisonOperator.NOTLIKE))
			throw new UnsupportedOperationException("Impossible to update the comparison operator" + ((compOperator != null) ? (" (" + compOperator.toADQL() + ")") : "") + " by " + newOperation.toADQL() + " because the two operands (\"" + leftOperand.toADQL() + "\" & \"" + rightOperand.toADQL() + "\") are not all Strings !");

		compOperator = newOperation;
	}

	/**
	 * Gets the right part of the comparison.
	 * 
	 * @return The right operand.
	 */
	public ADQLOperand getRightOperand(){
		return rightOperand;
	}

	/**
	 * Changes the right operand of this comparison.
	 * 
	 * @param newRightOperand					The new right operand.
	 * @throws NullPointerException				If the given operand is <i>null</i>.
	 * @throws UnsupportedOperationException	If the type of the given operand is incompatible with the left operand and/or with the comparison operator.
	 */
	public void setRightOperand(ADQLOperand newRightOperand) throws NullPointerException, UnsupportedOperationException{
		if (newRightOperand == null)
			throw new NullPointerException("Impossible to update the right operand of the comparison (" + toADQL() + ") with a NULL operand !");
		if (leftOperand != null && newRightOperand.isNumeric() != leftOperand.isNumeric() && newRightOperand.isString() != leftOperand.isString())
			throw new UnsupportedOperationException("Impossible to update the right operand of the comparison (" + toADQL() + ") with \"" + newRightOperand.toADQL() + "\" because its type is not compatible with the type of the left operand !");
		if (compOperator != null && newRightOperand.isNumeric() && (compOperator == ComparisonOperator.LIKE || compOperator == ComparisonOperator.NOTLIKE))
			throw new UnsupportedOperationException("Impossible to update the right operand of the comparison (" + toADQL() + ") with \"" + newRightOperand.toADQL() + "\" because the comparison operator " + compOperator.toADQL() + " is not applicable on numeric operands !");

		rightOperand = newRightOperand;
	}

	public ADQLObject getCopy() throws Exception{
		return new Comparison(this);
	}

	public String getName(){
		return compOperator.toADQL();
	}

	public ADQLIterator adqlIterator(){
		return new ADQLIterator(){

			private int index = -1;

			public ADQLObject next(){
				index++;
				if (index == 0)
					return leftOperand;
				else if (index == 1)
					return rightOperand;
				else
					throw new NoSuchElementException();
			}

			public boolean hasNext(){
				return index + 1 < 2;
			}

			public void replace(ADQLObject replacer) throws UnsupportedOperationException, IllegalStateException{
				if (index <= -1)
					throw new IllegalStateException("replace(ADQLObject) impossible: next() has not yet been called !");

				if (replacer == null)
					remove();
				else if (replacer instanceof ADQLOperand){
					if (index == 0)
						leftOperand = (ADQLOperand)replacer;
					else if (index == 1)
						rightOperand = (ADQLOperand)replacer;
				}else
					throw new UnsupportedOperationException("Impossible to replace an ADQLOperand by a " + replacer.getClass().getName() + " in a comparison !");
			}

			public void remove(){
				if (index <= -1)
					throw new IllegalStateException("remove() impossible: next() has not yet been called !");
				else
					throw new UnsupportedOperationException("Impossible to remove an operand from a comparison !");
			}
		};
	}

	public String toADQL(){
		return ((leftOperand == null) ? "NULL" : leftOperand.toADQL()) + " " + ((compOperator == null) ? "NULL" : compOperator.toADQL()) + " " + ((rightOperand == null) ? "NULL" : rightOperand.toADQL());
	}

}

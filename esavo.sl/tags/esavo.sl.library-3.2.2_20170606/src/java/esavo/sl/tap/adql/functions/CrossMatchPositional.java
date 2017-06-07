package esavo.sl.tap.adql.functions;

import java.util.Arrays;

import esavo.adql.query.ADQLObject;
import esavo.adql.query.operand.ADQLOperand;
import esavo.adql.query.operand.StringConstant;
import esavo.adql.query.operand.function.UserDefinedFunction;
import esavo.tap.metadata.TAPMetadata;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

public class CrossMatchPositional extends UserDefinedFunction{
	private String name;
	private ADQLOperand[] params;
	
	public CrossMatchPositional(String name, ADQLOperand[] params, UwsJobOwner owner) throws UwsException{
		
		if(params.length!= 6) 
			throw new UnsupportedOperationException("Incorrect number of parameters for this function");
		
		if(owner==null){
			throw new IllegalArgumentException("Owner is null");
		}else if (owner.getId() == null){
			throw new IllegalArgumentException("AuthUsername is null");
		}
		
		String userSchema = TAPMetadata.getUserSchema(owner.getId());
		
		//Add parameters and interchange to match DB function signature
		this.params = Arrays.copyOf(params, 7);		
		this.params[6]= this.params[5];
		this.params[5]= new StringConstant(userSchema);
		
		this.name = name;
	}

	@Override
	public boolean isNumeric() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isString() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ADQLObject getCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNbParameters() {
		// TODO Auto-generated method stub
		return params.length;
	}

	@Override
	public ADQLOperand[] getParameters() {
		return params;
	}

	@Override
	public ADQLOperand getParameter(int index)
			throws ArrayIndexOutOfBoundsException {
		return params[index];
	}

	@Override
	public ADQLOperand setParameter(int index, ADQLOperand replacer)
			throws ArrayIndexOutOfBoundsException, NullPointerException,
			Exception {
		return params[index] = replacer;
	}

}

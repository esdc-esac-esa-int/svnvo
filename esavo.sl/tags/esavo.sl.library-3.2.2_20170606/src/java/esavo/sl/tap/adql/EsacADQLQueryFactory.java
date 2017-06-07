package esavo.sl.tap.adql;

import esavo.sl.tap.adql.functions.CrossMatchPositional;
import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.query.operand.ADQLOperand;
import esavo.adql.query.operand.function.UserDefinedFunction;
import esavo.uws.owner.UwsJobOwner;

public class EsacADQLQueryFactory extends ADQLQueryFactory{
	
	UwsJobOwner owner = null;
	
	public EsacADQLQueryFactory(UwsJobOwner owner){
		//Allow user (ESAC) defined functions
		super(true);
		this.owner=owner;
	}
	
	/**
	 * <p>Creates the user defined functions called as the given name and with the given parameters.</p>
	 * @param name			Name of the user defined function to create.
	 * @param params		Parameters of the user defined function to create.
	 * 
	 * @return				The corresponding user defined function.
	 * 
	 * @throws Exception	An {@link UnsupportedOperationException} by default, otherwise any other type of error may be
	 * 						thrown if there is a problem while creating the function.
	 */
	@Override
	public UserDefinedFunction createUserDefinedFunction(String name, ADQLOperand[] params) throws Exception {
		if (allowUnknownFunctions) {
			//if("tap_schema.crossmatch_positional".equalsIgnoreCase(name)){
			String id = name.toLowerCase();
			if(id.matches(".*\\.{0,1}crossmatch_positional")){
				return new CrossMatchPositional(name, params, this.owner);
			}
		}
		throw new UnsupportedOperationException("No ADQL function called \""+name+"\" !");

	}
	
	
	//TODO TEST HARNESS
	
//	public static boolean test(String id){
//		if(id.matches(".*\\.{0,1}crossmatch_positional")){
//			System.out.println("OK");
//			return true;
//		}else{
//			System.out.println("No OK");
//			return false;
//		}
//	}
//	
//	public static void main(String[] args){
//		test("tap_schema.crossmatch_positional(x,y,z)");
//		test("tap_schemacrossmatch_positional");
//		test("tap_schema.crossmatch_positional");
//		test("crossmatch_positional");
//	}


}

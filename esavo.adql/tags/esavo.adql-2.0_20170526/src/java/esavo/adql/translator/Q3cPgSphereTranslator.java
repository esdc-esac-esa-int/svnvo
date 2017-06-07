package esavo.adql.translator;

import esavo.adql.query.operand.ADQLColumn;
import esavo.adql.query.operand.function.geometry.BoxFunction;
import esavo.adql.query.operand.function.geometry.CircleFunction;
import esavo.adql.query.operand.function.geometry.ContainsFunction;
import esavo.adql.query.operand.function.geometry.DistanceFunction;
import esavo.adql.query.operand.function.geometry.IntersectsFunction;
import esavo.adql.query.operand.function.geometry.PointFunction;
import esavo.adql.query.operand.function.geometry.PolygonFunction;
import esavo.adql.query.operand.function.geometry.ResolveTargetFunction;



/**
 * <p>Translates all ADQL objects into the SQL adaptation of Postgres+PgSphere.
 * Actually only the geometrical functions are translated in this class.
 * The other functions are managed by {@link PgSphereTranslator}.</p>
 * 
 * 
 */
public class Q3cPgSphereTranslator extends PgSphereTranslator {

	/**
	 * Builds a PgSphereTranslator which takes into account the case sensitivity on column names.
	 * It means that column names which have been written between double quotes, will be also translated between double quotes.
	 * 
	 * @see PostgreSQLTranslator#PostgreSQLTranslator()
	 */
	public Q3cPgSphereTranslator() {
		super();
	}

	/**
	 * Builds a PgSphereTranslator.
	 * 
	 * @param column	<i>true</i> to take into account the case sensitivity of column names, <i>false</i> otherwise.
	 * 
	 * @see PostgreSQLTranslator#PostgreSQLTranslator(boolean)
	 */
	public Q3cPgSphereTranslator(boolean column) {
		super(column);
	}

	/**
	 * Builds a PgSphereTranslator.
	 * 
	 * @param catalog	<i>true</i> to take into account the case sensitivity of catalog names, <i>false</i> otherwise.
	 * @param schema	<i>true</i> to take into account the case sensitivity of schema names, <i>false</i> otherwise.
	 * @param table		<i>true</i> to take into account the case sensitivity of table names, <i>false</i> otherwise.
	 * @param column	<i>true</i> to take into account the case sensitivity of column names, <i>false</i> otherwise.
	 * 
	 * @see PostgreSQLTranslator#PostgreSQLTranslator(boolean, boolean, boolean, boolean)
	 */
	public Q3cPgSphereTranslator(boolean catalog, boolean schema, boolean table, boolean column) {
		super(catalog, schema, table, column);
	}


	@Override	
	public String translate(ResolveTargetFunction resolveTarget) throws TranslationException {
		StringBuffer str = new StringBuffer("resolveTarget(");
		str.append(translate(resolveTarget.getTarget())).append(")");
		return str.toString();
	}


	@Override
	public String translate(DistanceFunction fct) throws TranslationException {
		// If the operands are Point-Point, translate to Q3c
		if( fct.getP1().getValue() instanceof PointFunction && fct.getP2().getValue() instanceof PointFunction ){
			PointFunction p1 = (PointFunction)fct.getP1().getValue();
			PointFunction p2 = (PointFunction)fct.getP2().getValue();
			String str="q3c_dist("+translate(p1.getCoord1())+","+translate(p1.getCoord2())
					+","+translate(p2.getCoord1())+","+translate(p2.getCoord2())+")";
			return str;
		}

		StringBuffer str = new StringBuffer("degrees(");
		str.append(translate(fct.getP1())).append(" <-> ").append(translate(fct.getP2())).append(")");
		return str.toString();
	}


	@Override
	public String translate(ContainsFunction fct) throws TranslationException {

		if(fct.getLeftParam().getValue() instanceof PointFunction ){
			if(fct.getRightParam().getValue() instanceof CircleFunction){
				// If the operands are Point-Circle, translate to Q3c
				PointFunction point = (PointFunction)fct.getLeftParam().getValue();
				CircleFunction circle = (CircleFunction)fct.getRightParam().getValue();
				String str = "(";


				//Identify if the operator contains columns or values
				if(!(circle.getCoord1() instanceof ADQLColumn) || !(circle.getCoord2() instanceof ADQLColumn)) {
					str+="q3c_join("+translate(circle.getCoord1())+","+translate(circle.getCoord2())
							+","+translate(point.getCoord1())+","+translate(point.getCoord2())+","+translate(circle.getRadius())+")";
					str+=")";					
				} else {
					str+="q3c_join("+translate(point.getCoord1())+","+translate(point.getCoord2())
							+","+translate(circle.getCoord1())+","+translate(circle.getCoord2())+","+translate(circle.getRadius())+")";
					str+=")";
				}

				return str;

			} else if(fct.getRightParam().getValue() instanceof PolygonFunction){
				
				// If the operands are Point-Polygon, translate to Q3c
				PointFunction point = (PointFunction)fct.getLeftParam().getValue();
				PolygonFunction polygon = (PolygonFunction)fct.getRightParam().getValue();

				String strPoly="";
				try{
					strPoly = "'{";
					if (polygon.getNbParameters() > 2){
						PointFunction pointPoly = new PointFunction(polygon.getCoordinateSystem(), polygon.getParameter(1), polygon.getParameter(2));
						strPoly+=translate(pointPoly.getCoord1())+","+translate(pointPoly.getCoord2());

						for(int i=3; i<polygon.getNbParameters() && i+1<polygon.getNbParameters(); i+=2){
							pointPoly.setCoord1(polygon.getParameter(i));
							pointPoly.setCoord2(polygon.getParameter(i+1));
							strPoly+=","+translate(pointPoly.getCoord1())+","+translate(pointPoly.getCoord2());
						}
					}

					strPoly+="}'";
				} catch (Exception e) {
					e.printStackTrace();
					throw new TranslationException(e);
				}

				String str = "(";
				str+="q3c_poly_query("+translate(point.getCoord1())+","+translate(point.getCoord2())
						+","+strPoly+")";
				str+=")";

				return str;
				
			} else if(fct.getRightParam().getValue() instanceof BoxFunction){
				
				PointFunction point = (PointFunction)fct.getLeftParam().getValue();
				BoxFunction 	box = (BoxFunction) fct.getRightParam().getValue();
				
				try {
					double coord1 = new Double(translate(box.getCoord1())).doubleValue();
					double coord2 = new Double(translate(box.getCoord2())).doubleValue();
					double height = new Double(translate(box.getHeight())).doubleValue();
					double width = new Double(translate(box.getWidth())).doubleValue();
					
					double maxRa = coord1 + width/2.;
					double minRa = coord1 - width/2.;
					
					double maxDec = coord2 + height/2.;
					double minDec = coord2 - height/2.;
					
					if(maxDec > 90) maxDec = 90.;
					if(minDec < -90) minDec = -90.;
					
					boolean inverseSearch = false;
					if(maxRa > 360) {
						maxRa = maxRa - 360;
						inverseSearch = true;
					}
					if(minRa < 0) {
						minRa = minRa + 360;
						inverseSearch = true;
					}
					if(inverseSearch) {
						double oldMinRa = minRa;
						minRa = maxRa;
						maxRa = oldMinRa;
					}
					
					
					String declinationConstraint = "(" + translate(point.getCoord2()) + "<" + maxDec + 
												   " AND " + 
												   translate(point.getCoord2()) + ">" + minDec + ")";
					
					String rightAscensionConstraint = "(" + translate(point.getCoord1()) + "<" + maxRa + 
							   " AND " + 
							   translate(point.getCoord1()) + ">" + minRa + ")";
					
					if(inverseSearch)  rightAscensionConstraint = "(NOT " + rightAscensionConstraint + ")";
					
					String str = "( " + rightAscensionConstraint + " AND " + declinationConstraint + " )";
					return str;		
							
				} catch(Exception e) {
					//All these conversions for box are not working properly (because these are not simple
					//numbers). It will do a normal pgsphere conversion
					//Exception will be raised and the final normal pgsphere conversion will be executed
				}
						
			}		
		} 
			
		
		if( fct.getLeftParam().getValue() instanceof CircleFunction ){
			if(fct.getRightParam().getValue() instanceof CircleFunction ){

				CircleFunction circleContained = (CircleFunction)fct.getLeftParam().getValue();
				CircleFunction circleContainer = (CircleFunction)fct.getRightParam().getValue();

				String str = "(";

				//Identify if the operator contains columns or values
				if(!(circleContainer.getCoord1() instanceof ADQLColumn) || !(circleContainer.getCoord2() instanceof ADQLColumn)) {
					str+="q3c_join("+translate(circleContainer.getCoord1())+","+translate(circleContainer.getCoord2())
							+","+translate(circleContained.getCoord1())+","+translate(circleContained.getCoord2())+ "," + 
							"(" + translate(circleContainer.getRadius()) + "-" + translate(circleContained.getRadius()) + ")"+")";
					str+=")";
				} else {
					str+="q3c_join("+translate(circleContained.getCoord1())+","+translate(circleContained.getCoord2())
							+","+translate(circleContainer.getCoord1())+","+translate(circleContainer.getCoord2())+ "," + 
							"(" + translate(circleContainer.getRadius()) + "-" + translate(circleContained.getRadius()) + ")"+")";
					str+=")";
				}

				return str;

			} 
		}


		StringBuffer str = new StringBuffer("(");
		str.append(translate(fct.getLeftParam())).append(" @ ").append(translate(fct.getRightParam())).append(")");
		return str.toString();
	}

	@Override
	public String translate(IntersectsFunction fct) throws TranslationException {

		boolean pointAndCircle 	= false;
		PointFunction point		= null;
		CircleFunction circle 	= null;
		
		//Checking if we have a point and a Circle
		if( fct.getLeftParam().getValue() instanceof PointFunction &&
							fct.getRightParam().getValue() instanceof CircleFunction ){
				point 	= (PointFunction)fct.getLeftParam().getValue();
				circle 	= (CircleFunction)fct.getRightParam().getValue();
				
				pointAndCircle = true;
		}
		if( fct.getLeftParam().getValue() instanceof CircleFunction &&
							fct.getRightParam().getValue() instanceof PointFunction ){

				point = (PointFunction)fct.getRightParam().getValue();
				circle = (CircleFunction)fct.getLeftParam().getValue();
				
				pointAndCircle = true;
		}
		//If this is the case, create q3c join that is faster
		if(pointAndCircle) {
			
			String str = "(";
			if(!(circle.getCoord1() instanceof ADQLColumn) || !(circle.getCoord2() instanceof ADQLColumn)) {
				str+="q3c_join("+translate(circle.getCoord1())+","+translate(circle.getCoord2())
						+","+translate(point.getCoord1())+","+translate(point.getCoord2())+","+translate(circle.getRadius())+")";
				str+=")";
			} else {
				str+="q3c_join("+translate(point.getCoord1())+","+translate(point.getCoord2())
						+","+translate(circle.getCoord1())+","+translate(circle.getCoord2())+","+translate(circle.getRadius())+")";
				str+=")";
			}

			return str;
		}
				
		
		StringBuffer str = new StringBuffer("(");
		str.append(translate(fct.getLeftParam())).append(" && ").append(translate(fct.getRightParam())).append(")");
		return str.toString();
	}

}

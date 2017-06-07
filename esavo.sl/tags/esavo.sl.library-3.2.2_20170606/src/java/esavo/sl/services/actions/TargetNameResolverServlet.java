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
package esavo.sl.services.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import esac.archive.absi.interfaces.common.model.exceptions.RemoteServiceException;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.EquatorialCoordinates;
import esac.archive.absi.modules.common.skycoords.AngleUnit;
import esac.archive.absi.modules.sl.targetresolver.TargetResolver.ResolverType;
import esavo.sl.services.nameresolution.service.cmd.ResolveTargetCommand;
import esavo.sl.services.nameresolution.util.geometry.GeometryUtils;

/**
 * 
 * This servlet provides the coordinates for a given target name.
 * It acts as a wrapper over the <code>ResolveTargetCommand<code>.
 * 
 * In order to invoke it, there is one mandatory param (TARGET_NAME), indicating the 
 * target to resolve and another optional (RESOLVER_TYPE) which specifies which of 
 * these resolvers to use -> Simbad, NED or both.
 * 
 * The output returned to the client is either the coordinates resolved or a 
 * string specifying TARGET NOT FOUND.
 * 
 * The format of the URL is the following:
 * http://\<host\>\:\<port\>/\<context\>/servlet/target-resolver?TARGET_NAME=<target_name>[&RESOLVER_TYPE=Simbad|NED|SN|NS]
 * 
 * And the output when the target is found:
 * 
 * 		RA_DEGREES=<ra>
 * 		DEC_DEGREES=<dec>
 * 		RA_HOURS=<ra_hours>
 * 		<time_taken>
 * 
 * Example of usage:
 * 
 * 		http://xsadev:8080/nxsa-sl/servlet/target-resolver?TARGET_NAME=m31
 * 		http://xsadev:8080/nxsa-sl/servlet/target-resolver?TARGET_NAME=m31&RESOLVER_TYPE=Simbad
 * 		http://xsadev:8080/nxsa-sl/servlet/target-resolver?TARGET_NAME=hip77052&RESOLVER_TYPE=NED
 * 
 * Example of response when the target is found:
 * 
 * 		RA_DEGREES=10.68470833
 * 		DEC_DEGREES=41.26875
 * 		RA_HOURS=0.7123138886666667
 * 		RA_SEXAGESIMAL=00h 42m 44.32s
 * 		DEC_SEXAGESIMAL=+41d 16' 07.49''
 * 		Time taken = 5351 ms
 *  
 * Or if the target is not found:
 * 
 * 		TARGET NOT FOUND
 * 		
 * 
 * @author Nicolas Fajersztejn
 * 
 */
public class TargetNameResolverServlet extends HttpServlet {

	public static final String SERVLET_PARAM_TARGET_NAME = "TARGET_NAME";
	public static final String SERVLET_PARAM_RESOLVER_TYPE = "RESOLVER_TYPE";
	public static final int STATUS_CODE_TARGET_NOT_FOUND = 999;
	public static final int STATUS_CODE_TARGET_RESOLVER_SERVICE_NOT_AVAILABLE = 998;

	
	/** Generated serial Version ID */
	private static final long serialVersionUID = -8355649484351561552L;

	private static Logger logger = Logger.getLogger(TargetNameResolverServlet.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		long t1 = System.currentTimeMillis();
		
		logger.log(Level.INFO, "");
		logger.log(Level.INFO, "");
		logger.log(Level.INFO, "===========================================================================================");
		logger.log(Level.INFO, "Inside TargetNameResolverServlet for [" + request.getQueryString() + "]");
		
		String targetName = request.getParameter(SERVLET_PARAM_TARGET_NAME);
		if (targetName == null)
		{
			String warnMsg = "Param [" + SERVLET_PARAM_TARGET_NAME + "] must be specified in URL";
			logger.log(Level.WARN, warnMsg);
			throw new IOException(warnMsg);
		}
		
		ResolverType resolverType = ResolverType.SIMBAD_NED;
		String resolverTypeString = request.getParameter(SERVLET_PARAM_RESOLVER_TYPE);
		if (resolverTypeString == null)
		{
			// Use default Resolver SIMBAD
			logger.log(Level.INFO, "Resolver Type not defined. Using default: " + resolverType);
		}
		else
		{
			if (resolverTypeString.equalsIgnoreCase("Simbad")) {
				resolverType = ResolverType.SIMBAD;
			}
			else if (resolverTypeString.equalsIgnoreCase("NED")) {
				resolverType = ResolverType.NED;
			}
			else if (resolverTypeString.equalsIgnoreCase(ResolverType.SIMBAD_NED.toString())) {
				resolverType = ResolverType.SIMBAD_NED;
			}
			else if (resolverTypeString.equalsIgnoreCase(ResolverType.NED_SIMBAD.toString())) {
				resolverType = ResolverType.NED_SIMBAD;
			}
			else
			{
				throw new IOException("Undefined ResolverType [" + resolverTypeString + "]. Valid values are: 'Simbad', 'NED', 'SN' or 'NS'");
			}
		}
		
		logger.log(Level.INFO, "[TARGET_NAME, SERVICE] = [" + targetName + "," + resolverType + "]");
		
		EquatorialCoordinates eqCoord = null;
		
		PrintWriter out = response.getWriter();		
		try {
			logger.log(Level.INFO, "Calling Resolver service...");
			ResolveTargetCommand resolveTargetCommand = new ResolveTargetCommand(targetName, resolverType);
			eqCoord = resolveTargetCommand.execute();
			if (eqCoord != null)
			{
				long t2 = System.currentTimeMillis();
				logger.log(Level.INFO, "Coordinates resolved --> " +
						"[RA, DEC] = [" + eqCoord.getRa() + "," + eqCoord.getDec() + "]. Time taken = " + (t2-t1) + " ms");
							 
				Double raDegrees = eqCoord.getRa().getValue() * AngleUnit.convert(AngleUnit.HOURS, AngleUnit.DEGREES);
				logger.log(Level.INFO, "RA in degrees = " + raDegrees);
				
				Double raHours = eqCoord.getRa().getValue();
				Double dec = eqCoord.getDec().getValue();

				String raSexagesimal = GeometryUtils.formatDecimalRa(new BigDecimal(raHours));
				String decSexagesimal = GeometryUtils.formatDecimalDegrees(new BigDecimal(dec));
				
				response.setContentType("text/plain");
				out.println("RA_DEGREES=" + raDegrees);
				out.println("DEC_DEGREES=" + dec);
				out.println("RA_HOURS=" + raHours);
				out.println("RA_SEXAGESIMAL=" + raSexagesimal);
				out.println("DEC_SEXAGESIMAL=" + decSexagesimal);
				out.println("Time taken = " + (t2-t1) + " ms");
			}
			else  // No results found
			{
				logger.log(Level.INFO, "No results found for target name [" + targetName + "]");
				logger.log(Level.INFO, "Setting HTTP Response Status code as [" + STATUS_CODE_TARGET_NOT_FOUND + "]");
				response.setStatus(STATUS_CODE_TARGET_NOT_FOUND);
				out.println("TARGET NOT FOUND");
			}
			
		} catch (RemoteServiceException e) {
			logger.log(Level.ERROR, "Error resolving coordinates", e);
			response.setStatus(STATUS_CODE_TARGET_RESOLVER_SERVICE_NOT_AVAILABLE);
			out.println("Target Resolver Service Not available");
			out.println("-------------------------------------");
			out.println("");
			out.println(e.getGeneratedStackTrace());
			//throw new ServletException(e);
		}
		finally
		{
			// Flush and close the PrintWriter
			try {
				out.flush();
				out.close();
			} catch (Exception e) {
				logger.log(Level.ERROR, "Exception closing PrintWriter", e);			
			}
		}
	}


	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}

	
	

}

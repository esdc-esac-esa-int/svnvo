package esavo.sl.services.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esac.archive.absi.interfaces.common.model.exceptions.RemoteServiceException;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.EquatorialCoordinates;
import esac.archive.absi.modules.common.skycoords.AngleUnit;
import esac.archive.absi.modules.sl.targetresolver.TargetResolver.ResolverType;

import esavo.sl.services.nameresolution.service.cmd.ResolveTargetCommand;

public class TargetListFileUploadServlet  extends AbstractFileUploadServlet {

	private static final long serialVersionUID = 628028460343316955L;
	
	public static final int STATUS_CODE_TARGET_RESOLVER_SERVICE_NOT_AVAILABLE = 998;
	public static final String TARGET_NAME_VALID_PATTERN = "[a-zA-Z0-9][-+\\* .\\w]*[\\w]";
	public static final String TARGET_RESOLVED_FIELD_SEPARATOR = "\t";
	public static final String TARGET_NOT_FOUND_STRING = "TARGET_NOT_FOUND";
	public static final String TARGET_WRONG_FORMAT_STRING = "TARGET_WRONG_FORMAT";


	/** Logger */
	private static Logger logger = Logger.getLogger(TargetListFileUploadServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("");
		logger.debug("=====================================================================");
		logger.debug("Inside TargetListFileUploadServlet.doGet()");
		logger.debug("GET not supported");
		//super.doGet(req, resp);
		return;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		logger.debug("");
		logger.debug("=====================================================================");
		logger.debug("Inside TargetListFileUploadServlet.doPost()");
		
		// Get the user details and log the action
		//UserTO userTO = super.getUserDetails(request);
		//UserUtils.logUserAction(userTO, NxsaDatabaseConstants.USER_ACTION_FILE_SEARCH_TARGET_LIST, request);

		super.doPost(request, response);
		logger.debug("End FileUploadServlet.doPost()");
	}

	
	
	@Override
	protected void performActionWithFileContent(PrintWriter out, Scanner scanner) throws RemoteServiceException {
		// first use a Scanner to get each line
		logger.debug("Processing line by line");
		out.println("");
		int numLine = 0;
		while (scanner.hasNextLine()) {
			numLine++;
			logger.debug("Line number " + numLine);

			String line = scanner.nextLine().trim();
			if (line.isEmpty())
			{
				continue;
			}

			// Check format of target name. If target name does not conform to the format, 
			// then exit with a TARGET_WRONG_FORMAT exception
			Pattern p = Pattern.compile(TARGET_NAME_VALID_PATTERN);
			Matcher m = p.matcher(line);
			if (!m.matches())
			{
				logger.debug("Wrong format in target name: [" + line + "]");
				out.println(TARGET_WRONG_FORMAT_STRING 
				+ TARGET_RESOLVED_FIELD_SEPARATOR + line 
				+ TARGET_RESOLVED_FIELD_SEPARATOR + numLine);
				break;
			}
			else
			{
				// Format is correct. Resolve target
				logger.debug("Resolving:	" + line);
				long t1 = System.currentTimeMillis();
				EquatorialCoordinates eqCoord = resolvetTarget(line);
				if (eqCoord != null) {
					Double raDegrees = eqCoord.getRa().getValue()
							* AngleUnit.convert(AngleUnit.HOURS, AngleUnit.DEGREES);
					Double decDegrees = eqCoord.getDec().getValue();

					long t2 = System.currentTimeMillis();
					long timeTakenMillis = t2 - t1;
					logger.debug("Target resolved in " + timeTakenMillis + " ms");
					out.println(line 
							+ TARGET_RESOLVED_FIELD_SEPARATOR + raDegrees 
							+ TARGET_RESOLVED_FIELD_SEPARATOR + decDegrees 
							+ TARGET_RESOLVED_FIELD_SEPARATOR + timeTakenMillis + "ms");
				} else {
					logger.debug("'" + line + "'	target not FOUND");
					//out = response.getWriter();
					out.println(TARGET_NOT_FOUND_STRING 
							+ TARGET_RESOLVED_FIELD_SEPARATOR + line 
							+ TARGET_RESOLVED_FIELD_SEPARATOR + numLine);
					break;
				}
				// processLine(line);
			}
		}
	}


	private EquatorialCoordinates resolvetTarget(String targetName) throws RemoteServiceException {
		ResolveTargetCommand resolveTargetCommand = new ResolveTargetCommand(targetName, ResolverType.SIMBAD_NED);
		return resolveTargetCommand.execute();
	}

}

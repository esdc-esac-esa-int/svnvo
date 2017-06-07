package esavo.sl.services.nameresolution.service.cmd;

import java.util.Collection;

import esac.archive.absi.interfaces.common.model.exceptions.RemoteServiceException;
import esac.archive.absi.interfaces.common.model.exceptions.RuntimeArchiveNestedException;
import esac.archive.absi.interfaces.sl.requestmanager.cmd.IRequestManagerCommand;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Angle;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Angle.Unit;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.EquatorialCoordinates;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.EquatorialCoordinates.Epoch;
import esac.archive.absi.modules.sl.targetresolver.AstronomicalObject;
import esac.archive.absi.modules.sl.targetresolver.TargetResolver;
import esac.archive.absi.modules.sl.targetresolver.TargetResolver.ResolverType;
import esac.archive.absi.modules.sl.targetresolver.exceptions.EmptyResultException;
import esac.archive.absi.modules.sl.targetresolver.exceptions.InvalidDataException;
import esac.archive.absi.modules.sl.targetresolver.exceptions.NoServicesAvailableException;

/**
 * Command that resolves a target name.
 * 
 * @author Daniel Tapiador - ESA/ESAC - Madrid, Spain
 *
 */
public class ResolveTargetCommand implements IRequestManagerCommand {

	protected String targetName;
	protected ResolverType resolverType = ResolverType.SIMBAD_NED;
	//protected String service;
	
	
	/**
	 * Constructor of the command.
	 * Using default Resolver type <code>ResolverType.SIMBAD_NED</code>
	 * 
	 * @param targetName
	 * @param service
	 */
	public ResolveTargetCommand(String targetName) {
		this.targetName = targetName;
	}
	
	
	/**
	 * Constructor of the command.
	 * 
	 * @param targetName
	 * @param service
	 */
	public ResolveTargetCommand(String targetName, ResolverType resolverType) {
		this.targetName = targetName;
		this.resolverType = resolverType;
	}
	
	/**
	 * Resolves the target name by calling to external resolving services
	 * through target resolver ABSI module.
	 * 
	 * @throws RemoteServiceException Any exception that has occurred
	 * during execution.
	 * 
	 * @return EquatorialCoordinates with the RA in hours and the DEC in
	 * degrees. Epoch is set to J2000.
	 */
	public EquatorialCoordinates execute() throws RemoteServiceException {
		try {
			TargetResolver resolver = new TargetResolver();
/*			ResolverType resolverType;
			if (service.equals(ResolverType.SIMBAD.toString())) {
				resolverType = ResolverType.SIMBAD;
			} else if (service.equals(ResolverType.NED.toString())) {
				resolverType = ResolverType.NED;
			} else {
				// SIMBAD and NED
				resolverType = ResolverType.SIMBAD_NED;
			}
*/			// Epoch set to J2000 by default
			Collection<AstronomicalObject> objects =
				resolver.resolve(targetName, this.resolverType);
			if (objects.size() > 0) {
				// Return first entry
				AstronomicalObject object = objects.iterator().next();
				EquatorialCoordinates coord = new EquatorialCoordinates(
						new Angle(object.getRaInHours(), Unit.HOURS),
						new Angle(object.getDecInDegrees(), Unit.DEGREES),
						Epoch.J2000);
				return coord;
			} else {
				// No objects
				return null;
			}
		} catch (NoServicesAvailableException e) {
			// Encapsulate and throw
			throw new RemoteServiceException(e);
		} catch (InvalidDataException e) {
			/**
			 * It should be an unchecked exception.
			 * TODO: It will have to be refactored in
			 * absi-sl-targetresolver-module.
			 */
			throw new RuntimeArchiveNestedException(e);
		} catch (EmptyResultException e) {
			/**
			 * It should not be an exception.
			 * TODO: It will have to be refactored in
			 * absi-sl-targetresolver-module.
			 */
			return null;
		}
	}
}

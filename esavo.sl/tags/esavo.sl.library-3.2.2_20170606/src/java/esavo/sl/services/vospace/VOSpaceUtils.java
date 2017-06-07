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
package esavo.sl.services.vospace;

import java.text.MessageFormat;

public class VOSpaceUtils {
	
	public static final String GACS_NODE = "GACS";
	public static final String DEFAULT_GACS_NODE_DESCRIPTION = "GACS results";
	
	/**
	 * 0: uri: vos://esavo!vospace/platest/GACS
	 * 1: description (e.g. 'GACS results')
	 */
	static final String GACS_NODE_CREATION_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	"<vos:node xmlns:vos=\"http://www.ivoa.net/xml/VOSpace/v2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"vos:ContainerNode\" uri=\"{0}\">"+
	"<vos:properties>"+
	"<vos:property uri=\"vos://esavo!vospace/properties#date\">24/02/2015 00:00:00</vos:property>"+
	"<vos:property uri=\"vos://esavo!vospace/properties#description\">{1}</vos:property>"+
	"<vos:property uri=\"vos://esavo!vospace/properties#length\">4096</vos:property>"+
	"</vos:properties>"+
	"<vos:nodes/>"+
	"</vos:node>";

	
	
	
	public static String formatNodeCreation(String uri, String description){
		return MessageFormat.format(GACS_NODE_CREATION_DATA, uri, description);
	}
	
}

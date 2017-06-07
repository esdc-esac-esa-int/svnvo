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
package esavo.tap;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.File;

import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsDefaultEventType;
import esavo.uws.event.UwsEventTypesRegistry;
import esavo.uws.factory.UwsDefaultFactory;
import esavo.uws.owner.UwsJobOwner;
import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.parser.QueryChecker;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.formatter.ResultSet2BinaryVotableFormatter;
import esavo.tap.formatter.ResultSet2JsonFormatter;
import esavo.tap.formatter.ResultSet2SVFormatter;
import esavo.tap.formatter.SVFormat;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.upload.Uploader;

public abstract class AbstractTAPFactory extends UwsDefaultFactory implements TAPFactory {

	protected final TAPService service;
	protected UwsJobOwner owner;
	
	private OutputFormat[] formats;


	protected AbstractTAPFactory(TAPService service, String appid, File storageDir, UwsConfiguration configuration) throws NullPointerException {
		super(appid, storageDir, configuration);

		this.service = service;
		
		// List all available outputs (VOTable & CSV):
		formats = new OutputFormat[3];
		//formats[0] = new ResultSet2VotableFormatter(this);
		formats[0] = new ResultSet2BinaryVotableFormatter(service);
		formats[1] = new ResultSet2SVFormatter(service,SVFormat.COMMA_SEPARATOR);
		formats[2] = new ResultSet2JsonFormatter(service);
		
		UwsDefaultEventType uet;
		UwsEventTypesRegistry eventsTypesRegistry = getEventsManager().getEventsTypeRegistry();
		uet = new UwsDefaultEventType(TAPFactory.TABLE_CREATED_EVENT, "Table created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(TAPFactory.TABLE_UPDATED_EVENT, "Table updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(TAPFactory.TABLE_REMOVED_EVENT, "Table removed event");
		eventsTypesRegistry.register(uet);

	}

	@Override
	public ADQLQueryFactory createQueryFactory(UwsJobOwner owner) throws TAPException{
		this.owner = owner;
		return new ADQLQueryFactory();
	}

	@Override
	public QueryChecker createQueryChecker(TAPSchema uploadSchema, UwsJobOwner owner, boolean includeAccessibleSharedItems) throws TAPException {
		return new DBCheckerOnDemand(service, owner, includeAccessibleSharedItems, uploadSchema);
	}

	public Uploader createUploader() throws TAPException {
		return new Uploader(service);
	}
	
	public OutputFormat getOutputFormat(String format){
		for(OutputFormat f : formats){
			if (f.getMimeType().equalsIgnoreCase(format) || f.getShortMimeType().equalsIgnoreCase(format))
				return f;
		}
		return null;

	}
	
	public OutputFormat[] getOutputFormats(){
		return formats;
	}

}

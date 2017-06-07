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

import esavo.uws.factory.UwsFactory;
import esavo.uws.owner.UwsJobOwner;
import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.parser.QueryChecker;
import esavo.adql.translator.ADQLTranslator;
import esavo.tap.db.DBConnection;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.log.TAPLog;
import esavo.tap.metadata.TAPMetadataWriter;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.upload.Uploader;

public interface TAPFactory extends UwsFactory {
	
	//Table related events: 40xx
	public static final int TABLE_CREATED_EVENT = 4000;
	public static final int TABLE_UPDATED_EVENT = 4001;
	public static final int TABLE_REMOVED_EVENT = 4002;
	
	public ADQLQueryFactory createQueryFactory(UwsJobOwner owner) throws TAPException;

	public QueryChecker createQueryChecker(TAPSchema uploadSchema, UwsJobOwner owner, boolean includeAccessibleSharedItems) throws TAPException;

	public ADQLTranslator createADQLTranslator() throws TAPException;

	public DBConnection createDBConnection(final String jobID, final String poolid) throws TAPException;

	public Uploader createUploader() throws TAPException;
	
	public TAPLog getLogger();

	public OutputFormat getOutputFormat(String format);
	
	public TAPMetadataWriter getMetadataWriter(String format);
	
	public OutputFormat[] getOutputFormats();
	
	public void setOutputFormats(OutputFormat[] formats);
	
}

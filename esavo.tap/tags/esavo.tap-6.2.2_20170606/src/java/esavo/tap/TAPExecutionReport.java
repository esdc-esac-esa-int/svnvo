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

import esavo.adql.db.DBColumn;
import esavo.tap.parameters.TAPParameters;

public class TAPExecutionReport {

	public final String jobID;
	public final String listID;
	public final String jobName;
	//public final boolean synchronous;
	public final TAPParameters parameters;

	public String sqlTranslation = null;
	public DBColumn[] resultingColumns = new DBColumn[0];

	protected final long[] durations = new long[]{-1,-1,-1,-1,-1};
	protected long totalDuration = -1;

	public boolean success = false;
	
	private long numberOfResults = 0;

	public TAPExecutionReport(final String jobID, final String listID, final String jobName, final TAPParameters params){
		this.jobID = jobID;
		this.listID = listID;
		this.jobName = jobName;
		parameters = params;
	}

	protected int getIndexDuration(final ExecutionProgression tapProgression){
		switch(tapProgression){
		case UPLOADING:
			return 0;
		case PARSING:
			return 1;
		case TRANSLATING:
			return 2;
		case EXECUTING_SQL:
			return 3;
		case WRITING_RESULT:
			return 4;
		default:
			return -1;
		}
	}

	public final long getDuration(final ExecutionProgression tapProgression){
		int indDuration = getIndexDuration(tapProgression);
		if (indDuration < 0 || indDuration >= durations.length)
			return -1;
		else
			return durations[indDuration];
	}

	public final void setDuration(final ExecutionProgression tapProgression, final long duration){
		int indDuration = getIndexDuration(tapProgression);
		if (indDuration < 0 || indDuration >= durations.length)
			return;
		else
			durations[indDuration] = duration;
	}

	public final long getUploadDuration(){
		return getDuration(ExecutionProgression.UPLOADING);
	}

	public final long getParsingDuration(){
		return getDuration(ExecutionProgression.PARSING);
	}

	public final long getTranslationDuration(){
		return getDuration(ExecutionProgression.TRANSLATING);
	}

	public final long getExecutionDuration(){
		return getDuration(ExecutionProgression.EXECUTING_SQL);
	}

	public final long getFormattingDuration(){
		return getDuration(ExecutionProgression.WRITING_RESULT);
	}

	public final long getTotalDuration(){
		return totalDuration;
	}

	public final void setTotalDuration(final long duration){
		totalDuration = duration;
	}
	
	public final String getJobName(){
		return jobName;
	}
	
	public final String getJobId(){
		return jobID;
	}

	/**
	 * @return the numberOfResults
	 */
	public long getNumberOfResults() {
		return numberOfResults;
	}
	
	/**
	 * Sets the numberOfResults
	 * @param numberOfResults number of results
	 */
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	

}

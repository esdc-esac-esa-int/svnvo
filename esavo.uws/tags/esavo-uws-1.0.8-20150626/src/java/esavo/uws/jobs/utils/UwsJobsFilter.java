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
package esavo.uws.jobs.utils;

public class UwsJobsFilter {
	
	private UwsJobDetailsComparison details;
	private boolean hasFilterByJobId = false;
	private boolean hasFilterByOwnerId = false;
	private boolean hasFilterByPhaseid = false;
	private boolean hasFilterByStartTime = false;
	private boolean hasFilterByEndTime = false;
	private boolean hasFilterByQuery = false;

	private boolean hasFilterByStartTimeLimit = false;
	private boolean hasFilterByEndTimeLimit = false;

	private boolean jobidComparisonLike = false;
	private boolean owneridComparisonLike = false;
	private boolean queryComparisonLike = false;
	private boolean phaseidComparisonLike = false;

	public UwsJobsFilter(){
		details = new UwsJobDetailsComparison();
	}
	
	public UwsJobDetailsComparison getJobFilter(){
		return details;
	}
	
	public void setFilterByJobId(String id, boolean like){
		hasFilterByJobId = true;
		jobidComparisonLike = like;
		details.setJobid(id);
	}

	public void setFilterByOwnerId(String id, boolean like){
		hasFilterByOwnerId = true;
		owneridComparisonLike = like;
		details.setOwnerid(id);
	}

	public void setFilterByPhaseId(String id, boolean like){
		hasFilterByPhaseid = true;
		phaseidComparisonLike = like;
		details.setPhaseid(id);
	}

	public void setFilterByQuery(String query, boolean like){
		hasFilterByQuery = true;
		queryComparisonLike = like;
		details.setQuery(query);
	}

	public void setFilterByStartTime(long time){
		hasFilterByStartTime = true;
		details.setStartTime(time);
	}

	public void setFilterByEndTime(long time){
		hasFilterByEndTime = true;
		details.setEndTime(time);
	}

	public void setFilterByStartTimeLimit(long time){
		hasFilterByStartTimeLimit = true;
		details.setStartTimeLimit(time);
	}

	public void setFilterByEndTimeLimit(long time){
		hasFilterByEndTimeLimit = true;
		details.setEndTimeLimit(time);
	}

	/**
	 * @return the hasFilterByJobId
	 */
	public boolean hasFilterByJobId() {
		return hasFilterByJobId;
	}

	/**
	 * @return the hasFilterByOwnerId
	 */
	public boolean hasFilterByOwnerId() {
		return hasFilterByOwnerId;
	}

	/**
	 * @return the hasFilterByPhaseid
	 */
	public boolean hasFilterByPhaseid() {
		return hasFilterByPhaseid;
	}

	/**
	 * @return the hasFilterByStartTime
	 */
	public boolean hasFilterByStartTime() {
		return hasFilterByStartTime;
	}

	/**
	 * @return the hasFilterByEndTime
	 */
	public boolean hasFilterByEndTime() {
		return hasFilterByEndTime;
	}

	/**
	 * @return the hasFilterByStartTimeLimit
	 */
	public boolean hasFilterByStartTimeLimit() {
		return hasFilterByStartTimeLimit;
	}

	/**
	 * @return the hasFilterByEndTimeLimit
	 */
	public boolean hasFilterByEndTimeLimit() {
		return hasFilterByEndTimeLimit;
	}

	/**
	 * @return the hasFilterByQuery
	 */
	public boolean hasFilterByQuery() {
		return hasFilterByQuery;
	}

//	/**
//	 * @return the cmpStartTime
//	 */
//	public Comparison getCmpStartTime() {
//		return cmpStartTime;
//	}
//
//	/**
//	 * @return the cmpEndTime
//	 */
//	public Comparison getCmpEndTime() {
//		return cmpEndTime;
//	}

	/**
	 * @return the jobidComparisonLike
	 */
	public boolean isJobidComparisonLike() {
		return jobidComparisonLike;
	}

	/**
	 * @return the owneridComparisonLike
	 */
	public boolean isOwneridComparisonLike() {
		return owneridComparisonLike;
	}

	/**
	 * @return the queryComparisonLike
	 */
	public boolean isQueryComparisonLike() {
		return queryComparisonLike;
	}

	/**
	 * @return the phaseidComparisonLike
	 */
	public boolean isPhaseidComparisonLike() {
		return phaseidComparisonLike;
	}
	
	@Override
	public String toString(){
		return "Filter: " + details.toString();
	}
	
//	public static Comparison getTranslatedCmp(String cmp){
//		if(cmp == null){
//			return Comparison.Equals;
//		} else if("=".equals(cmp)){
//			return Comparison.Equals;
//		} else if(">".equals(cmp)){
//			return Comparison.GreaterThan;
//		} else if("<".equals(cmp)){
//			return Comparison.LowerThan;
//		} else if(">=".equals(cmp)){
//			return Comparison.GreaterOrEqualsThan;
//		} else if("<=".equals(cmp)){
//			return Comparison.LowerOrEqualsThan;
//		} else {
//			return Comparison.Equals;
//		}
//	}

}

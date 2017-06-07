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
package esavo.uws.scheduler;

import org.junit.Assert;
import org.mockito.Mockito;
import org.junit.Test;

import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobInitArgs;
import esavo.uws.owner.UwsJobOwner;

public class UwsDefaultSchedulerTest {

	@Test
	public void testAddJob() {
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid("__TEST__");
		args.setOwner(UwsJobOwner.ANONYMOUS_OWNER);
		args.setPriority(2);
		UwsJob job = new UwsJob(args);
	
		
		
	
	}

}

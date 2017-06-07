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
package esavo.sl.test.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadData {
	
	/**
	 * Returns an inputstream to the resource to be loaded through the object 'loader'
	 * @param loader object to be used to obtain a classloader
	 * @param resource resource to find
	 * @return
	 */
	public static InputStream findResource(Class<?> loader, String resource){
		//This works for JUnit outside Eclipse.
		InputStream is = loader.getClassLoader().getResourceAsStream(resource);
		if(is == null){
			//In Eclipse, the previous method does not work.
			//In eclipse the following loader works.
			is = ReadData.class.getResourceAsStream(resource);
		}
		return is;
	}

	/**
	 * Reads the first line of the specified resource.
	 * @param loader object to be used to obtain a classloader
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static String readFirstLine(Class<?> loader, String resource) throws IOException {
		//Problems with JUnit
		//InputStream is = ReadData.class.getClass().getResourceAsStream(resource);
		InputStream is = findResource(loader, resource);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		try{
			return br.readLine();
		}finally{
			try{
				br.close();
			}catch(Exception e){
				
			}
			try{
				isr.close();
			}catch(Exception e){
				
			}
			try{
				is.close();
			}catch(Exception e){
				
			}
		}

	}
	
	
	/**
	 * Returns the size (in bytes) of the specified resource.
	 * @param loader object to be used to obtain a classloader
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static long getContentLength(Class<?> loader, String resource) throws IOException {
		//Problems with JUnit
		//InputStream is = ReadData.class.getClass().getResourceAsStream(resource);
		InputStream is = findResource(loader, resource);
		long counter = 0;
		try{
			while(is.read() != -1){
				counter++;
			}
		} finally {
			try{
				is.close();
			}catch(Exception e){
			}
		}
		return counter;
	}

	/**
	 * Loads a text resource. 'resource' can be relative (to this class) or absolute
	 * @param loader object to be used to obtain a classloader
	 * @param resource
	 * @return
	 * @throws IOException 
	 */
	public static String readDataTextAsResource(Class<?> loader, String resource) throws IOException{
		return readDataTextAsResource(loader, resource, false);
	}

	
	/**
	 * Loads a text resource. 'resource' can be relative (to this class) or absolute
	 * @param loader object to be used to obtain a classloader
	 * @param resource
	 * @param test debug
	 * @return
	 * @throws IOException 
	 */
	public static String readDataTextAsResource(Class<?> loader, String resource, boolean test) throws IOException {
		InputStream is = findResource(loader, resource);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		try{
			StringBuilder sb = new StringBuilder();
			String line;
			if(test){
				System.out.println("******************************start");
			}
			while((line = br.readLine()) != null){
				if(test){
					System.out.println("line: ------" + line + "------");
				}
				sb.append(line).append('\n');
			}
			if(test){
				System.out.println("******************************end");
			}
			return sb.toString();
		}finally{
			try{
				br.close();
			}catch(Exception e){
				
			}
			try{
				isr.close();
			}catch(Exception e){
				
			}
			try{
				is.close();
			}catch(Exception e){
				
			}
		}
	}

	public static String readFileAndRemoveParameter(Class<?> loader, String resource, String parameterToRemove) throws IOException{
		//InputStream is = TestUtils.class.getClass().getResourceAsStream(resource);
		InputStream is = findResource(loader, resource);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> lines = new ArrayList<String>();
		String lineToRead;
		String line;
		String param = "Content-Disposition: form-data; name=\"" + parameterToRemove + "\"";
		boolean remove = false;
		while((lineToRead = br.readLine()) != null){
			line = lineToRead.trim();
			if(remove){
				if(line.startsWith("----------------")){
					remove = false;
					lines.add(line);
				}
			}else{
				if(line.startsWith(param)){
					lines.remove(lines.size()-1); //removes prev boundary
					remove = true;
				}else{
					lines.add(line);
				}
			}
		}
		
		is.close();
		StringBuilder sb = new StringBuilder();
		for(String l: lines){
			sb.append(l).append('\r').append('\n');
		}
		return sb.toString();
	}
	
	
//	/**
//	 * Obtains a File object of a resource. 'resource' can be relative (to this class) or absolute
//	 * @param resource
//	 * @param test debug
//	 * @return
//	 * @throws IOException 
//	 * @throws URISyntaxException 
//	 */
//	public static File getFileAsResource(String resource) throws IOException, URISyntaxException{
//		return new File(ReadData.class.getClass().getResource(resource).toURI());
//		
//	}
}

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
package esavo.sl.dd.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.maxmind.geoip.LookupService;

import esavo.uws.output.UwsOutputUtils;

import org.apache.log4j.Logger;

/**
 * Usage:
 * <pre><tt>
 *      CountryResolverLite countryResolverLite; 
 * 		String resource = configuration.getProperty(DDProperties.GEO_IP_DAT_RESOURCE_NAME);
 *		String geoDstDir = configuration.getProperty(DDProperties.GEO_IP_DAT_DST_DIR);
 *		try {
 *			countryResolverLite = new CountryResolverLite(resource, geoDstDir);
 *		} catch (IOException e) {
 *			e.printStackTrace();
 *			countryResolverLite = null;
 *		}
 *
 *      ...
 *      
 * 		String ip = retrievalRequest.getIp();
 *		String country = null;
 *		if(countryResolverLite != null){
 *			country = countryResolverLite.resolveCountryFromIp(ip);
 *		}
 *
 *
 * </tt></pre>
 * @author jsegovia
 *
 */
public class CountryResolverLite {
    /**
     * Logging variable.
     */
    private static final Logger LOG = Logger.getLogger(CountryResolverLite.class);
    
    private String geoFilePath;


    /**
     * Private constructor.
     * @throws IOException 
     */
    public CountryResolverLite(String resource, String geoDstDir) throws IOException {
    	geoFilePath = dumpResourceIfNeeded(resource, geoDstDir);
        LOG.debug("CountryFile retrieved [" + geoFilePath + "]");
    }

    /**
     * Retrieve country from an ip address string.
     * @param ipAddress Ip address string (like "151.38.39.114")
     * @return Country.
     * @throws IOException
     * @throws Exception If any error is found.
     */
    public String resolveCountryFromIp(String ipAddress) throws IOException {
        LOG.debug("Into CountryResolver.resolveContryFromIp(" + ipAddress + ")");
        if (ipAddress == null) {
            return null;
        }
        File geoFile =  new File(geoFilePath);
        LookupService cl = new LookupService(geoFile, LookupService.GEOIP_MEMORY_CACHE);
        String country = cl.getCountry(ipAddress).getName();
        LOG.debug("Country found [" + country + "] for ip address [" + ipAddress + "]");
        cl.close();
        return country;
    }

    private String dumpResourceIfNeeded(String resource, String geoDstDir) throws IOException{
    	File fTmp = new File(resource);
    	String name = fTmp.getName();
    	File dst = new File(geoDstDir, name);
    	if(dst.exists()){
    		//file already exists
    		//return dst.getAbsolutePath();
    		dst.delete();
    	}
    	//dump content
    	OutputStream os = null;
    	InputStream is = null;
    	try{
    		os = new FileOutputStream(dst);
    		is = getClass().getResourceAsStream(resource);
    		UwsOutputUtils.dumpToStream(is, os);
    	}finally{
    		if(is != null){
    			try{
    				is.close();
    			}catch(IOException ioe){
    				
    			}
    		}
    		if(os != null){
    			try{
    				os.close();
    			}catch(IOException ioe){
    				
    			}
    		}
    	}
		return dst.getAbsolutePath();
    }
}

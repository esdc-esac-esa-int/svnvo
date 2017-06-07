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
package esavo.uws.share.storage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.share.UwsShareUser;

public class UwsLdapHandler {
	
	public static final String DEFAULT_SEARCH_BASE = "ou=People,o=esa.nl";
	
	private static final String LDAP_USER_IDENTIFIER = "uid";
	private static final String LDAP_USER_NAME = "cn";

	private String ldapSearchBase;
    private LdapContext ldapContext;
    
    public UwsLdapHandler(UwsConfiguration config) throws UwsException {
    	try {
			ldapContext = initContext(config);
		} catch (NamingException e) {
			throw new UwsException("Error creating LDAP context", e);
		}
    	ldapSearchBase = config.getProperty(UwsConfiguration.LDAP_SEARCH_BASE);
    	if(ldapSearchBase == null){
    		ldapSearchBase = DEFAULT_SEARCH_BASE;
    	}
    }
    
    private LdapContext initContext(UwsConfiguration config) throws NamingException{
    	Hashtable<String, String> environment = new Hashtable<String, String>();
    	Control[] connCtls = null;
    	String tmp;
    	tmp = config.getProperty(UwsConfiguration.LDAP_SERVER);
    	environment.put(Context.PROVIDER_URL, tmp);
    	environment.put(Context.SECURITY_AUTHENTICATION, "simple");
    	tmp = config.getProperty(UwsConfiguration.LDAP_USERNAME);
    	environment.put(Context.SECURITY_PRINCIPAL, tmp);
    	tmp = config.getProperty(UwsConfiguration.LDAP_PASSWORD);
    	environment.put(Context.SECURITY_CREDENTIALS, tmp);
    	environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	
    	return new InitialLdapContext(environment, connCtls);
    }
    
    public List<UwsShareUser> getUsers(String usersFilter, int maxResults) throws UwsException {
    	String filter;
    	if(usersFilter == null || "".equals(usersFilter)){
    		filter = "("+LDAP_USER_IDENTIFIER+"=*)";
    	} else {
    		//filter = "(|("+LDAP_USER_IDENTIFIER+"="+usersFilter+")("+LDAP_USER_NAME+"="+usersFilter+"))";
    		filter = "("+LDAP_USER_NAME+"="+usersFilter+")";
    		//filter = "(|("+LDAP_USER_NAME+"="+usersFilter+")("+LDAP_USER_IDENTIFIER+"="+usersFilter+"))";
    		if(usersFilter.indexOf('*') < 0){
    			filter = "(|("+LDAP_USER_NAME+"=*"+usersFilter+"*)("+LDAP_USER_IDENTIFIER+"="+usersFilter+"*))";
    		}else{
    			filter = "(|("+LDAP_USER_NAME+"="+usersFilter+")("+LDAP_USER_IDENTIFIER+"="+usersFilter+"))";
    		}
    	}
    	
    	SearchControls controls = new SearchControls();
    	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	//limit to 1000 results:
		if (maxResults >= 0) {
			controls.setCountLimit(maxResults);
		}
        //cons.setTimeLimit(30000);
    	//String[] attrIDs = {"uid","cn","mail","departmentnumber"};
    	String[] attrIDs = {LDAP_USER_IDENTIFIER,LDAP_USER_NAME}; //user id and full name
    	controls.setReturningAttributes(attrIDs);
    	
    	List<UwsShareUser> users = new ArrayList<UwsShareUser>();
    	NamingEnumeration<SearchResult> results;
    	//int counter = 0;
    	try {
			results = ldapContext.search(ldapSearchBase, filter, controls);
			String id;
			String name;
	    	while(results.hasMoreElements()){
	    		SearchResult r = results.next();
	    		//counter++;
	    		id = r.getAttributes().get(LDAP_USER_IDENTIFIER).get().toString();
	    		name = r.getAttributes().get(LDAP_USER_NAME).get().toString();
	    		UwsShareUser usu = new UwsShareUser(id, name);
	    		users.add(usu);
	    	}
		} catch (NamingException e) {
			throw new UwsException("Cannot search for users by filter '"+filter+"'", e);
		}
    	
    	return users;
    }

    
//    public List<UwsShareUser> getUsers2(String usersFilter, int maxResults) throws UwsException {
//    	List<String> filters = new ArrayList<String>();
//    	if(usersFilter == null || "".equals(usersFilter)){
//    		filters.add("("+LDAP_USER_IDENTIFIER+"=*)");
//    	} else {
//    		//filter = "(|("+LDAP_USER_IDENTIFIER+"="+usersFilter+")("+LDAP_USER_NAME+"="+usersFilter+"))";
//    		filters.add("("+LDAP_USER_NAME+"="+usersFilter+")");
//    		filters.add("("+LDAP_USER_IDENTIFIER+"="+usersFilter+")");
//    		//filter = "(|("+LDAP_USER_NAME+"="+usersFilter+")("+LDAP_USER_IDENTIFIER+"="+usersFilter+"))";
//    	}
//    	
//    	SearchControls controls = new SearchControls();
//    	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//    	//limit to 1000 results:
//		if (maxResults >= 0) {
//			controls.setCountLimit(maxResults);
//		}
//        //cons.setTimeLimit(30000);
//    	//String[] attrIDs = {"uid","cn","mail","departmentnumber"};
//    	String[] attrIDs = {LDAP_USER_IDENTIFIER,LDAP_USER_NAME}; //user id and full name
//    	controls.setReturningAttributes(attrIDs);
//    	
//    	List<UwsShareUser> users = new ArrayList<UwsShareUser>();
//    	List<String> idsAdded = new ArrayList<String>();
//    	NamingEnumeration<SearchResult> results;
//    	//int counter = 0;
//		for(String filter: filters){
//	    	try {
//				results = ldapContext.search(ldapSearchBase, filter, controls);
//				String id;
//				String name;
//		    	while(results.hasMoreElements()){
//		    		SearchResult r = results.next();
//		    		//counter++;
//		    		id = r.getAttributes().get(LDAP_USER_IDENTIFIER).get().toString();
//		    		if(idsAdded.contains(id)){
//		    			continue;
//		    		}
//		    		idsAdded.add(id);
//		    		name = r.getAttributes().get(LDAP_USER_NAME).get().toString();
//		    		UwsShareUser usu = new UwsShareUser(id, name);
//		    		users.add(usu);
//		    	}
//			} catch (NamingException e) {
//				throw new UwsException("Cannot search for users by filter '"+filter+"'", e);
//			}
//		}
//    	
//    	return users;
//    }

    public void updateUsers(List<UwsShareUser> users) throws UwsException {
    	if(users == null || users.size() < 1){
    		return;
    	}
    	String filter = createQueryForUsers(users);
    	SearchControls controls = new SearchControls();
    	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	String[] attrIDs = {LDAP_USER_IDENTIFIER,LDAP_USER_NAME}; //user id and full name
    	controls.setReturningAttributes(attrIDs);
    	
    	NamingEnumeration<SearchResult> results;
    	//int counter = 0;
    	try {
			results = ldapContext.search(ldapSearchBase, filter, controls);
			String id;
			String name;
	    	while(results.hasMoreElements()){
	    		SearchResult r = results.next();
	    		//counter++;
	    		id = r.getAttributes().get(LDAP_USER_IDENTIFIER).get().toString();
	    		name = r.getAttributes().get(LDAP_USER_NAME).get().toString();
	    		updateUser(users, id, name);
	    	}
		} catch (NamingException e) {
			throw new UwsException("Cannot search for users by filter '"+filter+"'", e);
		}
    }
    
//    public void updateUser(UwsShareUser user) throws UwsException {
//    	if(user == null){
//    		return;
//    	}
//    	String filter = "(" + LDAP_USER_IDENTIFIER + "=" + user.getId() + ")";
//    	SearchControls controls = new SearchControls();
//    	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//    	String[] attrIDs = {LDAP_USER_IDENTIFIER,LDAP_USER_NAME}; //user id and full name
//    	controls.setReturningAttributes(attrIDs);
//    	
//    	NamingEnumeration<SearchResult> results;
//    	//int counter = 0;
//    	try {
//			results = ldapContext.search(ldapSearchBase, filter, controls);
//			String name;
//	    	while(results.hasMoreElements()){
//	    		SearchResult r = results.next();
//	    		name = r.getAttributes().get(LDAP_USER_NAME).get().toString();
//	    		user.setName(name);
//	    	}
//		} catch (NamingException e) {
//			throw new UwsException("Cannot search for users by filter '"+filter+"'", e);
//		}
//    }
    
	public UwsShareUser getUserDetails(String userid) throws UwsException {
		if (userid == null) {
			return null;
		}
		String filter = "(" + LDAP_USER_IDENTIFIER + "=" + userid + ")";
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = { LDAP_USER_IDENTIFIER, LDAP_USER_NAME }; // user id and full name
		controls.setReturningAttributes(attrIDs);

		NamingEnumeration<SearchResult> results;
		// int counter = 0;
		try {
			results = ldapContext.search(ldapSearchBase, filter, controls);
			if (results.hasMoreElements()) {
				SearchResult r = results.next();
				String name = r.getAttributes().get(LDAP_USER_NAME).get().toString();
				UwsShareUser sharedUser = new UwsShareUser(userid, name);
				return sharedUser;
			} else {
				return null;
			}
		} catch (NamingException e) {
			throw new UwsException("Cannot search for users by filter '" + filter + "'", e);
		}
	}
    
    private String createQueryForUsers(List<UwsShareUser> users){
    	StringBuilder sb = new StringBuilder("(|");
    	for(UwsShareUser su: users){
    		sb.append("(").append(LDAP_USER_IDENTIFIER).append("=").append(su.getId()).append(")");
    	}
    	sb.append(")");
    	return sb.toString();
    }
    
    private boolean updateUser(List<UwsShareUser> users, String id, String name){
    	for(UwsShareUser su: users){
    		if(su.getId().equals(id)){
    			su.setName(name);
    			return true;
    		}
    	}
    	return false;
    }

}

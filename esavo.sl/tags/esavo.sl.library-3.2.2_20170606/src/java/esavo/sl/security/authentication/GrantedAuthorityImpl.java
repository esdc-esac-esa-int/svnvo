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
package esavo.sl.security.authentication;

import org.springframework.security.core.GrantedAuthority;
import java.io.Serializable;
import org.springframework.util.Assert;


/**
* Basic concrete implementation of a {@link GrantedAuthority}.<p>Stores a <code>String</code> representation of an
* authority granted to  the {@link Authentication} object.</p>
*
* @author Ben Alex
* @version $Id: GrantedAuthorityImpl.java 2735 2008-03-16 04:02:55Z benalex $
*/
public class GrantedAuthorityImpl implements GrantedAuthority, Serializable {
   //~ Instance fields ================================================================================================

   private static final long serialVersionUID = 1L;
   private String role;

   //~ Constructors ===================================================================================================

   public GrantedAuthorityImpl(String role) {
       super();
       Assert.hasText(role, "A granted authority textual representation is required");
       this.role = role;
   }

   //~ Methods ========================================================================================================

   public boolean equals(Object obj) {
       if (obj instanceof String) {
           return obj.equals(this.role);
       }

       if (obj instanceof GrantedAuthority) {
           GrantedAuthority attr = (GrantedAuthority) obj;

           return this.role.equals(attr.getAuthority());
       }

       return false;
   }

   public String getAuthority() {
       return this.role;
   }

   public int hashCode() {
       return this.role.hashCode();
   }

   public String toString() {
       return this.role;
   }

	public int compareTo(Object o) {
		if (o != null && o instanceof GrantedAuthority) {
			GrantedAuthority rhs = (GrantedAuthority) o;
			return this.role.compareTo(rhs.getAuthority());
		}
		return -1;
	}
}

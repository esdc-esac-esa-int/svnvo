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

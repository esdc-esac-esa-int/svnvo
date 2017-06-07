package esavo.sl.security;

public interface UserContextService {

	public String getCurrentUser();

	public String getCurrentSessionId();

	//public String getAnonymousUserId();

}
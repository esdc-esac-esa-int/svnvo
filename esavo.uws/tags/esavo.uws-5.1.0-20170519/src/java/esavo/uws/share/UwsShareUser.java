package esavo.uws.share;

public class UwsShareUser {

	private String id;
	private String name;

	public UwsShareUser(String id) {
		this(id, null);
	}

	public UwsShareUser(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return id + ": " + name;
	}

}

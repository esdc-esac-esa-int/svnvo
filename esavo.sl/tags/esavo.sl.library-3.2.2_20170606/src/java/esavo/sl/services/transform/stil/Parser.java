package esavo.sl.services.transform.stil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses Json data.
 * <p>The parser will skip some characters plus the items specified in 
 * {@link #readToken(int, int[])} and {@link #readTokens(int, int, int[])} methods.<br/>
 * The default items to skip are defined by {@link #DEFAULT_SKIP_ITEMS}.
 * </p>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class Parser {
	
	/**
	 * Default items to skip:
	 * <ul>
	 * <li>Blank space<li>
	 * <li>Tab space<li>
	 * <li>Double quote<li>
	 * </ul>
	 */
	public static final int DEFAULT_SKIP_ITEMS[] = {' ', '\t', '"'};
	
	/**
	 * Default space itme list to skip: 
	 * <ul>
	 * <li>Blank space<li>
	 * <li>Tab space<li>
	 * </ul>
	 */
	public static final int ONLY_SPACES_SKIP_ITEMS[] = {' ', '\t'};
	

	private InputStream inputStream;
	private int[] itemsToSkip;
	private boolean endOfStream;
	
	/**
	 * Constructor.
	 * @param inputStream data input stream.
	 */
	public Parser(InputStream inputStream){
		this.inputStream = inputStream;
		this.itemsToSkip = DEFAULT_SKIP_ITEMS;
		this.endOfStream = false;
	}
	
	/**
	 * Returns 'false' if the end of the stream has been reached.
	 * @return 'false' if the end of the stream has been reached.
	 */
	public boolean hasMoreData(){
		return !this.endOfStream;
	}
	
	private void endOfStream(){
		this.endOfStream = true;
	}
	
	/**
	 * Sets the default items to skip. It can be null.
	 * @param itemsToSkip items to skip.
	 */
	public void setItemsToSkip(int[] itemsToSkip){
		this.itemsToSkip = itemsToSkip;
	}
	
	/**
	 * Adds an item to skip
	 * @param item item to skip
	 */
	public void addItemToSkip(int item){
		int[] newArray = new int[itemsToSkip.length+1];
		for(int i = 0; i < itemsToSkip.length; i++){
			newArray[i] = itemsToSkip[i];
		}
		newArray[itemsToSkip.length] = item;
		itemsToSkip = newArray;
	}
	
	
	/**
	 * Parses the stream until the provided item is found.
	 * @param item item to search.
	 * @return 'true' if the item is found. 'false' if the end of the stream was reached without finding the item.
	 * @throws IOException 
	 */
	public boolean skipTo(int item) throws IOException{
		if(!hasMoreData()){
			return false;
		}
		int r;
		while(true){
			r = this.inputStream.read();
			if(r == -1){
				endOfStream();
				return false;
			}
			if(r == item){
				return true;
			}
		}
	}
	
	/**
	 * Reads a list of tokens. 'separator' is the delimiter between tokens. The list is finished when 'endItem' is found.<br/>
	 * It uses {@link #DEFAULT_SKIP_ITEMS} to skip items.<br/>
	 * Default skip items can be modified by using {@link #setItemsToSkip(int[])}<br/>
	 * To add more items to the default list, you may use {@link #readTokens(int, int, int[])}
	 * @param separator items separator.
	 * @param endItem item that specified the end of processing.
	 * @return a list of tokens
	 * @throws IOException
	 */
	public List<String> readTokens(int separator, int endItem) throws IOException{
		return readTokens(separator, endItem, null);
	}
	
	/**
	 * Reads a list of tokens. 'separator' is the delimiter between tokens. The list is finished when 'endItem' is found.<br/>
	 * Default skip items can be modified by using {@link #setItemsToSkip(int[])}
	 * @param separator items separator.
	 * @param endItem item that specified the end of processing.
	 * @param extraItemsToSkip extra items to skip (these items are added to the {@link #DEFAULT_SKIP_ITEMS} list.
	 * @return a list of tokens.
	 * @throws IOException
	 */
	public List<String> readTokens(int separator, int endItem, int[] extraItemsToSkip) throws IOException{
		if(!hasMoreData()){
			return null;
		}
		boolean foundQuotes = false;
		StringBuilder sb = new StringBuilder(); 
		List<String> items = null;
		int r;
		while(true){
			r = this.inputStream.read();
			if(r == -1){
				endOfStream();
				return finishReading(sb, items);
			}
			if(!foundQuotes){
				if(r == '"'){
					foundQuotes = true;
				}
				if(r == endItem){
					return finishReading(sb, items);
				}
				if(r == separator){
					if(items == null){
						 items = new ArrayList<String>();
					}
					items.add(sb.toString());
					sb.setLength(0);
				}
				if(shouldSkip(itemsToSkip, r)){
					continue;
				}
				if(shouldSkip(extraItemsToSkip, r)){
					continue;
				}
			}else{
				if(r == '"'){
					foundQuotes = false;
					if(shouldSkip(itemsToSkip, r)){
						continue;
					}
					if(shouldSkip(extraItemsToSkip, r)){
						continue;
					}
				}
			}
			sb.append(Character.toChars(r));
		}
	}
	
	private List<String> finishReading(StringBuilder sb, List<String> items){
		if(sb.length() > 0){
			if(items == null){
				 items = new ArrayList<String>();
			}
			items.add(sb.toString());
			sb.setLength(0);
		}
		return items;
	}
	
	/**
	 * Reads a token. 'separator' specifies the end of the token.<br/>
	 * Default skip items can be modified by using {@link #setItemsToSkip(int[])}<br/>
	 * To add more items to the default list, you may use {@link #readToken(int, int[])}
	 * @param separator end of this token.
	 * @return a token.
	 * @throws IOException
	 */
	public String readToken(int separator) throws IOException{
		return readToken(separator, itemsToSkip);
	}
	
	/**
	 * Reads a token. 'separator' specifies the end of the token.<br/>
	 * Default skip items can be modified by using {@link #setItemsToSkip(int[])}
	 * @param separator end of this token.
	 * @param extraItemsToSkip extra items to skip (these items are added to the {@link #DEFAULT_SKIP_ITEMS} list.
	 * @return a token.
	 * @throws IOException
	 */
	public String readToken(int separator, int[]extraItemsToSkip) throws IOException{
		if(!hasMoreData()){
			return null;
		}
		StringBuilder sb = new StringBuilder(); 
		int r;
		boolean foundQuotes = false;
		while(true){
			r = this.inputStream.read();
			if(r == -1){
				endOfStream();
				return null;
			}
			if(!foundQuotes){
				if(r == '"'){
					foundQuotes = true;
				}
				if(r == separator){
					break;
				}
				if(shouldSkip(itemsToSkip, r)){
					continue;
				}
				if(shouldSkip(extraItemsToSkip, r)){
					continue;
				}
			}else{
				if(r == '"'){
					foundQuotes = false;
					if(shouldSkip(itemsToSkip, r)){
						continue;
					}
					if(shouldSkip(extraItemsToSkip, r)){
						continue;
					}
				}
			}
			sb.append(Character.toChars(r));
		}
		return sb.toString();
	}
	
	
	/**
	 * Tests whether the item must be skipped.
	 * @param toSkip items to skip.
	 * @param item item to test.
	 * @return 'true' if the item must be skipped.
	 */
	private boolean shouldSkip(int[] toSkip, int item){
		if(toSkip == null){
			return false;
		}
		for(int i: toSkip){
			if(i == item){
				return true;
			}
		}
		return false;
	}

}

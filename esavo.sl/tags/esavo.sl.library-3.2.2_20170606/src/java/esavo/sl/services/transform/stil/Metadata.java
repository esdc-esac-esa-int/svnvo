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
package esavo.sl.services.transform.stil;
import java.util.ArrayList;
import java.util.List;


/**
 * Column data container. 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class Metadata {

	private String name;
	private String dataType;
	private String arraySize;
	private String description;
	private String unit;
	private String ucd;
	private String utype;
	
	/**
	 * Constructor.
	 * @param name column name.
	 * @param dataType column data type.
	 * @param arraySize column data array size.
	 * @param description column description.
	 */
	public Metadata(String name, String dataType, String arraySize, String description, String unit, String ucd, String utype){
		this.name = name;
		this.dataType = dataType;
		this.arraySize = arraySize;
		this.description = description;
		this.unit = unit;
		this.ucd = ucd;
		this.utype = utype;
	}
	
	/**
	 * Returns the column name.
	 * @return the column name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the column name.
	 * @param name column name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the column data type.
	 * @return the column data type.
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * Sets the column data type.
	 * @param dataType column data type.
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * Returns the column array data size.
	 * @return the column array data size.
	 */
	public String getArraySize() {
		return arraySize;
	}

	/**
	 * Sets the column array data size.
	 * @param arraySize column array data size.
	 */
	public void setArraySize(String arraySize) {
		this.arraySize = arraySize;
	}
	
	/**
	 * Returns the column description.
	 * @return the column description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the column description.
	 * @param description column description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getUcd() {
		return ucd;
	}

	public void setUcd(String ucd) {
		this.ucd = ucd;
	}

	public String getUtype() {
		return utype;
	}

	public void setUtype(String utype) {
		this.utype = utype;
	}

	@Override
	public String toString(){
		return "name: " + name + ", datatype: " + dataType + ", arraysize: " + arraySize + ", description: " + description + ", unit: " + unit + ", ucd: " + ucd + ", utype: " + utype;
	}
	
	/**
	 * Creates a list of {@link Metadata} objects based on the provided rows.<br/>
	 * Each row must have the format defined in {@link #parseMetadata(String)}.
	 * @param rows parsed rows.
	 * @return a list of {@link Metadata} objects based on the provided rows.
	 */
	public static List<Metadata> parseMetadata(List<String> rows){
		List<Metadata> metadata = new ArrayList<Metadata>();
		for(String row: rows){
			Metadata m;
			if(row.startsWith(",")){
				m = parseMetadata(row.substring(1));
			}else{
				m = parseMetadata(row);
			}
			metadata.add(m);
		}
		return metadata;
	}

	/**
	 * Creates a {@link Metadata} objects based on the provided item.<br/>
	 * The item must be as follows:
	 * <pre><tt>
	 * name:column_name,description:column_description,datatype:column_data_type,arraysize:data_array_size
	 * </tt></pre>
	 * @param item data to parse.
	 * @return a {@link Metadata} objects based on the provided item.
	 */
	public static Metadata parseMetadata(String item){
		if (item == null){
			throw new IllegalArgumentException("Input data cannot be null");
		}
		String[] items = createItems(item); //item.split(",");
		if(items.length < 3){
			throw new IllegalArgumentException("Invalid input data: found less than 3 items: " + item);
		}
		String name = parseValue(items, "name");
		String dataType = parseValue(items, "datatype");
		String arraySize = parseValue(items, "arraysize");
		String description = parseValue(items, "description");
		String unit = parseValue(items, "unit");
		String ucd = parseValue(items, "ucd");
		String utype = parseValue(items, "utype");
		return new Metadata(name, dataType, arraySize, description, unit, ucd, utype);
	}
	
	/**
	 * Item is:
	 * "name":"col_name", "description":"this is, a desc: of the table", ...
	 * @param item
	 * @return
	 */
	private static String[] createItems(String item){
		//split by ',' not in quotes
		boolean quotesFound = false;
		int counter = 0;
		char c;
		for(int i = 0; i < item.length(); i++){
			c = item.charAt(i);
			if(quotesFound){
				if(c == '"'){
					quotesFound = false;
				}
			}else{
				if(c == '"'){
					quotesFound = true;
				}else{
					if (c == ','){
						counter++;
					}
				}
			}
		}
		if(item.length() > 0){
			counter++; //last item
		}
		String[] items = new String[counter];
		StringBuilder sb = new StringBuilder();
		counter = 0;
		for(int i = 0; i < item.length(); i++){
			c = item.charAt(i);
			if(quotesFound){
				if(c == '"'){
					quotesFound = false;
				}else{
					sb.append(c);
				}
			}else{
				if(c == '"'){
					quotesFound = true;
				}else{
					if (c == ','){
						items[counter] = sb.toString();
						sb.setLength(0);
						counter++;
					}else{
						sb.append(c);
					}
				}
			}
		}
		if(item.length() > 0){
			items[counter] = sb.toString(); //last item
		}
		return items;
	}
	
	private static String parseValue(String[] items, String keyToFind){
		int pos;
		String key;
		for(String item: items){
			pos = item.indexOf(':');
			if(pos < 0){
				throw new IllegalArgumentException("Invalid data: missing ':' separator. Data: '"+item+"'");
			}
			//String[] keyValue = item.split(":");
			key = item.substring(0, pos);
//			if(keyValue.length != 2){
//				throw new IllegalArgumentException("Invalid data: expected 2 items. Found: " + keyValue.length + ". Data: '"+item+"'");
//			}
//			if(keyToFind.equals(keyValue[0])){
//				return keyValue[1];
//			}
			if(keyToFind.equals(key)){
				return item.substring(pos+1);
			}
		}
		return null;
	}
}

package esavo.sl.dd.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class DDRpFilterName implements FilenameFilter
{
	String wildPattern = null;
	
	public DDRpFilterName(String wildPattern) {
		this.wildPattern = wildPattern;
	}
    
    	public boolean accept(File dir, String name) {	
		return Pattern.matches(this.wildPattern,name);		
	}
}

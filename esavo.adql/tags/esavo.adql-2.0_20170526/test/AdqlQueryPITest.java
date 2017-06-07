import static org.junit.Assert.*;

import org.junit.Test;

import esavo.adql.parser.ADQLParser;
import esavo.adql.parser.ParseException;
import esavo.adql.query.ADQLQuery;
import esavo.adql.translator.PostgreSQLTranslator;
import esavo.adql.translator.TranslationException;

public class AdqlQueryPITest {

	@Test
	public void testSelectPI() {

		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery  adqlQuery 	= adqlParser.parseQuery("SELECT pi() FROM dual");
	
			PostgreSQLTranslator translator = new PostgreSQLTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sql);
		assertTrue(sql.equals("SELECT pi() AS PI\nFROM dual"));
	}	

}

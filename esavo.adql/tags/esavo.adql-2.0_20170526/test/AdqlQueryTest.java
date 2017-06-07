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
import static org.junit.Assert.*;

import org.junit.Test;

import esavo.adql.parser.ADQLParser;
import esavo.adql.parser.ParseException;
import esavo.adql.query.ADQLQuery;
import esavo.adql.translator.PostgreSQLTranslator;
import esavo.adql.translator.Q3cPgSphereTranslator;
import esavo.adql.translator.TranslationException;

public class AdqlQueryTest {

	@Test
	public void testSelect() {
		
		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("SELECT a,b,c FROM dual JOIN test on test.id=dual.id GROUP BY a ORDER BY dual.id");
			PostgreSQLTranslator translator = new PostgreSQLTranslator();
			sql = translator.translate(adqlQuery);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(sql.equals("SELECT a AS a , b AS b , c AS c\nFROM dual INNER JOIN test ON test.id = dual.id\nGROUP BY a\nORDER BY dual.id ASC"));
	}
	
	@Test
	public void testSelectMath() {
		
		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("SELECT abs(1), exp(2), sin(3), asin(3), cos(4), acos(4), tan(4), atan(4), round(10,1), sqrt(2)  FROM dual");
			PostgreSQLTranslator translator = new PostgreSQLTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		assertTrue(sql.equals("SELECT abs(1) AS ABS , exp(2) AS EXP , sin(3) AS SIN , asin(3) AS ASIN , cos(4) AS COS , acos(4) AS ACOS , "+ 
		"tan(4) AS TAN , atan(4) AS ATAN , round(10::numeric, 1) AS ROUND , sqrt(2) AS SQRT\nFROM dual"));
	}


	
	@Test
	public void testSelectBOX() {

		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("SELECT * FROM dual WHERE CONTAINS(POINT('ICRS', ra,dec), BOX('ICRS',10,15,2,3))=1");
			Q3cPgSphereTranslator translator = new Q3cPgSphereTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(sql.equals(	"SELECT *\n" +
								"FROM dual\n" +
								"WHERE ( (ra<11.0 AND ra>9.0) AND (dec<16.5 AND dec>13.5) ) = '1'"));
	}		
	
	@Test
	public void testSelectCircle() {

		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("SELECT * FROM dual WHERE CONTAINS(POINT('ICRS', ra,dec), CIRCLE('ICRS',10,15,2))=1");
			Q3cPgSphereTranslator translator = new Q3cPgSphereTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(sql.equals(	"SELECT *\n" +
								"FROM dual\n" +
								"WHERE (q3c_join(10,15,ra,dec,2)) = '1'"));
	}		
	
	@Test
	public void testIntersectsTwoCircles() {

		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("SELECT * FROM dual WHERE INTERSECTS(CIRCLE('ICRS',ra,dec,3), CIRCLE('ICRS',10,15,2))=1");
			Q3cPgSphereTranslator translator = new Q3cPgSphereTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(sql.equals(	"SELECT *\n" +
								"FROM dual\n" +
								"WHERE (scircle(spoint(radians(ra),radians(dec)),radians(3)) && scircle(spoint(radians(10),radians(15)),radians(2))) = '1'"));
	}	
	
	@Test
	public void testAsterics() {

		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("SELECT TOP 10 t.id_tycho, g.* FROM public.tycho2 AS t JOIN public.gaia_hip_tycho2_match AS g ON g.hyp_tyc_oid=t.id_tycho");
			PostgreSQLTranslator translator = new PostgreSQLTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(sql.equals(	"SELECT t.id_tycho AS id_tycho , g.*\n" +
								"FROM public.tycho2 AS t INNER JOIN public.gaia_hip_tycho2_match AS g ON g.hyp_tyc_oid = t.id_tycho\n" +
								"Limit 10"));
	}	

	@Test
	public void testCeiling() {

		String sql = "";
		try {
			
			ADQLParser adqlParser 	= new ADQLParser();
			ADQLQuery adqlQuery;
			adqlQuery 	= adqlParser.parseQuery("select ceiling(0.2) as d, ceiling(0.8) as u from dual");
			PostgreSQLTranslator translator = new PostgreSQLTranslator();
			sql = translator.translate(adqlQuery);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(sql.equals("SELECT ceil(0.2::numeric) AS d , ceil(0.8::numeric) AS u\nFROM dual"));
	}	
		
	
}

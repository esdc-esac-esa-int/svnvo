package esavo.sl.services.nameresolution.util.geometry;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.Test;

import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Angle;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Angle.Unit;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Coordinates2D;
import esac.archive.absi.modules.common.skycoords.AngleUnit;

public class GeometryUtilsTest {
	private static final double DELTA = 1e-9;
	
	@Test
	public void testIsEmpty(){
		Object o = null;
		Assert.assertTrue("Null object", GeometryUtils.isEmpty(o));
		String s = null;
		Assert.assertTrue("Null object", GeometryUtils.isEmpty(s));
		s = "";
		Assert.assertTrue("Null object", GeometryUtils.isEmpty(s));
		Assert.assertTrue("Null object", GeometryUtils.isEmpty((Object)s));
		
		//test-coverage
		GeometryUtils.test();
	}
	
	@Test
	public void testBreakDownDecimalDegrees(){
		String[] expected = {"0","0","0"};
		BigDecimal bd = null;
		String[] actual;
		
		//null bigdecimal
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "null value");
		
		//0.0
		expected = new String[]{"0", "0", "0.0"};
		bd = new BigDecimal(0.0);
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "0.0 value");
		
		//180.0
		bd = new BigDecimal(180.0);
		expected = new String[]{"180", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "180.0 value");
		
		//360.0
		bd = new BigDecimal(360.0);
		expected = new String[]{"360", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "360.0 value");

		//360.0 + 90.0
		bd = new BigDecimal(450.0);
		expected = new String[]{"450", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "450.0 value");
		
		//-180.0
		bd = new BigDecimal(-180.0);
		expected = new String[]{"180", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "-180.0 value");
		
		//-1.0
		bd = new BigDecimal(-1.0);
		expected = new String[]{"1", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalDegrees(bd);
		checkResults(expected, actual, "-1.0 value");		
	}
	
	@Test
	public void testBreakDownDecimalRa(){
		String[] expected = {"0","0","0"};
		BigDecimal bd = null;
		String[] actual;
		
		//null bigdecimal
		actual = GeometryUtils.breakDownDecimalRa(bd);
		checkResults(expected, actual, "null value");
		
		//0.0
		expected = new String[]{"0", "0", "0.0"};
		bd = new BigDecimal(0.0);
		actual = GeometryUtils.breakDownDecimalRa(bd);
		checkResults(expected, actual, "0.0 value");
		
		//180.0
		bd = new BigDecimal(180.0);
		expected = new String[]{"180", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalRa(bd);
		checkResults(expected, actual, "180.0 value");
		
		//360.0
		bd = new BigDecimal(360.0);
		expected = new String[]{"360", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalRa(bd);
		checkResults(expected, actual, "360.0 value");

		//360.0 + 90.0
		bd = new BigDecimal(450.0);
		expected = new String[]{"450", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalRa(bd);
		checkResults(expected, actual, "450.0 value");
		
		//-180.0
		bd = new BigDecimal(-180.0);
		expected = new String[]{"-180", "0", "0.0"};
		actual = GeometryUtils.breakDownDecimalRa(bd);
		checkResults(expected, actual, "-180.0 value");
	}
	
	private void checkResults(String[] expected, String[] actual, String msg){
		if(expected == null){
			Assert.assertNull(msg, actual);
		}
		Assert.assertEquals(msg, expected.length, actual.length);
		for(int i = 0; i < expected.length; i++){
			Assert.assertEquals(msg, expected[i], actual[i]);
		}
	}
	
	@Test
	public void testFormatDecimalDegrees(){
		String actual;
		BigDecimal bd;
		
		//Non BigDecimal
		actual = GeometryUtils.formatDecimalDegrees(null);
		Assert.assertEquals("", actual);
		
		//bd = 180
		bd = new BigDecimal(180.0);
		actual = GeometryUtils.formatDecimalDegrees(bd);
		Assert.assertEquals("+180d 00' 00.00''", actual);
		
		//bd = -180
		bd = new BigDecimal(-180.0);
		actual = GeometryUtils.formatDecimalDegrees(bd);
		Assert.assertEquals("-180d 00' 00.00''", actual);
		
		//bd = -1
		bd = new BigDecimal(-1.0);
		actual = GeometryUtils.formatDecimalDegrees(bd);
		Assert.assertEquals("-01d 00' 00.00''", actual);
	}
	
	@Test
	public void testFormatDecimalRa(){
		String actual;
		BigDecimal bd;
		
		//Non BigDecimal
		actual = GeometryUtils.formatDecimalRa(null);
		Assert.assertEquals("", actual);
		
		//bd = 180
		bd = new BigDecimal(180.0);
		actual = GeometryUtils.formatDecimalRa(bd);
		Assert.assertEquals("180h 00m 00.00s", actual);
		
		//bd = -180
		bd = new BigDecimal(-180.0);
		actual = GeometryUtils.formatDecimalRa(bd);
		Assert.assertEquals("-180h 00m 00.00s", actual);
		
		//bd = -1
		bd = new BigDecimal(-1.0);
		actual = GeometryUtils.formatDecimalRa(bd);
		Assert.assertEquals("-01h 00m 00.00s", actual);
	}
	
	
	@Test
	public void testConvertRaToDecimalHours() throws InvalidFormatException{
		double delta = 1e-9;
		Double expected;
		Double actual;
		Double deg;
		Double min;
		Double sec;

		//Nulls
		Assert.assertNull("Null values", GeometryUtils.convertRaToDecimalHours(null, null, null));
		
		//FormatException
		try{
			GeometryUtils.convertRaToDecimalHours(null, null, "x");
			Assert.fail("Exception expected: invalid format");
		}catch(InvalidFormatException ife){
		}
		
		//3600 sec.
		expected = new Double(1.0);
		sec = new Double(3600.0);
		actual = GeometryUtils.convertRaToDecimalHours(null, null, sec);
		Assert.assertEquals(sec + " seconds", expected, actual, delta);
		
		//60 min.
		expected = new Double(1.0);
		min = new Double(60.0);
		actual = GeometryUtils.convertRaToDecimalHours(null, min, null);
		Assert.assertEquals(min + " minutes", expected, actual, delta);
		
		//1 deg.
		expected = new Double(1.0);
		deg = new Double(1.0);
		actual = GeometryUtils.convertRaToDecimalHours(deg, null, null);
		Assert.assertEquals(deg + " deg", expected, actual, delta);
		
		//1 deg + 60 min + 3600 sec = 3 deg.
		expected = new Double(3.0);
		deg = new Double(1.0);
		min = new Double(60.0);
		sec = new Double(3600.0);
		actual = GeometryUtils.convertRaToDecimalHours(deg, min, sec);
		Assert.assertEquals(deg + "+" + min + "+" + sec, expected, actual, delta);
	}
	
	
	@Test
	public void testConvertDecToDecimalDegrees() throws InvalidFormatException{
		Double expected;
		Double actual;
		Double hrs;
		Double min;
		Double sec;
		
		//Nulls
		Assert.assertNull("Null values", GeometryUtils.convertDecToDecimalDegrees(null, null, null, false));
		
		//FormatException
		try{
			GeometryUtils.convertDecToDecimalDegrees(null, null, "x", false);
			Assert.fail("Exception expected: invalid format");
		}catch(InvalidFormatException ife){
		}
		
		//3600 sec.
		expected = new Double(1.0);
		sec = new Double(3600.0);
		actual = GeometryUtils.convertDecToDecimalDegrees(null, null, sec, false);
		Assert.assertEquals(sec + " seconds", expected, actual, DELTA);
		
		//60 min.
		expected = new Double(1.0);
		min = new Double(60.0);
		actual = GeometryUtils.convertDecToDecimalDegrees(null, min, null, false);
		Assert.assertEquals(min + " minutes", expected, actual, DELTA);
		
		//1 deg.
		expected = new Double(1.0);
		hrs = new Double(1.0);
		actual = GeometryUtils.convertDecToDecimalDegrees(hrs, null, null, false);
		Assert.assertEquals(hrs + " hrs", expected, actual, DELTA);
		
		//1 deg + 60 min + 3600 sec = 3 deg.
		expected = new Double(-3.0);
		hrs = new Double(1.0);
		min = new Double(60.0);
		sec = new Double(3600.0);
		actual = GeometryUtils.convertDecToDecimalDegrees(hrs, min, sec, true);
		Assert.assertEquals("-"+hrs + "+" + min + "+" + sec, expected, actual, DELTA);
	}

	@Test
	public void testConvertRaInDecimalHoursToDecimalDegrees(){
		double expected;
		double actual;
		Double raInDecimalHours;
		
		raInDecimalHours = new Double(24.0);
		expected = 360.0;
		actual = GeometryUtils.convertRaInDecimalHoursToDecimalDegrees(raInDecimalHours);
		Assert.assertEquals(""+raInDecimalHours, expected, actual, DELTA);
		
		raInDecimalHours = new Double(12.0);
		expected = 180.0;
		actual = GeometryUtils.convertRaInDecimalHoursToDecimalDegrees(raInDecimalHours);
		Assert.assertEquals(""+raInDecimalHours, expected, actual, DELTA);
		
		raInDecimalHours = new Double(1.0);
		expected = 15.0;
		actual = GeometryUtils.convertRaInDecimalHoursToDecimalDegrees(raInDecimalHours);
		Assert.assertEquals(""+raInDecimalHours, expected, actual, DELTA);
		
		raInDecimalHours = new Double(1.5);
		expected = 22.5;
		actual = GeometryUtils.convertRaInDecimalHoursToDecimalDegrees(raInDecimalHours);
		Assert.assertEquals(""+raInDecimalHours, expected, actual, DELTA);
		
	}
	
	@Test
	public void testFormatCoordinatesSeconds(){
		String actual;
		actual = GeometryUtils.formatCoordinatesSeconds(0);
		Assert.assertEquals("0.0", actual);
		
		actual = GeometryUtils.formatCoordinatesSeconds(60.0);
		Assert.assertEquals("60.", actual);
		
		actual = GeometryUtils.formatCoordinatesSeconds(100.4);
		Assert.assertEquals("100.", actual);
		
		actual = GeometryUtils.formatCoordinatesSeconds(15.04);
		Assert.assertEquals("15.04", actual);

		actual = GeometryUtils.formatCoordinatesSeconds(15.4);
		Assert.assertEquals("15.4", actual);
	}
	
	@Test
	public void testParseRa() throws InvalidFormatException{
		Assert.assertNull(GeometryUtils.parseRa(null));
		try{
			GeometryUtils.parseRa("x");
			Assert.fail("Invalid format exception expected");
		}catch(InvalidFormatException e){
			
		}
		
		Double actual;
		actual = GeometryUtils.parseRa("24");
		Assert.assertEquals("24", 24.0, actual, DELTA);
		actual = GeometryUtils.parseRa("24h");
		Assert.assertEquals("24h", 24.0, actual, DELTA);
		actual = GeometryUtils.parseRa("360d");
		Assert.assertEquals("360d", 24.0, actual, DELTA);
		
		actual = GeometryUtils.parseRa("1 30");
		Assert.assertEquals("1 30", 1.5, actual, DELTA);
		actual = GeometryUtils.parseRa("1:30");
		Assert.assertEquals("1:30", 1.5, actual, DELTA);
		actual = GeometryUtils.parseRa("1,30");
		Assert.assertEquals("1,30", 1.5, actual, DELTA);
		
		actual = GeometryUtils.parseRa("1 30 30");
		Assert.assertEquals("1 30 30", 1.5083333333, actual, DELTA);
		actual = GeometryUtils.parseRa("1:30:30");
		Assert.assertEquals("1:30:30", 1.5083333333, actual, DELTA);
		actual = GeometryUtils.parseRa("1,30,30");
		Assert.assertEquals("1,30,30", 1.5083333333, actual, DELTA);
		
		actual = GeometryUtils.parseRa("1h 30m 30s");
		Assert.assertEquals("1h 30m 30s", 1.5083333333, actual, DELTA);
	}
	
	@Test
	public void testParseLatitude_Dec() throws InvalidFormatException{
		//parseLatitude is a proxy for parseDec
		
		
		Assert.assertNull(GeometryUtils.parseLatitude(null));
		try{
			GeometryUtils.parseLatitude("x");
			Assert.fail("Invalid format exception expected");
		}catch(InvalidFormatException e){
			
		}
		
		Double actual;
		actual = GeometryUtils.parseLatitude("180");
		Assert.assertEquals("180", 180.0, actual, DELTA);
		actual = GeometryUtils.parseLatitude("180.5");
		Assert.assertEquals("108.5", 180.5, actual, DELTA);
		actual = GeometryUtils.parseLatitude("-180");
		Assert.assertEquals("-180", -180.0, actual, DELTA);
		actual = GeometryUtils.parseLatitude("-180.5");
		Assert.assertEquals("-180.5", -180.5, actual, DELTA);
		
		//dec: +- 99  (prev test: no limits!)
		//99
		actual = GeometryUtils.parseLatitude("99 30");
		Assert.assertEquals("99 30", 99.5, actual, DELTA);
		actual = GeometryUtils.parseLatitude("99:30");
		Assert.assertEquals("99:30", 99.5, actual, DELTA);
		actual = GeometryUtils.parseLatitude("99,30");
		Assert.assertEquals("99,30", 99.5, actual, DELTA);
		
		actual = GeometryUtils.parseLatitude("-99 30");
		Assert.assertEquals("-99 30", -99.5, actual, DELTA);
		actual = GeometryUtils.parseLatitude("-99:30");
		Assert.assertEquals("-99:30", -99.5, actual, DELTA);
		actual = GeometryUtils.parseLatitude("-99,30");
		Assert.assertEquals("-99,30", -99.5, actual, DELTA);
		
		actual = GeometryUtils.parseLatitude("99 30 30");
		Assert.assertEquals("99 30 30", 99.5083333333, actual, DELTA);
		actual = GeometryUtils.parseLatitude("99:30:30");
		Assert.assertEquals("99:30:30", 99.5083333333, actual, DELTA);
		actual = GeometryUtils.parseLatitude("99,30,30");
		Assert.assertEquals("99,30,30", 99.5083333333, actual, DELTA);
		
		actual = GeometryUtils.parseLatitude("-99 30 30");
		Assert.assertEquals("-99 30 30", -99.5083333333, actual, DELTA);
		actual = GeometryUtils.parseLatitude("-99:30:30");
		Assert.assertEquals("-99:30:30", -99.5083333333, actual, DELTA);
		actual = GeometryUtils.parseLatitude("-99,30,30");
		Assert.assertEquals("-99,30,30", -99.5083333333, actual, DELTA);
		
		actual = GeometryUtils.parseLatitude("99d 30m 30s");
		Assert.assertEquals("99d 30m 30s", 99.5083333333, actual, DELTA);
	}
	
	@Test
	public void testParseLongitude() throws InvalidFormatException{
		Assert.assertNull(GeometryUtils.parseLongitude(null));
		try{
			GeometryUtils.parseLongitude("x");
			Assert.fail("Invalid format exception expected");
		}catch(InvalidFormatException e){
			
		}
		
		Double actual;
		actual = GeometryUtils.parseLongitude("180");
		Assert.assertEquals("180", 180.0, actual, DELTA);
		actual = GeometryUtils.parseLongitude("180.5");
		Assert.assertEquals("108.5", 180.5, actual, DELTA);
		try{
			GeometryUtils.parseLongitude("180.5.6.7");
			Assert.fail("Exception expected.");
		}catch(InvalidFormatException e){
			
		}
		actual = GeometryUtils.parseLongitude("390.5");
		Assert.assertEquals("390.5", 390.5, actual, DELTA);
		
		//dec: +- 99  (prev test: no limits!)
		//99
		actual = GeometryUtils.parseLongitude("179 30");
		Assert.assertEquals("179 30", 179.5, actual, DELTA);
		actual = GeometryUtils.parseLongitude("179:30");
		Assert.assertEquals("179:30", 179.5, actual, DELTA);
		actual = GeometryUtils.parseLongitude("179,30");
		Assert.assertEquals("179,30", 179.5, actual, DELTA);
		
		//Allowed!
		actual = GeometryUtils.parseLongitude("999 30");
		Assert.assertEquals("999 30", 999.5, actual, DELTA);
		
		//
		actual = GeometryUtils.parseLongitude("179 30 30");
		Assert.assertEquals("179 30 30", 179.5083333333, actual, DELTA);
		actual = GeometryUtils.parseLongitude("179:30:30");
		Assert.assertEquals("179:30:30", 179.5083333333, actual, DELTA);
		actual = GeometryUtils.parseLongitude("179,30,30");
		Assert.assertEquals("179,30,30", 179.5083333333, actual, DELTA);
		
		//Allowed!
		actual = GeometryUtils.parseLongitude("999,30,30");
		Assert.assertEquals("999,30,30", 999.5083333333, actual, DELTA);

		//
		actual = GeometryUtils.parseLongitude("179d 30m 30s");
		Assert.assertEquals("179d 30m 30s", 179.5083333333, actual, DELTA);
		
		//Allowed!
		actual = GeometryUtils.parseLongitude("999d 30m 30s");
		Assert.assertEquals("999d 30m 30s", 999.5083333333, actual, DELTA);
	}
	
	
	@Test
	public void testCreateAngleObject(){
		Angle defaultAngle = new Angle(90.0, Unit.ARCMINUTES);
		Angle actual;
		Angle expected;

		actual = GeometryUtils.createAngleObject(null, null, defaultAngle);
		check(actual, defaultAngle, "null value");
		
		String value = "25.0";
		expected = new Angle(25.0, Unit.ARCMINUTES);
		actual = GeometryUtils.createAngleObject(value, null, defaultAngle);
		check(actual, expected, "null unit");
		
		//Wrong unit. Valid units are; d, ', '', m and s
		expected = new Angle(75.0, Unit.ARCMINUTES);
		actual = GeometryUtils.createAngleObject("75.0", Unit.DEGREES, defaultAngle);
		check(actual, expected, "wrong unit");
		
		expected = new Angle(75.0, Unit.DEGREES);
		actual = GeometryUtils.createAngleObject("75.0", "d", defaultAngle);
		check(actual, expected, "unit 'd'");
		
		expected = new Angle(75.0, Unit.ARCMINUTES);
		actual = GeometryUtils.createAngleObject("75.0", "'", defaultAngle);
		check(actual, expected, "unit '''");
		
		expected = new Angle(75.0, Unit.ARCSECONDS);
		actual = GeometryUtils.createAngleObject("75.0", "''", defaultAngle);
		check(actual, expected, "unit ''''");
		
		expected = new Angle(75.0, Unit.ARCMINUTES);
		actual = GeometryUtils.createAngleObject("75.0", "m", defaultAngle);
		check(actual, expected, "unit '''");
		
		expected = new Angle(75.0, Unit.ARCSECONDS);
		actual = GeometryUtils.createAngleObject("75.0", "s", defaultAngle);
		check(actual, expected, "unit ''''");
		
	}
	
	private void check(Angle actual, Angle expected, String msg){
		if(expected == null){
			Assert.assertNull(msg, actual);
		}else{
			Assert.assertEquals(msg + " (value)", expected.getValue(), actual.getValue(), DELTA);
			Assert.assertEquals(msg + " (unit)", expected.getUnit(), actual.getUnit());
		}
	}
	
	@Test
	public void testCreateEquatorialCoord(){
		double ra = 12.0;
		double dec = 90.0;
		Coordinates2D c2 = GeometryUtils.createEquatorialCoordinates(ra, dec);
		Assert.assertEquals("ra", ra, c2.getLongitude().getValue(), DELTA);
		Assert.assertEquals("ra", Unit.HOURS, c2.getLongitude().getUnit());
		Assert.assertEquals("dec", dec, c2.getLatitude().getValue(), DELTA);
		Assert.assertEquals("dec", Unit.DEGREES, c2.getLatitude().getUnit());
	}
	
	@Test
	public void testCreateRadiusUnits(){
		AngleUnit unit;
		unit = GeometryUtils.createRadiusUnits("x");
		Assert.assertEquals("Unknown unit", AngleUnit.DEGREES, unit);
		unit = GeometryUtils.createRadiusUnits("ArcMinutes");
		Assert.assertEquals("Unknown unit", AngleUnit.ARCMINUTES, unit);
		unit = GeometryUtils.createRadiusUnits("ArcSeconds");
		Assert.assertEquals("Unknown unit", AngleUnit.ARCSECONDS, unit);
		
	}

}

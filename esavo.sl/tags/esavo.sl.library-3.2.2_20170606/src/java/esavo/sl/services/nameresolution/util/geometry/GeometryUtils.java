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
package esavo.sl.services.nameresolution.util.geometry;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Angle;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.Coordinates2D;
import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.EquatorialCoordinates;
import esac.archive.absi.modules.common.skycoords.AngleUnit;

/*
 * Class responsible for gathering ancillary utilities related to the geometry.
 */
public class GeometryUtils {

   /**
    * Breaks down decimal degrees into degrees arc minutes and arc seconds.
    * All negative angles are converted into positive. The sign is added at
    * the format method
    * @param value in degrees
    * @return Object[3] = {degrees, arc minutes, arc seconds}
    */
   public static String[] breakDownDecimalDegrees(BigDecimal value) {
      String final_dd = "0";
      String final_mm = "0";
      String final_ss = "0";

      if (isEmpty(value)) {
         return new String[] {final_dd, final_mm, final_ss};
      }

      double doubleValue = value.doubleValue();
      final_dd = new Integer(Math.abs((int) doubleValue)).toString();

      // Gets the decimal part and convert to arcminutes
      double decimal_dd = doubleValue - (int) doubleValue;
      double value_mm = AngleUnit.convert(AngleUnit.DEGREES, AngleUnit.ARCMINUTES) * decimal_dd;
      final_mm = new Integer(Math.abs((int) value_mm)).toString();

      // Arcseconds
      double decimal_mm = value_mm - (int) value_mm;
      double value_ss = AngleUnit.convert(AngleUnit.ARCMINUTES, AngleUnit.ARCSECONDS) * decimal_mm;
      String tmp_final_ss = formatCoordinatesSeconds(value_ss);

      final_ss = tmp_final_ss.indexOf("-") > -1 ? tmp_final_ss.substring(1) : tmp_final_ss;

      return new String[] {final_dd, final_mm, final_ss};
   }

   /**
    * Breaks down decimal Ra in hours, minutes and seconds.
    * @param Ra in decimal hours
    * @return Object[3] = {hours, minutes, seconds}
    */
   public static String[] breakDownDecimalRa(BigDecimal ra) {
      String final_ra_hh = "0";
      String final_ra_mm = "0";
      String final_ra_ss = "0";

      if (isEmpty(ra)) {
         return new String[] {final_ra_hh, final_ra_mm, final_ra_ss};
      }

      // Casting input RA
      double doubleRa = ra.doubleValue();
      final_ra_hh = new Integer((int) doubleRa).toString();

      // Gets the decimal part and convert to minutes
      double decimal_ra_hh = doubleRa - (int) doubleRa;
      double ra_mm = AngleUnit.convert(AngleUnit.HOURS, AngleUnit.MINUTES) * decimal_ra_hh;
      final_ra_mm = new Integer((int) ra_mm).toString();

      // Seconds
      double decimal_ra_mm = ra_mm - (int) ra_mm;
      double ra_ss = AngleUnit.convert(AngleUnit.MINUTES, AngleUnit.SECONDS) * decimal_ra_mm;

      final_ra_ss = formatCoordinatesSeconds(ra_ss);

      // Returning values
      return new String[] {final_ra_hh, final_ra_mm, final_ra_ss};
   }

   /**
    * Breaks down decimal degrees into degrees, arcMinutes and arcSeconds and
    * creates a string, 45d 5' 45.6''.
    * @param value in degrees
    * @return String
    */
   public static String formatDecimalDegrees(BigDecimal decimalDegrees) {
      if (!(decimalDegrees instanceof BigDecimal)) {
         return "";
      }

      String[] result = breakDownDecimalDegrees(decimalDegrees);
      // Returning value
      return MessageFormat.format(GeometryConstants.DEGREES_FORMAT, new Object[] {
            (decimalDegrees.signum() == -1 ? "-" : "+")
                  + GeometryConstants.ITEM_COORDINATES_FORMAT.format(new Double(result[0])),
            GeometryConstants.ITEM_COORDINATES_FORMAT.format(new Double(result[1])),
            GeometryConstants.SECONDS_COORDINATES_FORMAT.format(new Double(result[2]))});
   }

   /**
    * Breaks down decimal RA into hours, minutes and seconds and creates a
    * string, 45h 5m 45.6s.
    * @param value in hours
    * @return String
    */
   public static String formatDecimalRa(BigDecimal raDecimalHours) {
      if (!(raDecimalHours instanceof BigDecimal)) {
         return "";
      }

      String[] result = breakDownDecimalRa(raDecimalHours);
      // Returning value
      return MessageFormat.format(GeometryConstants.RA_FORMAT, new Object[] {
            GeometryConstants.ITEM_COORDINATES_FORMAT.format(new Double(result[0])),
            GeometryConstants.ITEM_COORDINATES_FORMAT.format(new Double(result[1])),
            GeometryConstants.SECONDS_COORDINATES_FORMAT.format(new Double(result[2]))});
   }

   /**
    * Calculates the decimal RA from its hours, minutes and seconds.
    * @param hh
    * @param mm
    * @param ss
    * @return
    * @throws InvalidFormatException
    */
   public static Double convertRaToDecimalHours(Object hh, Object mm, Object ss)
         throws InvalidFormatException {
      if (isEmpty(hh) && isEmpty(mm) && isEmpty(ss)) {
         return null;
      }

      // Ra in seconds
      double totalRa = 0;
      try {
         // Hours
         if (!isEmpty(hh)) {
            totalRa += Double.parseDouble(hh.toString());
         }

         // Minutes
         if (!isEmpty(mm)) {
            totalRa += (AngleUnit.convert(AngleUnit.MINUTES, AngleUnit.HOURS) * Double
                  .parseDouble(mm.toString()));
         }

         // Seconds
         if (!isEmpty(ss)) {
            totalRa += (AngleUnit.convert(AngleUnit.SECONDS, AngleUnit.HOURS) * Double
                  .parseDouble(ss.toString()));
         }
      } catch (Exception e) {
         throw new InvalidFormatException();
      }

      return new Double(totalRa);
   }

   /**
    * Calculates the decimal degrees from its hours, arcMinutes and arcSeconds.
    * @param degrees
    * @param arcMin
    * @param arcSec
    * @return Double
    */
   public static Double convertDecToDecimalDegrees(Object degrees, Object arcMin, Object arcSec, boolean negative)
         throws InvalidFormatException {
      if (isEmpty(degrees) && isEmpty(arcMin) && isEmpty(arcSec)) {
         return null;
      }

      // Dec: in degrees
      double totalDec = 0;

      try {
         // Degrees
         if (!isEmpty(degrees)) {
            totalDec += Double.parseDouble(degrees.toString());
         }

         // ArcMinutes
         if (!isEmpty(arcMin)) {
            totalDec += (AngleUnit.convert(AngleUnit.ARCMINUTES, AngleUnit.DEGREES) * Double
                  .parseDouble(arcMin.toString()));
         }

         // ArcSeconds
         if (!isEmpty(arcSec)) {
            totalDec += (AngleUnit.convert(AngleUnit.ARCSECONDS, AngleUnit.DEGREES) * Double
                  .parseDouble(arcSec.toString()));
         }

         if (negative) {
            totalDec *= -1;
         }
      } catch (Exception e) {
         throw new InvalidFormatException();
      }
      return new Double(totalDec);
   }

   
   /**
    * Converts RA in decimal hours to decimal degrees.
    * 
    * @param raInDecimalHours
    * @return
    */
   public static Double convertRaInDecimalHoursToDecimalDegrees(Double raInDecimalHours)
   {
	   return raInDecimalHours * AngleUnit.convert(AngleUnit.HOURS, AngleUnit.DEGREES); 
   }
   
   
   /**
    * Formats seconds as xx.yy.
    * @return formatted seconds
    */
   public static String formatCoordinatesSeconds(double inputSeconds) {
      String castedSeconds = new Double(inputSeconds).toString();
      List<String> resultMatch = null;
      if (isEmpty(castedSeconds)
            || (resultMatch = fitsGroupedPattern(GeometryConstants.SECONDS_PATTERN, castedSeconds)) == null
            || resultMatch.size() < 2) {
         return castedSeconds;
      }

      String decimalSeconds = (String) resultMatch.get(1);
      return (String) resultMatch.get(0)
            + decimalSeconds.substring(0, (decimalSeconds.length() < 2 ? decimalSeconds.length()
                  : 2));
   }

   /**
    * Processes a Ra string and converts it to decimal hours. It can be one, two
    * or three number without units, or 1,2 or 3 numbers with units.
    * @throws InvalidFormatException
    */
   public static Double parseRa(String raValue) throws InvalidFormatException {
      if (isEmpty(raValue)) {
         return null;
      }

      List<String> listMatch = null;
      // 1 number: xx.yy [hd]
      if (!(listMatch = fitsGroupedPattern(GeometryConstants.RA_DECIMAL_PATTERN, raValue)).isEmpty()) {
         return convertToHoursRa(listMatch.get(0), listMatch.get(1));
      } else if (!(listMatch = fitsGroupedPattern(GeometryConstants.RA_TWO_NUMBERS_PATTERN, raValue))
            .isEmpty()
            || !(listMatch = fitsGroupedPattern(GeometryConstants.RA_THREE_NUMBERS_PATTERN, raValue))
                  .isEmpty()
            || !(listMatch = fitsGroupedPattern(GeometryConstants.RA_UNITS_PATTERN, raValue))
                  .isEmpty()) {
         // 2 or 3 numbers: hh [ ,:] mm [,:] ss.ss or with units
         return convertRaToDecimalHours(listMatch.get(0), listMatch.get(1),
                                   listMatch.size() > 2 ? listMatch.get(2) : null);
      }

      throw new InvalidFormatException();
   }

   /**
    * Processes a dec string and converts it to degrees. It can be one, two or
    * three numbers without units, or 1,2 or 3 numbers with units.
    * @throws InvalidFormatException
    */
   public static Double parseDec(String decvalue) throws InvalidFormatException {
      if (isEmpty(decvalue)) {
         return null;
      }

      List<String> listMatch = null;
      // 1 number: [+-] xx.yy [d]
      if (!(listMatch = fitsGroupedPattern(GeometryConstants.DEC_DECIMAL_PATTERN, decvalue))
            .isEmpty()) {
         return new Double(Double.parseDouble(listMatch.get(1).toString())
               * (GeometryConstants.MINUS_SIGN.equals(listMatch.get(0)) ? -1 : 1));
      }

      // 2 or 3 numbers: [+-] d [ ,:] m [,:] ss.ss
      if (!(listMatch = fitsGroupedPattern(GeometryConstants.DEC_TWO_NUMBERS_PATTERN, decvalue))
            .isEmpty()
            || !(listMatch = fitsGroupedPattern(GeometryConstants.DEC_THREE_NUMBERS_PATTERN,
                                                      decvalue)).isEmpty()
            || !(listMatch = fitsGroupedPattern(GeometryConstants.DEC_UNITS_PATTERN, decvalue))
                  .isEmpty()) {
         return GeometryUtils.convertDecToDecimalDegrees(listMatch.get(1), listMatch.get(2),
                                                      listMatch.size() > 3 ? listMatch.get(3)
                                                            : null, GeometryConstants.MINUS_SIGN
                                                            .equals(listMatch.get(0)));
      }
      throw new InvalidFormatException();
   }

   /**
    * Processes a Longitude string and converts it to degrees. It can be one
    * number, two or three without units, or 1,2 or 3 numbers with units.
    * @throws InvalidFormatException
    */
   public static Double parseLongitude(String longitudValue) throws InvalidFormatException {
      if (isEmpty(longitudValue)) {
         return null;
      }

      List<String> listMatch = null;
      // 1 number: xx.yy [hd]
      if (!(listMatch = fitsGroupedPattern(GeometryConstants.LONGITUD_DECIMAL_PATTERN, longitudValue)).isEmpty()) {
         try {
            return new Double(listMatch.get(0).toString());
         } catch (NumberFormatException e) {
            throw new InvalidFormatException();
         }
      } else if (!(listMatch = fitsGroupedPattern(GeometryConstants.LONGITUD_TWO_NUMBERS_PATTERN,
                                                        longitudValue)).isEmpty()
            || !(listMatch = fitsGroupedPattern(GeometryConstants.LONGITUD_THREE_NUMBERS_PATTERN,
                                                      longitudValue)).isEmpty()
            || !(listMatch = fitsGroupedPattern(GeometryConstants.LONGITUD_UNITS_PATTERN,
                                                      longitudValue)).isEmpty()) {
         // 2 or 3 numbers: hh [ ,:] mm [,:] ss.ss or with units

         return GeometryUtils.convertDecToDecimalDegrees(listMatch.get(0), listMatch.get(1),
                                                      listMatch.size() > 2 ? listMatch.get(2)
                                                            : null, false);
      }
      throw new InvalidFormatException();
   }

   /**
    * Processes Latitude and converts it to degrees. It can be one number, two
    * or three without units, or 1,2 or 3 numbers with units.
    * @throws InvalidFormatException
    */
   public static Double parseLatitude(String latitudeValue) throws InvalidFormatException {
      // Latitude behaves like dec
      return parseDec(latitudeValue);
   }

//   /**
//    * Processes one line belonging to a target file and returns a
//    * QueryTargetItem object.
//    * @param targetFileLine
//    * @return QueryTargetItem
//    * @throws InvalidFormatException
//    */
//   public static QueryTargetItem parseTargetLineToEqCoordinates(String targetLine,
//         Angle defaultRadii) throws InvalidFormatException {
//      if (targetLine == null) {
//         return null;
//      }
//
//      // Possible patterns: two numbers, six numbers, units and target.
//      List listMatch = null;
//      // Two numbers {ra[dh] [+-]dec [,radii] [radii unit]}
//      if (!(listMatch = fitsGroupedPattern(Constants.TWO_NUMBERS_TARGET_PATTERN, targetLine))
//            .isEmpty()) {
//         double ra = convertToHoursRa(listMatch.get(0), listMatch.get(1)).doubleValue();
//         double dec = new Double((String) listMatch.get(3)).doubleValue();
//         if (Constants.MINUS_SIGN.equals(listMatch.get(2))) {
//            dec *= -1;
//         }
//
//         Coordinates2D coordinates = createEquatorialCoordinates(ra, dec);
//
//         // Radii
//         Angle radii = createAngleObject(listMatch.get(4), listMatch.get(5), defaultRadii);
//         return new QueryTargetCoordinatesItem(coordinates, radii, targetLine);
//      }
//
//      // Six numbers {rah ram ras [+-] decd dec' dec'' [,radii] [radii
//      // unit]}
//      if (!(listMatch = fitsGroupedPattern(Constants.SIX_NUMBERS_TARGET_PATTERN, targetLine))
//            .isEmpty()
//            || !(listMatch = fitsGroupedPattern(Constants.UNITS_TARGET_PATTERN, targetLine))
//                  .isEmpty()) {
//         try {
//            Double ra = GeometryUtils.convertToDecimalRa(listMatch.get(0), listMatch.get(1),
//                                                         listMatch.get(2));
//            Double dec = GeometryUtils.convertToDecimalDegrees(listMatch.get(4), listMatch.get(5),
//                                                               listMatch.get(6),
//                                                               Constants.MINUS_SIGN
//                                                                     .equals(listMatch.get(3)));
//
//            Coordinates2D coordinates = createEquatorialCoordinates(ra.doubleValue(), dec
//                  .doubleValue());
//
//            // Radii {target [radii] [radii unit]}
//            Angle radii = createAngleObject(listMatch.get(7), listMatch.get(8), defaultRadii);
//            return new QueryTargetCoordinatesItem(coordinates, radii, targetLine);
//         } catch (Exception e) {
//            throw new InvalidFormatException();
//         }
//      }
//
//      // Target Name: {targetName [radii] [radii unit]}
//      if (!(listMatch = fitsGroupedPattern(Constants.TARGET_PATTERN, targetLine)).isEmpty()) {
//         // Radii
//         Angle radii = createAngleObject(listMatch.get(1), listMatch.get(2), defaultRadii);
//
//         return new QueryTargetNameItem((String) listMatch.get(0), radii, targetLine);
//      }
//
//      // If gets to here there is format error
//      throw new InvalidFormatException();
//   }

   /**
    * Returns the value of RA in hours based on two input objects, value and
    * unit. The last one can be empty, meaning the unit is the default one:
    * hours.
    * @param value
    * @param unit
    * @return
    */
   public static Double convertToHoursRa(Object value, Object unit) {
      // Casting to double
      double ra = new Double(value.toString()).doubleValue();

      // Unit
      if (!isEmpty(unit)
            && fitsPattern(GeometryConstants.UNIT_DEGREE, unit.toString()).booleanValue()) {
         ra *= AngleUnit.convert(AngleUnit.DEGREES, AngleUnit.HOURS);
      }
      return new Double(ra);
   }

   /**
    * Creates an Angle object.
    * @param value
    * @param unit
    * @param default
    * @return
    */
   public static Angle createAngleObject(Object value, Object unit, Angle defaultAngle) {
      if (isEmpty(value)) {
         return defaultAngle;
      }

      Angle.Unit finalUnit = Angle.Unit.ARCMINUTES;

      if (unit != null) {
         // Units: d ' '' or d m s
         if (fitsPattern(GeometryConstants.UNIT_AMIN, unit.toString()).booleanValue()) {
            finalUnit = Angle.Unit.ARCMINUTES;
         } else if (fitsPattern(GeometryConstants.UNIT_ASEC, unit.toString()).booleanValue()) {
            finalUnit = Angle.Unit.ARCSECONDS;
         } else if (fitsPattern(GeometryConstants.UNIT_DEGREE, unit.toString()).booleanValue()) {
            finalUnit = Angle.Unit.DEGREES;
         }
      }
      return new Angle(new Double((String) value).doubleValue(), finalUnit);
   }

   /**
    * Creates the equatorial coordinates2D object associated to the tupla ra and
    * dec.
    * @param ra
    * @param dec
    * @return Coordinates2D
    */
   public static Coordinates2D createEquatorialCoordinates(double ra, double dec) {
      // Equatorial coordinates
      EquatorialCoordinates equatorialCoordinates = new EquatorialCoordinates(new Angle(ra,
            Angle.Unit.HOURS), new Angle(dec, Angle.Unit.DEGREES), GeometryConstants.DEFAULT_EPOCH);

      // equatorialCoordinates.setRa(createAngleObject(ra, AngleUnit.HOURS));
      // equatorialCoordinates.setDec(createAngleObject(dec,
      // AngleUnit.DEGREES));
      // equatorialCoordinates.setEpoch(Constants.DEFAULT_EPOCH);

      return equatorialCoordinates;
   }

   /**
    * Creates the AngleUnit associated to the radius unit.
    * @param radiusUnit
    */
   public static AngleUnit createRadiusUnits(String radiusUnit) {
      if (AngleUnit.ARCMINUTES.getName().equalsIgnoreCase(radiusUnit)) {
         return AngleUnit.ARCMINUTES;
      } else if (AngleUnit.ARCSECONDS.getName().equalsIgnoreCase(radiusUnit)) {
         return AngleUnit.ARCSECONDS;
      } else {
         return AngleUnit.DEGREES;
      }
   }

//   /**
//    * Creates an Angle object.
//    * @param value
//    * @param unit
//    * @return
//    */
//   /*
//    * public static Angle createAngleObject(double value, Angle.Unit unit) { if
//    * (isEmpty(unit)) { return null; } return new Angle(value, unit); }
//    */

//   /**
//    * Creates an Angle object.
//    * @param value
//    * @param unit
//    * @return
//    */
//   /*
//    * public static Angle createAngleObject(double value, String unit) { if
//    * (isEmpty(unit)) { return null; } return new Angle(value,
//    * createRadiusUnits(unit)); }
//    */
   
   
   
   
   /**
    * Checks if the String is null and/or contains only empty spaces.
    * @param element to check
    * @return boolean
    */
   public static boolean isEmpty(Object element) {
      if (element == null) {
         return true;
      }

      return (element instanceof String) ? isEmpty(element.toString()) : false;
   }

   /**
    * Checks if the String is null and/or contains only empty spaces.
    * @param element to check
    * @return boolean
    */
   public static boolean isEmpty(String element) {
      return element == null || element.trim().length() == 0;
   }
   
   
   /**
    * Checks if a text fits a pattern.
    * @param pattern
    * @param text
    * @return
    */
   public static Boolean fitsPattern(String pattern, String text) {
      if (isEmpty(pattern) || isEmpty(text)) {
         return Boolean.FALSE;
      }

      Pattern thePattern = Pattern.compile(pattern);
      Matcher matcher = thePattern.matcher(text);

      return new Boolean(matcher.find());
   }

   /**
    * Checks if a text fits a pattern that contains groups delimited by ().
    * Returns a list with all those groups.
    * @param pattern
    * @param text
    * @return Matcher
    */
   public static List<String> fitsGroupedPattern(String pattern, String text) {
      List<String> result = new ArrayList<String>();
      if (isEmpty(pattern) || isEmpty(text)) {
         return result;
      }

      Pattern thePattern = Pattern.compile(pattern);
      Matcher matcher = thePattern.matcher(text);

      if (matcher.find()) {
         for (int i = 0; i < matcher.groupCount(); i++) {
            result.add(matcher.group(i + 1));
         }
      }

      return result;
   }   
   
   
   public static void test(){
	   double raDeg = 10.6847083;
	   double decDeg = 41.26875 ;
	   
       String raStr = GeometryUtils.formatDecimalRa(new BigDecimal(raDeg / 15.0));
       String decStr = GeometryUtils.formatDecimalDegrees(new BigDecimal(decDeg));

       String coordinates = raStr + " " + decStr; 
       System.out.println(coordinates);
       
       AngleUnit angle = AngleUnit.DEGREES;
       System.out.println("DEGREES to ARCMIN -> " + angle.convert(AngleUnit.ARCMINUTES));
       System.out.println("DEGREES to ARCSEC -> " + angle.convert(AngleUnit.ARCSECONDS));
       
       AngleUnit arcMin = AngleUnit.ARCMINUTES;
       System.out.println("ARCMIN to DEGREES -> " + arcMin.convert(AngleUnit.DEGREES));

       AngleUnit arcSec = AngleUnit.ARCSECONDS;
       System.out.println("ARCSEC to DEGREES -> " + arcSec.convert(AngleUnit.DEGREES));
       
		DecimalFormat secondsFormatter = new DecimalFormat("#.###");
		long t = 234;
		double timeInMs = (double)t;
		double timeInSec = timeInMs/1000;
		System.out.println(timeInSec);
		System.out.println(secondsFormatter.format(timeInSec));
		
   }

   
   public static void main(String[] args) {
	   test();
   }
}

package esavo.sl.services.nameresolution.util.geometry;


import java.text.DecimalFormat;
import java.text.NumberFormat;

import esac.archive.absi.modules.common.querybean.geometry.coordinatesystems.EquatorialCoordinates.Epoch;

/**
 * Includes general constants.
 * @author Monica Fernandez - ESAC/ESA - Madrid, Spain
 */

public class GeometryConstants {

   // ----> Formats
   public static final NumberFormat FLOAT_NUMBER_FORMAT = new DecimalFormat(".0000");

   public static final String SECONDS_PATTERN = "([1-5]?[0-9]\\.?)(.*)";

   public static final String PROGRESS_MSG_FORMAT = "Requesting {0} values ...";

   public static final String FLOAT_NUMBER_PATTERN = "(\\-?)\\d+\\.?\\d*";

   public static final String LIMITED_FLOAT_NUMBER_PATTERN = "^" + FLOAT_NUMBER_PATTERN + "$";

   public static final String DEGREES_FORMAT = "{0}d {1}'' " + "{2}''''";

   public static final String RA_FORMAT = "{0}h {1}m {2}s";

   public static final NumberFormat ITEM_COORDINATES_FORMAT = new DecimalFormat("00");

   public static final NumberFormat SECONDS_COORDINATES_FORMAT = new DecimalFormat("00.00");

   public static final String MINUS_SIGN = "-";

   public static final Epoch DEFAULT_EPOCH = Epoch.J2000;

   // Common
   private static final String BLANK = "\\s*";

   private static final String SEP = "[\\s\\,]+";

   private static final String SIGN = "([+-])?";

   private static final String BEGIN = "^" + BLANK;

   private static final String END = BLANK + "$";

   // Units
   public static final String UNIT_DEGREE = "^d$";

   public static final String UNIT_AMIN = "^(m|\\')$"; // arc minutes

   public static final String UNIT_ASEC = "^(s|\\'{2})$"; // arc seconds

   // Atomic elements
   private static final String HOUR = "([0-2]?[0-9])";

   private static final String MIN = "([0-5]?[0-9])";

   private static final String SEC = "([0-5]?[0-9]\\.?\\d*)";

   private static final String SEP2 = "[\\s\\:\\,]+";

   private static final String DEG = "(\\d{1,2})";

   private static final String DEG_LONG = "(\\d{1,3})";

   private static final String RADII = "[\\s\\,]*" + "(?:(\\d+\\.?\\d*)(d|m|s|\\'{1,2})?)?"
         + BLANK;

   private static final String HOUR_UNIT = "(?:([0-2]?[0-9])h)?";

   private static final String MIN_UNIT = "(?:([0-5]?[0-9])m)?";

   private static final String SEC_UNIT = "(?:([0-5]?[0-9]\\.?\\d*)s)?";

   private static final String DEG_UNIT = "(?:(\\d{1,2})d)?";

   private static final String DEG_LONG_UNIT = "(?:(\\d{1,3})d)?";

   private static final String AMIN_UNIT = "(?:([0-5]?[0-9])[m\\'])?";

   private static final String ASEC_UNIT = "(?:([0-5]?[0-9]\\.?\\d*)(?:s|\\'{2}))?";

   // RA format: 1, 2 or 3 numbers without units or any with them
   public static final String RA_DECIMAL_PATTERN = BEGIN + "([\\d\\.]+)([dh])?" + END;

   public static final String RA_TWO_NUMBERS_PATTERN = BEGIN + HOUR + SEP2 + MIN + END;

   public static final String RA_THREE_NUMBERS_PATTERN = BEGIN + HOUR + SEP2 + MIN + SEP2 + SEC
         + END;

   public static final String RA_UNITS_PATTERN = BEGIN + HOUR_UNIT + BLANK + MIN_UNIT + BLANK
         + SEC_UNIT + END;

   // DEC format: 1, 2 or 3 numbers without units or any with them
   public static final String DEC_DECIMAL_PATTERN = BEGIN + SIGN + BLANK + "([\\d\\.]+)d?" + END;

   public static final String DEC_TWO_NUMBERS_PATTERN = BEGIN + SIGN + BLANK + DEG + SEP2 + MIN
         + END;

   public static final String DEC_THREE_NUMBERS_PATTERN = BEGIN + SIGN + BLANK + DEG + SEP2 + MIN
         + SEP2 + SEC + END;

   public static final String DEC_UNITS_PATTERN = BEGIN + SIGN + DEG_UNIT + BLANK + AMIN_UNIT
         + BLANK + ASEC_UNIT + END;

   // LONGITUD format: 1, 2 or 3 numbers without units or any with them
   public static final String LONGITUD_DECIMAL_PATTERN = BEGIN + "([\\d\\.]+)d?" + END;

   public static final String LONGITUD_TWO_NUMBERS_PATTERN = BEGIN + DEG_LONG + SEP2 + MIN + END;

   public static final String LONGITUD_THREE_NUMBERS_PATTERN = BEGIN + DEG_LONG + SEP2 + MIN
         + SEP2 + SEC + END;

   public static final String LONGITUD_UNITS_PATTERN = BEGIN + DEG_LONG_UNIT + BLANK + AMIN_UNIT
         + BLANK + ASEC_UNIT + END;

   // Patterns used in targets file processing
   // TWO NUMBERS --> RA DEC [,RADII]: "454.53h , -6756.7d"
   public static final String TWO_NUMBERS_TARGET_PATTERN = BEGIN + "([\\d\\.]+)([dh])?" + SEP
         + SIGN + "([\\d\\.]+)d?" + RADII + END;

   // SIX NUMBERS: "12:1:2.2 45:3:3.9", "12 1 2.2, 45 3 3.9 4"
   // {rah ram ras [+-] decd dec' dec'' [radii] [radii unit]}
   public static final String SIX_NUMBERS_TARGET_PATTERN = BEGIN + HOUR + SEP2 + MIN + SEP2 + SEC
         + SEP + SIGN + DEG + SEP2 + MIN + SEP2 + SEC + RADII + END;

   // USING UNITS: "2h 31.99s -9d 2m5.9'' 7.2d", " 13h30m52.5s +01d4'3'' ,4 "
   // {[rah] [ram] [ras] [+-] [decd] [dec]' [dec''] [radii] [radii unit]}
   public static final String UNITS_TARGET_PATTERN = BEGIN + HOUR_UNIT + BLANK + MIN_UNIT + BLANK
         + SEC_UNIT + SEP + SIGN + DEG_UNIT + BLANK + AMIN_UNIT + BLANK + ASEC_UNIT + RADII + END;

   // Finaly, target: important: use , to separate radii
   public static final String TARGET_PATTERN = "^([^\\,]*)" + RADII + END;


}
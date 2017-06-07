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
/* Generated By:JavaCC: Do not edit this line. ADQLParserConstants.java */
package esavo.adql.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ADQLParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int LEFT_PAR = 2;
  /** RegularExpression Id. */
  int RIGHT_PAR = 3;
  /** RegularExpression Id. */
  int DOT = 4;
  /** RegularExpression Id. */
  int COMMA = 5;
  /** RegularExpression Id. */
  int EOQ = 6;
  /** RegularExpression Id. */
  int CONCAT = 7;
  /** RegularExpression Id. */
  int PLUS = 8;
  /** RegularExpression Id. */
  int MINUS = 9;
  /** RegularExpression Id. */
  int ASTERISK = 10;
  /** RegularExpression Id. */
  int DIVIDE = 11;
  /** RegularExpression Id. */
  int EQUAL = 12;
  /** RegularExpression Id. */
  int NOT_EQUAL = 13;
  /** RegularExpression Id. */
  int LESS_THAN = 14;
  /** RegularExpression Id. */
  int LESS_EQUAL_THAN = 15;
  /** RegularExpression Id. */
  int GREATER_THAN = 16;
  /** RegularExpression Id. */
  int GREATER_EQUAL_THAN = 17;
  /** RegularExpression Id. */
  int SELECT = 18;
  /** RegularExpression Id. */
  int QUANTIFIER = 19;
  /** RegularExpression Id. */
  int TOP = 20;
  /** RegularExpression Id. */
  int FROM = 21;
  /** RegularExpression Id. */
  int AS = 22;
  /** RegularExpression Id. */
  int NATURAL = 23;
  /** RegularExpression Id. */
  int INNER = 24;
  /** RegularExpression Id. */
  int OUTER = 25;
  /** RegularExpression Id. */
  int RIGHT = 26;
  /** RegularExpression Id. */
  int LEFT = 27;
  /** RegularExpression Id. */
  int FULL = 28;
  /** RegularExpression Id. */
  int JOIN = 29;
  /** RegularExpression Id. */
  int ON = 30;
  /** RegularExpression Id. */
  int USING = 31;
  /** RegularExpression Id. */
  int WHERE = 32;
  /** RegularExpression Id. */
  int AND = 33;
  /** RegularExpression Id. */
  int OR = 34;
  /** RegularExpression Id. */
  int NOT = 35;
  /** RegularExpression Id. */
  int IS = 36;
  /** RegularExpression Id. */
  int NULL = 37;
  /** RegularExpression Id. */
  int BETWEEN = 38;
  /** RegularExpression Id. */
  int LIKE = 39;
  /** RegularExpression Id. */
  int IN = 40;
  /** RegularExpression Id. */
  int EXISTS = 41;
  /** RegularExpression Id. */
  int GROUP_BY = 42;
  /** RegularExpression Id. */
  int HAVING = 43;
  /** RegularExpression Id. */
  int ORDER_BY = 44;
  /** RegularExpression Id. */
  int ASC = 45;
  /** RegularExpression Id. */
  int DESC = 46;
  /** RegularExpression Id. */
  int AVG = 47;
  /** RegularExpression Id. */
  int MAX = 48;
  /** RegularExpression Id. */
  int MIN = 49;
  /** RegularExpression Id. */
  int SUM = 50;
  /** RegularExpression Id. */
  int COUNT = 51;
  /** RegularExpression Id. */
  int BOX = 52;
  /** RegularExpression Id. */
  int CENTROID = 53;
  /** RegularExpression Id. */
  int CIRCLE = 54;
  /** RegularExpression Id. */
  int POINT = 55;
  /** RegularExpression Id. */
  int POLYGON = 56;
  /** RegularExpression Id. */
  int REGION = 57;
  /** RegularExpression Id. */
  int RESOLVETARGET = 58;
  /** RegularExpression Id. */
  int CONTAINS = 59;
  /** RegularExpression Id. */
  int INTERSECTS = 60;
  /** RegularExpression Id. */
  int AREA = 61;
  /** RegularExpression Id. */
  int COORD1 = 62;
  /** RegularExpression Id. */
  int COORD2 = 63;
  /** RegularExpression Id. */
  int COORDSYS = 64;
  /** RegularExpression Id. */
  int DISTANCE = 65;
  /** RegularExpression Id. */
  int ABS = 66;
  /** RegularExpression Id. */
  int CEILING = 67;
  /** RegularExpression Id. */
  int DEGREES = 68;
  /** RegularExpression Id. */
  int EXP = 69;
  /** RegularExpression Id. */
  int FLOOR = 70;
  /** RegularExpression Id. */
  int LOG = 71;
  /** RegularExpression Id. */
  int LOG10 = 72;
  /** RegularExpression Id. */
  int MOD = 73;
  /** RegularExpression Id. */
  int PI = 74;
  /** RegularExpression Id. */
  int POWER = 75;
  /** RegularExpression Id. */
  int RADIANS = 76;
  /** RegularExpression Id. */
  int RAND = 77;
  /** RegularExpression Id. */
  int ROUND = 78;
  /** RegularExpression Id. */
  int SQRT = 79;
  /** RegularExpression Id. */
  int TRUNCATE = 80;
  /** RegularExpression Id. */
  int ACOS = 81;
  /** RegularExpression Id. */
  int ASIN = 82;
  /** RegularExpression Id. */
  int ATAN = 83;
  /** RegularExpression Id. */
  int ATAN2 = 84;
  /** RegularExpression Id. */
  int COS = 85;
  /** RegularExpression Id. */
  int COT = 86;
  /** RegularExpression Id. */
  int SIN = 87;
  /** RegularExpression Id. */
  int TAN = 88;
  /** RegularExpression Id. */
  int STRING_LITERAL = 94;
  /** RegularExpression Id. */
  int DELIMITED_IDENTIFIER = 97;
  /** RegularExpression Id. */
  int REGULAR_IDENTIFIER = 98;
  /** RegularExpression Id. */
  int Letter = 99;
  /** RegularExpression Id. */
  int SCIENTIFIC_NUMBER = 100;
  /** RegularExpression Id. */
  int UNSIGNED_FLOAT = 101;
  /** RegularExpression Id. */
  int UNSIGNED_INTEGER = 102;
  /** RegularExpression Id. */
  int DIGIT = 103;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int WithinComment = 1;
  /** Lexical state. */
  int WithinString = 2;
  /** Lexical state. */
  int WithinDelimitedId = 3;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "<token of kind 1>",
    "\"(\"",
    "\")\"",
    "\".\"",
    "\",\"",
    "\";\"",
    "\"||\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"=\"",
    "<NOT_EQUAL>",
    "\"<\"",
    "\"<=\"",
    "\">\"",
    "\">=\"",
    "\"SELECT\"",
    "<QUANTIFIER>",
    "\"TOP\"",
    "\"FROM\"",
    "\"AS\"",
    "\"NATURAL\"",
    "\"INNER\"",
    "\"OUTER\"",
    "\"RIGHT\"",
    "\"LEFT\"",
    "\"FULL\"",
    "\"JOIN\"",
    "\"ON\"",
    "\"USING\"",
    "\"WHERE\"",
    "\"AND\"",
    "\"OR\"",
    "\"NOT\"",
    "\"IS\"",
    "\"NULL\"",
    "\"BETWEEN\"",
    "\"LIKE\"",
    "\"IN\"",
    "\"EXISTS\"",
    "\"GROUP BY\"",
    "\"HAVING\"",
    "\"ORDER BY\"",
    "\"ASC\"",
    "\"DESC\"",
    "\"AVG\"",
    "\"MAX\"",
    "\"MIN\"",
    "\"SUM\"",
    "\"COUNT\"",
    "\"BOX\"",
    "\"CENTROID\"",
    "\"CIRCLE\"",
    "\"POINT\"",
    "\"POLYGON\"",
    "\"REGION\"",
    "\"RESOLVETARGET\"",
    "\"CONTAINS\"",
    "\"INTERSECTS\"",
    "\"AREA\"",
    "\"COORD1\"",
    "\"COORD2\"",
    "\"COORDSYS\"",
    "\"DISTANCE\"",
    "\"ABS\"",
    "\"CEILING\"",
    "\"DEGREES\"",
    "\"EXP\"",
    "\"FLOOR\"",
    "\"LOG\"",
    "\"LOG10\"",
    "\"MOD\"",
    "\"PI\"",
    "\"POWER\"",
    "\"RADIANS\"",
    "\"RAND\"",
    "\"ROUND\"",
    "\"SQRT\"",
    "\"TRUNCATE\"",
    "\"ACOS\"",
    "\"ASIN\"",
    "\"ATAN\"",
    "\"ATAN2\"",
    "\"COS\"",
    "\"COT\"",
    "\"SIN\"",
    "\"TAN\"",
    "<token of kind 89>",
    "<token of kind 90>",
    "<token of kind 91>",
    "\"\\\'\"",
    "<token of kind 93>",
    "\"\\\'\"",
    "\"\\\"\"",
    "<token of kind 96>",
    "\"\\\"\"",
    "<REGULAR_IDENTIFIER>",
    "<Letter>",
    "<SCIENTIFIC_NUMBER>",
    "<UNSIGNED_FLOAT>",
    "<UNSIGNED_INTEGER>",
    "<DIGIT>",
  };

}

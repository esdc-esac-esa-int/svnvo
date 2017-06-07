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
package esavo.tap.formatter;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.jdbc.TypeMapper;
import uk.ac.starlink.table.jdbc.TypeMappers;
import uk.ac.starlink.table.jdbc.ValueHandler;

public class TapWriterValueHandler implements TypeMapper {

	private final TypeMapper _base = TypeMappers.DALI;

	@Override
	public uk.ac.starlink.table.jdbc.ValueHandler createValueHandler(ResultSetMetaData meta, int jcol) throws SQLException {
		ValueHandler baseHandler = _base.createValueHandler(meta, jcol);
		final ColumnInfo baseInfo = baseHandler.getColumnInfo();
		Class baseClazz = baseInfo.getContentClass();
		if (java.sql.Array.class.isAssignableFrom(baseClazz)) {
			final ColumnInfo info = new ColumnInfo(baseInfo);
			return new ValueHandler() {
				@Override
				public Object getValue(Object baseValue) {
					if (baseValue instanceof java.sql.Array) {
						try {
							return getArray((java.sql.Array) baseValue);
						} catch (SQLException e) {
							throw new IllegalArgumentException(
									"Invalid array value");
						}
					} else {
						return baseValue;
					}
				}

				@Override
				public ColumnInfo getColumnInfo() {
					return info;
				}
			};
		} else {
			return baseHandler;
		}
	}

	private Object getArray(java.sql.Array array) throws SQLException {
		if (array == null) {
			return null;
		}
		Object arr = array.getArray();
		Class<?> c = arr.getClass();
		if (c == Short[].class) {
			arr = getArray((Short[]) arr);
		} else if (c == Integer[].class) {
			arr = getArray((Integer[]) arr);
		} else if (c == Long[].class) {
			arr = getArray((Long[]) arr);
		} else if (c == Float[].class) {
			arr = getArray((Float[]) arr);
		} else if (c == Double[].class) {
			arr = getArray((Double[]) arr);
		} else if (c == Byte[].class) {
			arr = getArray((Byte[]) arr);
		} else if (c == Character[].class) {
			arr = getArray((Character[]) arr);
		} else if (c == String[].class) {
			arr = getArray((String[]) arr);
		} else {
			arr = tryMultipleDims(arr);
		}
		array.free();
		return arr;
	}
	
	private Object tryMultipleDims(Object arr) throws SQLException {
		Object[] o = (Object[])arr;
		Class<?> c = arr.getClass();
		String name = c.getName();
		if(name.contains("java.lang.Short")){
			List<Short> flatList = new ArrayList<Short>();
			shortFlattern(o, flatList);
			arr = flatList.toArray(new Short[flatList.size()]);
			arr = getArray((Short[]) arr);
		}else if (name.contains("java.lang.Integer")){
			List<Integer> flatList = new ArrayList<Integer>();
			integerFlattern(o, flatList);
			arr = flatList.toArray(new Integer[flatList.size()]);
			arr = getArray((Integer[]) arr);
		}else if (name.contains("java.lang.Long")){
			List<Long> flatList = new ArrayList<Long>();
			longFlattern(o, flatList);
			arr = flatList.toArray(new Long[flatList.size()]);
			arr = getArray((Long[]) arr);
		}else if (name.contains("java.lang.Float")){
			List<Float> flatList = new ArrayList<Float>();
			floatFlattern(o, flatList);
			arr = flatList.toArray(new Float[flatList.size()]);
			arr = getArray((Float[]) arr);
		}else if (name.contains("java.lang.Double")){
			List<Double> flatList = new ArrayList<Double>();
			doubleFlattern(o, flatList);
			arr = flatList.toArray(new Double[flatList.size()]);
			arr = getArray((Double[]) arr);
		}else if (name.contains("java.lang.Byte")){
			List<Byte> flatList = new ArrayList<Byte>();
			byteFlattern(o, flatList);
			arr = flatList.toArray(new Byte[flatList.size()]);
			arr = getArray((Byte[]) arr);
		}else if (name.contains("java.lang.Character")){
			List<Character> flatList = new ArrayList<Character>();
			charFlattern(o, flatList);
			arr = flatList.toArray(new Character[flatList.size()]);
			arr = getArray((Character[]) arr);
		}else if (name.contains("java.lang.String")){
			List<String> flatList = new ArrayList<String>();
			stringFlattern(o, flatList);
			arr = flatList.toArray(new String[flatList.size()]);
			arr = getArray((String[]) arr);
		}
		return arr;
	}
	
	
	private void shortFlattern(Object[] arr, List<Short> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Short) {
				flatList.add((Short) element);
			} else if (element instanceof Object[]) {
				shortFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Shorts or nested arrays of Shorts");
			}
		}
	}
	
	private void integerFlattern(Object[] arr, List<Integer> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Integer) {
				flatList.add((Integer) element);
			} else if (element instanceof Object[]) {
				integerFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Integers or nested arrays of Integers");
			}
		}
	}
	
	private void longFlattern(Object[] arr, List<Long> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Long) {
				flatList.add((Long) element);
			} else if (element instanceof Object[]) {
				longFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Longs or nested arrays of Longs");
			}
		}
	}

	private void floatFlattern(Object[] arr, List<Float> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Float) {
				flatList.add((Float) element);
			} else if (element instanceof Object[]) {
				floatFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Floats or nested arrays of Floats");
			}
		}
	}

	private void doubleFlattern(Object[] arr, List<Double> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Double) {
				flatList.add((Double) element);
			} else if (element instanceof Object[]) {
				doubleFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Doubles or nested arrays of Doubles");
			}
		}
	}

	private void byteFlattern(Object[] arr, List<Byte> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Byte) {
				flatList.add((Byte) element);
			} else if (element instanceof Object[]) {
				byteFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Bytes or nested arrays of Bytes");
			}
		}
	}

	private void charFlattern(Object[] arr, List<Character> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof Character) {
				flatList.add((Character) element);
			} else if (element instanceof Object[]) {
				charFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Bytes or nested arrays of Bytes");
			}
		}
	}

	private void stringFlattern(Object[] arr, List<String> flatList){
		if(arr == null){
			return;
		}
		for (Object element : arr) {
			if (element instanceof String) {
				flatList.add((String) element);
			} else if (element instanceof Object[]) {
				stringFlattern((Object[])element, flatList);
			} else {
				throw new IllegalArgumentException(
						"Input must be an array of Bytes or nested arrays of Bytes");
			}
		}
	}

	private int getNumDims(Class<?> type){
		Class<?> c = type.getComponentType();
		if(c == null){
			return 0;
		}else{
			return getNumDims(c) + 1;
		}
	}

	private Object getArray(Short[] data) {
		short[] result = new short[data.length];
		Short d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(Integer[] data) {
		int[] result = new int[data.length];
		Integer d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(Long[] data) {
		long[] result = new long[data.length];
		Long d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(Float[] data) {
		float[] result = new float[data.length];
		Float d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(Double[] data) {
		double[] result = new double[data.length];
		Double d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(Byte[] data) {
		byte[] result = new byte[data.length];
		Byte d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(Character[] data) {
		char[] result = new char[data.length];
		Character d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}

	private Object getArray(String[] data) {
		String[] result = new String[data.length];
		String d;
		for (int i = 0; i < data.length; i++) {
			d = data[i];
			if (d != null) {
				result[i] = d;
			}
		}
		return result;
	}
	
	@Override
	public List getColumnAuxDataInfos() {
		return _base.getColumnAuxDataInfos();
	}
}

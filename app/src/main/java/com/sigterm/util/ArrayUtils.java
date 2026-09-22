package com.sigterm.util;

public class ArrayUtils {

	public static int GetIndexOf(String value, String[] values){
		for(int i=0; i < values.length; i++){
			if(values[i].equals(value))
				return i;
		}
		return 0;
	}
	public static int GetIndexOf(int value, int[] values){
		for(int i=0; i < values.length; i++){
			if(values[i] == value)
				return i;
		}
		return 0;
	}
}

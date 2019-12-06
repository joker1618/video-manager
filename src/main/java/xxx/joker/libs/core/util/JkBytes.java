package xxx.joker.libs.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by f.barbano on 23/01/2018.
 */


public class JkBytes {

	public static byte setBit(int num) {
		return (byte)(0x01 << num);
	}
	public static byte setBits(int... nums) {
		byte b = 0x00;
		for(int num : nums) {
			b |= setBit(num);
		}
		return b;
	}
	public static byte setBits(List<Integer> bitsNum) {
		byte b = 0x00;
		for(int num : bitsNum) {
			b |= setBit(num);
		}
		return b;
	}

	public static byte[] mergeArrays(byte[]... arrays) {
		return mergeArrays(Arrays.asList(arrays));
	}
	public static byte[] mergeArrays(List<byte[]> arrayList) {
		int len = 0;
		for(byte[] arr : arrayList)	len += arr.length;

		int idx = 0;
		byte[] toRet = new byte[len];
		for(int i = 0; i < arrayList.size(); i++) {
			byte[] bi = arrayList.get(i);
			for(int j = 0; j < bi.length; j++, idx++) {
				toRet[idx] = bi[j];
			}
		}

		return toRet;
	}

	public static byte[] toByteArray(List<Byte> list) {
		byte[] arr = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}
		return arr;
	}
	public static List<Byte> toByteList(byte[] arr) {
		List<Byte> toRet = new ArrayList<>();
		for (byte b : arr) {
			toRet.add(b);
		}
		return toRet;
	}

	public static boolean isBitSet(byte b, int bitNum) {
		int expected = 0x01 << bitNum;
		return (b & (0x01 << bitNum)) == expected;
	}

	public static boolean isEquals(byte b, int num) {
		return b == (byte)num;
	}
	public static boolean areEquals(byte[] arr1, byte[] arr2) {
		if(arr1 == null && arr2 == null)	return true;
		if(arr1 == null || arr2 == null)	return false;
		if(arr1.length != arr2.length) 		return false;

		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != arr2[i]) {
				return false;
			}
		}

		return true;
	}

}

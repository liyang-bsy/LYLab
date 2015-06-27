package net.vicp.lylab.utils;

import net.vicp.lylab.core.exception.LYException;

public final class ByteFunction {
	/**
	 * 数字转byte
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] IntToBytes4(int integer) {
		byte[] bytes = new byte[4];
		for (int ix = 0; ix < 4; ++ix) {
			int offset = 32 - (ix + 1) * 8;
			bytes[ix] = (byte) ((integer >> offset) & 0xff);
		}
		return bytes;
	}

	/**
	 * byte转数字
	 * 
	 * @param bytes
	 * @return
	 */
	public static int Bytes4ToInt(byte[] bytes) {
		int integer = 0;
		for (int ix = 0; ix < 4; ++ix) {
			integer <<= 8;
			integer |= (bytes[ix] & 0xff);
		}
		return integer;
	}

	/**
	 * byte转数字
	 * 
	 * @param bytes
	 * @return
	 */
	public static int Bytes4ToInt(byte[] bytes, int offset) {
		if(bytes.length < offset + 4)
			throw new LYException("Out of bounds, byte length:" + bytes.length + ", but offset is:" + offset);
		try {
			int num = 0;
			for (int ix = offset; ix < offset + 4; ++ix) {
				num <<= 8;
				num |= (bytes[ix] & 0xff);
			}
			return num;
		} catch (Exception e) {
			return 0;
		}
	}
}

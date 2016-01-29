package net.vicp.lylab.utils.internet.protocol;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;

/**
 * A custom protocol easy transfer Objects through socket.<br>
 * Data will be transfered as JSON string.<br>
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.19
 * @version 2.0.0
 */
public class PairProtocol extends NonCloneableBaseObject implements Protocol {

	public static void main(String[] arg) {
		Protocol protocol = new PairProtocol();
		{
			byte[] bytes = protocol.encode(new Pair<String, String>("0000", "111111"));
			System.out.println(protocol.validate(bytes, bytes.length));
			System.out.println(protocol.decode(bytes));
		}
		{
			byte[] bytes = protocol.encode(new Pair<String, String>("aaaa", "bbbbb"));
			bytes = Arrays.copyOf(bytes, 1400);
			for (int i = 1200; i >= 20; i--)
				bytes[i] = bytes[i - 20];
			System.out.println(protocol.validate(bytes, 20, bytes.length));
			System.out.println(protocol.decode(bytes, 20));
		}
	}

	protected final byte[] head = "Pair".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };

	@Override
	public byte[] getHead() {
		return head;
	}
	
	@Deprecated
	@Override
	public byte[] encode(Object obj) {
		@SuppressWarnings("unchecked")
		Pair<String, String> pair = (Pair<String, String>) obj;
		return encode(pair);
	}

	public byte[] encode(Pair<String, String> pair) {
		if(pair == null)
			throw new NullPointerException("Parameter pair is null");
		if(StringUtils.isBlank(pair.getLeft()))
			throw new NullPointerException("Parameter pair.getLeft() is blank");
		if(StringUtils.isBlank(pair.getRight()))
			throw new NullPointerException("Parameter pair.getRight() is blank");

		byte[] key = pair.getLeft().toString().getBytes();
		byte[] value = null;
		try {
			value = pair.getRight().toString().getBytes(CoreDef.CHARSET());
		} catch (UnsupportedEncodingException e) {
			throw new LYException("Parameter pair.getRight() encode failed");
		}
		
		byte[] length = Utils.IntToBytes4(key.length + value.length + splitSignal.length);
		
		int size = head.length + splitSignal.length + length.length + splitSignal.length + key.length
				+ splitSignal.length + value.length;

		byte[] bytes = new byte[size];
		int i = 0;
		for (int j = 0; j < head.length; j++)
			bytes[i++] = head[j];
		for (int j = 0; j < splitSignal.length; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < length.length; j++)
			bytes[i++] = length[j];
		for (int j = 0; j < splitSignal.length; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < key.length; j++)
			bytes[i++] = key[j];
		for (int j = 0; j < splitSignal.length; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < value.length; j++)
			bytes[i++] = value[j];
		return bytes;
	}

	@Override
	public Pair<String, String> decode(byte[] bytes) {
		return decode(bytes, 0);
	}

	@Override
	public Pair<String, String> decode(byte[] bytes, int offset) {
		if (bytes == null)
			return null;
		if (!Utils.checkHead(bytes, offset, head))
			throw new LYException("Bad data package: mismatch head");
		try {
			int headEndPosition = offset + head.length;

			if (bytes.length - 4 < headEndPosition + splitSignal.length)
				return null;
			int length = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);
			int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;

			int keyLength = Algorithm.KMPSearch(bytes, splitSignal, lengthEndPosition + splitSignal.length);
			if (keyLength == -1)
				return null;
			int keyEndPosition = lengthEndPosition + splitSignal.length + keyLength;

			String key = new String(bytes, lengthEndPosition + splitSignal.length, keyLength);
			String value = new String(bytes, keyEndPosition + splitSignal.length,
					length - keyLength - splitSignal.length, CoreDef.CHARSET());

			if (StringUtils.isBlank(key))
				throw new NullPointerException("Parameter pair.getLeft() is blank");
			if (StringUtils.isBlank(value))
				throw new NullPointerException("Parameter pair.getRight() is blank");

			return new Pair<String, String>(key, value);
		} catch (Exception e) {
			String originData = null;
			try {
				originData = new String(bytes, CoreDef.CHARSET());
			} catch (Exception ex) {
				originData = "Convert failed:" + Utils.getStringFromException(ex);
			}
			throw new LYException("Failed to convert data[" + originData + "] into Pair<String, String>", e);
		}
	}

	@Override
	public int validate(byte[] bytes, int len) {
		return validate(bytes, 0, len);
	}

	@Override
	public int validate(byte[] bytes, int offset, int len) {
		if (bytes == null)
			throw new LYException("Parameter bytes is null");
		if (len - offset < head.length)
			return 0;
		if (!Utils.checkHead(bytes, offset, len, head))
			throw new LYException("Bad data package: mismatch head\n" + new String(bytes, offset, len - offset).trim()
					+ "\nOriginal(start from " + offset + "):\n" + new String(bytes).trim());

		int headEndPosition = offset + head.length;

		if (bytes.length - 4 < headEndPosition + splitSignal.length)
			return 0;
		int length = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);
		int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		
		int dataEndPosition = length + lengthEndPosition + splitSignal.length;
		if (len < dataEndPosition)
			return 0;
		return dataEndPosition;
	}

}

//package net.vicp.lylab.utils.internet.protocol;
//
//import java.io.UnsupportedEncodingException;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//
//import org.apache.commons.lang3.StringUtils;
//
//import net.vicp.lylab.core.CoreDef;
//import net.vicp.lylab.core.NonCloneableBaseObject;
//import net.vicp.lylab.core.exceptions.LYException;
//import net.vicp.lylab.core.interfaces.Confirm;
//import net.vicp.lylab.core.interfaces.Protocol;
//import net.vicp.lylab.core.model.Pair;
//import net.vicp.lylab.core.model.CacheMessage;
//import net.vicp.lylab.utils.Algorithm;
//import net.vicp.lylab.utils.Utils;
//
///**
// * A custom protocol easy transfer Objects through socket.<br>
// * Data will be transfered as JSON string.<br>
// * <br><br>
// * Release Under GNU Lesser General Public License (LGPL).
// * 
// * @author Young
// * @since 2015.07.19
// * @version 2.0.0
// */
//public class PairMessageProtocol extends NonCloneableBaseObject implements Protocol {
//
//	public static void main(String[] arg) {
//		Protocol protocol = new PairMessageProtocol();
//		{
//			new CacheMessage(key, left, right, renew, expireTime);
//			byte[] bytes = protocol.encode(new Pair<String, String>("0000", "111111"));
//			System.out.println(protocol.validate(bytes, bytes.length));
//			System.out.println(protocol.decode(bytes));
//		}
//		{
//			byte[] bytes = protocol.encode(new Pair<String, String>("aaaa", "bbbbb"));
//			bytes = Arrays.copyOf(bytes, 1400);
//			for (int i = 1200; i >= 20; i--)
//				bytes[i] = bytes[i - 20];
//			System.out.println(protocol.validate(bytes, 20, bytes.length));
//			System.out.println(protocol.decode(bytes, 20));
//		}
//	}
//
//	protected final byte[] head = "Pair".getBytes();
//	protected final byte[] splitSignal = new byte[] { -15 };
//
//	@Override
//	public byte[] getHead() {
//		return head;
//	}
//	
//	@Deprecated
//	@Override
//	public byte[] encode(Confirm obj) {
//		@SuppressWarnings("unchecked")
//		CacheMessage pair = CacheMessage.class.cast(obj);
//		return encode(pair);
//	}
//	
//	public final int writeNext(byte[] bytes, int offset, byte[] next) {
//		int i = offset;
//		for (int j = 0; j < next.length; j++)
//			bytes[i++] = next[j];
//		return i;
//	}
//
//	public byte[] encode(CacheMessage cm) {
//		if (StringUtils.isBlank(cm.getKey()))
//			throw new NullPointerException("Parameter pair.getLeft() is blank");
//		Pair<String, byte[]> pair = cm.getPair();
//		if (pair == null)
//			throw new NullPointerException("Parameter pair is null");
//		if (StringUtils.isBlank(pair.getLeft()))
//			throw new NullPointerException("Parameter pair.getLeft() is blank");
//		if (pair.getRight() == null)
//			throw new NullPointerException("Parameter pair.getRight() is null");
//
//		byte[] code = Utils.IntToBytes4(cm.getCode());
//		byte[] renew = cm.isRenew() ? Utils.IntToBytes4(0) : Utils.IntToBytes4(1);
//		byte[] expireTime = Utils.IntToBytes4(cm.getExpireTime());
//		byte[] key = pair.getLeft().getBytes();
//		byte[] left = pair.getLeft().getBytes();
//		byte[] right = pair.getRight();
//
//		int intLength = code.length + splitSignal.length + renew.length + splitSignal.length + expireTime.length
//				+ splitSignal.length + key.length + splitSignal.length + left.length + splitSignal.length
//				+ right.length;
//
//		byte[] byteLength = Utils.IntToBytes4(intLength);
//
//		int size = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length + intLength;
//
//		byte[] bytes = new byte[size];
//		int offset = 0;
//		offset = writeNext(bytes, offset, head);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, byteLength);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, code);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, renew);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, expireTime);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, key);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, left);
//		offset = writeNext(bytes, offset, splitSignal);
//		offset = writeNext(bytes, offset, right);
//		offset = writeNext(bytes, offset, splitSignal);
//		
//		ByteBuffer bb = ByteBuffer.allocate(size);
//		bb.put()
//		
//		return bytes;
//	}
//
//	@Override
//	public CacheMessage decode(byte[] bytes) {
//		return decode(bytes, 0);
//	}
//
//	@Override
//	public CacheMessage decode(byte[] bytes, int offset) {
//		if (bytes == null)
//			return null;
//		if (!Utils.checkHead(bytes, offset, head))
//			throw new LYException("Bad data package: mismatch head");
//
//		try {
//			int code;
//			String key;
//			Pair<String, byte[]> pair;
//			boolean renew;
//			int expireTime;
//			int headEndPosition = offset + head.length;
//
//			if (bytes.length - 4 < headEndPosition + splitSignal.length)
//				return null;
//			int length = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);
//			int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
//
//			int keyLength = Algorithm.KMPSearch(bytes, splitSignal, lengthEndPosition + splitSignal.length);
//			if (keyLength == -1)
//				return null;
//			int keyEndPosition = lengthEndPosition + splitSignal.length + keyLength;
//
//			int keyLength = Algorithm.KMPSearch(bytes, splitSignal, lengthEndPosition + splitSignal.length);
//			if (keyLength == -1)
//				return null;
//			int keyEndPosition = lengthEndPosition + splitSignal.length + keyLength;
//
//			String key = new String(bytes, lengthEndPosition + splitSignal.length, keyLength);
//			String value = new String(bytes, keyEndPosition + splitSignal.length,
//					length - keyLength - splitSignal.length, CoreDef.CHARSET());
//
//			if (StringUtils.isBlank(key))
//				throw new NullPointerException("Parameter pair.getLeft() is blank");
//			if (StringUtils.isBlank(value))
//				throw new NullPointerException("Parameter pair.getRight() is blank");
//
//			Pair<String, byte[]> pair = new Pair<>(key, value);
//			return new CacheMessage(key, pair);
//		} catch (Exception e) {
//			String originData = null;
//			try {
//				originData = new String(bytes, CoreDef.CHARSET());
//			} catch (Exception ex) {
//				originData = "Convert failed:" + Utils.getStringFromException(ex);
//			}
//			throw new LYException("Failed to convert data[" + originData + "] into Pair<String, String>", e);
//		}
//	}
//
//	@Override
//	public int validate(byte[] bytes, int len) {
//		return validate(bytes, 0, len);
//	}
//
//	@Override
//	public int validate(byte[] bytes, int offset, int len) {
//		if (bytes == null)
//			throw new LYException("Parameter bytes is null");
//		if (len - offset < head.length)
//			return 0;
//		if (!Utils.checkHead(bytes, offset, len, head))
//			throw new LYException("Bad data package: mismatch head\n" + new String(bytes, offset, len - offset).trim()
//					+ "\nOriginal(start from " + offset + "):\n" + new String(bytes).trim());
//
//		int headEndPosition = offset + head.length;
//
//		if (bytes.length - 4 < headEndPosition + splitSignal.length)
//			return 0;
//		int length = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);
//		int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
//		
//		int dataEndPosition = length + lengthEndPosition + splitSignal.length;
//		if (len < dataEndPosition)
//			return 0;
//		return dataEndPosition;
//	}
//
//}
//
///*
//
////int i = 0;
////for (int j = 0; j < head.length; j++)
////	bytes[i++] = head[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < byteLength.length; j++)
////	bytes[i++] = byteLength[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < code.length; j++)
////	bytes[i++] = code[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < renew.length; j++)
////	bytes[i++] = renew[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < expireTime.length; j++)
////	bytes[i++] = expireTime[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < key.length; j++)
////	bytes[i++] = key[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < left.length; j++)
////	bytes[i++] = left[j];
////for (int j = 0; j < splitSignal.length; j++)
////	bytes[i++] = splitSignal[j];
////for (int j = 0; j < right.length; j++)
////	bytes[i++] = right[j];
//*/
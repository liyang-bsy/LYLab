package net.vicp.lylab.utils.internet.protocol;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.CacheMessage;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;

/**
 * A custom protocol to transfer CacheMessage.<br>
 * Data will be transfered as organized byte array.<br>
 * <br>
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.19
 * @version 2.0.0
 */
public class CacheMessageProtocol extends NonCloneableBaseObject implements Protocol {

	protected final byte[] head = "Pair".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };

	@Override
	public byte[] getHead() {
		return head;
	}

	@Deprecated
	@Override
	public byte[] encode(Confirm obj) {
		CacheMessage pair = CacheMessage.class.cast(obj);
		return encode(pair);
	}

	public byte[] encode(CacheMessage cm) {
		if (StringUtils.isBlank(cm.getKey()))
			throw new NullPointerException("Parameter pair.getLeft() is blank");
		Pair<String, byte[]> pair = cm.getPair();
		if (pair == null)
			throw new NullPointerException("Parameter pair is null");
		if (StringUtils.isBlank(pair.getLeft()))
			throw new NullPointerException("Parameter pair.getLeft() is blank");
		if (pair.getRight() == null)
			throw new NullPointerException("Parameter pair.getRight() is null");

		byte[] code = Utils.IntToBytes4(cm.getCode());
		byte[] renew = cm.isRenew() ? Utils.IntToBytes4(1) : Utils.IntToBytes4(0);
		byte[] expireTime = Utils.IntToBytes4(cm.getExpireTime());
		byte[] key = cm.getKey().getBytes();
		byte[] left = pair.getLeft().getBytes();
		byte[] right = pair.getRight();

		int intLength = code.length + splitSignal.length + renew.length + splitSignal.length + expireTime.length
				+ splitSignal.length + key.length + splitSignal.length + left.length + splitSignal.length + right.length
				+ splitSignal.length;

		byte[] byteLength = Utils.IntToBytes4(intLength);

		int size = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length + intLength;

		ByteBuffer bb = ByteBuffer.allocate(size);
		bb.put(head);
		bb.put(splitSignal);
		bb.putInt(intLength);
		bb.putInt(cm.getCode());

		byte[] bytes = new byte[size];
		int offset = 0;
		offset = Utils.writeNext(bytes, offset, head);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, byteLength);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, code);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, renew);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, expireTime);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, key);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, left);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, right);
		offset = Utils.writeNext(bytes, offset, splitSignal);

		return bytes;
	}

	@Override
	public CacheMessage decode(byte[] bytes) {
		return decode(bytes, 0);
	}

	@Override
	public CacheMessage decode(byte[] bytes, int offset) {
		if (bytes == null)
			return null;
		if (!Utils.checkHead(bytes, offset, head))
			throw new LYException("Bad data package: mismatch head");
		try {
			int dataLength = 0;
			int endPosition = offset + head.length + splitSignal.length;
			if (bytes.length - 4 < endPosition)
				return null;
			int length = Utils.Bytes4ToInt(bytes, endPosition);
			int dataSegmentStart = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length;
			if (dataSegmentStart + length > bytes.length)
				return null;

			endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

			int code = Utils.Bytes4ToInt(bytes, endPosition);
			endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

			boolean renew = Utils.Bytes4ToInt(bytes, endPosition) == 0 ? false : true;
			endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

			int expireTime = Utils.Bytes4ToInt(bytes, endPosition);
			endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			String key = new String(bytes, endPosition, dataLength);
			endPosition = endPosition + dataLength + splitSignal.length;

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			String left = new String(bytes, endPosition, dataLength);
			endPosition = endPosition + dataLength + splitSignal.length;

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			byte[] right = new byte[dataLength];
			System.arraycopy(bytes, endPosition, right, 0, dataLength);
			endPosition = endPosition + dataLength + splitSignal.length;

			return new CacheMessage(code, key, left, right, renew, expireTime);
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

		int dataLength = 0;
		int endPosition = offset + head.length + splitSignal.length;
		if (bytes.length - 4 < endPosition)
			return 0;
		int length = Utils.Bytes4ToInt(bytes, endPosition);
		if (head.length + splitSignal.length + length + splitSignal.length > bytes.length)
			return 0;

		endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;
		endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;
		endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;
		endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

		dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
		if (dataLength == -1)
			return 0;
		endPosition = endPosition + dataLength + splitSignal.length;

		dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
		if (dataLength == -1)
			return 0;
		endPosition = endPosition + dataLength + splitSignal.length;

		dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
		if (dataLength == -1)
			return 0;
		endPosition = endPosition + dataLength + splitSignal.length;

		if (len < endPosition)
			return 0;
		return endPosition;
	}

}

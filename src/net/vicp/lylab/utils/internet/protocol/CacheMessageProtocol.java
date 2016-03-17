package net.vicp.lylab.utils.internet.protocol;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.CacheMessage;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.model.SimpleHeartBeat;
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

	protected final byte[] head = "RDMA".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };

	@Override
	public byte[] getHead() {
		return head;
	}

	@Deprecated
	@Override
	public byte[] encode(Confirm obj) {
		if(obj instanceof HeartBeat)
			return encode((HeartBeat) obj);
		CacheMessage pair = CacheMessage.class.cast(obj);
		return encode(pair);
	}
	
	public byte[] encode(HeartBeat hb) {
		int iLength = 0;

		byte[] byteLength = Utils.int2Bytes(iLength);

		int size = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length + iLength;

		byte[] bytes = new byte[size];
		int offset = 0;
		offset = Utils.writeNext(bytes, offset, head);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, byteLength);
		offset = Utils.writeNext(bytes, offset, splitSignal);

		return bytes;
	}

	public byte[] encode(CacheMessage cm) {
		if (StringUtils.isBlank(cm.getAction()))
			throw new NullPointerException("Parameter action is blank");
		Pair<String, byte[]> pair = cm.getPair();
		if (pair == null)
			throw new NullPointerException("Parameter pair is null");
		if (pair.getLeft() == null)
			pair.setLeft("");
		if (pair.getRight() == null)
			pair.setRight(new byte[0]);

		byte[] code = Utils.int2Bytes(cm.getCode());
		byte[] renew = cm.isRenew() ? new byte[] { 1 } : new byte[] { 0 };
		byte[] expireTime = Utils.int2Bytes(cm.getExpireTime());
		byte[] action = cm.getAction().getBytes();
		byte[] left = pair.getLeft().getBytes();
		byte[] right = pair.getRight();
		byte[] cmpData = cm.getCmpData();

		int iLength = code.length + splitSignal.length + renew.length + splitSignal.length + expireTime.length
				+ splitSignal.length + action.length + splitSignal.length + left.length + splitSignal.length + right.length
				+ splitSignal.length + cmpData.length + splitSignal.length;

		byte[] byteLength = Utils.int2Bytes(iLength);

		int size = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length + iLength;

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
		offset = Utils.writeNext(bytes, offset, action);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, left);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, right);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, cmpData);
		offset = Utils.writeNext(bytes, offset, splitSignal);

		return bytes;
	}

	@Override
	public Confirm decode(byte[] bytes) {
		return decode(bytes, 0);
	}

	@Override
	public Confirm decode(byte[] bytes, int offset) {
		if (bytes == null)
			return null;
		if (!Utils.checkHead(bytes, offset, head))
			throw new LYException("Bad data package: mismatch head");
		try {
			int dataLength = 0;
			int endPosition = offset + head.length + splitSignal.length;
			if (bytes.length - 4 < endPosition)
				return null;
			int length = Utils.bytes2Int(bytes, endPosition);
			endPosition = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length;
			if (endPosition + length > bytes.length)
				return null;
			if(length == 0)
				return new SimpleHeartBeat();

			int code = Utils.bytes2Int(bytes, endPosition);
			endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

			boolean renew = bytes[endPosition] == 0 ? false : true;
			endPosition = endPosition + CoreDef.SIZEOF_BOOLEAN + splitSignal.length;

			int expireTime = Utils.bytes2Int(bytes, endPosition);
			endPosition = endPosition + CoreDef.SIZEOF_INTEGER + splitSignal.length;

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			String action = new String(bytes, endPosition, dataLength);
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

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			byte[] cmpData = new byte[dataLength];
			System.arraycopy(bytes, endPosition, cmpData, 0, dataLength);
			endPosition = endPosition + dataLength + splitSignal.length;

			return new CacheMessage(code, action, left, right, renew, expireTime).setCmpData(cmpData);
		} catch (Exception e) {
			String originData = null;
			try {
				originData = new String(bytes, CoreDef.CHARSET());
			} catch (Exception ex) {
				originData = "Convert failed:" + Utils.getStringFromException(ex);
			}
			throw new LYException("Failed to convert data[" + originData + "] into CacheMessage", e);
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

		int endPosition = offset + head.length + splitSignal.length;
		if (len - 4 < endPosition)
			return 0;
		int length = Utils.bytes2Int(bytes, endPosition);
		endPosition += CoreDef.SIZEOF_INTEGER + splitSignal.length + length;

		if (len < endPosition)
			return 0;
		return endPosition;
	}

}

package net.vicp.lylab.core.model;

/**
 * Base cache message
 * 
 * @author Young Lee
 * 
 */
public class CacheMessage extends CodeConfirm {
	protected String action;
	protected Pair<String, byte[]> pair;
	protected byte[] cmpData;
	protected boolean renew;
	protected int expireTime;

	public CacheMessage(int code, String action, String left, byte[] right, boolean renew, int expireTime) {
		super(code);
		this.action = action;
		this.pair = new Pair<>(left, right);
		this.renew = renew;
		this.expireTime = expireTime;
		cmpData = new byte[0];
	}

	public CacheMessage(int code, String action) {
		this(code, action, "", new byte[0], false, 0);
	}

	public CacheMessage(int code) {
		this(code, "Invalid", "", new byte[0], false, 0);
	}

	public CacheMessage() {
		this(-1);
	}

	public CacheMessage copyBasicInfo(CacheMessage other) {
		setAction(other.getAction());
		setRenew(other.isRenew());
		setExpireTime(other.getExpireTime());
		getPair().setLeft(other.getPair().getLeft());
		return this;
	}

	public String getAction() {
		return action;
	}

	public CacheMessage setAction(String action) {
		this.action = action;
		return this;
	}

	public Pair<String, byte[]> getPair() {
		return pair;
	}

	public CacheMessage setPair(Pair<String, byte[]> pair) {
		this.pair = pair;
		return this;
	}

	public byte[] getCmpData() {
		return cmpData;
	}

	public CacheMessage setCmpData(byte[] cmpData) {
		this.cmpData = cmpData;
		return this;
	}

	public boolean isRenew() {
		return renew;
	}

	public CacheMessage setRenew(boolean renew) {
		this.renew = renew;
		return this;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public CacheMessage setExpireTime(int expireTime) {
		this.expireTime = expireTime;
		return this;
	}

	public CacheMessage success() {
		this.setCode(0);
		return this;
	}

	public CacheMessage fail(int code) {
		this.setCode(code);
		return this;
	}

	@Override
	public String toString() {
		return "CacheMessage [action=" + action + ", pair=" + pair + ", renew=" + renew + ", expireTime=" + expireTime
				+ ", code=" + code + "]";
	}

}

package net.vicp.lylab.core.model;

/**
 * Base cache message
 * 
 * @author Young Lee
 * 
 */
public class CacheMessage extends SimpleConfirm {
	protected String key;
	protected Pair<String, byte[]> pair;
	protected boolean renew;
	protected int expireTime;


	public CacheMessage(String key, String left, byte[] right, boolean renew, int expireTime) {
		super();
		this.key = key;
		this.pair = new Pair<>(left, right);
		this.renew = renew;
		this.expireTime = expireTime;
	}

	public CacheMessage() {
		super(-1);
		key = "Invalid";
		pair = new Pair<String, byte[]>();
		renew = false;
		expireTime = 0;
	}
	
	public void copyBasicInfo(CacheMessage other) {
		setKey(other.getKey());
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Pair<String, byte[]> getPair() {
		return pair;
	}

	public void setPair(Pair<String, byte[]> pair) {
		this.pair = pair;
	}

	public boolean isRenew() {
		return renew;
	}

	public void setRenew(boolean renew) {
		this.renew = renew;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public void success() {
		this.setCode(0);
	}

	public void fail(String msg) {
		this.setCode(1);
	}
	
	public void fail(int code, String msg) {
		this.setCode(1);
	}

	@Override
	public String toString() {
		return "Message [key=" + key + ", pair=" + pair + ", code=" + code + "]";
	}

}

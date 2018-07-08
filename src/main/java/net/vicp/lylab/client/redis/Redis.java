package net.vicp.lylab.client.redis;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.LifeCycle;
import redis.clients.jedis.Jedis;

/**
 * Redis access client
 * 
 * @author Young Lee
 */
public class Redis extends NonCloneableBaseObject implements LifeCycle {
	private Jedis jedis;
	private String ip;
	private int port;
	private String password;

	@Override
	public void initialize() {
		jedis = new Jedis(ip, port);
		jedis.auth(password);
	}

	@Override
	public void close() throws Exception {
		jedis.close();
	}

	public Long decr(String key) {
		return jedis.decr(key);
	}

	public Long decrBy(String key, long integer) {
		return jedis.decrBy(key, integer);
	}

	public Boolean exists(String key) {
		return jedis.exists(key);
	}

	public Long expire(String key, int seconds) {
		return jedis.expire(key, seconds);
	}

	public Long expireAt(String key, long unixTime) {
		return jedis.expireAt(key, unixTime);
	}

	public String get(String key) {
		return jedis.get(key);
	}

	public Long incr(String key) {
		return jedis.incr(key);
	}

	public Long incrBy(String key, long integer) {
		return jedis.incrBy(key, integer);
	}

	public String set(String key, String value, String nxxx, String expx,
			long time) {
		return jedis.set(key, value, nxxx, expx, time);
	}

	public String set(String key, String value) {
		return jedis.set(key, value);
	}

	// getter && setter
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

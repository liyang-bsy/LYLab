package net.vicp.lylab.mybatis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.utils.Utils;

import org.apache.ibatis.session.SqlSession;

public final class MultiSourcesSession extends NonCloneableBaseObject implements AutoCloseable {
	private SQLSessionFactory sqlSessionFactory;
	private Map<String, SqlSession> sessions = new HashMap<String, SqlSession>();
	
	public MultiSourcesSession() {
		for(String env:sqlSessionFactory.getEnvironments())
			sessions.put(env, sqlSessionFactory.getSqlSessionFactory(env).openSession(false));
	}

	@Override
	public void close() {
		for(String env:sqlSessionFactory.getEnvironments())
			try {
				sessions.remove(env).close();
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
	}

	public void commit() {
		for(String env:sqlSessionFactory.getEnvironments())
			sessions.get(env).commit();
	}

	public void rollback() {
		for(String env:sqlSessionFactory.getEnvironments())
			sessions.get(env).rollback();
	}

	public boolean containsKey(Object key) {
		return sessions.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return sessions.containsValue(value);
	}

	public SqlSession get(Object key) {
		return sessions.get(key);
	}

	public SqlSession put(String key, SqlSession value) {
		return sessions.put(key, value);
	}

	public Set<String> keySet() {
		return sessions.keySet();
	}
	
}

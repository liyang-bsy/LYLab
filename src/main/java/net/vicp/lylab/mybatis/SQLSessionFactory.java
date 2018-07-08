package net.vicp.lylab.mybatis;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.utils.Utils;

public final class SQLSessionFactory extends NonCloneableBaseObject implements Initializable {
	
	private Map<String, SqlSessionFactory> sqlSessionFactories;
	private String[] environments = new String[] { };
	private String resource = null;

	public void initialize() {
		sqlSessionFactories = new ConcurrentHashMap<String, SqlSessionFactory>();
		try {
			for (String env : environments) {
				Reader reader = Resources.getResourceAsReader(resource);
				sqlSessionFactories.put(env, new SqlSessionFactoryBuilder().build(reader, env));
				log.debug("Sql session factory [" + env + "] created");
			}
		} catch (IOException e) {
			log.error("Error creating sql session factory, reason:" + Utils.getStringFromException(e));
		}
	}

	public MultiSourcesSession newSessions() {
		MultiSourcesSession mss = new MultiSourcesSession(this);
		mss.initialize();
		return mss;
	}

	public SqlSessionFactory getSqlSessionFactory(String env) {
		return sqlSessionFactories.get(env);
	}

	public Set<String> getEnvironments() {
		return sqlSessionFactories.keySet();
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setEnvironments(String[] environments) {
		this.environments = environments;
	}

}

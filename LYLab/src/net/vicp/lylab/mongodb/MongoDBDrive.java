package net.vicp.lylab.mongodb;

import java.util.Arrays;

import net.vicp.lylab.core.UniqueBaseObject;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class MongoDBDrive extends UniqueBaseObject implements ApplicationListener {
	/**
	 * MongoDB host name or IP:port
	 * 127.0.0.1:27017	(Example)
	 */
	private String url;
	// 数据库名称
	private String database;
	// 数据库访问所需的用户名
	private String username;
	// 对应的密码
	private String password;
	// 是否显示查询字符串
	private Boolean showQuery;
	// MongoDB本体
	private MongoClient mongoClient;
	
	@Override
	public void onApplicationEvent(ApplicationEvent arg0) {
		getMongo();
	}
	
	private boolean init()
	{
		if(mongoClient != null) return false;
		self = this;
		try {
			MongoCredential credential = MongoCredential.createCredential(
					this.getUsername(),
					this.getDatabase(),
					this.getPassword().toCharArray());
			this.mongoClient = new MongoClient(
					new ServerAddress(this.getUrl()),
					Arrays.asList(credential));
			this.mongoClient.setWriteConcern(WriteConcern.NORMAL);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public MongoClient getMongo() {
		if(this.mongoClient == null) this.init();
		return this.mongoClient;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getShowQuery() {
		return showQuery;
	}

	public void setShowQuery(Boolean showQuery) {
		this.showQuery = showQuery;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

}

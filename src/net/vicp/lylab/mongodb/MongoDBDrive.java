package net.vicp.lylab.mongodb;

import java.util.Arrays;

import net.vicp.lylab.core.NonCloneableBaseObject;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class MongoDBDrive extends NonCloneableBaseObject implements ApplicationListener {
	
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
	// Keep unique
	private static MongoDBDrive instance = null;

	@Override
	public void onApplicationEvent(ApplicationEvent arg) {
		if(instance == null)
		{
			System.out.println("MongoDBDrive - Initialization started");
			
			instance = this;
			instance.init();
		}
	}
	
	private void init()
	{
		if(mongoClient != null) return;
		try {
			MongoCredential credential = MongoCredential.createCredential(
					MongoDBDrive.getInstance().getUsername(),
					MongoDBDrive.getInstance().getDatabase(),
					MongoDBDrive.getInstance().getPassword().toCharArray());
			MongoDBDrive.getInstance().mongoClient = new MongoClient(
					new ServerAddress(MongoDBDrive.getInstance().getUrl()),
					Arrays.asList(credential));
			MongoDBDrive.getInstance().mongoClient.setWriteConcern(WriteConcern.NORMAL);
		} catch (Exception e) {
			return;
		}
		return;
	}

	public MongoClient getMongo() {
		if(MongoDBDrive.getInstance().mongoClient == null) MongoDBDrive.getInstance().init();
		return MongoDBDrive.getInstance().mongoClient;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		MongoDBDrive.getInstance().url = url;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		MongoDBDrive.getInstance().database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		MongoDBDrive.getInstance().username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		MongoDBDrive.getInstance().password = password;
	}

	public Boolean getShowQuery() {
		return showQuery;
	}

	public void setShowQuery(Boolean showQuery) {
		MongoDBDrive.getInstance().showQuery = showQuery;
	}

	public static MongoClient getMongoClient() {
		return getInstance().mongoClient;
	}

	public static void setMongoClient(MongoClient mongoClient) {
		MongoDBDrive.getInstance().mongoClient = mongoClient;
	}
	
	public static MongoDBDrive getInstance() {
		return instance;
	}
	
	public static void setInstance(MongoDBDrive instance) {
		MongoDBDrive.instance = instance;
	}

}

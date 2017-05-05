package net.vicp.lylab.mongodb;

import java.util.Arrays;
import java.util.Map;

import org.bson.Document;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Initializable;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBDrive extends NonCloneableBaseObject implements Initializable {
	// 数据库连接的url，比如 127.0.0.1
	private String url;
	// 数据库名称
	private String database;
	// 数据库访问所需的用户名
	private String username;
	// 对应的密码
	private String password;
	// MongoDB本体
	private MongoClient mongoClient;
	// MongoDB对外提供读写功能的数据库实体
	private MongoDatabase mongoDatabase;
	
	private Map<String, MongoDBService> serviceCache;

	@Override
	public void initialize() {
		if (mongoClient != null) {
			return;
		}
		try {
			MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
			mongoClient = new MongoClient(new ServerAddress(url), Arrays.asList(credential));
			mongoDatabase = mongoClient.getDatabase(database);
		} catch (Exception e) {
			throw new LYException("无法初始化mongoDB连接", e);
		}
		return;
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

	public MongoDBService getMongoDBService(String collectionName) {
		if (serviceCache.containsKey(collectionName)) {
			return serviceCache.get(collectionName);
		}
		if(mongoDatabase == null)
			throw new LYException("mongoDatabase为null，可能尚未初始化");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
		MongoDBService mongoDBService = new MongoDBService(mongoCollection);
		serviceCache.put(collectionName, mongoDBService);
		return mongoDBService;
	}

}

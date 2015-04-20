package net.vicp.lylab.mongodb;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * BasicDao for MongoDBService
 * @author		liyang
 * @version		0.1.2
 */
public class MongoDBDao<T> extends BasicDAO<T, String> {
	public MongoDBDao(Class<T> entityClass) {
		super(entityClass, ((MongoDBDrive) MongoDBDrive.getSelf()).getMongo(), new Morphia(), ((MongoDBDrive) MongoDBDrive.getSelf()).getDatabase());
	}
}

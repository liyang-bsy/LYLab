package net.vicp.lylab.mongodb;

import java.util.List;

import net.vicp.lylab.core.model.OrderBy;
import net.vicp.lylab.core.model.Page;

import org.bson.Document;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * MongoDBService for MongoDBOperation
 * 
 * @author liyang
 * @version 0.0.1
 */
public class MongoDBService {

	MongoCollection<Document> mongoCollection;

	public MongoDBService(MongoCollection<Document> mongoCollection) {
		this.mongoCollection = mongoCollection;
	}

	// 查询除重
	public <TResult> DistinctIterable<TResult> distinct(String fieldName,
			Class<TResult> resultClass) {
		return mongoCollection.distinct(fieldName, resultClass);
	}

	// 条件查询
	public FindIterable<Document> find(Document filter) {
		return find(filter, new Page(), new OrderBy("_id", "desc"));
	}

	// 条件查询
	public FindIterable<Document> find(Document filter, Page page, OrderBy orderBy) {
		// FIXME 排序暂时没写
		return mongoCollection.find(filter).sort(new Document()).skip(page.getIndex()).limit(page.getPageSize());
	}

	// 查询统计
	public long count(Document filter) {
		return mongoCollection.count(filter);
	}

	// 增删改
	public void insert(Document document) {
		mongoCollection.insertOne(document);
	}

	public void insertMany(List<? extends Document> documents) {
		mongoCollection.insertMany(documents);
	}

	public UpdateResult update(String id, Document update) {
		Document filter = new Document();
		filter.put("_id", id);
		return mongoCollection.updateOne(filter, update);
	}

	public UpdateResult updateMany(Document filter, Document update) {
		return mongoCollection.updateMany(filter, update);
	}

	public DeleteResult delete(String id) {
		Document filter = new Document();
		filter.put("_id", id);
		return mongoCollection.deleteOne(filter);
	}

	public DeleteResult deleteMany(Document filter) {
		return mongoCollection.deleteMany(filter);
	}

	// 索引操作
	public ListIndexesIterable<Document> listIndexes() {
		return mongoCollection.listIndexes();
	}

	public String createIndex(Document keys) {
		return mongoCollection.createIndex(keys);
	}

	public void dropIndex(String indexName) {
		mongoCollection.dropIndex(indexName);
	}

}

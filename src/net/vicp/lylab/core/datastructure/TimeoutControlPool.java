package net.vicp.lylab.core.datastructure;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDefine;

/**
 * 
 * @author liyang
 *
 * STL: “池”，自动管理的并发池
 *
 */
public class TimeoutControlPool {// extends AbstractPool<BaseObject> {

//	public TimeoutControlPool() {
//		super(new ConcurrentHashMap<Long, BaseObject>());
//	}
//	
//	public TimeoutControlPool(Map<Long, BaseObject> c) {
//		super(c);
//	}
//
//	@Override
//	public void close() throws IOException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public synchronized void add(BaseObject t) {
//		while (true) {
//			Integer size = container.size();
//			if (size >= MAX_SIZE) {
//				try {
//					container.wait(CoreDefine.waitingThreshold);
//					continue;
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			if (size <= MAX_SIZE && size >= 0) {
//				if(maxId == Long.MAX_VALUE) maxId = 0L;
//				t.setObjId(maxId);
//				container.put(maxId, t);
//				break;
//			}
//		}
//		
//	}
//
//	@Override
//	public BaseObject accessNextOne() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public BaseObject accessRandomOne() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<BaseObject> accessMany(int amount) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public BaseObject remove(long t) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	
}

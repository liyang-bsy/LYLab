package net.vicp.lylab.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;

/**
 * Initializer is used to initial LifeCycle registered with config
 * @author Young
 *
 */
public final class Singleton extends NonCloneableBaseObject implements LifeCycle {
	private static Map<String, Object> singletonManager;
	private static AtomicBoolean inited = new AtomicBoolean(false);
	private static Singleton instance = null;

	private Singleton() {
		initialize();
	}

	@Override
	public void initialize() {
		if(inited.getAndSet(true) == true)
			return;
		log.info("Initializer - Initialization started");
		List<String> keyList = CoreDef.config.getConfig("Singleton").keyList();
		singletonManager = new ConcurrentHashMap<String, Object>(keyList.size());
		Object tmp;
		for (String key : keyList) {
			try {
				tmp = CoreDef.config.getConfig("Singleton").getObject(key);
				singletonManager.put(key, tmp);
				if (tmp.getClass() == String.class) {
					tmp = CoreDef.config.getConfig("Singleton").getNewInstance(key);
					if (tmp instanceof Initializable) {
						try {
							((Initializable) tmp).initialize();
							log.info(tmp.getClass().getSimpleName() + " - Started");
						} catch (Throwable t) {
							log.error(Utils.getStringFromThrowable(t));
						}
					}
				}
				singletonManager.put(key, tmp);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	@Override
	public void close() {
		inited.set(false);
		log.info("Initializer - Termination started");
		Object obj;
		for (String key : singletonManager.keySet()) {
			obj = singletonManager.get(key);
			if (obj instanceof AutoCloseable) {
				try {
					((AutoCloseable) obj).close();
					log.info(obj.getClass().getSimpleName() + " - Closed");
				} catch (Exception e) {
					log.info(obj.getClass().getSimpleName() + " - Close failed:" + Utils.getStringFromException(e));
				}
			}
		}
	}

	public static Object get(String key) {
		return singletonManager.get(key);
	}
	
	public synchronized static void createInstance() {
		if(instance == null)
			instance = new Singleton();
	}
	
	public synchronized static void destroyInstance() {
		if(instance != null)
			instance.close();
		instance = null;
	}

}

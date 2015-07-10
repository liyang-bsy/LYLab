package net.vicp.lylab.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.config.TreeConfig;

/**
 * Initializer is used to initial LifeCycle registered with config
 * @author Young
 *
 */
public final class GlobalInitializer extends NonCloneableBaseObject implements LifeCycle {
	private static Config config;
	private static Map<String, Object> singletonManager;
	private static AtomicBoolean inited = new AtomicBoolean(false);
	private static GlobalInitializer instance = null;

	private GlobalInitializer(String configFile) {
		config = new TreeConfig(configFile);
		initialize();
	}

	private GlobalInitializer(Config config) {
		GlobalInitializer.config = config;
		initialize();
	}

	@Override
	public void initialize() {
		if(inited.getAndSet(true) == true)
			return;
		log.info("Initializer - Initialization started");
		Set<String> keySet = config.keySet();
		singletonManager = new ConcurrentHashMap<String, Object>(keySet.size());
		Object tmp;
		for (String key : keySet) {
			try {
				Class<?> instanceClass = Class.forName(config.getString(key));
				tmp = instanceClass.newInstance();
				if (tmp instanceof LifeCycle) {
					log.info(tmp.getClass().getSimpleName() + " - Initialized");
					((LifeCycle) tmp).initialize();
				}
				singletonManager.put(key, tmp);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	@Override
	public void terminate() {
		inited.set(false);
		log.info("Initializer - Termination started");
		Object obj;
		for (String key : singletonManager.keySet()) {
			obj = singletonManager.get(key);
			if (obj instanceof LifeCycle) {
				log.info(obj.getClass().getSimpleName() + " - Terminated");
				((LifeCycle) obj).terminate();
			}
		}
	}
	
	private Object _get(String key) {
		return singletonManager.get(key);
	}

	public static Object get(String key) {
		return getInstance()._get(key);
	}
	
	public static GlobalInitializer getInstance() {
		if(instance == null)
			createInstance(config);
		return instance;
	}

	public synchronized static void createInstance(Config config) {
		if(instance == null)
			instance = new GlobalInitializer(config);
	}

	public synchronized static void createInstance(String configFile) {
		if(instance == null)
			instance = new GlobalInitializer(configFile);
	}

	public static void setConfig(Config config) {
		GlobalInitializer.config = config;
	}

}

package net.vicp.lylab.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.interfaces.InitializeConfig;
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
	private static TreeConfig rootConfig;
	private static Map<String, Object> singletonManager;
	private static AtomicBoolean inited = new AtomicBoolean(false);
	private static GlobalInitializer instance = null;

	private GlobalInitializer(Config config, TreeConfig rootConfig) {
		GlobalInitializer.config = config;
		GlobalInitializer.rootConfig = rootConfig;
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
				if (rootConfig != null && tmp instanceof InitializeConfig) {
					((InitializeConfig) tmp).obtainConfig(rootConfig.getConfig(key));
				}
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

	public static Object get(String key) {
		return singletonManager.get(key);
	}
	
	public synchronized static void createInstance(Config config, TreeConfig rootConfig) {
		if(instance == null)
			instance = new GlobalInitializer(config, rootConfig);
	}
	
	public synchronized static void destroyInstance() {
		if(instance != null)
			instance.terminate();
		instance = null;
	}

	public static void setConfig(Config config) {
		GlobalInitializer.config = config;
	}

	public static void setRootConfig(TreeConfig rootConfig) {
		GlobalInitializer.rootConfig = rootConfig;
	}

}
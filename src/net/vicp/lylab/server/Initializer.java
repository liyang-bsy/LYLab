package net.vicp.lylab.server;

import java.util.Set;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.config.TreeConfig;

public class Initializer extends NonCloneableBaseObject implements LifeCycle {
	private Config config;
	private Object[] objectManager;
	
	public Initializer(String configFile)
	{
		this.config = new TreeConfig(configFile);
	}
	
	@Override
	public void initialize() {
		Set<String> keySet = config.keySet();
		objectManager = new Object[keySet.size()];
		int i = 0;
		for(String key:keySet)
		{
			try {
				Class<?> instanceClass = Class.forName(config.getString(key));
				objectManager[i] = instanceClass.newInstance();
				if(objectManager[i] instanceof LifeCycle)
					((LifeCycle) objectManager[i]).initialize();
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
			i++;
		}
	}

	@Override
	public void terminate() {
		for(Object obj: objectManager)
			if(obj instanceof LifeCycle)
				((LifeCycle) obj).terminate();
	}

}

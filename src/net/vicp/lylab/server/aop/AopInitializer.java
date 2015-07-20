package net.vicp.lylab.server.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.Utils;

public class AopInitializer extends NonCloneableBaseObject implements LifeCycle, InitializeConfig {
	protected Config config;
	protected List<Filter> filterChain;

	@Override
	public void initialize() {
		if(config == null) return;
		filterChain = new ArrayList<Filter>();

		Set<String> keySet = config.keySet();
		for (String key : keySet) {
			try {
				Class<?> instanceClass = Class.forName(config.getString(key));
				Filter tmp = (Filter) instanceClass.newInstance();
				filterChain.add(tmp);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
		Aop.setFilterChain(filterChain);
	}

	@Override
	public void close() {
	}

	@Override
	public void obtainConfig(Config config) {
		this.config = config;
	}

}
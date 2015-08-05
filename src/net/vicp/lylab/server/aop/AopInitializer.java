package net.vicp.lylab.server.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;

public class AopInitializer extends NonCloneableBaseObject implements Initializable {
	protected List<Filter> filterChain;

	@Override
	public void initialize() {
		filterChain = new ArrayList<Filter>();

		Set<String> keySet = CoreDef.config.getConfig("AopInitializer").keySet();
		for (String key : keySet) {
			try {
				Class<?> instanceClass = Class.forName(CoreDef.config.getConfig("AopInitializer").getString(key));
				Filter tmp = (Filter) instanceClass.newInstance();
				filterChain.add(tmp);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
		Aop.setFilterChain(filterChain);
	}

}
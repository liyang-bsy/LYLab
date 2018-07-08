package net.vicp.lylab.server.dispatcher;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.server.filter.Filter;

public abstract class AbstractDispatcher<I extends Confirm, O extends Confirm> extends NonCloneableBaseObject implements Dispatcher<I, O> {
	protected List<Filter<I, O>> filterChain = new ArrayList<Filter<I, O>>();

	protected void logger(I request, O response) {
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() throws Exception {
		for (Filter<I, O> filter : filterChain)
			filter.close();
	}

	protected O filterChain(Socket client, I request) {
		// do start filter
		if (filterChain != null && filterChain.size() != 0)
			for (Filter<I, O> filter : filterChain) {
				O ret = null;
				if ((ret = filter.doFilter(client, request)) != null)
					return ret;
			}
		return null;
	}

	public List<Filter<I, O>> getFilterChain() {
		return filterChain;
	}

	public void setFilterChain(List<Filter<I, O>> filterChain) {
		this.filterChain = filterChain;
	}

}
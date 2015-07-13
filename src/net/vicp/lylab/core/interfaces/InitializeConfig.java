package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.utils.config.Config;

public interface InitializeConfig {
	/**
	 * This should be called before initialize()<br>
	 * And its data will be used in initialize()/terminate()
	 * @param config config object
	 */
	public void obtainConfig(Config config);
}

package net.vicp.lylab.utils.permanent;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.Utils;

public class AsyncDiskStorage extends DiskStorage {
	private static final long serialVersionUID = 6315043692230051343L;

	protected String caller;
	protected Long forceSaveInterval = CoreDef.DEFAULT_PERMANENT_INTERVAL;
	
	protected List<String> container = new ArrayList<String>();

	public AsyncDiskStorage(String filePath, String fileSuffix, String caller) {
		super(filePath, fileSuffix);
		this.caller = caller;
		this.begin("AsyncDiskPermanent - " + caller);
	}
	
	@Override
	public boolean appendLine(String entry) {
		if (working.get())
			synchronized (lock) {
				return container.add(entry);
			}
		return false;
	}
	
	@Override
	public void close() throws Exception {
		synchronized (lock) {
			working.set(false);
			long wait= 100L;
			thread.interrupt();
			while (join(wait)) {
				log.info("Waiting for finish, interval=" + wait);
				wait*=2;
			}
			List<String> data = getContainer();
			if(!data.isEmpty())
				writer.writeLine(data);
			writer.close();
		}
	}

	@Override
	public void exec() {
		int circle = 0;
		while (working.get()) {
			try {
				List<String> data = getContainer();
				if(!data.isEmpty())
					writer.writeLine(data);
				else
					circle++;
				if (circle > forceSaveInterval) {
					circle = 0;
					writer.close();
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				log.error("Permanent procedure got an exception:" + Utils.getStringFromException(e));
			}
		}
	}

	protected List<String> getContainer() {
		synchronized (lock) {
			List<String> tmp = container;
			container = new ArrayList<String>();
			return tmp;
		}
	}
	
}

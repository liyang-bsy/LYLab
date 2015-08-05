package net.vicp.lylab.utils.permanent;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.StringLineWriter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.tq.LoneWolf;

public class AsyncDiskPermanent extends LoneWolf implements LifeCycle {
	private static final long serialVersionUID = 6315043692230051343L;

	StringLineWriter writer;
	protected String caller;
	protected String permanentFilePath;
	protected String permanentFileSuffix;
	protected Long forceSaveInterval = CoreDef.DEFAULT_PERMANENT_INTERVAL;
	protected AtomicBoolean working = new AtomicBoolean(false);
	
	protected List<String> container = new ArrayList<String>();
	
	public AsyncDiskPermanent(String filePath, String fileSuffix, String caller) {
		permanentFilePath = filePath;
		permanentFileSuffix = fileSuffix;
		this.caller = caller;
		writer = new StringLineWriter(filePath);
	}
	
	public boolean append(String entry) {
		synchronized (lock) {
			if(working.get())
				return container.add(entry);
			return false;
		}
	}
	
	@Override
	public void initialize() {
		synchronized (lock) {
			if(!working.compareAndSet(false, true)) return;
			this.begin("AsyncDiskPermanent - " + caller);
		}
	}

	@Override
	public void close() throws Exception {
		synchronized (lock) {
			working.set(false);
			Thread.sleep(1000);
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
				if (circle > 60) {
					circle = 0;
					writer.close();
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				log.error("Permanent procedure got an exception:" + Utils.getStringFromException(e));
			}
		}
	}

	public void setPath(String path) {
		writer.setPath(path);
	}

	public void setSuffix(String suffix) {
		writer.setSuffix(suffix);
	}

	public void setMaxLine(int maxLine) {
		writer.setMaxLine(maxLine);
	}

	protected List<String> getContainer() {
		synchronized (lock) {
			List<String> tmp = container;
			container = new ArrayList<String>();
			return tmp;
		}
	}
	
}

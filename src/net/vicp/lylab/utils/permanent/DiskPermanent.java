package net.vicp.lylab.utils.permanent;

import net.vicp.lylab.utils.StringLineWriter;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.tq.LoneWolf;

public class DiskPermanent extends LoneWolf implements AutoCloseable {
	private static final long serialVersionUID = 3954424314501852319L;
	
	StringLineWriter writer;
	protected AtomicBoolean working = new AtomicBoolean(false);
	
	public DiskPermanent(String filePath, String fileSuffix) {
		writer = new StringLineWriter(filePath);
		writer.setSuffix(fileSuffix);
		working.set(true);
	}
	
	public boolean appendLine(String entry) {
		synchronized (lock) {
			if(working.get()) {
				writer.writeLine(entry);
				return true;
			}
			return false;
		}
	}
	
	@Override
	public void close() throws Exception {
		writer.close();
	}

	@Override
	public void exec() {
		// do nothing
		return;
	}

	public void setMaxLine(int maxLine) {
		writer.setMaxLine(maxLine);
	}

}

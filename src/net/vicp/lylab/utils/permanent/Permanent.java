package net.vicp.lylab.utils.permanent;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.DataSource;
import net.vicp.lylab.core.interfaces.Transcode;
import net.vicp.lylab.utils.FileLineWriter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.LoneWolf;

public class Permanent extends LoneWolf {
	private static final long serialVersionUID = -3988291118484641950L;
	
	protected List<Transcode> container;
	protected DataSource<Transcode> controller;

	protected String postfix;
	protected String savePath;
	protected Integer maxSize;
	protected Integer tick;

	public Permanent(DataSource<Transcode> controller, String postfix, String savePath) {
		this(controller, postfix, savePath, CoreDef.DEFAULT_PERMANENT_MAX_SIZE, CoreDef.DEFAULT_PERMANENT_TICK);
	}
	
	public Permanent(DataSource<Transcode> controller, String postfix, String savePath, Integer maxSize, Integer tick) {
		container = new ArrayList<Transcode>(maxSize);
		this.controller = controller;
		this.postfix = postfix;
		this.savePath = savePath;
		this.maxSize = maxSize;
		this.tick = tick;
	}
	
	public List<Transcode> getContainer() {
		synchronized (lock) {
			List<Transcode> ret = container;
			container = new ArrayList<Transcode>();
			return ret;
		}
	}
	
	public void add(Transcode item)
	{
		synchronized (lock) {
			container.add(item);
		}
	}
	
	public int count() {
		return container.size();
	}
	
	@Override
	public void exec() {
		FileLineWriter flw = new FileLineWriter(savePath);
		flw.setMaxLine(maxSize);
		flw.setPostfix(postfix);
		int circle = 0;
		do {
			try {
				if (count() < maxSize) {
					circle++;
					Thread.sleep(CoreDef.WAITING);
				} else {
					flw.writeLine(getContainer());
					circle = 0;
				}
				if (circle > tick) {
					if (count() > 0) {
						flw.writeLine(getContainer());
						circle = 0;
					}
					flw.close();
				}
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		} while (controller.running());
		flw.writeLine(getContainer());
		flw.close();
	}

}

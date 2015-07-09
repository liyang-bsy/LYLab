package net.vicp.lylab.utils.permanent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoLifeCycle;
import net.vicp.lylab.core.interfaces.DataSource;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transcode;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.internet.impl.SimpleHeartBeat;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.tq.LoneWolf;
//import flexjson.JSONSerializer;

public class Receiver extends LoneWolf implements AutoLifeCycle, DataSource<Transcode> {
	private static final long serialVersionUID = 6899867945909316326L;

	protected SequenceTemporaryPool<Transcode> container;
	
	protected AtomicInteger threadCount = new AtomicInteger(0);
	protected Integer maxSize;
	protected Double threshold;
	protected String postfix;
	protected String savePath;
	protected String savedFileName;
	protected AtomicBoolean running = new AtomicBoolean(true);

	protected String host;
	protected Integer port;

	protected boolean savePastLogs;
	
	public Receiver(String host, Integer port, String postfix, String savePath) {
		this.host = host;
		this.port = port;
		this.postfix = postfix;
		this.savePath = savePath;
	}
	
	@Override
	public boolean running() {
		return running.get();
	}

	@Override
	public boolean hasNext() {
		return container.size() != 0;
	}

	@Override
	public void takeback(Transcode t) {
		container.add(0, t);
	}

	@Override
	public int threadCountInc()
	{
		return threadCount.incrementAndGet();
	}
	
	@Override
	public int threadCountDec()
	{
		return threadCount.decrementAndGet();
	}
	
	/**
	 * 取一条数据
	 * @return
	 */
	@Override
	public Transcode accessOne() {
		if (container.isEmpty()) {
			return null;
		}
		Transcode tmp = container.accessOne();
		return tmp;
	}

	/**
	 * 批量从文件读入为导出日志
	 * @param fileName
	 */
	private void readData(String fileName)
	{
		File file = new File(fileName);
		if(!file.exists()) return;
		List<String> rawDataList = Utils.readFileByLines(fileName);
		if(rawDataList.isEmpty()) return;
		for (String rawData : rawDataList) {
			try {
				//TODO
//				item = 
				Utils.serialize(rawData);
//				container.add(protocol.toObject());
			} catch (Exception e) {
				throw new LYException("Unable to decode data");
			}
		}

		if(savePastLogs)
		{
			// 按照日期将文件放到日期名目录下，并改名字
			String basePath = savePath + Utils.format(new Date(), "yyyyMMdd") + File.separator;
			Utils.createDirectory(basePath);
			
			String newFileName = basePath + file.getName().replace(".log", ".haveload");
			boolean renameret = file.renameTo(new File(newFileName));
			if(!renameret) log.error("Rename loaded data failed");
		}
		else
			Utils.deleteFile(fileName);
	}

	/**
	 * 初始化流程
	 */
	@Override
	public void initialize() {
		// 从上次安全关闭时只处理了部分的数据中，读取未成功转移的数据
		container = new SequenceTemporaryPool<Transcode>();
		File file = new File(savedFileName);
		if(file.exists())
		{
			List<String> list = Utils.readFileByLines(savedFileName);
			for(String item:list)
				try {
					Protocol protocol = ProtocolUtils.fromBytes(item.getBytes(CoreDef.CHARSET));
					container.add(protocol.toObject());
				} catch (Exception e) {
					log.error("Unable to decode data:\n" + Utils.getStringFromException(e));
				}
			// 一次读完后立刻删掉，确保这组数据不会被再次使用
			Utils.deleteFile(savedFileName);
		}
//		new AtomicStrongReference().get().begin();
		Permanent perm = new Permanent(this, postfix, savePath);
//		try {
//			perm.get(permanentClass, this, postfix, savePath);
//		} catch (Exception e) {
//			log.error("Can not create dispatcher:\n" + Utils.getStringFromException(e));
//		}
		perm.begin("Permanent-" + threadCount);
	}
	
	@Override
	public void exec() {
		initialize();
		running.set(true);
		while(running())
		{
			// 如果容器中量少与阈值(暂定1/5)，那么就随便找个还没转移的日志，存入容器
			if(container.size() < threshold*maxSize)
			{
				List<String> fList = Utils.getFileList(savePath, "log");
				if(!fList.isEmpty()) readData(fList.get(0));
			}
			// 如果当前存活的转移线程小于阈值，则增加线程
			if(threadCount.get() < CoreDef.THREE)
			{
				threadCount.incrementAndGet();
				@SuppressWarnings("resource")
				Dispatcher disp = new Dispatcher(this, host, port, new SimpleHeartBeat());
//				try {
//					disp.get(dispatcherClass, this, host, port, new SimpleHeartBeat());
//				} catch (Exception e) {
//					log.error("Can not create dispatcher:\n" + Utils.getStringFromException(e));
//				}
				disp.begin("Dispatcher-" + threadCount);
			}
			else {
				try {
					Thread.sleep(CoreDef.WAITING_LONG);
				} catch (InterruptedException e) { break; }
			}
		}
		terminate();
	}

	@Override
	public void terminate() {
		if(running.compareAndSet(true, false)) return;
		// 等待所有转移线程都结束了，再停止自己
		try {
			while(threadCount.get() > 0)
			{
					Thread.sleep(CoreDef.WAITING);
			}
			// 如果容器不为空，则将当前容器里的所有内容存入磁盘
			if(!container.isEmpty())
			{
				File p = new File(savedFileName);
				if(!p.exists()) p.createNewFile();
				FileOutputStream fos = new FileOutputStream(p);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, CoreDef.CHARSET));
				for(Transcode item:container)
					writer.append(Utils.serialize(item));
				fos.close();
			}
		} catch (Exception e) {
			log.error("Terminate failed:\t" + Utils.getStringFromException(e));
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getSavedFileName() {
		return savedFileName;
	}

	public void setSavedFileName(String savedFileName) {
		this.savedFileName = savedFileName;
	}

	public boolean isSavePastLogs() {
		return savePastLogs;
	}

	public void setSavePastLogs(boolean savePastLogs) {
		this.savePastLogs = savePastLogs;
	}

}

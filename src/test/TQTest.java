package test;

import java.text.DecimalFormat;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.tq.LYTaskQueue;

public class TQTest extends Thread {
	public static AtomicInteger a = new AtomicInteger(0), af = new AtomicInteger(0), total = new AtomicInteger(0);

	public void run() {
		
		CoreDef.config.reload("C:/config.txt");
		LYTaskQueue tq = (LYTaskQueue) CoreDef.config.getObject("LYTaskQueue");
		tq.setMaxQueue(500000);
		tq.setMaxThread(100);

		boolean recalc = true;
		
		//稳定以后才开始进行计算
		Integer recalcTimeInteger = 0;
		long start = System.currentTimeMillis();
		for(int j = 0;j<30;j+=1)
		{
			try {
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (tq.getTaskCount() < 100000)
				for (int _i = 0; _i < 100000; _i++)
					tq.addTask(new TTask());
//			while (Thread.activeCount() < 100000)
//				for(int _i =0 ;_i<100000;_i++)
//					new TThread().start();
			int ta = a.getAndSet(0);
			total.getAndAdd(ta);
//			a.set(0);
			af.set(0);
			if(recalc && j > 8)
			{
				recalcTimeInteger = j;
				recalc = false;
				total.set(0);
				System.out.println("recalc");
			}
			System.out.println("second:" + j + "\n\ttotal:" + total.get() + "\t\taverage:" 
					+ new DecimalFormat("0.00").format(1.0*total.get()/(j-recalcTimeInteger)));
			System.out.println("\ta:" + ta + "\tCurrentThread:" + Thread.activeCount());
			System.out.println("\tTask:" + tq.getTaskCount() + "\tThread:" + tq.getThreadCount());
		}

		System.out.println(start - System.currentTimeMillis());
		System.exit(0);
//		
//		CoreDef.config.deepClose();
	}
	public static void main(String[] args) throws InterruptedException {
		new TQTest().start();
	}
	
}

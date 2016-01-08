package test;

public class TThread extends Thread {

	@Override
	public void run() {
		ComplexWork.work();
//		System.out.println("我完成了");
		TQTest.a.incrementAndGet();
//		TQTest.total.incrementAndGet();
	}

}

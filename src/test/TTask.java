package test;

import net.vicp.lylab.utils.tq.Task;

public class TTask extends Task {
	private static final long serialVersionUID = 3781197136371226779L;

	@Override
	public void exec() {
		ComplexWork.work();
//		System.out.println("我完成了");
	}
	
	@Override
	protected void aftermath() {
		TQTest.a.incrementAndGet();
//		TQTest.total.incrementAndGet();
		super.aftermath();
	}

}

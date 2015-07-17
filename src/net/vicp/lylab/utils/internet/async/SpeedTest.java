package net.vicp.lylab.utils.internet.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.tq.Task;

public class SpeedTest extends Task {
	private static final long serialVersionUID = -7906859854268693304L;
	
	private static AtomicInteger access = new AtomicInteger(0);

	public static void main(String[] args) throws Exception {
		new SpeedTest().begin();
		for(int i=0;i<1000;i++)
		{
			System.out.println("s=" + access.getAndSet(0));
			Thread.sleep(1000);
		}
	}

	public void exec() {
		try {
			byte[] bytes = "这是一串中文".getBytes("UTF-8");
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			CharsetDecoder ae = Charset.forName("UTF-8").newDecoder();
			ae.decode(buffer).toString();
			while(true)
			{
//				String s = charsetDecoder.decode(buffer).toString();
				String s = new String(bytes,"UTF-8");
				access.incrementAndGet();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.interfaces.Transcode;

public class MyData implements Transcode<MyData>{

	String value;
	
	@Override
	public Protocol encode()
	{
		Protocol p = null;
		try {
			p = new Protocol(this.getClass().getName().getBytes()
					,"head".getBytes()
					,value.getBytes("UTF-8"));
		} catch (Exception e) { }
		return p;
	}

	@Override
	public MyData decode(Protocol protocol)
	{
		MyData my = null;
		try {
			String value = new String(protocol.getData(), "UTF-8");
			my = new MyData();
			my.setValue(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return my;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}

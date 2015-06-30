package net.vicp.lylab.utils.internet.protocol;

import flexjson.JSONDeserializer;
import net.vicp.lylab.core.TranscodeObject;
import net.vicp.lylab.core.exception.LYException;

public class LYLabProtocol extends AbstractProtocol {

	protected static final byte[] head = "LYLab".getBytes();
	protected static final byte[] splitSignal = new byte[] { -15 };
	
	@Override
	public byte[] getHead() {
		return head;
	}
	
	@Override
	public byte[] getSplitSignal() {
		return splitSignal;
	}
	
	public LYLabProtocol() {
		this(new byte[] { }, new byte[] { });
	}
	
	public LYLabProtocol(TranscodeObject tp) {
		this(tp.encode());
	}

	public LYLabProtocol(AbstractProtocol protocol) {
		this(protocol.getClassName(), protocol.getData());
	}
	
	public LYLabProtocol(Class<?> clazz, byte[] data) {
		this(clazz.getName().getBytes(), data);
	}
	
	public LYLabProtocol(byte[] className, byte[] data) {
		super(className, data);
	}
	
	public TranscodeObject decodeJsonDataToObject()
	{
		AbstractProtocol protocol = this;
		if(protocol.getClassName() == null)
			throw new LYException("Inner className is null");
		if(protocol.getData() == null)
			throw new LYException("Inner data is null");
		try {
			return new JSONDeserializer<TranscodeObject>().use(null, Class.forName(protocol.transformClassName())).deserialize(protocol.transformData());
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + protocol.transformClassName() + ". Maybe the data isn't json?", e);
		}
	}

}

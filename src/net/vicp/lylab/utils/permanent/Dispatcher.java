package net.vicp.lylab.utils.permanent;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.DataSource;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transcode;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.impl.Message;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
/**
 * Failed
 * @author Young
 *
 */
public class Dispatcher extends ClientLongSocket {
	private static final long serialVersionUID = -5015422983388601708L;
	
	protected DataSource<Transcode> controller;
	
	public Dispatcher(DataSource<Transcode> controller, String host, Integer port, HeartBeat heartBeat) {
		super(host, port, heartBeat);
		this.controller = controller;
	}
	
	public void exec()
	{
		try {
			connect();
			while (controller.running()) {
				Transcode t = controller.accessOne();
				if (t != null) {
					if (doRequest(t.encode().toBytes()) == null)
						controller.takeback(t);
				} else
					await(CoreDef.WAITING);
			}
		} catch (Exception e) {
			throw new LYException("Connect break", e);
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new LYException("Why?", e);
			}
		}
	}

	@Override
	public byte[] doRequest(byte[] request) {
		byte[] ret = request(request);
		if (ret != null)
		{
			Protocol p = ProtocolUtils.fromBytes(ret);
			if(p != null)
			{
				Transcode item = p.toObject();
				if(item instanceof Message)
				{
					if(((Message) item).getCode() == 0)
						// finally it succeed...
						return ret;
				}
			}
		}
		return null;
	}
	
	@Override
	protected void aftermath() {
		controller.threadCountDec();
		super.aftermath();
	}
	
}

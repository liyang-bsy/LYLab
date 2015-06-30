package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.utils.internet.HeartBeat;

public interface HeartBeatSender {
	public boolean sendHeartBeat(HeartBeat heartBeat);
	
}

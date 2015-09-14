package net.vicp.lylab.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.platform.OSInfo;

public class Command extends NonCloneableBaseObject {
	private static final Runtime rt = Runtime.getRuntime();

	public static Pair<Integer, String> execute(String cmd) {
		String[] cmdSeq = { "", "/c", "" };
		if(OSInfo.isWindows())
			cmdSeq[0] = "cmd";
		else
			cmdSeq[0] = "/bin/bash";
		cmdSeq[2] = cmd;
		int code = -1;
		String msg = "[Not Avaliable]";
		try {
			Process ps = rt.exec(cmdSeq);
			msg = loadStream(ps.getInputStream());
			code = ps.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Pair<Integer, String>(code, msg);
	}

	// read an input-stream into a String
	static String loadStream(InputStream is) throws IOException {
		String str = "";
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "GBK"));
		StringBuilder buffer = new StringBuilder();
		while ((str = bufferedReader.readLine()) != null) {
			buffer.append(str);
			buffer.append('\n');
		}
		return buffer.toString();
	}
}

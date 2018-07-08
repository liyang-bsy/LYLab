package net.vicp.lylab.utils.permanent;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;

import net.vicp.lylab.utils.Utils;

public class DailyRollingAsyncDiskStorage extends AsyncDiskStorage {
	private static final long serialVersionUID = -3151642054290000095L;

	public DailyRollingAsyncDiskStorage(String filePath, String fileSuffix,
			String caller) {
		super(filePath + getTodayDateString(), fileSuffix, caller);
		basePath = filePath;
	}

	@Override
	public void exec() {
		int circle = 0;
		while (working.get()) {
			try {
				List<String> data = getContainer();
				if (!data.isEmpty())
					writer.writeLine(data);
				else
					circle++;
				if (circle > forceSaveInterval) {
					circle = 0;
					writer.close();
					writer.setPath(basePath + getTodayDateString());
				}
				Thread.sleep(60000);
			} catch (Exception e) {
				log.error("Permanent procedure got an exception:"
						+ Utils.getStringFromException(e));
			}
		}
	}

	public static String getTodayDateString() {
		return DateFormatUtils.format(new Date(), "yyyy/MM/dd");
	}

}

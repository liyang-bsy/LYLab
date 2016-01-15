package net.vicp.lylab.utils.permanent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.utils.Utils;

public class JsonPermanent extends Permanent {
	
	public List<Object> readFromDisk() {
		List<Object> list = new ArrayList<>();
		if (fileName != null && instanceClassName != null) {
			File file = new File(fileName);
			if (file.exists()) {
				List<String> rawJsons = Utils.readFileByLines(fileName);
				try {
					for (String json : rawJsons) {
						try {
							list.add(Utils.deserialize(instanceClassName, json));
						} catch (Exception e) {
							log.error("Unable to deserialize data into class (" + instanceClassName + "):" + CoreDef.LINE_SEPARATOR + json + CoreDef.LINE_SEPARATOR + "reason:"
									+ Utils.getStringFromException(e));
						}
					}
				} catch (Exception e) {
					throw new LYException("Unable to load data from permanent file", e);
				}
			}
		}
		return list;
	}

	@Override
	public void saveToDisk(Iterable<?> container) {
		if (fileName != null) {
			Iterator<?> iterator = container.iterator();
			if (iterator.hasNext()) {
				File p = new File(fileName);
				if (p.exists())
					Utils.deleteFile(fileName);
				if (!p.exists()) {
					try {
						p.createNewFile();
					} catch (IOException e) {
						log.error("Permanent process error (This may cause data loss!), reason:" + Utils.getStringFromException(e));
					}
				}

				try (OutputStream os = new FileOutputStream(p);) {
					while (iterator.hasNext())
						os.write((Utils.serialize(iterator.next()) + CoreDef.LINE_SEPARATOR).getBytes(CoreDef.CHARSET()));
				} catch (IOException e) {
					log.error("Permanent process error (This will cause data loss!), reason:" + Utils.getStringFromException(e));
				}
			}
		}
	}

}

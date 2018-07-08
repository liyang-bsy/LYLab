package net.vicp.lylab.utils.permanent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.utils.Utils;

public class ObjectPermanent extends Permanent {
	public List<Object> readFromDisk() {
		List<Object> list = new ArrayList<>();
		if (fileName != null) {
			File file = new File(fileName);
			if (file.exists()) {
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));) {
					Integer total = (Integer) ois.readObject();
					while (total-- > 0)
						list.add(ois.readObject());
					ois.close();
					Utils.deleteFile(fileName);
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
				try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(p));) {
					List<Object> list = new ArrayList<>();
					while (iterator.hasNext())
						list.add(iterator.next());
					oos.writeObject(new Integer(list.size()));
					Iterator<Object> listIterator = list.iterator();
					while (listIterator.hasNext())
						oos.writeObject(listIterator.next());
				} catch (IOException e) {
					log.error("Permanent process error (This will cause data loss!), reason:" + Utils.getStringFromException(e));
				}
			}
		}
	}

}

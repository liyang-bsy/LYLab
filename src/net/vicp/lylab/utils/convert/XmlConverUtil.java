package net.vicp.lylab.utils.convert;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import net.vicp.lylab.core.NonCloneableBaseObject;

public abstract class XmlConverUtil extends NonCloneableBaseObject {
	/**
	 * XML to Object, <b>ONLY</b> support simple format like follow:
	 * <br>&lt;key&gt;value&lt;/key&gt;
	 * 
	 * @param xml
	 * @return
	 * @throws DocumentException 
	 */
	public static Map<String, Object> xml2Object(String xml) {
		try {
			if (!xml.matches("^<xml>[\\S\\s]*</xml>$"))
				xml = "<xml>" + xml + "</xml>";
			Document document = DocumentHelper.parseText(xml);
			Element rootElement = document.getRootElement();
			return dfs(rootElement);
		} catch (DocumentException e) {
			throw new RuntimeException("Bad xml format:" + xml);
		}
	}

	private static Map<String, Object> dfs(Element element) {
		Map<String, Object> container = new HashMap<>();
		Set<String> names = new HashSet<>();
		for (Element e : element.elements())
			names.add(e.getName());

		for (String name : names) {
			List<Element> node = element.elements(name);
			if (node.size() != 1) {
				// List mode
				List<Object> list = new ArrayList<>();
				for (int j = 0; j < node.size(); j++) {
					Element child = (Element) node.get(j);
					System.out.println(child.getName());
					list.add(dfs(child));
				}
				container.put(name, list);
			} else {
				// Map mode
				for (Iterator<Element> it = node.iterator(); it.hasNext();) {
					Element child = (Element) it.next();
					if (child.elements().isEmpty())
						container.put(child.getName(), child.getText());
					else
						container.put(child.getName(), dfs(child));
				}
			}
		}
		return container;
	}

	/**
	 * Export Document to XML
	 * 
	 * @param document
	 * @return
	 */
	public static String doc2String(Document document) {
		String s = "";
		try {
			// 使用输出流来进行转化
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 使用UTF-8编码
			OutputFormat format = new OutputFormat("   ", true, "UTF-8");
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(document);
			s = out.toString("UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}

}

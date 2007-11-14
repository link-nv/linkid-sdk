package net.link.safeonline.performance.drivers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Utilities for evaluating XPath expressions.
 * 
 * @author mbillemo
 */
public class XPathUtil {

	private CachedXPathAPI xpath;

	/**
	 * Create a new XPathUtil instance.
	 */
	public XPathUtil() {

		flush();
	}

	public void flush() {

		this.xpath = new CachedXPathAPI();
	}

	public boolean getBoolean(Node contextNode, String expression,
			Object... arguments) throws TransformerException {

		return getObject(contextNode, expression, arguments).bool();
	}

	public Node getNode(Node contextNode, String expression,
			Object... arguments) throws TransformerException {

		return this.xpath.selectSingleNode(contextNode, String.format(
				expression, arguments));
	}

	public List<Node> getNodes(Node contextNode, String expression,
			Object... arguments) throws TransformerException {

		List<Node> nodes = new ArrayList<Node>();
		NodeIterator iterator = getObject(contextNode, expression, arguments)
				.nodeset();

		while (true) {
			Node node = iterator.nextNode();
			if (node == null)
				break;

			nodes.add(node);
		}

		return nodes;
	}

	public double getNumber(Node contextNode, String expression,
			Object... arguments) throws TransformerException {

		return getObject(contextNode, expression, arguments).num();
	}

	public XObject getObject(Node contextNode, String expression,
			Object... arguments) throws TransformerException {

		return this.xpath.eval(contextNode, String
				.format(expression, arguments));
	}

	public String getString(Node contextNode, String expression,
			Object... arguments) throws TransformerException {

		return getObject(contextNode, expression, arguments).str();
	}
}
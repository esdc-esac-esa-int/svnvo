/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.utils.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Utility for working with an XML files.
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsXmlManager {

    private static final Logger LOG = Logger.getLogger(UwsXmlManager.class.getName());

    private Document doc;
    private File file;

    /**
     * Constructor.
     * @param rootid root element identifier
     */
    public UwsXmlManager(String rootid) {
        doc = createDomDocument();
        Element eRoot = doc.createElement(rootid);
        doc.appendChild(eRoot);
    }

    /**
     * Constructor.
     * @param f XML file
     * @throws IOException any I/O error
     */
    public UwsXmlManager(File f) throws IOException {
        doc = parseXmlFile(f, false);
        file = f;
    }

    /**
     * Constructor.
     * @param is XML file.
     * @throws IOException any I/O error
     */
    public UwsXmlManager(InputStream is) throws IOException {
        doc = parseXmlFile(is, false);
        file = null;
    }

    /**
     * Imports an element.
     * @param eSrc source element
     * @param deep deep copy
     * @return a new Node with a copy
     */
    public Node importElement(Element eSrc, boolean deep) {
        return doc.importNode(eSrc, deep);
    }

    /**
     * Returns the file associated to this xml.
     * @return null or the file object associated to this xml.
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file associated to this xml.
     * @param f xml file
     */
    public void setFile(File f) {
        file = f;
    }
    
    /**
     * Returns this xml document.
     * @return this xml document.
     */
    public Document getDocument(){
    	return doc;
    }
    
    /**
     * Creates an element: it is not attached to any other element yet!
     * @param id element name (tag)
     * @return a new element (not attached to any other)
     */
    public Element createElement(String id){
    	return doc.createElement(id);
    }

    /**
     * Gets the XML root element
     * @return the root element
     */
    public Element getRootElement() {
        return doc.getDocumentElement();
    }

    /**
     * Creates an empty document
     * @return an empty document
     */
    public static Document createDomDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = createDomDocumentBuilder(factory);
        return createDomDocument(builder);
    }

    /**
     * Creates a DOM document.
     * @param builder DocumentBuilder.
     * @return a DOM document.
     */
    static Document createDomDocument(DocumentBuilder builder){
        if(builder != null){
            return builder.newDocument();
        } else {
            return null;
        }
    }

    /**
     * Creates a DocumentBuilder from a factory.
     * @param factory DocumentBuilderFactory.
     * @return a DocumentBuilder.
     */
    static DocumentBuilder createDomDocumentBuilder(DocumentBuilderFactory factory){
        try {
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOG.finest("Ignored exception when creating empty document: " + e.getMessage());
        }
        return null;
    }

    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     * @param file file to parse
     * @param validating flag to validate the document.
     * @return a XML document
     * @exception IOException if any error is found when parsing the file.
     */
    public static Document parseXmlFile(File file, boolean validating) throws IOException {
        return parseXmlFile(DocumentBuilderFactory.newInstance(), file, validating);
    }

    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     * @param factory DocumentBuilder factory.
     * @param file file to parse
     * @param validating flag to validate the document.
     * @return a XML document
     * @exception IOException if any error is found when parsing the file.
     */
    static Document parseXmlFile(DocumentBuilderFactory factory, File file, boolean validating) throws IOException {
        factory.setValidating(validating);
        try {
            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(file);
            return doc;
        } catch (SAXException e) {
            // A parsing error occurred; the xml input is not valid
            IOException ioe = new IOException("Error reading file '" + file.getAbsolutePath() + "':\n" +
                    e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (ParserConfigurationException e) {
            IOException ioe = new IOException("Error reading file '" + file.getAbsolutePath() + "':\n" +
                    e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (IOException e) {
            IOException ioe = new IOException("Error reading file '" + file.getAbsolutePath() + "':\n" +
                    e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Parses an XML from an InputStream and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     * @param is XML to parse
     * @param validating flag to validate the document.
     * @return a XML document
     * @exception IOException if any error is found when parsing the file.
     */
    public static Document parseXmlFile(InputStream is, boolean validating) throws IOException {
        return parseXmlFile(DocumentBuilderFactory.newInstance(), is, validating);
    }

    static Document parseXmlFile(DocumentBuilderFactory factory, InputStream is, boolean validating)
            throws IOException {
        factory.setValidating(validating);
        try {
            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(is);
            return doc;
        } catch (SAXException e) {
            // A parsing error occurred; the xml input is not valid
            IOException ioe = new IOException("Error reading XML:\n" + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (ParserConfigurationException e) {
            IOException ioe = new IOException("Error reading XML:\n" + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (IOException e) {
            IOException ioe = new IOException("Error reading XML:\n" + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Writes the XML document into a file.
     * @param file file
     * @throws IOException any I/O error
     */
    public void writeXmlFile(File file) throws IOException {
        writeXmlFile(doc, file);
        this.file = file;
    }

    /**
     * Returns the XML as string.
     * @return the XML as string.
     * @throws IOException any I/O error.
     */
    public String getXmlAsString() throws IOException {
        return getXmlAsString(doc);
    }

    /**
     * Writes a XML document into a file
     * @param doc document to write
     * @param file file
     * @throws IOException wrapper for several XML exceptions
     */
    public static void writeXmlFile(Document doc, File file) throws IOException {
        writeXmlFile(TransformerFactory.newInstance(), doc, file);
    }

    static void writeXmlFile(TransformerFactory factory, Document doc, File file) throws IOException {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            //File file = new File(filename);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = factory.newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            IOException ioe = new IOException("Error writting file '" + file.getAbsolutePath() + "':\n"
                    + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (TransformerException e) {
            IOException ioe = new IOException("Error writting file '" + file.getAbsolutePath() + "':\n"
                    + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Returns the XML document as String
     * @param doc document
     * @return the XML document as String
     * @throws IOException wrapper for different XML exceptions
     */
    public static String getXmlAsString(Document doc) throws IOException {
        return getXmlAsString(TransformerFactory.newInstance(), doc);
    }


    static String getXmlAsString(TransformerFactory factory, Document doc) throws IOException {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            //File file = new File(filename);
            StringWriter sw = new StringWriter();
            Result result = new StreamResult(sw);

            // Write the DOM document to the file
            Transformer xformer = factory.newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
            return sw.toString();
        } catch (TransformerConfigurationException e) {
            IOException ioe = new IOException("Error writting document:\n" + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (TransformerException e) {
            IOException ioe = new IOException("Error writting document:\n" + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
    
    /**
     * Returns a CDATA content from a node.
     * It gets the first child of the node and it expects the child is a 'org.w3c.dom.CharacterData' object.
     * @param n
     * @return
     */
	public static String getCDataFromNode(Node n) {
		if (n == null) {
			return null;
		}
		Node child = n.getFirstChild();
		if (child == null) {
			return null;
		}
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return null;
	}

}

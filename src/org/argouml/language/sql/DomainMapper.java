// $Id$
// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.language.sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class providing access to domain mappings. Domain mappings are required to
 * map uml datatypes (domains in db-language) to datatypes of a target database.
 * This way there can be modelled <code>String</code>s, <code>int</code>s
 * or <code>double</code>s for a database model. The DomainMapper maps
 * <code>String</code> to a <code>VARCHAR(100)</code>, <code>int</code>
 * to <code>INTEGER</code> or <code>double</code> to
 * <code>DOUBLE PRECISION</code>, according its configuration. This
 * configuration can be changed in the settings dialog.
 * 
 * @author drahmann
 */
public class DomainMapper {
    private static final String ROOT_TAG = "<tns:mappings "
            + "xmlns:tns=\"http://www.argouml.org/Namespace/argouml-sql\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\""
            + "http://www.argouml.org/Namespace/argouml-sql domainmapping.xsd \">";

    private static final String XML_FILE_NAME = "domainmapping.xml";

    private static final String XML_TAG = "<?xml version=\"1.0\" "
            + "encoding=\"UTF-8\"?>";

    private Map databases;

    private String indent;

    private static final Logger LOG = Logger.getLogger(DomainMapper.class);

    /**
     * Creates a new DomainMapper.
     * 
     */
    public DomainMapper() {
        databases = new HashMap();
        load();
    }

    /**
     * Clears all mappings for the specified database code creator.
     * 
     * @param codeCreatorClass The class object of the code creator.
     */
    public void clear(Class codeCreatorClass) {
        getMappingsFor(codeCreatorClass).clear();
    }

    /**
     * Returns the datatype for a given domain and code creator. The domain
     * itself is returned if
     * <ul>
     * <li>there does not exist a mapping for the given code creator or</li>
     * <li>there is no defined mapping for the given domain</li>
     * </ul>
     * 
     * @param codeCreatorClass
     *            The class of the code creator
     * @param domain
     *            The domain
     * @return The database-specific datatype for the given domain
     */
    public String getDatatype(Class codeCreatorClass, String domain) {
        Map mappings = getMappingsFor(codeCreatorClass);
        String datatype = domain;
        if (mappings != null) {
            String dt = (String) mappings.get(domain);
            if (dt != null) {
                datatype = dt;
            }
        }

        return datatype;
    }

    private Map getMappingsFor(String codeCreatorClassName) {
        Map mappings = (Map) databases.get(codeCreatorClassName);
        if (mappings == null) {
            mappings = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            databases.put(codeCreatorClassName, mappings);
        }
        return mappings;
    }

    /**
     * 
     * @param codeCreatorClass
     * @return All mappings for the given database code creator class.
     */
    public Map getMappingsFor(Class codeCreatorClass) {
        return getMappingsFor(codeCreatorClass.getName());
    }

    private File getFile() {
        File result = null;
        String dir = Argo.getDirectory() + File.separator + "ext";
        result = new File(dir, XML_FILE_NAME);
        return result;
    }

    /**
     * Load all mappings from the file domainmapping.xml located in the same
     * directory than the module.
     */
    public void load() {
        File file = getFile();

        if (!file.exists()) {
            return;
        }
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.parse(file);
            Element root = document.getDocumentElement();
            NodeList childs = root.getChildNodes();

            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                NamedNodeMap attributes = child.getAttributes();
                String name = attributes.getNamedItem("name").getTextContent();

                Map mappings = getMappingsFor(name);
                readMappings(mappings, child.getChildNodes());
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Exception", e);
        } catch (SAXException e) {
            LOG.error("Exception", e);
        } catch (IOException e) {
            LOG.error("Exception", e);
        }
    }

    /**
     * Save all mappings to the file domainmapping.xml located in the same
     * directory than the module.
     */
    public void save() {
        File file = getFile();

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);

            fw.write(XML_TAG);
            fw.write(GeneratorSql.LINE_SEPARATOR);
            fw.write(ROOT_TAG);
            fw.write(GeneratorSql.LINE_SEPARATOR);

            indent = "\t";
            Set dbEntries = databases.entrySet();
            for (Iterator it = dbEntries.iterator(); it.hasNext();) {
                Entry entry = (Entry) it.next();
                String className = (String) entry.getKey();
                Map mappings = (Map) entry.getValue();

                StringBuffer sb = new StringBuffer();
                sb.append(indent);
                sb.append("<tns:database name=\"");
                sb.append(className);
                sb.append("\">").append(GeneratorSql.LINE_SEPARATOR);
                fw.write(sb.toString());

                writeMappings(fw, mappings);

                fw.write("</tns:database>");
            }

            fw.write("</tns:mappings>");
            fw.close();
        } catch (IOException e) {
            LOG.error("Exception", e);
        }
    }

    /**
     * Set specified mapping for the given database code creator class.
     * 
     * @param codeCreatorClass
     *            The class of the code creator for which this mapping should be
     *            set.
     * @param domain
     *            The domain (uml datatype).
     * @param datatype
     *            The datatype (database-specific).
     */
    public void setDatatype(Class codeCreatorClass, String domain,
            String datatype) {
        Map mappings = getMappingsFor(codeCreatorClass);
        mappings.put(domain, datatype);
    }

    private void readMappings(Map mappings, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node mapping = nodes.item(i);
            if (mapping.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            NamedNodeMap attributes = mapping.getAttributes();
            Node src = attributes.getNamedItem("umltype");
            Node dst = attributes.getNamedItem("dbtype");
            String srcText = src.getTextContent();
            String dstText = dst.getTextContent();

            mappings.put(srcText, dstText);
        }
    }

    private void writeMappings(FileWriter fw, Map mappings) throws IOException {
        String oldIndent = indent;
        indent += "\t";
        Set entries = mappings.entrySet();
        for (Iterator it = entries.iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            String domain = (String) entry.getKey();
            String datatype = (String) entry.getValue();

            StringBuffer sb = new StringBuffer();
            sb.append(indent);
            sb.append("<tns:mapping umltype=\"");
            sb.append(domain);
            sb.append("\" dbtype=\"");
            sb.append(datatype);
            sb.append("\" />").append(GeneratorSql.LINE_SEPARATOR);

            fw.write(sb.toString());
        }
        indent = oldIndent;
    }
}

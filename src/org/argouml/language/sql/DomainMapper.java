// $Id: eclipse-argo-codetemplates.xml 11347 2006-10-26 22:37:44Z linus $
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class DomainMapper {
    private static final String XML_FILE_NAME = "domainmapping.xml";

    private Logger LOG = Logger.getLogger(getClass());

    private Map databases;

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

    public DomainMapper() {
        databases = new HashMap();
        String filename = getClass().getResource(XML_FILE_NAME).getPath();
        File xmlFile = new File(filename);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.parse(xmlFile);
            Element root = document.getDocumentElement();

            // String uri = root.getNamespaceURI();
            // NodeList childs = root.getElementsByTagNameNS(uri, "database");
            NodeList childs = root.getChildNodes();

            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                
                NamedNodeMap attributes = child.getAttributes();
                String name = attributes.getNamedItem("name").getTextContent();

                Map mappings = (Map) databases.get(name);
                if (mappings == null) {
                    mappings = new HashMap();
                    databases.put(name, mappings);
                }

                readMappings(mappings, child.getChildNodes());
            }

        } catch (ParserConfigurationException e) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e);
        } catch (SAXException e) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e);
        } catch (IOException e) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e);
        }
    }

    /**
     * Returns the datatype for a given domain and code creator. The domain
     * itself is returned if
     * <ul>
     * <li>there does not exist a mapping for the given code creator or</li>
     * <li>there is no defined mapping for the given domain</li>
     * </ul>
     * 
     * @param codeCreator
     *            The code creator
     * @param domain
     *            The domain
     * @return The database-specific datatype for the given domain
     */
    public String getDatatype(Object codeCreator, String domain) {
        Map mappings = (Map) databases.get(codeCreator.getClass().getName());
        String datatype = domain;
        if (mappings != null) {
            String dt = (String) mappings.get(domain);
            if (dt != null) {
                datatype = dt;
            }
        }

        return datatype;
    }
}

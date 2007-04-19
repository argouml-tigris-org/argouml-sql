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
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.argouml.kernel.Project;
import org.argouml.model.Model;
import org.argouml.persistence.AbstractFilePersister;
import org.argouml.persistence.OpenException;
import org.argouml.persistence.PersistenceManager;
import org.argouml.ui.ArgoDiagram;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.generator.GeneratorHelper;
import org.argouml.uml.generator.GeneratorManager;
import org.argouml.uml.generator.Language;
import org.argouml.uml.generator.SourceUnit;

/**
 * Class for testing code creation.
 * 
 * @author Anne und Kai
 */
public class TestCodeCreation extends TestCaseSql {
    public void testCodeCreation() throws OpenException, InterruptedException {
        String filename = "test.zargo";

        URL url = getClass().getResource(filename);
        assertTrue("Unintended failure: resource to be tested is not found: "
                + filename + ", converted to URL: " + url, url != null);
        AbstractFilePersister persister = PersistenceManager.getInstance()
                .getPersisterFromFileName(filename);
        File testfile = new File(url.getFile());

        Project p = persister.doLoad(testfile);
        ArgoDiagram activeDiagram = (ArgoDiagram) p.getDiagrams().get(0);

        assertTrue(activeDiagram instanceof UMLClassDiagram);

        UMLClassDiagram d = (UMLClassDiagram) activeDiagram;
        Vector classes = new Vector();
        List nodes = d.getNodes();
        Iterator elems = nodes.iterator();
        while (elems.hasNext()) {
            Object owner = elems.next();
            if (!Model.getFacade().isAClass(owner)
                    && !Model.getFacade().isAInterface(owner)) {

                continue;

            }
            String name = Model.getFacade().getName(owner);
            if (name == null || name.length() == 0
                    || Character.isDigit(name.charAt(0))) {

                continue;

            }
            classes.addElement(owner);
        }

        Language lang = GeneratorHelper.makeLanguage(SqlInit.LANGUAGE_NAME);
        GeneratorManager.getInstance().addGenerator(lang,
                GeneratorSql.getInstance());
        Collection files = GeneratorHelper.generate(lang, classes, false);

        assertEquals(1, files.size());

        SourceUnit generatedFile = (SourceUnit) files.iterator().next();
        String content = generatedFile.getContent();

        // TODO compare generated content with some reference
    }
}

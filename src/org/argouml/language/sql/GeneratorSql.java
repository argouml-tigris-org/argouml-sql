// $Id$
// Copyright (c) 2006 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.argouml.model.Model;
import org.argouml.uml.generator.CodeGenerator;
import org.argouml.uml.generator.SourceUnit;

/**
 * SQL generator
 */
class GeneratorSql implements CodeGenerator {
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static final String PRIMARY_KEY_STEREOTYPE = "PK";

    static final String FOREIGN_KEY_STEREOTYPE = "FK";

    static final String NOT_NULL_STEREOTYPE = "NOT NULL";

    static final String NULL_STEREOTYPE = "NULL";

    static final String SOURCE_COLUMN_TAGGED_VALUE = "source column";

    static final String ASSOCIATION_NAME_TAGGED_VALUE = "association name";

    /**
     * The instances.
     */
    private static final GeneratorSql INSTANCE = new GeneratorSql();

    /**
     * Constructor.
     */
    private GeneratorSql() {
        // Cannot be created from somewhere else.
    }

    /**
     * @return the singleton instance.
     */
    public static GeneratorSql getInstance() {
        return INSTANCE;
    }

    /**
     * Generate code for the specified classifiers. If generation of
     * dependencies is requested, then every file the specified elements depends
     * on is generated too (e.g. if the class MyClass has an attribute of type
     * OtherClass, then files for OtherClass are generated too).
     * 
     * @param elements
     *            the UML model elements to generate code for.
     * @param deps
     *            Recursively generate dependency files too.
     * @return A collection of {@link org.argouml.uml.generator.SourceUnit}
     *         objects. The collection may be empty if no file is generated.
     * @see org.argouml.uml.generator.CodeGenerator#generate( Collection,
     *      boolean)
     */
    public Collection generate(Collection elements, boolean deps) {

        ModelValidator validator = new ModelValidator();
        Collection problems = validator.validate(elements);
        if (problems.size() > 0) {
            // model not valid, do something
            StringBuffer sb = new StringBuffer();
            for (Iterator it = problems.iterator(); it.hasNext();) {
                String s = (String) it.next();
                sb.append(s).append(LINE_SEPARATOR);
            }
            
            String sourceCode = sb.toString();
            SourceUnit su = new SourceUnit("E:\\Test.sql", sourceCode);
            Collection result = new ArrayList();
            result.add(su);
            return result;
        }

        // Just some testing for understanding the model and facade
        StringBuffer sb = new StringBuffer();
        SqlCodeCreator creator = new FirebirdSqlCodeCreator();

        Iterator it = elements.iterator();
        Collection tableDefinitions = new HashSet();
        while (it.hasNext()) {
            Object element = it.next();

            if (Model.getFacade().isAClass(element)) {
                TableDefinition td = new TableDefinition();
                td.setName(Model.getFacade().getName(element));
                td.setColumnDefinitions(getColumnDefinitions(element));

                tableDefinitions.add(td);
            }
        }

        // String s = creator.createTable(td);
        // sb.append(s);

        String sourceCode = sb.toString();
        SourceUnit su = new SourceUnit("E:\\Test.sql", sourceCode);
        Collection result = new ArrayList();
        result.add(su);
        return result;
    }

    private List getColumnDefinitions(Object element) {
        List columnDefinitions = new ArrayList();
        Iterator itAttributes = Model.getFacade().getAttributes(element)
                .iterator();
        while (itAttributes.hasNext()) {
            Object attribute = itAttributes.next();

            ColumnDefinition cd = new ColumnDefinition();
            cd.setName(Model.getFacade().getName(attribute));

            Object type = Model.getFacade().getType(attribute);
            cd.setDatatype(Model.getFacade().getName(type));

            if (Model.getFacade().isStereotype(attribute, "NOT NULL")) {
                cd.setNullable(false);
            }

            columnDefinitions.add(cd);
        }

        return columnDefinitions;
    }

    /**
     * Generate files for the specified classifiers.
     * 
     * @see #generate(Collection, boolean)
     * @param elements
     *            the UML model elements to generate code for.
     * @param path
     *            The source base path.
     * @param deps
     *            Recursively generate dependency files too.
     * @return The filenames (with relative path) as a collection of Strings.
     *         The collection may be empty if no file will be generated.
     * @see org.argouml.uml.generator.CodeGenerator#generateFiles( Collection,
     *      String, boolean)
     */
    public Collection generateFiles(Collection elements, String path,
            boolean deps) {
        return generate(elements, deps);
    }

    /**
     * Returns a list of files that will be generated from the specified
     * modelelements.
     * 
     * @see #generate(Collection, boolean)
     * @param elements
     *            the UML model elements to generate code for.
     * @param deps
     *            Recursively generate dependency files too.
     * @return The filenames (with relative path) as a collection of Strings.
     *         The collection may be empty if no file will be generated.
     * @see org.argouml.uml.generator.CodeGenerator#generateFileList(
     *      Collection, boolean)
     */
    public Collection generateFileList(Collection elements, boolean deps) {
        throw new Error("Not yet implemented");
    }
} /* end class GeneratorSql */

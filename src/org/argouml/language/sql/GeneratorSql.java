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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.application.api.Configuration;
import org.argouml.model.Model;
import org.argouml.ui.ExceptionDialog;
import org.argouml.ui.ProjectBrowser;
import org.argouml.ui.SelectCodeCreatorDialog;
import org.argouml.uml.generator.CodeGenerator;
import org.argouml.uml.generator.TempFileUtils;

/**
 * SQL generator
 */
public class GeneratorSql implements CodeGenerator {
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static final String PRIMARY_KEY_STEREOTYPE = "PK";

    static final String FOREIGN_KEY_STEREOTYPE = "FK";

    static final String NOT_NULL_STEREOTYPE = "NOT NULL";

    static final String NULL_STEREOTYPE = "NULL";

    static final String SOURCE_COLUMN_TAGGED_VALUE = "source column";

    static final String ASSOCIATION_NAME_TAGGED_VALUE = "association name";

    private Logger LOG = Logger.getLogger(getClass());

    /**
     * The instances.
     */
    private static final GeneratorSql INSTANCE = new GeneratorSql();

    private DomainMapper domainMapper;

    private SqlCodeCreator sqlCodeCreator;

    private List sqlCodeCreators;

    /**
     * Constructor.
     */
    private GeneratorSql() {
        domainMapper = new DomainMapper();

        sqlCodeCreators = new ArrayList();

        URL url = getClass().getResource("GeneratorSql.class");
        String extForm = url.toExternalForm();

        if (extForm.startsWith("file:")) {
            String className = getClass().getName();
            // ... - 7 because of length of ".class" and the trailing "/"
            extForm = extForm.substring(0, extForm.length()
                    - className.length() - 7);
        }

        SqlCreatorLoader el = new SqlCreatorLoader();
        try {
            URI uri = new URI(extForm);
            Collection classes = el.getLoadableClassesFromUri(uri,
                    SqlCodeCreator.class);
            sqlCodeCreators.addAll(classes);
        } catch (URISyntaxException e) {
            LOG.error("Exception", e);
            System.out.println(e.getStackTrace());
        }
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
        LOG.debug("generate() called");
        File tmpdir = null;
        try {
            tmpdir = TempFileUtils.createTempDir();
            if (tmpdir != null) {
                generateFiles(elements, tmpdir.getPath(), deps);
                return TempFileUtils.readAllFiles(tmpdir);
            }
            return Collections.EMPTY_LIST;
        } finally {
            if (tmpdir != null) {
                TempFileUtils.deleteDir(tmpdir);
            }
            LOG.debug("generate() terminated");
        }
    }

    private TableDefinition getTableDefinition(Object element) {
        TableDefinition tableDefinition = new TableDefinition();
        tableDefinition.setName(Model.getFacade().getName(element));

        Iterator itAttributes = Model.getFacade().getAttributes(element)
                .iterator();
        while (itAttributes.hasNext()) {
            Object attribute = itAttributes.next();

            String name = Model.getFacade().getName(attribute);

            ColumnDefinition cd = new ColumnDefinition();
            cd.setName(name);

            Object domain = Model.getFacade().getType(attribute);
            String domainName = Model.getFacade().getName(domain);
            String datatype = domainMapper.getDatatype(sqlCodeCreator
                    .getClass(), domainName);
            cd.setDatatype(datatype);

            if (Utils.isNull(attribute)) {
                cd.setNullable(Boolean.TRUE);
            } else if (Utils.isNotNull(attribute)) {
                cd.setNullable(Boolean.FALSE);
            } else {
                cd.setNullable(null);
            }

            tableDefinition.addColumnDefinition(cd);

            if (Utils.isPk(attribute)) {
                cd.setNullable(Boolean.FALSE);
                tableDefinition.addPrimaryKeyField(name);
            }
        }

        return tableDefinition;
    }

    private void setNullable(TableDefinition tableDef, List columnNames,
            boolean nullable) {
        for (Iterator it = columnNames.iterator(); it.hasNext();) {
            String name = (String) it.next();
            tableDef.getColumnDefinition(name).setNullable(
                    Boolean.valueOf(nullable));
        }
    }

    private String generateCode(Collection elements) {
        sqlCodeCreator = new FirebirdSqlCodeCreator();
        tableDefinitions = new HashMap();
        foreignKeyDefinitions = new ArrayList();

        for (Iterator it = elements.iterator(); it.hasNext();) {
            Object element = it.next();
            if (Model.getFacade().isAClass(element)) {
                TableDefinition tableDef = getTableDefinition(element);
                tableDefinitions.put(element, tableDef);
            }
        }

        if (elements.size() > 1) {
            for (Iterator it = elements.iterator(); it.hasNext();) {
                Object element = it.next();
                Collection fkDefs = getForeignKeyDefinitions(element);
                TableDefinition tableDef = (TableDefinition) tableDefinitions
                        .get(element);
                for (Iterator it2 = fkDefs.iterator(); it2.hasNext();) {
                    ForeignKeyDefinition fkDef = (ForeignKeyDefinition) it2
                            .next();

                    if (fkDef.getReferencesLower() == 0) {
                        setNullable(tableDef, fkDef.getColumnNames(), true);
                    } else {
                        setNullable(tableDef, fkDef.getColumnNames(), false);
                    }
                }
                foreignKeyDefinitions.addAll(fkDefs);
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("-- Table definitions").append(LINE_SEPARATOR);
        for (Iterator it = tableDefinitions.values().iterator(); it.hasNext();) {
            TableDefinition tableDef = (TableDefinition) it.next();
            sb.append(sqlCodeCreator.createTable(tableDef));
        }

        if (elements.size() > 1) {
            sb.append("-- Foreign key definitions").append(LINE_SEPARATOR);
            for (Iterator it = foreignKeyDefinitions.iterator(); it.hasNext();) {
                ForeignKeyDefinition fkDef = (ForeignKeyDefinition) it.next();
                sb.append(sqlCodeCreator.createForeignKey(fkDef));
            }
        }

        return sb.toString();
    }

    private Map tableDefinitions;

    private List foreignKeyDefinitions;

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
        String filename = "script.sql";
        if (!path.endsWith(FILE_SEPARATOR)) {
            path += FILE_SEPARATOR;
        }

        Collection result = new ArrayList();
        String fullFilename = path + filename;

        LOG.debug("validating model");
        ModelValidator validator = new ModelValidator();
        List problems = validator.validate(elements);
        if (problems.size() > 0 && elements.size() > 1) {
            LOG.debug("model not valid, exiting code generation");
            String error = Utils.stringsToString(problems, LINE_SEPARATOR);

            ExceptionDialog ed = new ExceptionDialog(ProjectBrowser
                    .getInstance(), "Error in model", "Model not valid", error);
            ed.setModal(true);
            ed.setVisible(true);
        } else if (SelectCodeCreatorDialog.execute()) {
            String code = generateCode(elements);
            writeFile(fullFilename, code);
            result.add(fullFilename);
        }
        return result;
    }

    private void writeFile(String filename, String content) {
        BufferedWriter fos = null;
        try {
            String inputSrcEnc = Configuration
                    .getString(Argo.KEY_INPUT_SOURCE_ENCODING);
            if (inputSrcEnc == null || inputSrcEnc.trim().equals("")) {
                inputSrcEnc = System.getProperty("file.encoding");
            }
            fos = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), inputSrcEnc));
            fos.write(content);
        } catch (IOException e) {
            LOG.error("IO Exception: " + e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOG.error("FAILED: " + filename);
            }
        }
    }

    private Collection getForeignKeyDefinitions(Object relation) {
        Collection fkDefs = new HashSet();
        Collection assocEnds = Model.getFacade().getAssociationEnds(relation);

        for (Iterator it = assocEnds.iterator(); it.hasNext();) {
            Object assocEnd = it.next();

            Collection otherAssocEnds = Model.getFacade()
                    .getOtherAssociationEnds(assocEnd);
            Object otherAssocEnd = otherAssocEnds.iterator().next();

            ForeignKeyDefinition fkDef = getFkDef(relation, assocEnd,
                    otherAssocEnd);
            if (fkDef != null) {
                fkDefs.add(fkDef);
            }
        }

        return fkDefs;
    }

    private ForeignKeyDefinition getFkDef(Object relation, Object assocEnd,
            Object otherAssocEnd) {
        Object assoc = Model.getFacade().getAssociation(assocEnd);
        List fkAttributes = Utils.getFkAttributes(relation, assoc);
        int otherUpper = Model.getFacade().getUpper(otherAssocEnd);
        if (otherUpper != 1 || fkAttributes.size() == 0) {
            return null;
        }
        ForeignKeyDefinition fkDef = new ForeignKeyDefinition();

        List srcAttributes = new ArrayList();

        Object srcRelation = Model.getFacade().getClassifier(otherAssocEnd);
        for (Iterator it2 = fkAttributes.iterator(); it2.hasNext();) {
            Object fkAttr = it2.next();
            Object srcAttr = Utils.getSourceAttribute(fkAttr, srcRelation);
            srcAttributes.add(srcAttr);
        }

        // List colNames = new ArrayList();
        TableDefinition tableDef = (TableDefinition) tableDefinitions
                .get(relation);
        fkDef.setTable(tableDef);
        for (Iterator it = fkAttributes.iterator(); it.hasNext();) {
            Object fkAttr = it.next();
            // colNames.add(Model.getFacade().getName(fkAttr));

            ColumnDefinition colDef = tableDef.getColumnDefinition(Model
                    .getFacade().getName(fkAttr));
            fkDef.addColumnDefinition(colDef);
        }

        // List refColNames = new ArrayList();
        tableDef = (TableDefinition) tableDefinitions.get(srcRelation);
        fkDef.setReferencesTable(tableDef);
        for (Iterator it = srcAttributes.iterator(); it.hasNext();) {
            Object srcAttr = it.next();
            // refColNames.add(Model.getFacade().getName(srcAttr));

            ColumnDefinition colDef = tableDef.getColumnDefinition(Model
                    .getFacade().getName(srcAttr));
            fkDef.addReferencesColumn(colDef);
        }

        int lower = Model.getFacade().getLower(assocEnd);
        int upper = Model.getFacade().getUpper(assocEnd);
        int refLower = Model.getFacade().getLower(otherAssocEnd);
        int refUpper = Model.getFacade().getUpper(otherAssocEnd);

        fkDef.setForeignKeyName(Model.getFacade().getName(assoc));

        // fkDef.setTableName(Model.getFacade().getName(relation));
        // fkDef.setColumnNames(colNames);

        // fkDef.setReferencesTableName(Model.getFacade().getName(srcRelation));
        // fkDef.setReferencesColumnNames(refColNames);

        fkDef.setLower(lower);
        fkDef.setUpper(upper);
        fkDef.setReferencesLower(refLower);
        fkDef.setReferencesUpper(refUpper);

        return fkDef;
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

    public List getSqlCodeCreators() {
        return sqlCodeCreators;
    }

    public DomainMapper getDomainMapper() {
        return domainMapper;
    }
} /* end class GeneratorSql */

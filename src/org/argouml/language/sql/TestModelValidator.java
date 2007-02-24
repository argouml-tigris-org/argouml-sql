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
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.ui.ProjectBrowser;

public class TestModelValidator extends TestCase {
    private Object namespace;

    private Object intType;

    /**
     * Load a sample model with diagrams.
     * 
     * @throws Exception
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        Object mmodel = Model.getModelManagementFactory().createModel();
        Model.getCoreHelper().setName(mmodel, "untitledModel");
        Model.getModelManagementFactory().setRootModel(mmodel);
        namespace = Model.getModelManagementFactory().createPackage();
        intType = Model.getCoreFactory().buildDataType("int", namespace);
        Model.getCoreHelper().setName(namespace, "untitledNamespace");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRule1() {
        Object valid = buildRelation("Valid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid);
        Collection problems = mv.validate(elements);
        assertTrue(problems.size() == 0);

        Object invalid = Model.getCoreFactory()
                .buildClass("Invalid", namespace);
        Model.getCoreFactory().buildAttribute(invalid, namespace, intType);

        elements.clear();
        elements.add(invalid);
        problems = mv.validate(elements);
        assertTrue(problems.size() == 1);
    }

    private Object buildRelation(String name) {
        return buildRelation(name, "id");
    }

    private Object buildRelation(String name, String pkAttrName) {
        Object relation = Model.getCoreFactory().buildClass(name, namespace);
        addPrimaryKeyAttribute(relation, pkAttrName);
        return relation;
    }

    private Object addPrimaryKeyAttribute(Object relation) {
        return addPrimaryKeyAttribute(relation, "");
    }

    private Object addPrimaryKeyAttribute(Object relation, String attrName) {
        Object pkAttribute = Model.getCoreFactory().buildAttribute(relation,
                namespace, intType);
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(pkAttribute, "PK", namespace);
        Model.getCoreHelper().addStereotype(pkAttribute, stereotype);
        Model.getCoreHelper().setName(pkAttribute, attrName);
        return pkAttribute;
    }

    private Object addForeignKeyAttribute(Object relation) {
        return addForeignKeyAttribute(relation, "");
    }

    private Object addForeignKeyAttribute(Object relation, String attrName) {
        Object fkAttribute = Model.getCoreFactory().buildAttribute(relation,
                namespace, intType);
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(fkAttribute, "FK", namespace);
        Model.getCoreHelper().addStereotype(fkAttribute, stereotype);
        Model.getCoreHelper().setName(fkAttribute, attrName);
        return fkAttribute;
    }

    private void setFkAttributeAssocName(Object fkAttribute, String assocName) {
        if (assocName != null && assocName.length() > 0) {
            Object taggedValue = Model.getExtensionMechanismsFactory()
                    .buildTaggedValue(
                            GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE,
                            assocName);
            Model.getExtensionMechanismsHelper().addTaggedValue(fkAttribute,
                    taggedValue);
        }
    }

    private void setFkAttributeSrcCol(Object fkAttribute, String srcColName) {
        if (srcColName != null && srcColName.length() > 0) {
            Object taggedValue = Model.getExtensionMechanismsFactory()
                    .buildTaggedValue(GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE,
                            srcColName);
            Model.getExtensionMechanismsHelper().addTaggedValue(fkAttribute,
                    taggedValue);
        }
    }

    public void testRule2Valid() {
        Object valid1 = buildRelation("Valid1");
        Object valid2 = buildRelation("Valid2");

        Model.getCoreFactory().buildAssociation(valid1, false, valid2, false,
                "fk_valid");
        Object fkAttribute = addForeignKeyAttribute(valid1);
        setFkAttributeAssocName(fkAttribute, "fk_valid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule2Invalid() {
        Object invalid1 = buildRelation("Invalid1");
        Object invalid2 = buildRelation("Invalid2");

        Model.getCoreFactory().buildAssociation(invalid1, false, invalid2,
                false, "fk_invalid");
        addForeignKeyAttribute(invalid1);

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        Collection problems = mv.validate(elements);

        assertEquals(2, problems.size());
    }

    public void testRule3Valid() {
        Object valid1 = buildRelation("Valid1", "valid1_id");
        Object valid2 = buildRelation("Valid2", "valid2_id1");
        addPrimaryKeyAttribute(valid2, "valid2_id2");
        Object valid = buildRelation("Valid", "valid_id");

        Model.getCoreFactory().buildAssociation(valid, false, valid1, false,
                "fk_valid1");
        Object fkAttribute = addForeignKeyAttribute(valid, "ref_to_valid1_id");
        setFkAttributeAssocName(fkAttribute, "fk_valid1");

        Model.getCoreFactory().buildAssociation(valid, false, valid2, false,
                "fk_valid2");
        fkAttribute = addForeignKeyAttribute(valid, "valid2_id1");
        setFkAttributeAssocName(fkAttribute, "fk_valid2");

        fkAttribute = addForeignKeyAttribute(valid, "some_id");
        setFkAttributeAssocName(fkAttribute, "fk_valid2");
        setFkAttributeSrcCol(fkAttribute, "valid2_id2");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        elements.add(valid);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule3Invalid() {
        Object invalid1 = buildRelation("Invalid1", "invalid1_id1");
        addPrimaryKeyAttribute(invalid1, "invalid2_id2");
        Object invalid = buildRelation("Invalid", "invalid_id");

        Model.getCoreFactory().buildAssociation(invalid, false, invalid1,
                false, "fk_invalid1");
        Object fkAttribute = addForeignKeyAttribute(invalid, "invalid1_id");
        setFkAttributeAssocName(fkAttribute, "fk_invalid1");

        fkAttribute = addForeignKeyAttribute(invalid, "some_id");
        setFkAttributeAssocName(fkAttribute, "fk_invalid1");
        setFkAttributeSrcCol(fkAttribute, "not_existing");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid);
        Collection problems = mv.validate(elements);

        assertEquals(2, problems.size());
    }

    public void testRule4Valid() {
        Object valid1 = buildRelation("Valid1", "valid1_id");
        Object valid2 = buildRelation("Valid2", "valid2_id");

        Object association = Model.getCoreFactory().buildAssociation(valid1,
                false, valid2, false, "fk_valid");
        Collection conns = Model.getFacade().getConnections(association);
        assertEquals(2, conns.size());

        Iterator it = conns.iterator();
        Object valid1End = it.next();
        Object valid2End = it.next();
        Object mult1 = Model.getDataTypesFactory().createMultiplicity(0, -1);
        Object mult2 = Model.getDataTypesFactory().createMultiplicity(1, 1);

        Model.getCoreHelper().setMultiplicity(valid1End, mult1);
        Model.getCoreHelper().setMultiplicity(valid2End, mult2);

        Object fkAttribute = addForeignKeyAttribute(valid1, "valid2_id");
        setFkAttributeAssocName(fkAttribute, "fk_valid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule4Invalid() {
        Object invalid1 = buildRelation("Invalid1", "invalid1_id");
        Object invalid2 = buildRelation("Invalid2", "invalid2_id");
        Object association = Model.getCoreFactory().buildAssociation(invalid1,
                false, invalid2, false, "fk_invalid");
        Object fkAttribute = addForeignKeyAttribute(invalid2, "invalid1_id");
        setFkAttributeAssocName(fkAttribute, "fk_invalid");

        Collection conns = Model.getFacade().getConnections(association);
        assertEquals(2, conns.size());

        Iterator it = conns.iterator();
        Object valid1End = it.next();
        Object valid2End = it.next();
        Object mult1 = Model.getDataTypesFactory().createMultiplicity(0, -1);
        Object mult2 = Model.getDataTypesFactory().createMultiplicity(1, 1);

        Model.getCoreHelper().setMultiplicity(valid1End, mult1);
        Model.getCoreHelper().setMultiplicity(valid2End, mult2);

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        Collection problems = mv.validate(elements);

        assertEquals(1, problems.size());
    }

    public void testRule5Valid() {
        Object valid1 = buildRelation("Valid1", "valid1_id");
        Object valid2 = buildRelation("Valid2", "valid2_id");
        Object valid = buildRelation("Valid", "valid_id");

        Object association = Model.getCoreFactory().buildAssociation(valid1,
                false, valid, false, "fk_valid1");
        Object fkAttribute = addForeignKeyAttribute(valid1, "valid_id");
        setFkAttributeAssocName(fkAttribute, "fk_valid1");

        Collection conns = Model.getFacade().getConnections(association);
        assertEquals(2, conns.size());

        Iterator it = conns.iterator();
        Object valid1End = it.next();
        Object validEnd = it.next();
        Object mult1 = Model.getDataTypesFactory().createMultiplicity(0, -1);
        Object mult = Model.getDataTypesFactory().createMultiplicity(0, 1);

        Model.getCoreHelper().setMultiplicity(valid1End, mult1);
        Model.getCoreHelper().setMultiplicity(validEnd, mult);

        association = Model.getCoreFactory().buildAssociation(valid2,
                false, valid, false, "fk_valid2");
        fkAttribute = addForeignKeyAttribute(valid2, "valid_id");
        setFkAttributeAssocName(fkAttribute, "fk_valid2");

        conns = Model.getFacade().getConnections(association);
        assertEquals(2, conns.size());

        it = conns.iterator();
        Object valid2End = it.next();
        validEnd = it.next();
        Object mult2 = Model.getDataTypesFactory().createMultiplicity(0, -1);
        mult = Model.getDataTypesFactory().createMultiplicity(0, 1);

        Model.getCoreHelper().setMultiplicity(valid2End, mult2);
        Model.getCoreHelper().setMultiplicity(validEnd, mult);

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        elements.add(valid);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }
}

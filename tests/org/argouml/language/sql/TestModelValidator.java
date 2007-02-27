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

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.argouml.model.Model;

public class TestModelValidator extends TestCaseSql {
    public void testRule1() {
        Object valid = helper.buildRelation("Valid", "valid_id");

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

    public void testRule2Valid() {
        Object valid1 = helper.buildRelation("Valid1", "valid1_id");
        Object valid2 = helper.buildRelation("Valid2", "valid2_id");

        Model.getCoreFactory().buildAssociation(valid1, false, valid2, false,
                "fk_valid");
        Object fkAttribute = helper.addForeignKeyAttribute(valid1, "valid2_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule2Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid2 = helper.buildRelation("Invalid2", "invalid2_id");

        Model.getCoreFactory().buildAssociation(invalid1, false, invalid2,
                false, "fk_invalid");
        helper.addForeignKeyAttribute(invalid1, "invalid2_id");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        Collection problems = mv.validate(elements);

        assertEquals(2, problems.size());
    }

    public void testRule3Valid() {
        Object valid1 = helper.buildRelation("Valid1", "valid1_id");
        Object valid2 = helper.buildRelation("Valid2", "valid2_id1");
        helper.addPrimaryKeyAttribute(valid2, "valid2_id2");
        Object valid = helper.buildRelation("Valid", "valid_id");

        Model.getCoreFactory().buildAssociation(valid, false, valid1, false,
                "fk_valid1");
        Object fkAttribute = helper.addForeignKeyAttribute(valid,
                "ref_to_valid1_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid1");

        Model.getCoreFactory().buildAssociation(valid, false, valid2, false,
                "fk_valid2");
        fkAttribute = helper.addForeignKeyAttribute(valid, "valid2_id1");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid2");

        fkAttribute = helper.addForeignKeyAttribute(valid, "some_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid2");
        helper.setFkAttributeSrcCol(fkAttribute, "valid2_id2");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        elements.add(valid);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule3Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id1");
        helper.addPrimaryKeyAttribute(invalid1, "invalid2_id2");
        Object invalid = helper.buildRelation("Invalid", "invalid_id");

        Model.getCoreFactory().buildAssociation(invalid, false, invalid1,
                false, "fk_invalid1");
        Object fkAttribute = helper.addForeignKeyAttribute(invalid,
                "invalid1_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid1");

        fkAttribute = helper.addForeignKeyAttribute(invalid, "some_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid1");
        helper.setFkAttributeSrcCol(fkAttribute, "not_existing");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid);
        Collection problems = mv.validate(elements);

        assertEquals(2, problems.size());
    }

    public void testRule4Valid() {
        Object valid1 = helper.buildRelation("Valid1", "valid1_id");
        Object valid2 = helper.buildRelation("Valid2", "valid2_id");

        helper.buildAssociation(valid1, 0, -1, valid2, 1, 1, "fk_valid");

        Object fkAttribute = helper.addForeignKeyAttribute(valid1, "valid2_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule4Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid2 = helper.buildRelation("Invalid2", "invalid2_id");
        helper.buildAssociation(invalid1, 0, -1, invalid2, 1, 1, "fk_invalid");
        Object fkAttribute = helper.addForeignKeyAttribute(invalid2,
                "invalid1_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        Collection problems = mv.validate(elements);

        assertEquals(1, problems.size());
    }

    public void testRule5Valid() {
        Object valid1 = helper.buildRelation("Valid1", "valid1_id");
        Object valid2 = helper.buildRelation("Valid2", "valid2_id");
        Object valid = helper.buildRelation("Valid", "valid_id");

        helper.buildAssociation(valid1, 0, -1, valid, 0, 1, "fk_valid1");
        Object fkAttribute = helper.addForeignKeyAttribute(valid1, "valid_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid1");

        helper.buildAssociation(valid2, 0, -1, valid, 0, 1, "fk_valid2");
        fkAttribute = helper.addForeignKeyAttribute(valid2, "valid_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid2");
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(fkAttribute, "NULL", namespace);
        Model.getCoreHelper().addStereotype(fkAttribute, stereotype);

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        elements.add(valid);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule5Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid = helper.buildRelation("Invalid", "invalid_id");

        helper.buildAssociation(invalid1, 0, -1, invalid, 1, 1, "fk_invalid1");

        Object fkAttribute = helper.addForeignKeyAttribute(invalid1,
                "invalid_id");
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(fkAttribute, "NULL", namespace);
        Model.getCoreHelper().addStereotype(fkAttribute, stereotype);

        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid1");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid);
        Collection problems = mv.validate(elements);

        assertEquals(1, problems.size());
    }

    public void testRule6Valid() {
        Object valid1 = helper.buildRelation("Valid1", "valid1_id");
        Object valid2 = helper.buildRelation("Valid2", "valid2_id");
        Object valid = helper.buildRelation("Valid", "valid_id");

        helper.buildAssociation(valid1, 0, -1, valid, 1, 1, "fk_valid1");
        Object fkAttribute = helper.addForeignKeyAttribute(valid1, "valid_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid1");

        helper.buildAssociation(valid2, 0, -1, valid, 1, 1, "fk_valid2");
        fkAttribute = helper.addForeignKeyAttribute(valid2, "valid_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid2");
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(fkAttribute, "NOT NULL", namespace);
        Model.getCoreHelper().addStereotype(fkAttribute, stereotype);

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        elements.add(valid);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    public void testRule6Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid = helper.buildRelation("Invalid", "invalid_id");

        helper.buildAssociation(invalid1, 0, -1, invalid, 0, 1, "fk_invalid1");

        Object fkAttribute = helper.addForeignKeyAttribute(invalid1,
                "invalid_id");
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(fkAttribute, "NOT NULL", namespace);
        Model.getCoreHelper().addStereotype(fkAttribute, stereotype);

        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid1");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid);
        Collection problems = mv.validate(elements);

        assertEquals(1, problems.size());
    }

    public static Test suite() {
        return new TestSuite(TestModelValidator.class);
    }
}

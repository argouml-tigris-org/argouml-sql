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

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.argouml.model.Model;

/**
 * Test class for MethodValidator. Builds a diagram which is valid for some rule
 * and then checks if validation reports no error. Builds another diagram which
 * is invalid for some rule an checks if validation reports errors.
 * 
 * TODO Commit a list of rules and corresponding valid and invalid diagrams.
 * 
 * @author drahmann
 */
public class TestModelValidator extends BaseTestCaseSql {
    /**
     * Tests if ModelValidator considers a relation with a pk-attribute valid.
     */
    public void testRule1Valid() {
        Object valid = helper.buildRelation("Valid", "valid_id");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid);
        Collection problems = mv.validate(elements);
        assertTrue(problems.size() == 0);
    }

    /**
     * Tests if ModelValidator considers a relation without any pk-attribute
     * invalid.
     */
    public void testRule1Invalid() {
        Object invalid = Model.getCoreFactory()
                .buildClass("Invalid", namespace);
        Model.getCoreFactory().buildAttribute(invalid, namespace, intType);

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid);
        Collection problems = mv.validate(elements);
        assertTrue(problems.size() == 1);
    }

    /**
     * Tests if ModelValidator considers a fk-attribute valid if
     * <ul>
     * <li>it has a tagged value GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE
     * with the value of an association ending at the relation that contains the
     * attribute</li>
     * <li>the other side multiplicity upper limit of an association is one</li>
     * </ul>
     * 
     * Further checks if ModelValidator considers an association valid if
     * <ul>
     * <li>it is at most 1:n</li>
     * <li>a fk-attribute is defined for the association</li>
     * </ul>
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute without a tagged value
     * GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE invalid.
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute valid if it can clearly
     * determine the source column.
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute invalid if it cannot
     * clearly determine the source column.
     * 
     */
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

    /**
     * @deprecated Also checked by rule2Valid.
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute invalid if the upper
     * limit of the multiplicity of the opposite side of the association is
     * greater than 1.
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute valid if
     * <ul>
     * <li>it is not of the stereotype GeneratorSql.NULL_STEREOTYPE</li>
     * <li>it is of the stereotype GeneratorSql.NULL_STEREOTYPE and the lower
     * limit of the multiplicity of the opposite side of the association is zero</li>
     * </ul>
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute invalid if is of the
     * stereotype GeneratorSql.NULL_STEREOTYPE and the lower limit of the
     * multiplicity of the opposite side of the association is one.
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute valid if
     * <ul>
     * <li>it is not of the stereotype GeneratorSql.NOT_NULL_STEREOTYPE</li>
     * <li>it is of the stereotype GeneratorSql.NOT_NULL_STEREOTYPE and the
     * lower limit of the multiplicity of the opposite side of the association
     * is one</li>
     * </ul>
     */
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

    /**
     * Tests if ModelValidator considers a fk-attribute invalid if it is of the
     * stereotype GeneratorSql.NOT_NULL_STEREOTYPE and the lower limit of the
     * multiplicity of the opposite side of the association is zero
     */
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

    /**
     * Tests if ModelValidator considers a model valid if all association names
     * are unique.
     */
    public void testRule7Valid() {
        Object valid1 = helper.buildRelation("Valid1", "valid1_id");
        Object valid2 = helper.buildRelation("Valid2", "valid2_id");
        Object valid3 = helper.buildRelation("Valid3", "valid3_id");

        helper.buildAssociation(valid2, 0, -1, valid1, 1, 1, "fk_valid12");
        Object fkAttribute = helper.addForeignKeyAttribute(valid2, "valid1_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid12");

        helper.buildAssociation(valid3, 0, -1, valid2, 1, 1, "fk_valid23");
        fkAttribute = helper.addForeignKeyAttribute(valid3, "valid2_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_valid23");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(valid1);
        elements.add(valid2);
        elements.add(valid3);
        Collection problems = mv.validate(elements);

        assertEquals(0, problems.size());
    }

    /**
     * Tests if ModelValidator considers a model invalid if some association
     * names are equal.
     */
    public void testRule7Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid2 = helper.buildRelation("Invalid2", "invalid2_id");
        Object invalid3 = helper.buildRelation("Invalid3", "invalid3_id");

        helper.buildAssociation(invalid2, 0, -1, invalid1, 1, 1, "fk_invalid");
        Object fkAttribute = helper.addForeignKeyAttribute(invalid2,
                "invalid1_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid");

        helper.buildAssociation(invalid3, 0, -1, invalid2, 1, 1, "fk_invalid");
        fkAttribute = helper.addForeignKeyAttribute(invalid3, "invalid2_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        elements.add(invalid3);
        Collection problems = mv.validate(elements);

        assertTrue(problems.size() > 0);
    }

    /**
     * Tests if ModelValidator considers an association invalid if it is n:m.
     */
    public void testRule8Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid2 = helper.buildRelation("Invalid2", "invalid2_id");

        helper.buildAssociation(invalid1, 0, -1, invalid2, 0, -1, "fk_invalid");
        Object fkAttribute = helper.addForeignKeyAttribute(invalid1,
                "invalid2_id");
        helper.setFkAttributeAssocName(fkAttribute, "fk_invalid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        Collection problems = mv.validate(elements);

        assertTrue(problems.size() > 0);
    }

    /**
     * Tests if ModelValidator considers an association invalid if there exists
     * no fk-attribute which references the association.
     */
    public void testRule9Invalid() {
        Object invalid1 = helper.buildRelation("Invalid1", "invalid1_id");
        Object invalid2 = helper.buildRelation("Invalid2", "invalid2_id");

        helper.buildAssociation(invalid1, 0, -1, invalid2, 0, -1, "fk_invalid");

        ModelValidator mv = new ModelValidator();
        Collection elements = new HashSet();
        elements.add(invalid1);
        elements.add(invalid2);
        Collection problems = mv.validate(elements);

        assertTrue(problems.size() > 0);
    }

    /**
     * 
     * @return A TestSuite containing TestModelValidator.
     */
    public static Test suite() {
        return new TestSuite(TestModelValidator.class);
    }
}

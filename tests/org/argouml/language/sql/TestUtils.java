/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    drahmann
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.argouml.model.Model;

/**
 * Test class for Utils. Contains methods for reaching full base path coverage
 * of Utils' methods (except the very simple ones with only one path).
 * 
 * @author drahmann
 */
public class TestUtils extends BaseTestCaseSql {
    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.getAssociationForName(Object, String)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#getAssociationForName(Object,String))
     */
    public void no_testGetAssociationForNameObjectString1() {
        Object relation1 = helper.buildRelation("Relation1", "rel1_id");
        Object relation2 = helper.buildRelation("Relation2", "rel2_id");
        Object fkAttr = helper.addForeignKeyAttribute(relation1, "rel2_id");
        helper.setFkAttributeAssocName(fkAttr, "fk");
        Object association = helper.buildAssociation(relation1, 0, -1,
                relation2, 0, 1, "fk");

        Object methodReturn = Utils.getAssociationForName(relation1, "fk");
        assertEquals(association, methodReturn);

        methodReturn = Utils
                .getAssociationForName(relation1, "fk_not_existing");
        assertEquals(null, methodReturn);
    }

    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.getAssociationForName(Object, String)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#getAssociationForName(Object,String))
     */
    public void testGetAssociationForNameObjectString2() {
        Object relation1 = helper.buildRelation("Relation1", "rel1_id");

        // Method under test
        Object methodReturn = Utils.getAssociationForName(relation1, "fk");

        assertEquals(null, methodReturn);
    }

    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.getAttributeForName(Object, String)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#getAttributeForName(Object,String))
     */
    public void testGetAttributeForNameObjectString() {
        Object relation1 = Model.getCoreFactory().buildClass("Relation1",
                namespace);

        Object methodReturn = Utils.getAttributeForName(relation1, "rel1_id");
        assertEquals(null, methodReturn);

        Object pkAttr = helper.addPrimaryKeyAttribute(relation1, "rel1_id");
        methodReturn = Utils.getAttributeForName(relation1, "rel1_id");
        assertEquals(pkAttr, methodReturn);

        methodReturn = Utils.getAttributeForName(relation1, "not_existing");
        assertEquals(null, methodReturn);
    }

    /**
     * Test method for 'org.argouml.language.sql.Utils.getFkAttribute(Object,
     * Object)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#getFkAttributes(Object,Object))
     */
    public void no_testGetFkAttributesObjectObject() {
        Object relation1 = Model.getCoreFactory().buildClass("Relation1",
                namespace);
        Object relation2 = helper.buildRelation("Relation2", "rel2_id");

        Object association = helper.buildAssociation(relation1, 0, -1,
                relation2, 0, 1, "fk_12");

        List methodReturn = Utils.getFkAttributes(relation2, association);
        assertEquals(0, methodReturn.size());

        methodReturn = Utils.getFkAttributes(relation1, association);
        assertEquals(0, methodReturn.size());

        Object fkAttr = helper.addForeignKeyAttribute(relation1, "rel2_id");
        helper.setFkAttributeAssocName(fkAttr, "fk_12");

        methodReturn = Utils.getFkAttributes(relation1, association);
        assertTrue(methodReturn.contains(fkAttr));
        assertEquals(1, methodReturn.size());
    }

    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.getPrimaryKeyAttributes(Object)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#getPrimaryKeyAttributes(Object))
     */
    public void testGetPrimaryKeyAttributesObject() {
        Object relation1 = Model.getCoreFactory().buildClass("Relation1",
                namespace);
        Collection methodReturn = Utils.getPrimaryKeyAttributes(relation1);
        assertEquals(0, methodReturn.size());

        Object attr = Model.getCoreFactory()
                .buildAttribute2(relation1, intType);
        methodReturn = Utils.getPrimaryKeyAttributes(relation1);
        assertEquals(0, methodReturn.size());

        Model.getCoreHelper().removeFeature(relation1, attr);
        Object pkAttr = helper.addPrimaryKeyAttribute(relation1, "rel1_id");
        methodReturn = Utils.getPrimaryKeyAttributes(relation1);
        assertEquals(pkAttr, methodReturn.iterator().next());
    }

    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.getSourceAttribute(Object, Object)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#getSourceAttribute(Object,Object))
     */
    public void testGetSourceAttributeObjectObject() {
        Object relation1 = helper.buildRelation("Relation1", "rel1_id");
        Object relation2 = helper.buildRelation("Relation2", "rel2_id");

        Object fkAttr = helper.addForeignKeyAttribute(relation1, "rel2_id");
        Object pkAttr = Utils.getAttributeForName(relation2, "rel2_id");

        Object methodReturn = Utils.getSourceAttribute(fkAttr, relation2);
        assertEquals(methodReturn, pkAttr);

        fkAttr = helper.addForeignKeyAttribute(relation1, "some_id");
        methodReturn = Utils.getSourceAttribute(fkAttr, relation2);
        assertEquals(methodReturn, pkAttr);

        Object srcCol = Model.getExtensionMechanismsFactory().buildTaggedValue(
                GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE, "rel2_id");
        Model.getExtensionMechanismsHelper().addTaggedValue(fkAttr, srcCol);
        methodReturn = Utils.getSourceAttribute(fkAttr, relation2);
        assertEquals(methodReturn, pkAttr);

        helper.addPrimaryKeyAttribute(relation2, "rel2_id2");
        fkAttr = helper.addForeignKeyAttribute(relation1, "rel2_id5");
        methodReturn = Utils.getSourceAttribute(fkAttr, relation2);
        assertEquals(null, methodReturn);
    }

    public static Test suite() {
        return new TestSuite(TestUtils.class);
    }

    /**
     * Test method for 'org.argouml.language.sql.Utils.stringsToString(List,
     * String)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#stringsToString(List,String))
     */
    public void testStringsToStringListString() {
        List strings = new ArrayList();
        String separators = ",";

        String string = Utils.stringsToString(strings, separators);
        assertEquals(string, "");

        strings.add("string1");
        string = Utils.stringsToString(strings, separators);
        assertEquals(string, "string1");

        strings.add("string2");
        string = Utils.stringsToString(strings, separators);
        assertEquals(string, "string1,string2");
    }

    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.stringToStringList(String, String)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#stringToStringList(String,String))
     * 
     */
    public void testStringToStringListStringString() {
        String s = "";
        List l = Utils.stringToStringList(s, " ");
        assertEquals(0, l.size());

        s = "some string value";
        l = Utils.stringToStringList(s, " ");
        assertEquals(s, Utils.stringsToString(l, " "));
    }

    /**
     * Test method for
     * 'org.argouml.language.sql.Utils.stringToStringList(String)'
     * 
     * @CoViewTest (coview_methodundertest=org.argouml.language.sql
     *             .Utils#stringToStringList(String))
     */
    public void testStringToStringListString() {
        String s = "";
        List l = Utils.stringToStringList(s);
        assertEquals(0, l.size());

        s = "some string value";
        l = Utils.stringToStringList(s);
        assertEquals(s, Utils.stringsToString(l, " "));
    }
}

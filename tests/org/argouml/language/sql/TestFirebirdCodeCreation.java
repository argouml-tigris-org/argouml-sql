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

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Class for testing code creation.
 * 
 * @author Anne und Kai
 */
public class TestFirebirdCodeCreation extends BaseTestCaseSql {
    private void compareWhenOneToOne(ForeignKeyDefinition fd,
            StringBuffer sbExpectedCode, FirebirdSqlCodeCreator c) {
        // FK for (1,1):(0,1)-relationship
        fd.setUpper(1);
        c.resetCounters();
        String generatedCode = c.createForeignKey(fd);

        String exceptionName = "EXC_ONE_TO_ONE_VIOLATED1";

        StringBuffer sbExpTriggerBody = new StringBuffer();
        sbExpTriggerBody.append("DECLARE VARIABLE x INTEGER; ");
        sbExpTriggerBody.append("BEGIN ");
        sbExpTriggerBody.append("SELECT COUNT(*) FROM A ");
        sbExpTriggerBody.append("WHERE b_id = NEW.b_id AND ");
        sbExpTriggerBody.append("id <> NEW.id INTO :x; ");
        sbExpTriggerBody.append("IF (:x = 1) THEN EXCEPTION ");
        sbExpTriggerBody.append(exceptionName).append("; ");
        sbExpTriggerBody.append("END !! ");

        sbExpectedCode.append("CREATE EXCEPTION ").append(exceptionName);
        sbExpectedCode.append(" 'One record in B references ");
        sbExpectedCode.append("more than one record in A'; ");

        sbExpectedCode.append("SET TERM !! ; ");

        sbExpectedCode.append("CREATE TRIGGER trig_bef_ins_A FOR A ");
        sbExpectedCode.append("BEFORE INSERT AS ");

        sbExpectedCode.append(sbExpTriggerBody);

        sbExpectedCode.append("CREATE TRIGGER trig_bef_upd_A FOR A ");
        sbExpectedCode.append("BEFORE UPDATE AS ");

        sbExpectedCode.append(sbExpTriggerBody);

        sbExpectedCode.append("SET TERM ; !! ");

        compare(generatedCode, sbExpectedCode.toString());
    }

    public void testCreateForeignKeyNotNull() {
        TableDefinition tdA = new TableDefinition();
        tdA.setName("A");
        tdA.addColumnDefinition(new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE));
        ColumnDefinition cd_A_b_id = new ColumnDefinition("INTEGER", "b_id",
                Boolean.FALSE);
        tdA.addColumnDefinition(cd_A_b_id);
        tdA.addPrimaryKeyField("id");

        TableDefinition tdB = new TableDefinition();
        tdB.setName("B");
        ColumnDefinition cd_B_id = new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE);
        tdB.addColumnDefinition(cd_B_id);
        tdB.addPrimaryKeyField("id");

        ForeignKeyDefinition fd = new ForeignKeyDefinition();
        fd.setTable(tdA);
        fd.addColumnDefinition(cd_A_b_id);
        fd.setLower(0);
        fd.setUpper(-1);

        fd.setReferencesTable(tdB);
        fd.addReferencesColumn(cd_B_id);
        fd.setReferencesLower(1);
        fd.setReferencesUpper(1);

        fd.setForeignKeyName("fk_test");

        // FK for (1,1):(0,n)-relationship
        FirebirdSqlCodeCreator c = new FirebirdSqlCodeCreator();
        String generatedCode = c.createForeignKey(fd);

        StringBuffer sbExpectedCode = new StringBuffer();
        sbExpectedCode.append("ALTER TABLE A ADD CONSTRAINT fk_test ");
        sbExpectedCode.append("FOREIGN KEY (b_id) REFERENCES B (id) ");
        sbExpectedCode.append("ON DELETE CASCADE; ");
        String expectedCode = sbExpectedCode.toString();

        compare(generatedCode, expectedCode);
        compareWhenOneToOne(fd, sbExpectedCode, c);
    }
    
    public void testMultipleFkColumns() {
        TableDefinition tdA = new TableDefinition();
        tdA.setName("A");
        tdA.addColumnDefinition(new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE));
        ColumnDefinition cd_A_b_id = new ColumnDefinition("INTEGER", "b_id",
                Boolean.FALSE);
        ColumnDefinition cd_A_b_id2 = new ColumnDefinition("INTEGER", "b_id2",
                Boolean.FALSE);
        tdA.addColumnDefinition(cd_A_b_id);
        tdA.addColumnDefinition(cd_A_b_id2);
        tdA.addPrimaryKeyField("id");

        TableDefinition tdB = new TableDefinition();
        tdB.setName("B");
        ColumnDefinition cd_B_id = new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE);
        ColumnDefinition cd_B_id2 = new ColumnDefinition("INTEGER", "id2",
                Boolean.FALSE);
        tdB.addColumnDefinition(cd_B_id);
        tdB.addColumnDefinition(cd_B_id2);
        tdB.addPrimaryKeyField("id");
        tdB.addPrimaryKeyField("id2");

        ForeignKeyDefinition fd = new ForeignKeyDefinition();
        fd.setTable(tdA);
        fd.addColumnDefinition(cd_A_b_id);
        fd.addColumnDefinition(cd_A_b_id2);
        fd.setLower(0);
        fd.setUpper(1);

        fd.setReferencesTable(tdB);
        fd.addReferencesColumn(cd_B_id);
        fd.addReferencesColumn(cd_B_id2);
        fd.setReferencesLower(1);
        fd.setReferencesUpper(1);

        fd.setForeignKeyName("fk_test");
        
        StringBuffer sbExpectedCode = new StringBuffer();
        sbExpectedCode.append("ALTER TABLE A ADD CONSTRAINT fk_test ");
        sbExpectedCode.append("FOREIGN KEY (b_id,b_id2) REFERENCES B (id,id2) ");
        sbExpectedCode.append("ON DELETE CASCADE; ");
        String exceptionName = "EXC_ONE_TO_ONE_VIOLATED1";

        StringBuffer sbExpTriggerBody = new StringBuffer();
        sbExpTriggerBody.append("DECLARE VARIABLE x INTEGER; ");
        sbExpTriggerBody.append("BEGIN ");
        sbExpTriggerBody.append("SELECT COUNT(*) FROM A ");
        sbExpTriggerBody.append("WHERE b_id = NEW.b_id AND ");
        sbExpTriggerBody.append("b_id2 = NEW.b_id2 AND ");
        sbExpTriggerBody.append("id <> NEW.id INTO :x; ");
        sbExpTriggerBody.append("IF (:x = 1) THEN EXCEPTION ");
        sbExpTriggerBody.append(exceptionName).append("; ");
        sbExpTriggerBody.append("END !! ");

        sbExpectedCode.append("CREATE EXCEPTION ").append(exceptionName);
        sbExpectedCode.append(" 'One record in B references ");
        sbExpectedCode.append("more than one record in A'; ");

        sbExpectedCode.append("SET TERM !! ; ");

        sbExpectedCode.append("CREATE TRIGGER trig_bef_ins_A FOR A ");
        sbExpectedCode.append("BEFORE INSERT AS ");

        sbExpectedCode.append(sbExpTriggerBody);

        sbExpectedCode.append("CREATE TRIGGER trig_bef_upd_A FOR A ");
        sbExpectedCode.append("BEFORE UPDATE AS ");

        sbExpectedCode.append(sbExpTriggerBody);

        sbExpectedCode.append("SET TERM ; !! ");

        FirebirdSqlCodeCreator c = new FirebirdSqlCodeCreator();
        String generatedCode = c.createForeignKey(fd);
        
        compare(generatedCode, sbExpectedCode.toString());
    }

    public void testCreateForeignKeyNull() {
        TableDefinition tdA = new TableDefinition();
        tdA.setName("A");
        tdA.addColumnDefinition(new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE));
        ColumnDefinition cd_A_b_id = new ColumnDefinition("INTEGER", "b_id",
                Boolean.FALSE);
        tdA.addColumnDefinition(cd_A_b_id);
        tdA.addPrimaryKeyField("id");

        TableDefinition tdB = new TableDefinition();
        tdB.setName("B");
        ColumnDefinition cd_B_id = new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE);
        tdB.addColumnDefinition(cd_B_id);
        tdB.addPrimaryKeyField("id");

        ForeignKeyDefinition fd = new ForeignKeyDefinition();
        fd.setTable(tdA);
        fd.addColumnDefinition(cd_A_b_id);
        fd.setLower(0);
        fd.setUpper(-1);

        fd.setReferencesTable(tdB);
        fd.addReferencesColumn(cd_B_id);
        fd.setReferencesLower(0);
        fd.setReferencesUpper(1);

        fd.setForeignKeyName("fk_test");

        FirebirdSqlCodeCreator c = new FirebirdSqlCodeCreator();
        String generatedCode = c.createForeignKey(fd);

        StringBuffer sbExpectedCode = new StringBuffer();
        sbExpectedCode.append("ALTER TABLE A ADD CONSTRAINT fk_test ");
        sbExpectedCode.append("FOREIGN KEY (b_id) REFERENCES B (id) ");
        sbExpectedCode.append("ON DELETE SET NULL; ");
        String expectedCode = sbExpectedCode.toString();

        compare(generatedCode, expectedCode);
        compareWhenOneToOne(fd, sbExpectedCode, c);
    }

    public void testCreateTable() {
        TableDefinition td = new TableDefinition();
        td.addColumnDefinition(new ColumnDefinition("INTEGER", "id",
                Boolean.FALSE));
        td.addColumnDefinition(new ColumnDefinition("VARCHAR(100)", "name",
                Boolean.TRUE));

        td.setName("Tabelle");
        td.addPrimaryKeyField("id");

        FirebirdSqlCodeCreator c = new FirebirdSqlCodeCreator();
        String generatedCode = c.createTable(td);

        StringBuffer sbExpectedCode = new StringBuffer();
        sbExpectedCode.append("CREATE TABLE Tabelle ( ");
        sbExpectedCode.append("id INTEGER NOT NULL, ");
        sbExpectedCode.append("name VARCHAR(100), ");
        sbExpectedCode.append("CONSTRAINT PK1 PRIMARY KEY (id) ");
        sbExpectedCode.append(");");
        String expectedCode = sbExpectedCode.toString();

        compare(generatedCode, expectedCode);
    }

    public void testGetName() {
        FirebirdSqlCodeCreator c = new FirebirdSqlCodeCreator();
        assertEquals(c.getName(), "Firebird");
    }

    private void compare(String generatedCode, String expectedCode) {
        List expectedTokens = Utils.stringToStringList(expectedCode);
        List generatedTokens = Utils.stringToStringList(generatedCode);

        Iterator itGenerated = generatedTokens.iterator();
        Iterator itExpected = expectedTokens.iterator();

        while (itGenerated.hasNext() && itExpected.hasNext()) {
            String generated = (String) itGenerated.next();
            String expected = (String) itExpected.next();

            generated = generated.trim();
            expected = expected.trim();

            assertEquals(generated, expected);
        }

        assertFalse(itGenerated.hasNext());
        assertFalse(itExpected.hasNext());
    }

    public static Test suite() {
        return new TestSuite(TestFirebirdCodeCreation.class);
    }
}

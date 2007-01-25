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

import java.util.Iterator;
import java.util.List;

public class FirebirdSqlCodeCreator implements SqlCodeCreator {
    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    public String createForeignKey(ForeignKeyDefinition foreignKeyDefinition) {
        String tableName = foreignKeyDefinition.getTableName();
        List columnNames = foreignKeyDefinition.getColumnNames();
        String referencesTableName = foreignKeyDefinition
                .getReferencesTableName();
        List referencesColumnNames = foreignKeyDefinition
                .getReferencesColumnNames();
        String foreignKeyName = foreignKeyDefinition.getForeignKeyName();

        StringBuffer sb = new StringBuffer();
        sb.append("ALTER TABLE ");
        sb.append(tableName);
        sb.append(" ADD CONSTRAINT ");
        sb.append(foreignKeyName);
        sb.append(" FOREIGN KEY (");
        Iterator it = columnNames.iterator();
        boolean firstIteration = true;
        while (it.hasNext()) {
            if (!firstIteration) {
                sb.append(",");
            }
            String columnName = (String) it.next();
            sb.append(columnName);
        }
        sb.append(") REFERENCES ");
        sb.append(referencesTableName);
        sb.append(" (");
        it = referencesColumnNames.iterator();
        firstIteration = true;
        while (it.hasNext()) {
            if (!firstIteration) {
                sb.append(",");
            }
            String columnName = (String) it.next();
            sb.append(columnName);
        }
        sb.append(");");

        return sb.toString();
    }

    public String getPhysicalDatatype(String logicalDatatype) {
        String result = logicalDatatype;
        if (logicalDatatype.equals("TEXT")) {
            result = "VARCHAR(4000)";
        }
        return result;
    }

    public String createIndex(IndexDefinition indexDefinition) {
        // TODO: Auto-generated method stub
        return null;
    }

    public String createTable(TableDefinition tableDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ");
        sb.append(tableDefinition.getName());
        sb.append(LINE_SEPARATOR);
        sb.append("();");
        return sb.toString();
    }
}

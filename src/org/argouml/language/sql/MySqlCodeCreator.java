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

import java.util.Iterator;
import java.util.List;

/**
 * Class for creating DDL statements for MySQL.
 * 
 * @author Kai
 */
public class MySqlCodeCreator implements SqlCodeCreator {
    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    private int primaryKeyCounter;

    public String createForeignKey(ForeignKeyDefinition foreignKeyDefinition) {
        String tableName = foreignKeyDefinition.getTableName();
        List columnNames = foreignKeyDefinition.getColumnNames();
        String referencesTableName = foreignKeyDefinition
                .getReferencesTableName();
        List referencesColumnNames = foreignKeyDefinition
                .getReferencesColumnNames();
        String foreignKeyName = foreignKeyDefinition.getForeignKeyName();

        StringBuffer sb = new StringBuffer();
        sb.append("ALTER TABLE ").append(tableName);
        sb.append(" ADD CONSTRAINT ").append(foreignKeyName);
        sb.append(" FOREIGN KEY ").append(foreignKeyName).append(" (");
        sb.append(Utils.stringsToCommaString(columnNames));
        sb.append(") REFERENCES ").append(referencesTableName).append(" (");
        sb.append(Utils.stringsToCommaString(referencesColumnNames));
        sb.append(");");

        return sb.toString();
    }

    public String createIndex(IndexDefinition indexDefinition) {
        // TODO: Auto-generated method stub
        return null;
    }

    public String createTable(TableDefinition tableDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ");
        sb.append(tableDefinition.getName());
        sb.append(" (").append(LINE_SEPARATOR);

        Iterator it = tableDefinition.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            ColumnDefinition colDef = (ColumnDefinition) it.next();
            sb.append(colDef.getName()).append(" ");
            sb.append(colDef.getDatatype());
            Boolean nullable = colDef.getNullable();
            if (nullable != null) {
                if (nullable.equals(Boolean.TRUE)) {
                    sb.append(" ").append("NULL");
                } else if (nullable.equals(Boolean.FALSE)) {
                    sb.append(" ").append("NOT NULL");
                }
            }
            sb.append(",").append(LINE_SEPARATOR);
        }

        StringBuffer sbPk = new StringBuffer();
        it = tableDefinition.getPrimaryKey().iterator();
        while (it.hasNext()) {
            String primaryKeyField = (String) it.next();
            if (sbPk.length() > 0) {
                sbPk.append(", ");
            }
            sbPk.append(primaryKeyField);
        }

        sb.append("PRIMARY KEY (");
        sb.append(sbPk);
        sb.append(")").append(LINE_SEPARATOR);

        sb.append(");");

        primaryKeyCounter++;

        return sb.toString();
    }

    public String getPhysicalDatatype(String logicalDatatype) {
        // TODO: Auto-generated method stub
        return null;
    }

}

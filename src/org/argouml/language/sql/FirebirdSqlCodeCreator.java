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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for creating DDL statements for Firebird.
 *
 * @author Kai
 */
public class FirebirdSqlCodeCreator implements SqlCodeCreator {
    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    private int primaryKeyCounter;

    /**
     * Construct a new code creator.
     *
     */
    public FirebirdSqlCodeCreator() {
        primaryKeyCounter = 1;
    }

    /**
     * 
     * @param fkDef
     * @return 
     */
    private String storedProcForInserting(ForeignKeyDefinition fkDef) {
        StringBuffer sb = new StringBuffer();
        String tableName = fkDef.getTableName();
        String refTableName = fkDef.getReferencesTableName();
        List columnNames = fkDef.getColumnNames();
        List referencesColumnNames = fkDef.getReferencesColumnNames();
        String storedProcName = "insert_" + refTableName;

        // sb.append(disallowInsert(tableName));
        // sb.append(disallowInsert(refTableName));

        sb.append("/* stored procedure for inserting a record in ");
        sb.append(tableName).append(" and ").append(refTableName).append(" */");
        sb.append(LINE_SEPARATOR);

        sb.append("SET TERM !! ;").append(LINE_SEPARATOR);
        sb.append("CREATE PROCEDURE ").append(storedProcName);
        sb.append(LINE_SEPARATOR);

        List params = new ArrayList();
        List columns = fkDef.getTable().getPrimaryKey();
        StringBuffer paramsSb = new StringBuffer();
        for (Iterator it = columns.iterator(); it.hasNext();) {
            if (paramsSb.length() > 0) {
                paramsSb.append(",");
            }
            String name = (String) it.next();
            ColumnDefinition colDef = fkDef.getTable()
                    .getColumnDefinition(name);
            String paramName = tableName + "_" + colDef.getName();
            String dataType = colDef.getDatatype();
            paramsSb.append(paramName).append(" ").append(dataType);
            params.add(":" + paramName);
        }

        List refParams = new ArrayList();
        List refColumns = fkDef.getReferencesTable().getPrimaryKey();
        for (Iterator it = refColumns.iterator(); it.hasNext();) {
            if (paramsSb.length() > 0) {
                paramsSb.append(",");
            }
            String name = (String) it.next();
            ColumnDefinition colDef = fkDef.getReferencesTable()
                    .getColumnDefinition(name);
            String paramName = refTableName + "_" + name;
            paramsSb.append(paramName).append(" ");
            paramsSb.append(colDef.getDatatype());
            refParams.add(":" + paramName);
        }
        sb.append("(").append(paramsSb).append(")");
        sb.append(LINE_SEPARATOR);

        sb.append("AS BEGIN ").append(LINE_SEPARATOR);

        sb.append("    INSERT INTO ").append(refTableName);
        sb.append(" (");
        sb.append(Utils.stringsToCommaString(referencesColumnNames));
        sb.append(") VALUES (");
        sb.append(Utils.stringsToCommaString(refParams));
        sb.append(");").append(LINE_SEPARATOR);

        List cols = new ArrayList();
        cols.addAll(fkDef.getTable().getPrimaryKey());
        cols.addAll(columnNames);

        sb.append("    INSERT INTO ").append(tableName).append(" (");
        sb.append(Utils.stringsToCommaString(cols));
        sb.append(") VALUES (");

        cols.clear();
        cols.addAll(params);
        cols.addAll(refParams);

        sb.append(Utils.stringsToCommaString(cols));
        sb.append(");").append(LINE_SEPARATOR);

        sb.append("END !!").append(LINE_SEPARATOR);
        sb.append("SET TERM ; !!");

        return sb.toString();
    }

    /**
     * Insert should be disallowed for the given table.
     * 
     * @param tableName
     * @return Generated DDL statement
     */
    private String disallowInsert(String tableName) {
        String exceptionName = "exception_" + tableName;

        StringBuffer sb = new StringBuffer();
        sb.append("/* disallow insert for ").append(tableName).append(" */");
        sb.append(LINE_SEPARATOR);

        sb.append("CREATE EXCEPTION ").append(exceptionName);
        sb.append(" 'INSERT not allowed.';");
        sb.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        sb.append("SET TERM !! ;").append(LINE_SEPARATOR);

        sb.append("CREATE TRIGGER ");
        sb.append(tableName).append("_before_insert");
        sb.append(" FOR ").append(tableName);
        sb.append(LINE_SEPARATOR);

        sb.append("BEFORE INSERT AS").append(LINE_SEPARATOR);
        sb.append("BEGIN").append(LINE_SEPARATOR);
        sb.append("    EXCEPTION ").append(exceptionName).append(";");
        sb.append(LINE_SEPARATOR);
        sb.append("END !!").append(LINE_SEPARATOR);

        sb.append("SET TERM ; !!").append(LINE_SEPARATOR);

        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    private String create1Nto11(ForeignKeyDefinition foreignKeyDefinition) {
        String tableName = foreignKeyDefinition.getTableName();
        String refTableName = foreignKeyDefinition.getReferencesTableName();
        List columnNames = foreignKeyDefinition.getColumnNames();
        List refColNames = foreignKeyDefinition.getReferencesColumnNames();
        List pkCols = foreignKeyDefinition.getTable().getPrimaryKey();

        StringBuffer sb = new StringBuffer();

        sb.append(storedProcForInserting(foreignKeyDefinition));

        sb.append("/* Check if there is at least one record remaining in ");
        sb.append(tableName).append("referencing ").append(refTableName);
        sb.append(" after deletion */").append(LINE_SEPARATOR);

        String exceptionName = tableName + "_exception";
        sb.append("CREATE EXCEPTION ").append(exceptionName);
        sb.append(" 'Delete of ").append(tableName).append(" violates ");
        sb.append(foreignKeyDefinition.getForeignKeyName()).append("';");
        sb.append(LINE_SEPARATOR);

        sb.append("SET TERM !! ;").append(LINE_SEPARATOR);

        sb.append("CREATE TRIGGER ");
        sb.append(tableName).append("_before_delete");
        sb.append(" FOR ").append(tableName).append(LINE_SEPARATOR);
        sb.append("BEFORE DELETE AS").append(LINE_SEPARATOR);
        sb.append("    DECLARE VARIABLE x INTEGER;").append(LINE_SEPARATOR);
        sb.append("BEGIN").append(LINE_SEPARATOR);
        sb.append("    SELECT COUNT(*) FROM ").append(tableName);
        sb.append(" WHERE ");

        StringBuffer whereSb = new StringBuffer();
        for (Iterator it = columnNames.iterator(); it.hasNext();) {
            if (whereSb.length() > 0) {
                whereSb.append(" AND ");
            }
            String colName = (String) it.next();
            whereSb.append(colName).append(" = OLD.").append(colName);
        }
        sb.append(whereSb).append(" AND (");

        whereSb = new StringBuffer();
        for (Iterator it = pkCols.iterator(); it.hasNext();) {
            if (whereSb.length() > 0) {
                whereSb.append(" OR ");
            }
            String pkColName = (String) it.next();
            whereSb.append(pkColName).append(" <> OLD.").append(pkColName);
        }

        sb.append(whereSb).append(") INTO :x;").append(LINE_SEPARATOR);
        sb.append("    IF (:x = 0) THEN EXCEPTION ");
        sb.append(exceptionName).append(";");
        sb.append(LINE_SEPARATOR);

        sb.append("END !!").append(LINE_SEPARATOR);
        sb.append("SET TERM ; !!").append(LINE_SEPARATOR);

        return sb.toString();
    }

    private String create1Nto01(ForeignKeyDefinition foreignKeyDefinition) {
        StringBuffer sb = new StringBuffer();
        String tableName = foreignKeyDefinition.getTableName();
        String referencesTableName = foreignKeyDefinition
                .getReferencesTableName();
        List columnNames = foreignKeyDefinition.getColumnNames();
        List referencesColumnNames = foreignKeyDefinition
                .getReferencesColumnNames();

        sb.append(storedProcForInserting(foreignKeyDefinition));

        return sb.toString();
    }

    /**
     * Generate DDL statements for the specified foreign key definition
     * 
     * @param foreignKeyDefinition
     *            The foreign key definition
     * @return The generated DDL statement
     * 
     * @see org.argouml.language.sql.SqlCodeCreator#createForeignKey(org.argouml.language.sql.ForeignKeyDefinition)
     */
    public String createForeignKey(ForeignKeyDefinition foreignKeyDefinition) {
        String tableName = foreignKeyDefinition.getTableName();
        List columnNames = foreignKeyDefinition.getColumnNames();
        String referencesTableName = foreignKeyDefinition
                .getReferencesTableName();
        List referencesColumnNames = foreignKeyDefinition
                .getReferencesColumnNames();
        String foreignKeyName = foreignKeyDefinition.getForeignKeyName();

        StringBuffer sb = new StringBuffer();
        sb.append("/* Foreign key ").append(foreignKeyName).append(" */");
        sb.append(LINE_SEPARATOR);
        sb.append("ALTER TABLE ").append(tableName);
        sb.append(" ADD CONSTRAINT ").append(foreignKeyName).append(
                LINE_SEPARATOR);
        sb.append("FOREIGN KEY (");
        sb.append(Utils.stringsToCommaString(columnNames));
        sb.append(") REFERENCES ");
        sb.append(referencesTableName).append(" (");
        sb.append(Utils.stringsToCommaString(referencesColumnNames));
        sb.append(")");

        int refLower = foreignKeyDefinition.getReferencesLower();

        if (refLower == 0) {
            sb.append(" ON DELETE SET NULL");
        } else {
            sb.append(" ON DELETE CASCADE");
        }

        sb.append(";").append(LINE_SEPARATOR);
        sb.append(LINE_SEPARATOR);

        // Since Firebird doesn't support deferred constraints (yet), code
        // for the X:(1,n)-relationships cannot be generated.
        // int upper = foreignKeyDefinition.getUpper();
        // int lower = foreignKeyDefinition.getLower();
        // if (lower == 1 && upper == -1) {
        // if (refLower == 1) {
        // sb.append(create1Nto11(foreignKeyDefinition));
        // } else {
        // sb.append(create1Nto01(foreignKeyDefinition));
        // }
        // }

        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    public String createTable(TableDefinition tableDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ");
        sb.append(tableDefinition.getName()).append(" (");
        sb.append(LINE_SEPARATOR);

        Iterator it = tableDefinition.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            ColumnDefinition colDef = (ColumnDefinition) it.next();
            sb.append("    ");
            sb.append(colDef.getName()).append(" ");
            sb.append(colDef.getDatatype());
            Boolean nullable = colDef.getNullable();
            if (nullable != null) {
                if (nullable.equals(Boolean.FALSE)) {
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

        sb.append("CONSTRAINT PK").append(primaryKeyCounter);
        sb.append(" PRIMARY KEY (").append(sbPk).append(")");
        sb.append(LINE_SEPARATOR);

        sb.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        primaryKeyCounter++;

        return sb.toString();
    }

    public String getName() {
        return "Firebird";
    }
}

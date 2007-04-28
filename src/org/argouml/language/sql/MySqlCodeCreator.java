package org.argouml.language.sql;


import java.util.Iterator;
import java.util.List;

import org.argouml.language.sql.ColumnDefinition;
import org.argouml.language.sql.ForeignKeyDefinition;
import org.argouml.language.sql.SqlCodeCreator;
import org.argouml.language.sql.TableDefinition;
import org.argouml.language.sql.Utils;

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

    public String getName() {
        return "MySQL";
    }
}

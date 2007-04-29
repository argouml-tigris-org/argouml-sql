package org.argouml.language.sql;

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
     * Generate DDL statements for the specified foreign key definition.
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
        // sb.append("/* Foreign key ").append(foreignKeyName).append(" */");
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

        return sb.toString();
    }

    /**
     * Generates DDL statements for creating a table according to the parameter
     * <code>tableDefinition</code>.
     * 
     * @param tableDefinition
     *            A <code>TableDefinition</code> object that holds alls
     *            necessary data for generating code.
     * @return The generated code.
     * @see org.argouml.language.sql.SqlCodeCreator#createTable(org.argouml.language.sql.TableDefinition)
     */
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

    /**
     * @return The name of this code creator.
     * @see org.argouml.language.sql.SqlCodeCreator#getName()
     */
    public String getName() {
        return "Firebird";
    }
}

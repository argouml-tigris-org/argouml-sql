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

public class ForeignKeyDefinition {
    private TableDefinition table;

    private List columns;

    private TableDefinition referencesTable;

    private List referencesColumns;

    private String foreignKeyName;

    private int lower;

    private int upper;

    private int referencesLower;

    private int referencesUpper;

    public ForeignKeyDefinition() {
        columns = new ArrayList();
        referencesColumns = new ArrayList();
    }
    
    public int getReferencesUpper() {
        return referencesUpper;
    }

    public void setReferencesUpper(int referencesUpper) {
        this.referencesUpper = referencesUpper;
    }

    public List getColumnNames() {
        List columnNames = new ArrayList();
        for (Iterator it = columns.iterator(); it.hasNext();) {
            ColumnDefinition cd = (ColumnDefinition) it.next();
            columnNames.add(cd.getName());
        }
        return columnNames;
    }

    // public void setColumnNames(List columnNames) {
    // this.columnNames = columnNames;
    // }
    public void addColumnDefinition(ColumnDefinition colDef) {
        columns.add(colDef);
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    public List getReferencesColumnNames() {
        List referencesColumnNames = new ArrayList();
        for (Iterator it = referencesColumns.iterator(); it.hasNext();) {
            ColumnDefinition cd = (ColumnDefinition) it.next();
            referencesColumnNames.add(cd.getName());
        }
        return referencesColumnNames;
    }

    // public void setReferencesColumnNames(List referencesColumnNames) {
    // this.referencesColumnNames = referencesColumnNames;
    // }
    public void addReferencesColumn(ColumnDefinition colDef) {
        referencesColumns.add(colDef);
    }

    public String getReferencesTableName() {
        return referencesTable.getName();
    }
//
//    public void setReferencesTableName(String referencesTableName) {
//        this.referencesTableName = referencesTableName;
//    }
//
    public String getTableName() {
        return table.getName();
    }
//
//    public void setTableName(String tableName) {
//        this.tableName = tableName;
//    }

    public int getLower() {
        return lower;
    }

    public void setLower(int lower) {
        this.lower = lower;
    }

    public int getReferencesLower() {
        return referencesLower;
    }

    public void setReferencesLower(int referencesLower) {
        this.referencesLower = referencesLower;
    }

    public int getUpper() {
        return upper;
    }

    public void setUpper(int upper) {
        this.upper = upper;
    }

    public List getColumns() {
        return columns;
    }
    
    public List getReferencesColumns() {
        return referencesColumns;
    }

    public TableDefinition getReferencesTable() {
        return referencesTable;
    }

    public void setReferencesTable(TableDefinition referencesTable) {
        this.referencesTable = referencesTable;
    }

    public TableDefinition getTable() {
        return table;
    }

    public void setTable(TableDefinition table) {
        this.table = table;
    }
}

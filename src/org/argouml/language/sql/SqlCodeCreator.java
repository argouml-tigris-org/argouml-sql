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
import java.util.List;

public interface SqlCodeCreator {
    /**
     * Method to generate a CREATE TABLE statement. The code for all columns
     * is also generated. 
     * 
     * @param tableName The name of the table.
     * @param columnDefinitions List of ColumnDefinitions for the table.
     * @return A string containing the SQL-Code.
     */
    String createTable(String tableName, List columnDefinitions);

    /**
     * Creates sql for a foreign key. The sql for foreign keys is generated
     * after the sql for all tables is generated.
     * 
     * @param tableName
     *            The name of the table containing the foreign key.
     * @param columnNames
     *            A list of strings containing the names of the columns the
     *            foreign key consists of.
     * @param referencesTableName
     *            The name of the table the foreign key references to.
     * @param referencesColumnNames
     *            A list of strings containing the names of the referenced
     *            columns.
     * @param foreignKeyName
     *            The name of the foreign key.
     * @return The created sql statement.
     */
    String createForeignKey(String tableName, List columnNames,
            String referencesTableName, List referencesColumnNames,
            String foreignKeyName);

    /**
     * Creates sql for an index definition. The sql for index definitions is
     * generated after the sql for all tables is generated.
     * 
     * @param indexName
     *            The name of the index.
     * @param tableName
     *            The table of the index.
     * @param columnNames
     *            A list of strings containing the names of the columns the
     *            index consists of.
     * @param descending
     *            Whether the index is descending.
     * @return The created sql statement.
     */
    String createIndex(String indexName, String tableName, List columnNames,
            boolean descending);
}

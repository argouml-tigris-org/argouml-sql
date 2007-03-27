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

/**
 * Interface providing methods for creating SQL statements.
 * 
 * @author Kai Drahmann
 */
public interface SqlCodeCreator {
    /**
     * Method to generate a CREATE TABLE statement. The code for all columns is
     * also generated.
     * 
     * @param tableDefinition
     *            The definition of the table.
     * @return A string containing the SQL-Code.
     */
    String createTable(TableDefinition tableDefinition);

    /**
     * Creates sql for a foreign key. The sql for foreign keys is generated
     * after the sql for all tables is generated.
     * 
     * @param foreignKeyDefinition
     *            The definition of the foreign key.
     * @return The created sql statement.
     */
    String createForeignKey(ForeignKeyDefinition foreignKeyDefinition);

    /**
     * Creates sql for an index definition. The sql for index definitions is
     * generated after the sql for all tables is generated.
     * 
     * @param indexDefinition
     *            The definition of the index.
     * @return The created sql statement.
     * @deprecated
     */
    String createIndex(IndexDefinition indexDefinition);

    /**
     * Some datatypes cannot be named equal for all existing database systems.
     * An example is the treating of long text columns. This method returns the
     * appropriate database-typename for this cases. If the type used in the
     * model already is correct it should be returned.
     * 
     * @param logicalDatatype
     *            The typename used in the model.
     * @return The typename to use in the SQL statement.
     * @deprecated
     */    
    String getPhysicalDatatype(String logicalDatatype);
}

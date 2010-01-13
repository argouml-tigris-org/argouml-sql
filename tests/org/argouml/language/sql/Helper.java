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

import java.util.Collection;
import java.util.Iterator;

import org.argouml.model.Model;

/**
 * Helper class for unit tests. Defines methods for simplifying building
 * relational models.
 * 
 * @author drahmann
 */
final class Helper {
    private Object defaultType;

    private Object defaultNamespace;

    /**
     * Set the default namespace.
     * 
     * @param defaultNamespace
     */
    public void setDefaultNamespace(Object defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    /**
     * Get the default datatype for creating attributes.
     * 
     * @return The default datatype
     */
    public Object getDefaultType() {
        return defaultType;
    }

    /**
     * Set the default datatype for creating attributes.
     * 
     * @param defaultType
     */
    public void setDefaultType(Object defaultType) {
        this.defaultType = defaultType;
    }

    /**
     * 
     * @return The default namespace
     */
    public Object getDefaultNamespace() {
        return defaultNamespace;
    }

    /**
     * Adds a foreign key attribute with the given name to the given relation.
     * The attribute is created in the default namespace.
     * 
     * @param relation
     *            The relation.
     * @param attrName
     *            The name.
     * @return The added foreign key attribute.
     */
    public Object addForeignKeyAttribute(Object relation, String attrName) {
        return addForeignKeyAttribute(defaultNamespace, relation, attrName);
    }

    /**
     * Adds a foreign key attribute with the given name to the given relation.
     * The attribute is created in the given namespace.
     * 
     * @param namespace
     * @param relation
     * @param attrName
     * @return The added foreign key attribute.
     */
    public Object addForeignKeyAttribute(Object namespace, Object relation,
            String attrName) {
        Object fkAttribute = Model.getCoreFactory().buildAttribute2(relation,
                defaultType);
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(fkAttribute, "FK", namespace);
        Model.getCoreHelper().addStereotype(fkAttribute, stereotype);
        Model.getCoreHelper().setName(fkAttribute, attrName);
        return fkAttribute;
    }

    /**
     * Adds a primary key attribute with the given name to the given relation.
     * The attribute is created in the default namespace.
     * 
     * @param relation
     * @param attrName
     * @return The added primary key attribute.
     */
    public Object addPrimaryKeyAttribute(Object relation, String attrName) {
        return addPrimaryKeyAttribute(defaultNamespace, relation, attrName);
    }

    /**
     * Adds a primary key attribute with the given name to the given relation.
     * The attribute is created in the given namespace.
     * 
     * @param relation
     * @param attrName
     * @param namespace
     * @return The added primary key attribute.
     */
    public Object addPrimaryKeyAttribute(Object namespace, Object relation,
            String attrName) {
        Object pkAttribute = Model.getCoreFactory().buildAttribute2(relation,
                defaultType);
        Object stereotype = Model.getExtensionMechanismsFactory()
                .buildStereotype(pkAttribute, "PK", namespace);
        Model.getCoreHelper().addStereotype(pkAttribute, stereotype);
        Model.getCoreHelper().setName(pkAttribute, attrName);
        return pkAttribute;
    }

    /**
     * Adds an association between the given relations. The multiplicities and
     * the name is set to the given arguments.
     * 
     * @param relation1
     * @param lower1
     * @param upper1
     * @param relation2
     * @param lower2
     * @param upper2
     * @param name
     * @return The association.
     */
    public Object buildAssociation(Object relation1, int lower1, int upper1,
            Object relation2, int lower2, int upper2, String name) {
        Object association = Model.getCoreFactory().buildAssociation(relation1,
                false, relation2, false, name);

        Collection conns = Model.getFacade().getConnections(association);

        Iterator it = conns.iterator();
        Object relation1End = it.next();
        Object relation2End = it.next();
        Object mult1 = Model.getDataTypesFactory().createMultiplicity(lower1,
                upper1);
        Object mult2 = Model.getDataTypesFactory().createMultiplicity(lower2,
                upper2);

        Model.getCoreHelper().setMultiplicity(relation1End, mult1);
        Model.getCoreHelper().setMultiplicity(relation2End, mult2);

        return association;
    }

    /**
     * Build a new relation with the given name and a primary key attribute with
     * the name "id".
     * 
     * @param name
     * @return The relation.
     */
    public Object buildRelation(String name) {
        return buildRelation(defaultNamespace, name, "id");
    }

    /**
     * Build a new relation with the given name and a primary key attribute with
     * the given name. The relation is created in the default namespace.
     * 
     * @param name
     * @param pkAttrName
     * @return The relation.
     */
    public Object buildRelation(String name, String pkAttrName) {
        return buildRelation(defaultNamespace, name, pkAttrName);
    }

    /**
     * Build a new relation with the given name and a primary key attribute with
     * the given name. The relation is created in the given namespace.
     * 
     * @param namespace
     * @param name
     * @param pkAttrName
     * @return The relation
     */
    public Object buildRelation(Object namespace, String name, String pkAttrName) {
        Object relation = Model.getCoreFactory().buildClass(name, namespace);
        addPrimaryKeyAttribute(namespace, relation, pkAttrName);
        return relation;
    }

    /**
     * Sets the association name for the given foreign key attribute to the
     * given name.
     * 
     * @param fkAttribute
     * @param assocName
     */
    public void setFkAttributeAssocName(Object fkAttribute, String assocName) {
        if (assocName != null && assocName.length() > 0) {
            Object taggedValue = Model.getExtensionMechanismsFactory()
                    .buildTaggedValue(
                            GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE,
                            assocName);
            Model.getExtensionMechanismsHelper().addTaggedValue(fkAttribute,
                    taggedValue);
        }
    }

    /**
     * Sets the source column for the given foreign key attribute to the given
     * name.
     * 
     * @param fkAttribute
     * @param srcColName
     */
    public void setFkAttributeSrcCol(Object fkAttribute, String srcColName) {
        if (srcColName != null && srcColName.length() > 0) {
            Object taggedValue = Model.getExtensionMechanismsFactory()
                    .buildTaggedValue(GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE,
                            srcColName);
            Model.getExtensionMechanismsHelper().addTaggedValue(fkAttribute,
                    taggedValue);
        }
    }
}

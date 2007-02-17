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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.argouml.model.Model;

class ModelValidator {
    private List problems;

    public ModelValidator() {
    }

    public Collection validate(Collection elements) {
        problems = new ArrayList();

        Iterator it = elements.iterator();
        while (it.hasNext()) {
            Object entity = it.next();
            if (Model.getFacade().isAClass(entity)
                    && !Model.getFacade().isAAssociationClass(entity)) {
                validateEntity(entity);
            }
        }
        
        return problems;
    }

    private void validateEntity(Object entity) {
        validatePrimaryKey(entity);

        validateAssociations(entity);

        Collection attributes = Model.getFacade().getAttributes(entity);
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attribute = it.next();
            validateFkAttribute(entity, attribute);
        }

    }

    /**
     * Checks if every foreign key attribute is referencing an association.
     * 
     * @param entity
     * @param attribute
     */
    private void validateFkAttribute(Object entity, Object attribute) {
        String assocName = Model.getFacade().getTaggedValueValue(attribute,
                GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

        Object association = Utils.getAssociationForName(entity, assocName); 
        if (association == null) {
            problems.add("more than one association named " + assocName + 
                    " for entity " + Model.getFacade().getName(entity));
        }
        
         
    }

    /**
     * Checks if every entity has a primary key.
     * 
     * @param entity
     *            The entity to check.
     */
    private void validatePrimaryKey(Object entity) {
        List attributes = Model.getFacade().getAttributes(entity);
        Iterator it = attributes.iterator();
        while (it.hasNext()) {
            Object attribute = it.next();
            if (Model.getFacade().isStereotype(attribute,
                    GeneratorSql.PRIMARY_KEY_STEREOTYPE)) {
                return;
            }
        }
        problems.add("Kein Primärschlüssel für "
                + Model.getFacade().getName(entity) + " definiert");
    }

    /**
     * check rules B...
     * 
     * @param entity
     */
    private void validateAssociations(Object entity) {
        Collection associationEnds = Model.getFacade().getAssociationEnds(
                entity);
        Iterator it = associationEnds.iterator();
        while (it.hasNext()) {
            Object entityAssocEnd = it.next();
            validateAssociation(entity, entityAssocEnd);
        }
    }

    /**
     * Validate a many to many association between two classes.
     * 
     * @param association
     */
    private void validateManyToMany(Object association) {
        if (Model.getFacade().isAAssociationClass(association)) {
            Collection attributes = Model.getFacade()
                    .getAttributes(association);
            Iterator it = attributes.iterator();
            while (it.hasNext()) {
                Object attribute = it.next();
                // TODO implement many-to-many-validation
            }
        }
    }

    private void validateFkConsistence(Object entity, Object entityAssocEnd,
            Object otherAssocEnd) {
        Object association = Model.getFacade().getAssociation(entityAssocEnd);
        Object otherEntity = Model.getFacade().getClassifier(otherAssocEnd);
        Object fkAttribute = Utils.getFkAttribute(otherEntity, association);

        int entityLower = Model.getFacade().getLower(entityAssocEnd);

        if (!isFkAttributeConsistent(fkAttribute, entity, entityLower)) {
            problems.add("Konflikt zwischen Mult. und Erforderl. FK "
                    + Model.getFacade().getName(fkAttribute));
        }
    }

    /**
     * <p>
     * Checks if the foreign key attribute <code>fkAttribute</code> references
     * a column in <code>entity</code>. <code>fkAttribute</code> references
     * <code>entity</code> if all of the following conditions evaluate to
     * <code>true</code>:
     * <ol>
     * <li>fkAttribute does not contain a tagged value "table" or the
     * fkAttribute has exactly one tagged value "table" for which the data value
     * equals entity.name
     * <li>entity contains an attribute which name equals fkAttribute.name or
     * the fkAttribute has exactly one tagged value for which the data value
     * "sourceColumn" equals the name of an attribute of entity
     * </ol>
     * 
     * @param fkAttribute
     *            The foreign key attribute
     * @param entity
     *            The entity
     * @return <code>true</code> if <code>foreignKey</code> references a
     *         column in <code>entity</code>
     */
    private boolean isFkAttributeReferencingEntity(final Object fkAttribute,
            final Object entity) {
        String sourceColumn = Model.getFacade().getTaggedValueValue(
                fkAttribute, GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE);
        String associationName = Model.getFacade().getTaggedValueValue(
                fkAttribute, GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

        boolean tableReferencing = false;

        boolean columnReferencing = false;
        Collection entityAttributes = Model.getFacade().getAttributes(entity);
        Iterator it = entityAttributes.iterator();
        while (it.hasNext()) {
            Object attribute = it.next();
            if (sourceColumn.equals(Model.getFacade().getName(attribute))) {
                columnReferencing = true;
                break;
            }
        }

        return tableReferencing && columnReferencing;
    }

    /**
     * <p>
     * Checks if the <code>foreignKey</code> is of a stereotype NULL/NOT NULL
     * and if it conflicts with the multiplicity of the association end. A
     * conflict results from one of these constellations:
     * <ol>
     * <li>attribute is of stereotype NOT NULL, the corresponding association
     * end multiplicity is 0..1
     * <li>attribute is of stereotype NULL, the corresponding association end
     * multiplicity is 1
     * </ol>
     * <p>
     * If attribute is none of these two stereotypes there is no conflict.
     * <p>
     * Checks rules B9.1 and B9.2.
     * 
     * @param attribute
     *            The foreign key attribute to check
     * @param entity
     *            The entity the foreign key should refer to
     * @param lowerBound
     *            The lower multiplicity of the corresponding association end
     * @return <code>true</code> if there is no conflict
     */
    private boolean isFkAttributeConsistent(final Object attribute,
            final Object entity, final int lowerBound) {
        boolean consistent = true;

        boolean isFk = Utils.isFk(attribute);
        if (isFk) {
            boolean isReferencingEntity = isFkAttributeReferencingEntity(
                    attribute, entity);
            if (isReferencingEntity) {
                if (lowerBound == 0
                        && Model.getFacade().isStereotype(attribute,
                                GeneratorSql.NOT_NULL_STEREOTYPE)) {
                    consistent = false;
                } else if (lowerBound == 1
                        && Model.getFacade().isStereotype(attribute,
                                GeneratorSql.NULL_STEREOTYPE)) {
                    consistent = false;
                }
            }
        }

        return consistent;
    }

    private void validateOneToMany(Object entity, Object entityAssocEnd,
            Object otherAssocEnd) {
        Object association = Model.getFacade().getAssociation(entityAssocEnd);
        Object otherEntity = Model.getFacade().getClassifier(otherAssocEnd);
        Object fkAttribute = Utils.getFkAttribute(otherEntity, association);

        int entityLower = Model.getFacade().getLower(entityAssocEnd);

        if (!isFkAttributeConsistent(fkAttribute, entity, entityLower)) {
            problems.add("Konflikt zwischen Mult. und Erforderl. FK "
                    + Model.getFacade().getName(fkAttribute));
        }
    }

    private void validateOneToOne(Object entity, Object entityAssocEnd,
            Object otherAssocEnd) {
        int entityLower = Model.getFacade().getLower(entityAssocEnd);
        int otherLower = Model.getFacade().getLower(otherAssocEnd);

        if (entityLower == 1 && otherLower == 1) {

        } else {
            validateFkConsistence(entity, entityAssocEnd, otherAssocEnd);
        }
    }

    private void validateAssociation(Object entity, Object entityAssocEnd) {
        Collection otherAssocEnds = Model.getFacade().getOtherAssociationEnds(
                entityAssocEnd);
        if (otherAssocEnds.size() == 1) {
            Object otherAssocEnd = otherAssocEnds.iterator().next();
            int entityUpper = Model.getFacade().getUpper(entityAssocEnd);
            int otherUpper = Model.getFacade().getUpper(otherAssocEnd);

            Object association = Model.getFacade().getAssociation(
                    entityAssocEnd);

            if (entityUpper > 1 && otherUpper > 1) {
                validateManyToMany(association);
            } else if (entityUpper == 1 && otherUpper > 1) {
                validateOneToMany(entity, entityAssocEnd, otherAssocEnd);
            } else if (entityUpper == 1 && otherUpper == 1) {
                validateOneToOne(entity, entityAssocEnd, otherAssocEnd);
            }
        } else {
            problems.add("Mehrseitige Beziehung noch nicht unterstützt!");
        }
    }
}

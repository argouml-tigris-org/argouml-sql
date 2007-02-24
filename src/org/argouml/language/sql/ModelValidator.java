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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.argouml.model.Model;

class ModelValidator {
    private Map associationForName = new HashMap();

    private Map fkAttrForAssoc = new HashMap();

    private List problems;

    public ModelValidator() {
    }

    /**
     * Validate the specified elements.
     * 
     * @param elements
     * @return
     */
    public Collection validate(Collection elements) {
        problems = new ArrayList();

        for (Iterator it = elements.iterator(); it.hasNext();) {
            Object entity = it.next();
            if (Model.getFacade().isAClass(entity)
                    && !Model.getFacade().isAAssociationClass(entity)) {
                validateEntity(entity);
            }
        }

        Set entries = associationForName.entrySet();
        for (Iterator it = entries.iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            String assocName = (String) entry.getKey();
            Object association = entry.getValue();
            Object fkAttribute = fkAttrForAssoc.get(association);
            if (fkAttribute == null) {
                problems.add("Foreign key attribute missing for association "
                        + assocName);
            }
        }

        return problems;
    }

    private void validateEntity(Object entity) {
        validatePrimaryKey(entity);

        Collection attributes = Model.getFacade().getAttributes(entity);
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attribute = it.next();
            if (Utils.isFk(attribute)) {
                validateFkAttribute(entity, attribute);
            }
        }

        validateAssociations(entity);
    }

    /**
     * Checks if every entity has a primary key. (rule 1)
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
     * Checks if a foreign key attribute is referencing an association. Further
     * checks if this foreign key attribute is referencing an attribute in
     * another entity. Checks rules 2 to 6.
     * 
     * @param entity
     * @param attribute
     */
    private void validateFkAttribute(Object entity, Object attribute) {
        String entName = Model.getFacade().getName(entity);
        String attrName = Model.getFacade().getName(attribute);
        String assocName = Model.getFacade().getTaggedValueValue(attribute,
                GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

        Object association = Utils.getAssociationForName(entity, assocName);
        if (association == null) {
            problems.add("association named " + assocName + " for entity "
                    + Model.getFacade().getName(entity) + " not found");
        } else {
            fkAttrForAssoc.put(association, attribute);

            Object entityAssocEnd = Model.getFacade().getAssociationEnd(entity,
                    association);
            Collection otherAssocEnds = Model.getFacade()
                    .getOtherAssociationEnds(entityAssocEnd);

            if (otherAssocEnds.size() == 1) {
                Object otherAssocEnd = otherAssocEnds.iterator().next();
                Object otherEntity = Model.getFacade().getClassifier(
                        otherAssocEnd);

                Object srcAttr = getSourceAttribute(attribute, otherEntity);
                if (srcAttr == null) {
                    problems.add("fk attribute " + entName + "." + attrName
                            + " does not reference " + " an attribute in "
                            + Model.getFacade().getName(otherEntity));
                }

                int otherUpper = Model.getFacade().getUpper(otherAssocEnd);
                if (otherUpper != 1) {
                    problems.add("foreign key attribute " + entName + "."
                            + attrName
                            + " cannot be used to reference multiple "
                            + Model.getFacade().getName(otherEntity));
                }

                int otherLower = Model.getFacade().getLower(otherAssocEnd);
                validateFkConsistence(entity, attribute, otherLower);
            }
        }
    }

    /**
     * Get the attribute a foreign key attribute is referencing to.
     * 
     * @param fkAttribute
     *            The foreign key attribute.
     * @param srcEntity
     *            The entity the foreign key is referencing to.
     * @return The referenced attribute.
     */
    private Object getSourceAttribute(Object fkAttribute, Object srcEntity) {
        String srcColName = Model.getFacade().getTaggedValueValue(fkAttribute,
                GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE);
        if (srcColName.equals("")) {
            srcColName = Model.getFacade().getName(fkAttribute);
        }
        Object srcAttr = Utils.getAttributeForName(srcEntity, srcColName);
        if (srcAttr == null) {
            Collection pkAttrs = Utils.getPrimaryKeyAttributes(srcEntity);
            if (pkAttrs.size() == 1) {
                srcAttr = pkAttrs.iterator().next();
            }
        }
        return srcAttr;
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
     * Checks rules 5 and 6.
     * 
     * @param fkAttribute
     *            The foreign key attribute to check
     * @param entity
     *            The entity the foreign key should refer to
     * @param lowerBound
     *            The lower multiplicity of the corresponding association end
     */
    private void validateFkConsistence(final Object entity,
            final Object fkAttribute, final int lowerBound) {
        String entName = Model.getFacade().getName(entity);
        String attrName = Model.getFacade().getName(fkAttribute);

        if (Model.getFacade().isStereotype(fkAttribute, "NULL")
                && lowerBound == 1) {
            problems.add("conflict in " + entName + "." + attrName + ": "
                    + "attribute is nullable and association lower bound "
                    + "is one");
        } else if (Model.getFacade().isStereotype(fkAttribute, "NOT NULL")
                && lowerBound == 0) {
            problems.add("conflict in " + entName + "." + attrName + ": "
                    + "attribute is not nullable and association lower "
                    + "bound is zero");
        }
    }

    /**
     * Validate every association for entity.
     * 
     * @param entity
     */
    private void validateAssociations(Object entity) {
        Collection associationEnds = Model.getFacade().getAssociationEnds(
                entity);
        Iterator it = associationEnds.iterator();
        while (it.hasNext()) {
            Object entityAssocEnd = it.next();
            Object association = Model.getFacade().getAssociation(
                    entityAssocEnd);
            validateAssociation(association);
        }
    }

    private Set validatedAssociations = new HashSet();

    /**
     * Validate the specified association. The association needs to have a
     * unique name, must be binary and at most 1:n. And there must exist a
     * foreign key attribute for an association.
     * 
     * @param association
     */
    private void validateAssociation(Object association) {
        if (validatedAssociations.contains(association)) {
            return;
        }
        
        validatedAssociations.add(association);

        String assocName = Model.getFacade().getName(association);
        if (associationForName.containsKey(assocName)) {
            problems.add("Association name " + assocName
                    + " found more than once");
        } else {
            associationForName.put(assocName, association);
        }

        Collection assocEnds = Model.getFacade().getConnections(association);
        if (assocEnds.size() != 2) {
            problems.add("Association " + assocName + " is not binary");
        } else {
            Iterator it = assocEnds.iterator();

            Object assocEnd1 = it.next();
            Object assocEnd2 = it.next();

            int end1Upper = Model.getFacade().getUpper(assocEnd1);
            int end2Upper = Model.getFacade().getUpper(assocEnd2);

            if (end1Upper != 1 && end2Upper != 1) {
                problems.add("Association " + assocName + " is n:m (not "
                        + "allowed in a relational data model)");
            }
        }
    }
}

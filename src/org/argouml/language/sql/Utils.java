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

import org.argouml.model.Facade;
import org.argouml.model.Model;

final class Utils {
    /**
     * Return an association named <code>assocName</code> that is connected to
     * relation.
     * 
     * @param relation
     *            The relation where to search the association.
     * @param assocName
     *            The name of the association to search.
     * @return The association if found, <code>null</code> else.
     */
    public static Object getAssociationForName(Object relation, String assocName) {
        Object association = null;

        Collection assocEnds = Model.getFacade().getAssociationEnds(relation);
        for (Iterator it = assocEnds.iterator(); it.hasNext();) {
            Object assocEnd = it.next();
            Object assoc = Model.getFacade().getAssociation(assocEnd);
            String name = Model.getFacade().getName(assoc);
            if (name.equals(assocName)) {
                association = assoc;
            }
        }

        return association;
    }

    /**
     * Search the attribute named <code>attributeName</code> in the given
     * relation. If there exist more than one attribute the first one is
     * returned.
     * 
     * @param relation
     *            The relation in which to search the attribute.
     * @param attributeName
     *            The name of the attribute to search.
     * @return The attribute if found, <code>null</code> else.
     */
    public static Object getAttributeForName(Object relation,
            String attributeName) {
        Object attribute = null;

        Collection attributes = Model.getFacade().getAttributes(relation);
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attr = it.next();
            if (Model.getFacade().getName(attr).equals(attributeName)) {
                attribute = attr;
                break;
            }
        }

        return attribute;
    }

    /**
     * Build a list of all foreign key attributes that refer a specific
     * association.
     * 
     * @param relation
     *            The relation which contains the fk-attributes.
     * @param association
     *            The association for which to return the fk-attributes.
     * @return A list of all attributes. If there is no attribute, an empty list
     *         is returned.
     */
    public static List getFkAttribute(Object relation, Object association) {
        String assocName = Model.getFacade().getName(association);

        Collection attributes = Model.getFacade().getAttributes(relation);
        Iterator it = attributes.iterator();
        List fkAttributes = new ArrayList();
        while (it.hasNext()) {
            Object attribute = it.next();
            String s = Model.getFacade().getTaggedValueValue(attribute,
                    GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

            if (s.equals(assocName)) {
                fkAttributes.add(attribute);
            }
        }

        return fkAttributes;
    }

    /**
     * Build a list of all primary key attributes of entity.
     * 
     * @param relation
     *            The relation for which to return the pk-attributes.
     * @return A list of all primary key attributes. If there is no
     *         pk-attribute, the list is empty.
     */
    public static List getPrimaryKeyAttributes(Object relation) {
        List result = new ArrayList();

        Collection attributes = Model.getFacade().getAttributes(relation);

        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attribute = it.next();
            if (isPk(attribute)) {
                result.add(attribute);
            }
        }

        return result;
    }

    /**
     * Returns if an attribute is a foreign key attribute. Effectively checks if
     * the attribute is of stereotype
     * {@link GeneratorSql#FOREIGN_KEY_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is a fk-attribute, <code>false</code>
     *         else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isFk(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.FOREIGN_KEY_STEREOTYPE);
    }

    /**
     * Returns if an attribute is a primary key attribute. Effectively checks if
     * the attribute is of stereotype
     * {@link GeneratorSql#PRIMARY_KEY_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is a pk-attribute, <code>false</code>
     *         else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isPk(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.PRIMARY_KEY_STEREOTYPE);
    }

    /**
     * Returns if an attribute is not nullable. Effectively checks if the
     * attribute is of stereotype {@link GeneratorSql#NOT_NULL_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is not nullable, <code>false</code>
     *         else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isNotNull(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.NOT_NULL_STEREOTYPE);
    }

    /**
     * Returns if an attribute is nullable. Effectively checks if the attribute
     * is of stereotype {@link GeneratorSql#NULL_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is nullable, <code>false</code> else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isNull(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.NULL_STEREOTYPE);
    }

    /**
     * Get the attribute a foreign key attribute is referencing to. Returns
     * <code>null</code> if the source attribute cannot be determined.
     * 
     * @param fkAttribute
     *            The foreign key attribute.
     * @param srcRelation
     *            The entity the foreign key is referencing to.
     * @return The referenced attribute.
     */
    public static Object getSourceAttribute(Object fkAttribute,
            Object srcRelation) {
        String srcColName = Model.getFacade().getTaggedValueValue(fkAttribute,
                GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE);
        Object srcAttr = null;
        if (srcColName.equals("")) {
            srcColName = Model.getFacade().getName(fkAttribute);
            srcAttr = Utils.getAttributeForName(srcRelation, srcColName);
            if (srcAttr == null) {
                Collection pkAttrs = Utils.getPrimaryKeyAttributes(srcRelation);
                if (pkAttrs.size() == 1) {
                    srcAttr = pkAttrs.iterator().next();
                }
            }
        } else {
            srcAttr = Utils.getAttributeForName(srcRelation, srcColName);
        }
        return srcAttr;
    }

    public static String stringsToString(List strings, String separators) {
        StringBuffer sb = new StringBuffer();
        Iterator it = strings.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        return sb.toString();
    }
    
    public static String stringsToCommaString(List strings) {
        return stringsToString(strings, ",");
    }
    
    /**
     * Private constructor so no instance can be created.
     */
    private Utils() {

    }
}

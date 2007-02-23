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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.argouml.model.Model;

final class Utils {
    public static Object getAssociationForName(Object entity, String assocName) {
        Object association = null;

        Collection assocEnds = Model.getFacade().getAssociationEnds(entity);
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

    public static Object getAttributeForName(Object entity, String attributeName) {
        Object attribute = null;

        Collection attributes = Model.getFacade().getAttributes(entity);
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attr = it.next();
            if (Model.getFacade().getName(attr).equals(attributeName)) {
                attribute = attr;
                break;
            }
        }

        return attribute;
    }

    public static Object getFkAttribute(Object entity, Object association) {
        String assocName = Model.getFacade().getName(association);

        Collection attributes = Model.getFacade().getAttributes(entity);
        Iterator it = attributes.iterator();
        Object fkAttribute = null;
        while (it.hasNext()) {
            Object attribute = it.next();
            String s = Model.getFacade().getTaggedValueValue(attribute,
                    GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

            if (s.equals(assocName)) {
                fkAttribute = attribute;
            }
        }

        return fkAttribute;
    }

    /**
     * Build a collection of all primary key attributes of entity.
     * 
     * @param entity
     * @return
     */
    public static Collection getPrimaryKeyAttributes(Object entity) {
        Collection result = new HashSet();

        Collection attributes = Model.getFacade().getAttributes(entity);

        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attribute = it.next();
            if (isPk(attribute)) {
                result.add(attribute);
            }
        }

        return result;
    }

    public static boolean isFk(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.FOREIGN_KEY_STEREOTYPE);
    }

    public static boolean isPk(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.PRIMARY_KEY_STEREOTYPE);
    }

    private Utils() {

    }
}

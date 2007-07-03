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

import junit.framework.TestCase;

import org.argouml.model.InitializeModel;
import org.argouml.model.Model;

/**
 * Base test case class for SQL module. Builds an empty model before every test
 * and provides an object with methods for easy creation of some model elements.
 * 
 * @author Anne und Kai
 */
public abstract class BaseTestCaseSql extends TestCase {
    /**
     * The namespace in which all model elements should be created. 
     */
    protected Object namespace;

    /**
     * The default data type.
     */
    protected Object intType;

    /**
     * Instance of test helper class.
     */
    protected Helper helper;

    /**
     * Create an empty model.
     * 
     * @throws Exception If something works wrong.
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        InitializeModel.initializeDefault();
        
        Object mmodel = Model.getModelManagementFactory().createModel();
        Model.getCoreHelper().setName(mmodel, "untitledModel");
        Model.getModelManagementFactory().setRootModel(mmodel);
        namespace = Model.getModelManagementFactory().createPackage();
        intType = Model.getCoreFactory().buildDataType("int", namespace);
        Model.getCoreHelper().setName(namespace, "untitledNamespace");

        helper = new Helper();
        helper.setDefaultType(intType);
        helper.setDefaultNamespace(namespace);
    }
}

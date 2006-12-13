// $Id$
// Copyright (c) 2006 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
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

import org.argouml.moduleloader.ModuleInterface;
import org.argouml.uml.generator.CodeGenerator;
import org.argouml.uml.generator.GeneratorHelper;
import org.argouml.uml.generator.GeneratorManager;
import org.argouml.uml.generator.Language;
import org.argouml.application.helpers.ResourceLoaderWrapper;


/**
 * SQL generator
 */
public class GeneratorSql implements CodeGenerator, ModuleInterface {

    /**
     * The language that we are implementing.
     */
    private static final String LANGUAGE_NAME = "SQL";

    /**
     * The prepared struct for registering.
     */
    private static final Language MY_LANG =
	GeneratorHelper.makeLanguage(LANGUAGE_NAME,
                ResourceLoaderWrapper.lookupIconResource(
                        LANGUAGE_NAME + "Notation"));




    /**
     * Generate code for the specified classifiers. If generation of
     * dependencies is requested, then every file the specified elements
     * depends on is generated too (e.g. if the class MyClass has an attribute
     * of type OtherClass, then files for OtherClass are generated too).
     *
     * @param elements the UML model elements to generate code for.
     * @param deps Recursively generate dependency files too.
     * @return A collection of {@link org.argouml.uml.generator.SourceUnit}
     *         objects. The collection may be empty if no file is generated.
     * @see org.argouml.uml.generator.CodeGenerator#generate(
     *         Collection, boolean)
     */
    public Collection generate(Collection elements, boolean deps) {
	
	throw new Error("Not yet implemented");
    }

    /**
     * Generate files for the specified classifiers.
     * @see #generate(Collection, boolean)
     * @param elements the UML model elements to generate code for.
     * @param path The source base path.
     * @param deps Recursively generate dependency files too.
     * @return The filenames (with relative path) as a collection of Strings.
     * The collection may be empty if no file will be generated.
     * @see org.argouml.uml.generator.CodeGenerator#generateFiles(
     *         Collection, String, boolean)
     */
    public Collection generateFiles(
	    Collection elements,
	    String path,
	    boolean deps) {
	Collection units = generate(elements, deps);
	throw new Error("Not yet implemented");
    }

    /**
     * Returns a list of files that will be generated from the specified
     * modelelements.
     * @see #generate(Collection, boolean)
     * @param elements the UML model elements to generate code for.
     * @param deps Recursively generate dependency files too.
     * @return The filenames (with relative path) as a collection of Strings.
     * The collection may be empty if no file will be generated.
     * @see org.argouml.uml.generator.CodeGenerator#generateFileList(
     *         Collection, boolean)
     */
    public Collection generateFileList(Collection elements, boolean deps) {
	throw new Error("Not yet implemented");
    }


    /**
     * Method to enable the module.<p>
     *
     * If it cannot enable the module because some other module is
     * not enabled it can return <code>false</code>.
     * In that case the module loader will defer this attempt until
     * all other modules are loaded (or until some more of ArgoUML is loaded
     * if at startup). Eventually it is only this and some other modules
     * that is not loaded and they will then be listed as having problems.
     *
     * @return true if all went well
     */
    public boolean enable() {
        GeneratorManager.getInstance().addGenerator(MY_LANG, this);
        return true;
    }

    /**
     * Method to disable the module.<p>
     *
     * If we cannot disable the module because some other module relies
     * on it, we return false. This will then make it impossible to turn off.
     * (An error is signalled at the attempt).
     *
     * @return true if all went well.
     */
    public boolean disable() {
        GeneratorManager.getInstance().removeGenerator(MY_LANG);
        return true;
    }

    /**
     * The name of the module.<p>
     *
     * This should be a short string. For the purpose of having the GUI
     * that turns on and off the module look nice there is no whitespace in
     * this string (no spaces, tabs or newlines).<p>
     *
     * This name is also used as the key internally when modules checks for
     * other modules, if they are available.
     *
     * @return the name (A String).
     */
    public String getName() {
	return "GeneratorSql";
    }

    /**
     * The info about the module.<p>
     *
     * This returns texts with information about the module.<p>
     *
     * The possible informations are retrieved by giving any of the
     * arguments:<ul>
     * <li>{@link #DESCRIPTION}
     * <li>{@link #AUTHOR}
     * <li>{@link #VERSION}
     * <li>{@link #DOWNLOADSITE}
     * </ul>
     *
     * If a module does not provide a specific piece of information,
     * <code>null</code> can be returned. Hence the normal implementation
     * should be:<pre>
     * public String getInfo(int type) {
     *     switch (type) {
     *     case DESCRIPTION:
     *         return "This module does ...";
     *     case AUTHOR:
     *         return "Annie Coder";
     *     default:
     *         return null;
     * }
     * </pre>
     *
     * @param type The type of information.
     * @return The description. A String.
     */
    public String getInfo(int type) {
        switch (type) {
        case DESCRIPTION:
            return "SQL Generator";
        case AUTHOR:
            return "Kai ???";
        case VERSION:
            return "$Id$";
        default:
            return null;
        }
    }

} /* end class GeneratorSql */

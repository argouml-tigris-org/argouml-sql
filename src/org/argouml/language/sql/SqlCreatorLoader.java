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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

/**
 * Class for looking through the argouml extension dir for classes implementing
 * {@link SqlCodeCreator}. Creates an instance of all found classes.
 * 
 * @author drahmann
 */
public class SqlCreatorLoader {
    /**
     * Scans through the given jar file looking for classes. If a .class file is
     * found in the .jar a class name is build. Then the specified class loader
     * is used to try to load the class. If the class can be loaded it is added
     * to the result.
     * 
     * @param classLoader
     *            The {@link ClassLoader} to use for loading classes.
     * @param jarFile
     *            The {@link JarFile} to scan through.
     * @return A {@link Collection} of {@link Class} objects.
     */
    private Collection getClassesFromJar(ClassLoader classLoader,
            JarFile jarFile) {
        Collection classes = new HashSet();
        Enumeration e = jarFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String name = ze.getName();
            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                name = name.replace("/", ".");

                try {
                    classes.add(classLoader.loadClass(name));
                } catch (ClassNotFoundException e1) {
                    LOG.info("Class could not be loaded from jar: " + name);
                }
            }
        }
        return classes;
    }

    private static final Logger LOG = Logger.getLogger(SqlCreatorLoader.class);

    /**
     * Scans through the given directory for .jar- and .class-files. If such
     * files are found a class name is build and it is tries to load the class.
     * If it is possible the loaded class is added to the result list.
     * 
     * @param dir
     *            The directory to scan through.
     * @return A {@link Collection} of {@link Class} objects.
     */
    private Collection getClassesFromDir(File dir) {
        Collection result = new HashSet();

        if (!dir.exists()) {
            return result;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                result.addAll(getClassesFromDir(file));
            } else if (file.getName().endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(file);
                    ClassLoader cl = new URLClassLoader(new URL[] { file
                            .toURL() }, getClass().getClassLoader());
                    result.addAll(getClassesFromJar(cl, jarFile));
                } catch (IOException e) {
                    LOG.error("Exception", e);
                }
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName();
                className = className.substring(0, className.length()
                        - Utils.CLASS_SUFFIX.length());
                String packageName = dir.getAbsolutePath();
                packageName = packageName.substring(moduleDir.getPath()
                        .length());
                if (packageName.startsWith(File.separator)) {
                    packageName = packageName.substring(1);
                }
                packageName = packageName.replace(File.separatorChar, '.');

                className = packageName + "." + className;
                try {
                    result.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    LOG.info("Class could not be loaded from .class-file: "
                            + className);
                }
            }
        }

        return result;
    }

    private File moduleDir;

    /**
     * Scans through the argouml extension dir and looks for classes
     * implementing {@link SqlCodeCreator}. If such a class is found, it is
     * tried to instanciate it using the standard constructor. If this can be
     * done the instance is added to the <code>Collection</code> that will be
     * returned.
     * 
     * @return A <code>Collection</code> containing all found classes that
     *         implement {@link SqlCodeCreator}.
     */
    public Collection getCodeCreators() {
        Collection classes = new HashSet();
        moduleDir = new File(Utils.getModuleRoot());
        classes.addAll(getClassesFromDir(moduleDir));

        Collection result = new HashSet();
        for (Iterator it = classes.iterator(); it.hasNext();) {
            Class foundClass = (Class) it.next();
            if (foundClass != SqlCodeCreator.class
                    && SqlCodeCreator.class.isAssignableFrom(foundClass)) {
                result.add(foundClass);
            }
        }

        return result;
    }
}

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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.persistence.PersistenceManager;

/**
 * Class for looking through the argouml extension dir for classes implementing
 * {@link SqlCodeCreator}. Creates an instance of all found classes.
 * 
 * @author drahmann
 */
public class SqlCreatorLoader {
    private Collection getClassesFromJar(ClassLoader classLoader,
            JarFile jarFile) throws ClassNotFoundException {
        Collection classes = new HashSet();
        Enumeration e = jarFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String name = ze.getName();
            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                name = name.replace("/", ".");

                classes.add(classLoader.loadClass(name));// lass.forName(name));
            }
        }
        return classes;
    }

    private Collection getClassesFromDirectory(String packageName,
            File directory) {
        Collection classes = new HashSet();
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                String subPackageName = "";
                if (packageName.length() > 0) {
                    subPackageName = packageName + ".";
                }
                subPackageName += files[i].getName();
                classes
                        .addAll(getClassesFromDirectory(subPackageName,
                                files[i]));
            }

            try {
                String name = files[i].getName();
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6);

                    String fullClassName = packageName + "." + className;
                    Class foundClass = getClass().getClassLoader().loadClass(
                            fullClassName);
                    classes.add(foundClass);
                }
            } catch (ClassNotFoundException e) {
                // LOG.error("Exception", e);
            }
        }
        return classes;
    }

    private static final Logger LOG = Logger.getLogger(SqlCreatorLoader.class);

    private Collection getCodeCreators(File dir) {
        Collection result = new HashSet();

        if (!dir.exists()) {
            return result;
        }
        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                result.addAll(getCodeCreators(file));
            } else if (file.getName().endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(file);
                    ClassLoader cl = new URLClassLoader(new URL[] { file
                            .toURL() }, getClass().getClassLoader());
                    result.addAll(getClassesFromJar(cl, jarFile));
                } catch (IOException e) {
                    LOG.error("Exception", e);
                } catch (ClassNotFoundException e) {
                    LOG.error("Exception", e);
                }
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName();
                className = className.substring(0, className.length() - 6);
                String packageName = dir.getAbsolutePath();
                packageName = packageName.substring(extdir.getPath().length());
            }
        }

        return result;
    }

    private File extdir;

    /**
     * The prefix in URL:s that are files.
     */
    private static final String FILE_PREFIX = "file:";

    /**
     * The prefix in URL:s that are jars.
     */
    private static final String JAR_PREFIX = "jar:";

    /**
     * Class file suffix.
     */
    public static final String CLASS_SUFFIX = ".class";

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
        // Use a little trick to find out where Argo is being loaded from.
        String extForm = getClass().getResource(Argo.ARGOINI).toExternalForm();
        String argoRoot = extForm.substring(0, extForm.length()
                - Argo.ARGOINI.length());

        // If it's a jar, clean it up and make it look like a file url
        if (argoRoot.startsWith(JAR_PREFIX)) {
            argoRoot = argoRoot.substring(JAR_PREFIX.length());
            if (argoRoot.endsWith("!")) {
                argoRoot = argoRoot.substring(0, argoRoot.length() - 1);
            }
        }

        String argoHome = null;

        if (argoRoot != null) {
            LOG.info("argoRoot is " + argoRoot);
            if (argoRoot.startsWith(FILE_PREFIX)) {
                argoHome = new File(argoRoot.substring(FILE_PREFIX.length()))
                        .getAbsoluteFile().getParent();
            } else {
                argoHome = new File(argoRoot).getAbsoluteFile().getParent();
            }

            try {
                argoHome = java.net.URLDecoder.decode(argoHome,
                        PersistenceManager.getEncoding());
            } catch (UnsupportedEncodingException e) {
                LOG.warn("Encoding " + PersistenceManager.getEncoding()
                        + " is unknown.");
            }

            LOG.info("argoHome is " + argoHome);
        }

        String extdirName = null;
        if (argoHome != null) {
            if (argoHome.startsWith(FILE_PREFIX)) {
                extdirName = argoHome.substring(FILE_PREFIX.length())
                        + File.separator + "ext";
            } else {
                extdirName = argoHome + File.separator + "ext";
            }
        }

        Collection classes = new HashSet();
        String dirname = extdirName;
        extdir = new File(dirname);
        classes.addAll(getCodeCreators(extdir));
        File userdir = new File(System.getProperty("user.dir"));
        classes.addAll(getCodeCreators(userdir));

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

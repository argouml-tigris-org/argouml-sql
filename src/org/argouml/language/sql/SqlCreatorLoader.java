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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class SqlCreatorLoader {
    private Collection getClassesFromJar(JarFile jarFile)
            throws ClassNotFoundException {
        Collection classes = new HashSet();
        Enumeration e = jarFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String name = ze.getName();
            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                name = name.replace("/", ".");
                classes.add(Class.forName(name));
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
                    // Package p = getClass().getPackage();
                    // String packageName = p.getName();
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

    public Collection getLoadableClassesFromUri(URI uri, Class superclass) {
        Collection foundClasses = new HashSet();
        try {
            String scheme = uri.getScheme();
            if (scheme.equalsIgnoreCase("jar")) {
                uri = new URI(uri.getSchemeSpecificPart());
                StringTokenizer st = new StringTokenizer(uri.toString(), "!");

                String jarFileName = st.nextToken();
                String internalFileName = st.nextToken();
                if (internalFileName.startsWith("/")) {
                    internalFileName = internalFileName.substring(1);
                }
                uri = new URI(jarFileName);
                JarFile jf = new JarFile(uri.getSchemeSpecificPart());
                foundClasses.addAll(getClassesFromJar(jf));
            } else if (scheme.equalsIgnoreCase("file")) {
                File dir = new File(uri);
                if (dir.isDirectory()) {
                    foundClasses.addAll(getClassesFromDirectory("", dir));
                }
            }
        } catch (URISyntaxException e) {
            // LOG.error("Exception", e);
        } catch (IOException e) {
            // LOG.error("Exception", e);
        } catch (ClassNotFoundException e) {
            // LOG.error("Exception", e);
        }

        Collection returnClasses = foundClasses;
        if (superclass != null) {
            returnClasses = new HashSet();
            for (Iterator it = foundClasses.iterator(); it.hasNext();) {
                Class foundClass = (Class) it.next();
                if (foundClass != superclass
                        && superclass.isAssignableFrom(foundClass)) {
                    returnClasses.add(foundClass);
                }
            }
        }

        return returnClasses;
    }

}

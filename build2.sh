#! /bin/sh
# $Id: build2.sh 93 2006-10-25 06:12:20Z tfmorris $
#
 
# +-------------------------------------------------------------------------+
# | Verify and Set Required Environment Variables                           |
# +-------------------------------------------------------------------------+
if [ "$JAVA_HOME" = "" ] ; then
	echo "***************************************************************"
	echo "  ERROR: JAVA_HOME environment variable not found."
	echo ""
	echo "  Please set JAVA_HOME to the Java JDK installation directory."
	echo "***************************************************************"
	exit 1
fi

#
# build.sh always calls the version of ant distributed with ArgoUML
#
ANT_HOME=../argouml-core-tools/apache-ant-1.6.5

echo ANT_HOME is: $ANT_HOME
echo
echo Starting Ant...
echo

$ANT_HOME/bin/ant -Doverride.build.properties=../argouml/eclipse-ant-build.properties -Dargo.root.dir=`pwd`/.. $*

#exit

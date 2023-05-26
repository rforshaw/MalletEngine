echo "Compiling Android Mallet Engine"
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
/snap/bin/gradle clean zip build installDebug

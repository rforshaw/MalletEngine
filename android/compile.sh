echo "Compiling Android Mallet Engine"
ant clean
ant debug

adb install -r bin/AndroidMalletEngine-debug.apk
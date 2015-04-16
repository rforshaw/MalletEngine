#! /bin/bash

j2objc --prefixes prefixes.properties  -use-arc --doc-comments -d ../ios/src -sourcepath ../src `find ../src -name '*.java' -not -path "*/desktop/*" -not -path "*/malleteditor/*" -not -path "*/android/*"`
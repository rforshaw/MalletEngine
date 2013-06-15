@echo off
setLocal EnableDelayedExpansion

set CLASSPATH="
for /R ./ %%a in (*.jar) do (
	set CLASSPATH=!CLASSPATH!;%%a
)

set CLASSPATH=!CLASSPATH!"
echo !CLASSPATH!

java -Dsun.java2d.noddraw=true -Dsun.java2d.opengl=true com.linxonline.mallet.main.Main
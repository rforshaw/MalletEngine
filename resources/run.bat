@echo off
setLocal EnableDelayedExpansion

set CLASSPATH="
for /R ./ %%a in (*.jar) do (
	set CLASSPATH=!CLASSPATH!;%%a
)

set CLASSPATH=!CLASSPATH!"
echo !CLASSPATH!

java -Dsun.awt.noerasebackground=true com.linxonline.mallet.core.desktop.DesktopTestMain

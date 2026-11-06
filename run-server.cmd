@echo off
set MAVEN_CMD=C:\Tools\maven\apache-maven-3.9.9\bin\mvn.cmd
echo Starting FoodTracker WebAPI server...
call "%MAVEN_CMD%" exec:java
pause


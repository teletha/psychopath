@echo off
SET bee="%JAVA_HOME%/lib/bee/bee-0.13.0.jar"
if not exist %bee% (
  echo %bee% is not found, try to download it from network.
  curl -#L -o %bee% https://jitpack.io/com/github/teletha/bee/0.13.0/bee-0.13.0.jar
)
java -javaagent:%bee% -cp %bee% bee.Bee %*

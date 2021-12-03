@echo off
SET bee="%JAVA_HOME%/lib/bee/bee-0.17.0.jar"
if not exist %bee% (
  echo %bee% is not found, try to download it from network.
  curl -#L -o %bee% https://github.com/teletha/bee/raw/master/bee-0.10.0.jar
)
java -javaagent:%bee% -cp %bee% bee.Bee %*

@echo off
setlocal
if defined JAVA_HOME ( 
	set PATH=%JAVA_HOME%\bin;%PATH% 
)

set JAVA_OPTS=-XX:+TieredCompilation -Djava.net.useSystemProxies=true %JAVA_OPTS%
if not defined BUILD_NUMBER ( set BUILD_NUMBER=dev-build )
set version=@version@
set artifact=jcompilo
set group=com/googlecode/%artifact%
set repo=repo.bodar.com
set dir=lib
set jar=%dir%\%artifact%.jar
set pack=%dir%\%artifact%.pack.gz
set url=http://%repo%/%group%/%artifact%/%version%/%artifact%-%version%
set remote_file=%url%.pack.gz
set remote_sh=%url%.cmd

if /i "%1"=="update" (
    del /q %jar% %pack% 2>null
)

if not exist %jar% ( 
    mkdir %dir% 2>null
    wget -O %pack% %remote_file% || exit /b
    unpack200 %pack% %jar% || exit /b
    del /q %pack%
    wget -O $0 ${remote_sh} || exit /b
)

java -showversion -Dbuild.number=%BUILD_NUMBER% %JAVA_OPTS% -jar %jar% %*

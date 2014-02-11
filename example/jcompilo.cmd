@echo off

setlocal
set JAVA_OPTS=-Djava.net.useSystemProxies=true %JAVA_OPTS%
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
    wget -O %pack% %remote_file%
    unpack200 %pack% %jar% 
    del /q %pack%
)

java -showversion -Dbuild.number=%BUILD_NUMBER% %JAVA_OPTS% -jar %jar% %*
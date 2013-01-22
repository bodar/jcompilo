#!/bin/sh

version=@version@
artifact=jcompilo
group=com/googlecode/${artifact}
repo=repo.bodar.com.s3.amazonaws.com
dir=lib/
jar=${dir}${artifact}.jar
remote-jar=http://${repo}/${group}/${artifact}/${version}/${artifact}-${version}.jar
remote-sh=http://${repo}/${group}/${artifact}/${version}/${artifact}-${version}.sh

if [ $1 = "-u" ]; then 
	rm ${jar}
	shift 1
fi

if [ ! -f ${local} ]; then
	mkdir -p ${dir} 
	wget -O ${jar} ${remote-jar} || curl -o ${jar} ${remote-jar}
	wget -O $0 ${remote-sh} || curl -o $0 ${remote-sh}
fi
exec java -jar ${jar} $*
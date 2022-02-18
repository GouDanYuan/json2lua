workSpace=${WORKSPACE}
serverPath=${workSpace}"/server"
clientPath=${workSpace}"/client"
md5Path=${workSpace}"/md5_cached"
clientMD5File="client_md5.txt"
#lang=${LANG}
lang="zh-Hans"
clientRoot=${workSpace}"/client/game/Assets/distribute/lang_"${lang}/data/
json2luaJAR=${workSpace}"/json2lua.jar"
gitBranch=${GIT_BRANCH}

ntpdate pool.ntp.org

function print()
{
    echo ""
    echo "/-----------------------------------------------------------"
    echo "| [`date +%Y-%m-%d\ %H:%M:%S`] $1"
    echo "\-----------------------------------------------------------"
}

print "clientRoot:"${clientRoot}
print "workSpace:"${workSpace}

fixLang=${lang}
if [ "${lang}" = "hk" ]; then
 fixLang="tcn"
fi
svnRoot="svn://192.168.1.7/server/"${SERVER_BRANCH}"/json/"${fixLang}
serverRoot="${serverPath}/${fixLang}/"
if [ ! -d ${md5Path} ]; then
 mkdir ${md5Path}
fi
if [ ! -d $serverPath ]; then
 mkdir ${serverPath}
fi
if [ ! -d $clientPath ]; then
 mkdir ${clientPath}
fi
if [ -d ${serverRoot} ]; then
 rm -rf ${serverRoot}
fi
pwd
cd ${clientPath}
git reset --hard HEAD
git clean -df
git fetch -v
git checkout -B ${gitBranch} remotes/origin/${gitBranch}
git pull
cd ${serverPath}
svn co ${svnRoot} --username *** --password ***
cd ${serverRoot}
pwd
find -maxdepth 1 -name "*.json" -print0 | xargs -0 md5sum > ${workSpace}/md5_cached/server_md5.txt
pwd
cd ${clientRoot}
if [ $FAST = "true" ]; then
 md5sum -c ${workSpace}/md5_cached/server_md5.txt |grep "FAILED"|sed 's/: FAILED open or read//g'|sed 's/: FAILED//g'|sed 's/.\///g' > ${workSpace}/md5_cached/changed_json.txt
else
 cat ${workSpace}/md5_cached/server_md5.txt | grep -o  "./.*" | sed 's/.\///g' > ${workSpace}/md5_cached/changed_json.txt
fi
pwd
cd ${workSpace}
pwd
convertList=""
for line in $(cat ${workSpace}/md5_cached/changed_json.txt)
do
 origin=$serverRoot$line
 dest=$clientRoot$line
 echo $origin
 echo $dest
 mv -f $origin $dest
 convertList=$convertList" "$dest
done
echo $convertList
java -jar $json2luaJAR $convertList
cd ${clientRoot}
git add -A
git commit -m "convert json ${svnRoot}"
git push origin ${gitBranch}
print "convert success"


dir=$(ls -l $1 |awk '/^d/ {print $NF}')
for i in $dir
do
 find $1$i -maxdepth 1 -name "*.json" -print0 | xargs -0 md5sum | sed "s/server\/$lang\/$i\///g" > md5_$i.txt
done
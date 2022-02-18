#!/bin/bash

CLIENT_MULTIPLE_JSON_PATH="Assets/CapstonesScripts/distribute/lang_zh-Hans/data"
echo "CLIENT_MULTIPLE_JSON_PATH="$CLIENT_MULTIPLE_JSON_PATH
# jsonList=$(find ${CLIENT_MULTIPLE_JSON_PATH} -maxdepth 10 -name "*.json" | sed 's/'${cutPath}'//g')
jsonList=$(find ${CLIENT_MULTIPLE_JSON_PATH} -maxdepth 1 -name "*.json")

echo -e "jsonList = "${jsonList}

java -jar json2lua.jar ${jsonList}

echo 'done'
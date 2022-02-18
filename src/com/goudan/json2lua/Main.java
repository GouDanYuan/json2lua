package com.goudan.json2lua;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        String mPath = args[0];
//        String mPath = "Assets/CapstonesScripts/distribute/lang_zh-Hans/data/Card.json";
        for (String mPath: args) {
            System.out.println("ready begin path:" + mPath);
            AutomatedConvertJson(mPath);
        }
        System.out.println("json2lua end");
    }

    private static void AutomatedConvertJson(String path) throws IOException {
        if (!Utils.IsNullOrBlank(path)) {
            if (path.toLowerCase().endsWith(".json")) {
                String dst = path.substring(0, path.length() - ".json".length()) + ".lua";
                if (dst.contains("/ItemContent.lua") || dst.contains("/Item.lua") || dst.contains("/Paster.lua") || dst.contains("/Card.lua")) {
                    int averageNum = 2000;
                    if (path.contains("Card.json")) {
                        averageNum = 800;
                    }
                    Json2LuaConverter.ConvertJson2LuaMultiple(path, averageNum);
                }
                else {
                    Json2LuaConverter.ConvertToLua(path, dst);
                }
            }
        }
        else{
            System.out.println("path is not correct!!");
            System.exit(1);
        }
    }
}

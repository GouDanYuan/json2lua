package com.goudan.json2lua;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Json2LuaConverter {
    private static String newLine = System.getProperty("line.separator");
    static void ConvertJson2LuaMultiple(String path) throws IOException {
        int averageNum = 2000;
        ConvertJson2LuaMultiple(path, averageNum);
    }

    static void ConvertJson2LuaMultiple(String path, int averageNum) throws IOException {
        int jsonLength = ".json".length();
        int sptLength = "Assets/CapstonesScripts/spt/".length();
        int distributeLength = "Assets/CapstonesScripts/distribute/".length();
        String part = path.substring(0, path.length() - jsonLength);
        String dst = part + ".lua";
        String subPath = part + "/parts/";
        if (part.startsWith("Assets/CapstonesScripts/spt/")) {
            part = part.substring(sptLength);
        }
        else if (part.startsWith("Assets/CapstonesScripts/distribute/")) {
            part = part.substring(distributeLength);
            int index = part.indexOf('/');
            if (index > 0) {
                part = part.substring(index + 1);
            }
        }

        File subFile = new File(subPath);
        if (subFile.exists()) {
            String[] subfiles = Utils.GetAllFiles(subPath);
            for (String subfile:subfiles) {
                if (subfile.endsWith(".json") || subfile.endsWith(".lua")) {
                    Utils.DeleteFile(subfile);
                }
            }
        }

        String srcstr = Utils.OpenReadText(path);
        int jsontype = 0;
        int readpos = 0;

        if (srcstr != null) {
            while (readpos < srcstr.length()) {
                char ch = srcstr.charAt(readpos++);
                if (ch == '{') {
                    jsontype = 1;
                    break;
                }
                else if (ch == '[') {
                    jsontype = 2;
                    break;
                }
                else if (ch == '\"') {
                    break;
                }
            }
        }
        if (jsontype == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(newLine + "local var = {} \n\nlocal Mt = { } \nMt.__add = function(t1, t2) \n   for key, data in pairs(t2) do\n      t1[key] = data\n   end \n   return t1 \nend \n\nlocal splitTable = {  \n\t");
            int nextpartindex = 1;
            while (true) {
                int keystart = 0;
                int count = 0;
                while (readpos < srcstr.length()) {
                    char ch = srcstr.charAt(readpos);
                    if (ch == '\"') {
                        keystart = readpos;
                        break;
                    }
                    ++readpos;
                }
                if (keystart > 0) {
                    int keyend = EncloseJsonToken(srcstr, keystart);
                    String key = DecodeJsonString(srcstr.substring(keystart + 1, keyend));
                    StringBuilder sbfilekey = new StringBuilder();
                    boolean usefilekey = false;
                    for (char ch : key.toCharArray()) {
                        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                            sbfilekey.append(ch);
                        }
                        else {
                            usefilekey = true;
                            sbfilekey.append("_");
                        }
                    }
                    if (usefilekey) {
                        sbfilekey.insert(0, nextpartindex++);
                    }
                    String filekey = sbfilekey.toString();
                    int valpos = keystart;
                    int valposend = 0;
                    String nextKey = "";
                    readpos = keyend + 1;
                    while (readpos < srcstr.length()) {
                        char ch = srcstr.charAt(readpos++);
                        if (ch == ',') {
                            valposend = readpos - 1;
                            count++;
                            if (count >= averageNum) break;
                            char temp = srcstr.charAt(readpos);
                            if (temp == '"') {
                                int nextKeyend = EncloseJsonToken(srcstr, readpos);
                                if (nextKeyend > 0) {
                                    String value = DecodeJsonString(srcstr.substring(readpos + 1, nextKeyend));
                                    nextKey = value;
                                }
                            }
                        }
                        else if (ch == '"' || ch == '{' || ch == '[') {
                            int pend = EncloseJsonToken(srcstr, readpos - 1);
                            valposend = pend;
                            readpos = pend + 1;
                        }
                    }

                    sb.append("setmetatable(require('");
                    int startIndex = part.indexOf("Assets/");
                    String requireStr = part.substring(startIndex);
                    requireStr = requireStr.replace('/', '.');
                    int startRequireIndex = requireStr.indexOf(".data.");
                    requireStr = requireStr.substring(startRequireIndex + 1);
                    sb.append(requireStr);
                    sb.append(".parts.part");
                    sb.append(filekey + "_" + nextKey);
                    sb.append("'), Mt),"+"\n\t");
                    if (valpos > 0 && valposend > 0) {
                        String jsonsub = srcstr.substring(valpos, valposend + 1);
                        jsonsub = "{" + jsonsub + "}";
                        String luasub = ConvertToLua(jsonsub);
                        OutputStreamWriter subsw = Utils.OpenWrite(subPath + "part" + filekey + "_" + nextKey + ".lua");
                        assert subsw != null;
                        subsw.write("local null = nil");
                        subsw.write(newLine);
                        subsw.write("local var = ");
                        subsw.write(luasub);
                        subsw.write(newLine);
                        subsw.write("return var");
                        subsw.close();
                        OutputStreamWriter subjsonsw = Utils.OpenWrite(subPath + "part" + filekey + "_" + nextKey + ".json");
                        assert subjsonsw != null;
                        subjsonsw.write(jsonsub);
                        subjsonsw.close();
                    }

                    continue;
                }
                break;
            }
            sb.append(newLine);
            sb.append("} \n\nfor i, v in ipairs(splitTable) do \n   var = var + v \nend \n");
            sb.append("return var");
            OutputStreamWriter destsw = Utils.OpenWrite(dst);
            destsw.write(sb.toString());
            destsw.close();
        }
        else {
            ConvertToLua(path, dst);
        }

        if (subFile.exists()) {
            String[] subfiles = Utils.GetAllFiles(subPath);
            for (String subfile:subfiles) {
                if (subfile.endsWith(".meta")) {
                    File sFile = new File(subfile.substring(0, subfile.length() - ".meta".length()));
                    if (!sFile.exists()) {
                        Utils.DeleteFile(subfile);
                    }
                }
            }
            subfiles = Utils.GetAllFiles(subPath);
            if (subfiles.length <= 0)
            {
                Utils.DeleteFile(subPath);
            }
        }
    }

    static class Json2LuaConverterState {
        public enum BlockType {
            Plain,
            String,
            Object,
            Array,
        }
        BlockType type = BlockType.Plain;
        int start = 0;
        int end = 0;
        boolean keyParsed = false;
        boolean esc = false;
    }
    static int EncloseJsonToken(String json, int startIndex) {
        Stack<Json2LuaConverterState> statestack = new Stack<Json2LuaConverterState>();
        int pos = startIndex;
        Json2LuaConverterState context = new Json2LuaConverterState();
        while (pos < json.length()) {
            char ch = json.charAt(pos++);
            if (context.type == Json2LuaConverterState.BlockType.String) {
                if (context.esc) {
                    context.esc = false;
                }
                else {
                    if (ch == '\\') {
                        context.esc = true;
                    }
                    else {
                        if (ch == '\"') {
                            context = statestack.pop();
                            if (statestack.size() <= 0) {
                                return pos - 1;
                            }
                        }
                    }
                }
            }
            else {
                if (ch == '{') {
                    statestack.push(context);
                    context = new Json2LuaConverterState();
                    context.type = Json2LuaConverterState.BlockType.Object;
                }
                else if (ch == '[') {
                    statestack.push(context);
                    context = new Json2LuaConverterState();
                    context.type = Json2LuaConverterState.BlockType.Array;
                }
                else if (ch == ']') {
                    context = statestack.pop();
                    if (statestack.size() <= 0) {
                        return pos - 1;
                    }
                }
                else if (ch == '}') {
                    context = statestack.pop();
                    if (statestack.size() <= 0) {
                        return pos - 1;
                    }
                }
                else if (ch == '\"') {
                    statestack.push(context);
                    context = new Json2LuaConverterState();
                    context.type = Json2LuaConverterState.BlockType.String;
                    context.start = pos - 1;
                }
            }
        }
        return json.length() - 1;
    }
    static String ConvertToLua(String json) {
        Stack<Json2LuaConverterState> statestack = new Stack<Json2LuaConverterState>();
        Json2LuaConverterState context = new Json2LuaConverterState();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < json.length(); ++i) {
            char ch = json.charAt(i);
            if (context.type == Json2LuaConverterState.BlockType.String) {
                if (context.esc) {
                    context.esc = false;
                    continue;
                }
                else {
                    if (ch == '\\') {
                        context.esc = true;
                        continue;
                    }
                    else {
                        if (ch == '\"') {
                            String sub = json.substring(context.start + 1, i);
                            String conv = NormalizeLineBreaks(DecodeJsonString(sub));
                            EncodeLuaString(conv, 0, conv.length() - 1, sb);
                            sb.append("]=]");
                            context = statestack.pop();
                            if (context.type == Json2LuaConverterState.BlockType.Object && !context.keyParsed) {
                                sb.append(" ]");
                            }
                        }
                        continue;
                    }
                }
            }
            else
            {
                if (ch == '{') {
                    sb.append(newLine);
                    for (int j = 0; j < statestack.size(); j++) {
                        sb.append('\t');
                    }
                    statestack.push(context);
                    context = new Json2LuaConverterState();
                    context.type = Json2LuaConverterState.BlockType.Object;
                }
                else if (ch == '[') {
                    sb.append(newLine);
                    for (int j = 0; j < statestack.size(); j++) {
                        sb.append('\t');
                    }
                    statestack.push(context);
                    context = new Json2LuaConverterState();
                    context.type = Json2LuaConverterState.BlockType.Array;
                    sb.append('{');
                    continue;
                }
                else if (ch == ']') {
                    context = statestack.pop();
                    sb.append(newLine);
                    for (int j = 0; j < statestack.size(); j++) {
                        sb.append('\t');
                    }
                    sb.append('}');
                    continue;
                }
                else if (ch == '}') {
                    context = statestack.pop();
                    sb.append(newLine);
                    for (int j = 0; j < statestack.size(); j++) {
                        sb.append('\t');
                    }
                }
                else if (ch == '\"') {
                    if (context.type == Json2LuaConverterState.BlockType.Object && !context.keyParsed) {
                        sb.append(newLine);
                        for (int j = 0; j < statestack.size(); j++) {
                            sb.append('\t');
                        }
                        sb.append("[ ");
                    }
                    statestack.push(context);
                    context = new Json2LuaConverterState();
                    context.type = Json2LuaConverterState.BlockType.String;
                    context.start = i;
                    sb.append("[=[");
                    continue;
                }
                else if (ch == ':') {
                    if (context.type == Json2LuaConverterState.BlockType.Object) {
                        sb.append('=');
                        context.keyParsed = true;
                        continue;
                    }
                }
                else if (ch == ',')
                {
                    if (context.type == Json2LuaConverterState.BlockType.Object) {
                        context.keyParsed = false;
                    }
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }
    static void ConvertToLua(String fsrc, String fdst) throws IOException {
        Utils.DeleteFile(fdst);
        Utils.CreateFolder(Utils.GetDirectoryName(fdst));

        String sr = Utils.OpenReadText(fsrc);
        OutputStreamWriter sw = Utils.OpenWrite(fdst);
        assert sw != null;
        sw.write("local null = nil");
        sw.write(newLine);
        sw.write("local var = ");
        assert sr != null;
        sw.write(ConvertToLua(sr));
        sw.write(newLine);
        sw.write("return var");
        sw.flush();
        sw.close();
    }
    static String DecodeJsonString(String src) {
        src = src.replace("\\\\", "\\");
        src = src.replace("\\\"", "\"");
        src = src.replace("\\/", "/");
        src = src.replace("\\b", "\b");
        src = src.replace("\\f", "\f");
        src = src.replace("\\n", "\n");
        src = src.replace("\\r", "\r");
        src = src.replace("\\t", "\t");
        String reg = "\\\\u[0-9a-fA-F]{4}";
        Pattern r = Pattern.compile(reg);
        Matcher m = r.matcher(src);
        StringBuffer sb = new StringBuffer() ;
        while( m.find() ){
            String tmp = m.group();
            String str = tmp.substring(2, 6);
            char c = (char)Integer.valueOf(str, 16).intValue();
            String cStr = String.valueOf(c);
//            System.out.println("origin str:" + str + "  parse char:" + c + "  string res:" + cStr);
            m.appendReplacement(sb, cStr);
        }
        m.appendTail(sb);
        return sb.toString();
    }
    static void EncodeLuaString(String src, int startIndex, int endIndex, StringBuilder sb) {
        for (int i = startIndex; i <= endIndex; ++i) {
            char ch = src.charAt(i);
            sb.append(ch);
        }
    }
    static String NormalizeLineBreaks(String input) {
        StringBuilder builder = new StringBuilder((int)(input.length() * 1.1));

        boolean lastWasCR = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (lastWasCR) {
                lastWasCR = false;
                if (c == '\n') {
                    continue; // Already written \r\n
                }
            }
            switch (c) {
                case '\r':
                    builder.append(newLine);
                    lastWasCR = true;
                    break;
                case '\n':
                    builder.append(newLine);
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }
}

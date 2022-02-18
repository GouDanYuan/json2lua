package com.goudan.json2lua;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class Utils {
    static String[] GetAllFiles(String dir) {
        ArrayList<String> list = new ArrayList<>();
        getAllFileName(dir, list);
        return list.toArray(new String[0]);
    }

    private static void getAllFileName(String path, ArrayList<String> list) {
        File file = new File(path);
        String[] strings = file.list();
        assert strings != null;
        File[] files = file.listFiles(pathname -> true);
        assert files != null;
        for (File value : files)
            if (value.isDirectory()) getAllFileName(value.getAbsolutePath(), list);
            else list.add(value.getAbsolutePath());
    }

    static void DeleteFile(String path) {
        for (int i = 0; i <= 3; ++i)
            try {
                File file = new File(path);
                if (file.exists()) {
                    boolean isDelete = file.delete();
                    if (isDelete) {
                        System.out.println("DeleteFile success " + path);
                        return;
                    }
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace().toString());
            }
    }

    static String OpenReadText(String path) {
        try {
            File file = new File(path);
            FileInputStream stream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            while (reader.ready()) sb.append((char) reader.read());
            reader.close();
            stream.close();
            return sb.toString();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace().toString());
        }
        return null;
    }

    static OutputStreamWriter OpenWrite(String path) throws FileNotFoundException, UnsupportedEncodingException {
        DeleteFile(path);
        CreateFolder(GetDirectoryName(path));
        for (int i = 0; i <= 3; ++i) { /* retry 3 times*/
            try {
                FileOutputStream fs = new FileOutputStream(path, true);
                fs.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                OutputStreamWriter writer = new OutputStreamWriter(fs, StandardCharsets.UTF_8);
                return writer;
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace().toString());
            }
        }

        return null;
    }

    static void CreateFolder(String path) {
        for (int i = 0; i <= 3; ++i) { /* retry 3 times*/
            try {
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.mkdirs();
                    System.out.println("CreateFolder success " + path);
                }
                Thread.sleep(1);
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace().toString());
            }
        }
    }

    static String GetDirectoryName(String path) {
        String dirPath = path.substring(0, path.lastIndexOf("/"));
        return dirPath;
    }

    static boolean DeleteDir(String path) {
        File rootFile = new File(path);
        if (rootFile.isDirectory()) {
            String[] children = rootFile.list();
            for (String child : children) {
                boolean success = DeleteDir(child);
                if (!success) return false;
            }
        }
        return rootFile.delete();
    }

    public static boolean IsNullOrBlank(String param) { return param == null || param.trim().length() == 0; }
}

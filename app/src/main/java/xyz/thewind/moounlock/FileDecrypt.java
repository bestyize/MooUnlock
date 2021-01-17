package xyz.thewind.moounlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FileDecrypt {
    public static boolean decrypt(String fullPath){
        if(fullPath.endsWith(".uc")||fullPath.endsWith(".uc!")){
            return WyMusicDecrypt.decrypt(fullPath);
        }else {
            return QQMusicDecrypt.decrypt(fullPath);
        }
    }


}

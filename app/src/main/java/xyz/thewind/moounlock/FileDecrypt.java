package xyz.thewind.moounlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FileDecrypt {
    private static final int[] map={0x77,0x48,0x32,0x73,0xDE,0xF2,0xC0,0xC8,0x95,0xEC,0x30,0xB2,0x51,0xC3,0xE1,0xA0,0x9E,0xE6,0x9D,0xCF,0xFA,0x7F,0x14,0xD1,0xCE,0xB8,0xDC,0xC3,0x4A,0x67,0x93,0xD6,0x28,0xC2,0x91,0x70,0xCA,0x8D,0xA2,0xA4,0xF0,8,0x61,0x90,0x7E,0x6F,0xA2,0xE0,0xEB,0xAE,0x3E,0xB6,0x67,0xC7,0x92,0xF4,0x91,0xB5,0xF6,0x6C,0x5E,0x84,0x40,0xF7,0xF3,0x1B,2,0x7F,0xD5,0xAB,0x41,0x89,0x28,0xF4,0x25,0xCC,0x52,0x11,0xAD,0x43,0x68,0xA6,0x41,0x8B,0x84,0xB5,0xFF,0x2C,0x92,0x4A,0x26,0xD8,0x47,0x6A,0x7C,0x95,0x61,0xCC,0xE6,0xCB,0xBB,0x3F,0x47,0x58,0x89,0x75,0xC3,0x75,0xA1,0xD9,0xAF,0xCC,8,0x73,0x17,0xDC,0xAA,0x9A,0xA2,0x16,0x41,0xD8,0xA2,6,0xC6,0x8B,0xFC,0x66,0x34,0x9F,0xCF,0x18,0x23,0xA0,0xA,0x74,0xE7,0x2B,0x27,0x70,0x92,0xE9,0xAF,0x37,0xE6,0x8C,0xA7,0xBC,0x62,0x65,0x9C,0xC2,8,0xC9,0x88,0xB3,0xF3,0x43,0xAC,0x74,0x2C,0xF,0xD4,0xAF,0xA1,0xC3,1,0x64,0x95,0x4E,0x48,0x9F,0xF4,0x35,0x78,0x95,0x7A,0x39,0xD6,0x6A,0xA0,0x6D,0x40,0xE8,0x4F,0xA8,0xEF,0x11,0x1D,0xF3,0x1B,0x3F,0x3F,7,0xDD,0x6F,0x5B,0x19,0x30,0x19,0xFB,0xEF,0xE,0x37,0xF0,0xE,0xCD,0x16,0x49,0xFE,0x53,0x47,0x13,0x1A,0xBD,0xA4,0xF1,0x40,0x19,0x60,0xE,0xED,0x68,9,6,0x5F,0x4D,0xCF,0x3D,0x1A,0xFE,0x20,0x77,0xE4,0xD9,0xDA,0xF9,0xA4,0x2B,0x76,0x1C,0x71,0xDB,0,0xBC,0xFD,0xC,0x6C,0xA5,0x47,0xF7,0xF6,0,0x79,0x4A,0x11};

    /**
     * 手机端解密文件解密
     * @param fullPath
     * @return
     */
    public static boolean decrypt(String fullPath){
        try {
            String sign=".";
            if(fullPath.contains(".bkc")){//bkcmp3\bkcflac
                sign=".bkc";
            }else if(fullPath.contains(".qmc")){//qmcflac\qmc0\qmc2\qmc3
                sign=".qmc";
            }else {
                return decryptCache(fullPath);
            }
            String decryptFilePath=fullPath.replace(sign,".");
            String postFix=decryptFilePath.substring(decryptFilePath.indexOf("."));
            if(postFix.equals(".0")||postFix.equals(".2")||postFix.equals(".3")){
                decryptFilePath=decryptFilePath.replace(postFix,".mp3");
            }else {
                decryptFilePath=decryptFilePath.replace(".efe","");
            }
            File file=new File(fullPath);
            FileInputStream is=new FileInputStream(file);
            FileOutputStream fout=new FileOutputStream(decryptFilePath);
            byte[] b=new byte[8192];
            int len=0;
            long total=0;
            while ((len=is.read(b))!=-1){
                for (int i=0;i<len;i++){
                    b[i]^=encMap(total+i);
                }
                fout.write(b,0,len);
                total+=len;
            }
            is.close();
            fout.close();
            file.delete();
            System.out.println(readMetaInfo(decryptFilePath));
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }
    private static int encMap(long len){
        int pos= (int) (len%0x7fff);
        return map[(pos*pos+80923)%256];
    }

    /**
     * 缓存文件解密
     * @param fullPath
     * @return
     */
    public static boolean decryptCache(String fullPath){
        if(!fullPath.endsWith(".mqcc")&&!fullPath.endsWith(".efe")){
            return false;
        }
        try {
            String decryptFilePath="";
            if(fullPath.endsWith(".mqcc")){
                File decryptFile=new File(fullPath);
                if(fullPath.endsWith(".flac.mqcc")){
                    decryptFilePath=fullPath.replace(".mqcc","");
                }else {
                    decryptFilePath=fullPath.replace(".mqcc",".mp3");
                }

                Map<String,String> metaMap=readMetaInfo(fullPath);
                String newName=metaMap.get("singerName")+" - "+metaMap.get("songName")+decryptFilePath.substring(decryptFilePath.lastIndexOf("."));
                String newFullPath=decryptFilePath.substring(0,decryptFilePath.lastIndexOf(File.separator)+1)+newName;
                decryptFile.renameTo(new File(newFullPath));
                decryptFile.delete();
                return true;
            }
            if(fullPath.endsWith("flac.efe")){
                decryptFilePath=fullPath.replace(".efe","");
            }else {
                decryptFilePath=fullPath.replace(".efe",".mp3");
            }
            //byte[] key="996996".getBytes(StandardCharsets.UTF_8);
            byte[] key="ffffffffee72619d000000000033c587".getBytes(StandardCharsets.UTF_8);
            File file=new File(fullPath);
            FileInputStream is=new FileInputStream(file);
            File decryptFile=new File(decryptFilePath);
            FileOutputStream fout=new FileOutputStream(decryptFile);
            byte[] b=new byte[204800];
            int keyPos=0;
            int mod=0;
            int len=0;
            int curr=0;
            while ((len=is.read(b))!=-1){
                for(int i=0;i<len&&curr<5242880;i++){
                    if(mod==0){
                        b[i]^=key[keyPos];
                    }
                    int temp=keyPos+1;
                    if(temp==key.length){
                        mod=(mod+1)%2;
                        keyPos=0;
                    }else {
                        keyPos=temp;
                    }
                }
                fout.write(b,0,len);
                curr+=len;
            }
            is.close();
            fout.close();
            Map<String,String> metaMap=readMetaInfo(decryptFilePath);
            String newName=metaMap.get("singerName")+" - "+metaMap.get("songName")+decryptFilePath.substring(decryptFilePath.lastIndexOf("."));
            String newFullPath=decryptFilePath.replace(decryptFile.getName(),newName);
            decryptFile.renameTo(new File(newFullPath));
            file.delete();
            System.out.println(readMetaInfo(newFullPath));
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private static Map<String,String> readMetaInfo(String fullPath){
        Map<String,String> map=new HashMap<>();
        try {
            File file=new File(fullPath);
            FileInputStream is=new FileInputStream(file);
            byte[] bytes=new byte[1024];//元数据在开始的1k内
            if(is.read(bytes)!=-1){
                map.put("songName",extraField(bytes,"TITLE"));
                map.put("singerName",extraField(bytes,"ARTIST"));
                map.put("album",extraField(bytes,"ALBUM"));
            }
            is.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return map;
    }

    private static String extraField(byte[] bytes,String field){
        for (int i=0;i<bytes.length;i++){
            if(field.length()>2&&(i+2)<bytes.length&&bytes.length>field.length()&&bytes[i]==field.charAt(0)&&bytes[i+1]==field.charAt(1)&&bytes[i+2]==field.charAt(2)){
                int titleStart=i+field.length()+1;
                int titleEnd=titleStart;
                while (bytes[titleEnd]!=0){
                    titleEnd++;
                }
                byte arr[]=new byte[titleEnd-titleStart-1];
                for (int j=0;j<arr.length;j++){
                    arr[j]=bytes[titleStart++];
                }
                return new String(arr,StandardCharsets.UTF_8);
            }
        }
        return "";
    }

}

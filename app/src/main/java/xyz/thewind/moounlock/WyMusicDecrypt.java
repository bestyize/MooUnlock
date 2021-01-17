package xyz.thewind.moounlock;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WyMusicDecrypt {

    public static boolean decrypt(String fullPath){
        if(fullPath.endsWith(".uc")||fullPath.endsWith(".uc!")){
            try {
                boolean succ=decryptCacheFile(fullPath);
                renameMusicFile(fullPath+".temp");
                new File(fullPath).delete();
                return succ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    private static boolean decryptCacheFile(String fullPath) throws Exception {
        File file = new File(fullPath);
        FileInputStream is = new FileInputStream(file);
        FileOutputStream fout = new FileOutputStream(fullPath+".temp");
        byte[] b = new byte[8192];
        int len = 0;
        while ((len = is.read(b)) != -1) {
            for (int i = 0; i < len; i++) {
                b[i]^= 0xa3;
            }
            fout.write(b, 0, len);
        }
        is.close();
        fout.close();
        return true;
    }

    /**
     * 解密后的音乐数据
     * @param fullPath
     * @return
     */
    private static String renameMusicFile(String fullPath){
        File decryptFile=new File(fullPath);
        if(!decryptFile.exists()){
            return "";
        }
        String newName=getCorrectMusicFileName(fullPath);
        boolean succ=decryptFile.renameTo(new File(newName));
        if(succ){
            //decryptFile.delete();
        }
        return newName;
    }
    /**
     * 读取歌曲的元数据,获得歌手名+歌曲名
     * @param fullPath
     * @return
     */
    private static String getCorrectMusicFileName(String fullPath){
        String dir=fullPath.substring(0,fullPath.lastIndexOf(File.separator)+1);
        Map<String,String> metaMap=readMetaInfoFromWeb(fullPath);

        String newName=metaMap.get("singerName")+" - "+metaMap.get("songName")+"."+metaMap.get("type");
        return dir+newName;
    }

    /**
     * 读取元数据
     * @param fullPath
     * @return
     */
    private static Map<String,String> readMetaInfo(String fullPath){
        Map<String,String> map=new HashMap<>();
        byte[] bytes=readBytes(fullPath,0,0x1000);//元数据在开始的8k内
        map.put("songName",extraField(bytes,"Title"));
        map.put("singerName",extraField(bytes,"Artist"));
        map.put("album",extraField(bytes,"Album"));
        if(bytes[0]=='f'){
            map.put("type","flac");
        }else {
            map.put("type","mp3");
        }
        return map;
    }

    /**
     * 提取音乐文件的字段
     * @param bytes
     * @param field
     * @return
     */
    private static String extraField(byte[] bytes,String field){
        byte fb[]=field.getBytes();
        for (int i=0;i<bytes.length;i++){
            if(field.length()>2&&(i+2)<bytes.length&&bytes.length>field.length()&&equal(bytes[i],fb[0])&&equal(bytes[i+1],fb[1])&&equal(bytes[i+2],fb[2])){
                int titleStart=i+field.length()+1;
                int titleEnd=titleStart;
                while (bytes[titleEnd]!=0){
                    titleEnd++;
                }
                byte arr[]=new byte[titleEnd-titleStart-1];
                for (int j=0;j<arr.length;j++){
                    arr[j]=bytes[titleStart++];
                }
                return new String(arr, StandardCharsets.UTF_8);
            }
        }
        return "";
    }
    //
    private static Map<String,String> readMetaInfoFromWeb(String fullPath){
        Map<String,String> map=new HashMap<>();
        byte[] bytes=readBytes(fullPath,0,0x1);//元数据在开始的8k内
        if(bytes[0]=='f'){
            map.put("type","flac");
        }else {
            map.put("type","mp3");
        }
        String fullName=fullPath.substring(fullPath.lastIndexOf(File.separator)+1);
        if(!fullName.contains("-")){
            return map;
        }
        Map<String,String> headerMap=new HashMap<>();
        headerMap.put("User-Agent","Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36 Edg/87.0.664.75");
        String songId=fullName.substring(0,fullName.indexOf("-"));
        String response= HttpRequestHelper.downloadWebSiteUseGet("https://music.163.com/api/song/detail?ids=%5b"+songId+"%5d",headerMap);
        if(response.contains("\"songs\":[{\"name\":\"")&&response.contains("\"album\":{\"name\":\"")&&response.contains("\"artists\":[{\"name\":\"")){
            response=response.substring(response.indexOf("\"songs\":[{\"name\":\"")+"\"songs\":[{\"name\":\"".length());
            map.put("songName",response.substring(0,response.indexOf("\"")));
            response=response.substring(response.indexOf("\"album\":{\"name\":\"")+"\"album\":{\"name\":\"".length());
            map.put("album",response.substring(0,response.indexOf("\"")));
            response=response.substring(response.indexOf("\"artists\":[{\"name\":\"")+"\"artists\":[{\"name\":\"".length());
            map.put("singerName",response.substring(0,response.indexOf("\"")));
        }
        //setMetaInfo(fullPath,map);
        System.out.println(map.toString());
        return map;
    }

    private static void setMetaInfo(String fullPath,Map<String,String> metaMap){
        byte[] blankBlock=new byte[128];
        Arrays.fill(blankBlock, (byte) 0);
        String album=metaMap.get("album");
        String songName=metaMap.get("songName");
        String singerName=metaMap.get("singerName");
        setBytes(fullPath,("TITLE="+songName).getBytes(StandardCharsets.UTF_8),findBinaryBlock(fullPath,blankBlock)+3);
        setBytes(fullPath,("ALBUM="+album).getBytes(StandardCharsets.UTF_8),findBinaryBlock(fullPath,blankBlock)+3);
        setBytes(fullPath,("ARTIST="+singerName).getBytes(StandardCharsets.UTF_8),findBinaryBlock(fullPath,blankBlock)+3);

    }

    private static boolean equal(byte a,byte b){
        return a==b||Math.abs(a-b)==('a'-'A');
    }

    /**
     * 读取某一段数据
     * @param encryptFilePath
     * @param start
     * @param size
     * @return
     */
    private static byte[] readBytes(String encryptFilePath, long start, int size) {
        byte[] key = new byte[size];
        try {
            RandomAccessFile raf = new RandomAccessFile(encryptFilePath, "r");
            raf.seek(start);
            raf.readFully(key);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }

    private static boolean setBytes(String fullPath,byte[] bytes,long start){
        try {
            RandomAccessFile raf = new RandomAccessFile(fullPath, "rw");
            raf.seek(start);
            raf.write(bytes,0,bytes.length);
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int findBinaryBlock(String fullPath,byte[] bytes){
        byte[] cacheBlock=readBytes(fullPath,0,0x1000);
        for (int i=0;i<cacheBlock.length;i++){
            if(cacheBlock[i]==bytes[0]||(Math.abs(cacheBlock[i]-bytes[0])==0x20)){
                int curr=i;
                for (int j=0;j<bytes.length;j++){
                    if((Math.abs(cacheBlock[curr]-bytes[j])!=0x20&&cacheBlock[curr]!=bytes[j])){
                        curr++;
                        break;
                    }
                    curr++;
                }
                if(curr-i==bytes.length){
                    return i;
                }
            }
        }
        return -1;
    }

}

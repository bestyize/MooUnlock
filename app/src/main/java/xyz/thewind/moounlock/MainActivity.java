package xyz.thewind.moounlock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.PipedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xyz.thewind.moounlock.event.DecryptMsg;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private RecyclerView rvLocalFile;
    private TextView tvJoinQQ;
    private List<LocalFileBean> localFileBeanList=new ArrayList<>();
    private LocalFileAdapter localFileAdapter;
    private Set<String> musicPathSet=new HashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        initView();
        requestPermission();
        refreshData();
    }

    private void initView(){
        rvLocalFile=findViewById(R.id.rv_local_file);
        tvJoinQQ=findViewById(R.id.tvJoinQQ);
        tvJoinQQ.setOnClickListener(v -> joinQQGroup("YGr6qALm-Uccz7rTuZYHM46C0yNRESGi"));
        rvLocalFile.setLayoutManager(new LinearLayoutManager(this));
        localFileAdapter=new LocalFileAdapter(localFileBeanList);
        rvLocalFile.setAdapter(localFileAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_main_refresh:
                refreshData();
                return true;
            case R.id.menu_main_help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onFileStateChanged(String msg){
        refreshData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onFileDecryptFinished(DecryptMsg decryptMsg){
        Toast.makeText(getApplicationContext(),decryptMsg.msg,Toast.LENGTH_LONG).show();
    }


    private void refreshData(){
        localFileBeanList.clear();
        String path= Environment.getExternalStorageDirectory().getAbsolutePath() ;
        String mooPath=path+ File.separator+"blackkey"+File.separator+"download"+File.separator;
        String qqmusicPath=path+File.separator+"qqmusic"+File.separator+"song"+File.separator;
        String qqmusicCachePath=path+File.separator+"qqmusic"+File.separator+"cache"+File.separator;
        String mooCachePath=path+"/Android/data/com.tencent.blackkey/files/sd_card_migrated/audio_cache/";
        String wymusicCachePath=path+"/netease/cloudmusic/Cache/Music1/";
        musicPathSet.add(mooPath);
        musicPathSet.add(qqmusicPath);
        musicPathSet.add(qqmusicCachePath);
        musicPathSet.add(mooCachePath);
        musicPathSet.add(wymusicCachePath);

        for (String fold:musicPathSet){
            File folder=new File(fold);
            if(folder.exists()){
                File[] files=folder.listFiles();
                if(files!=null){
                    for (File file:files){
                        if(file.length()<0x500000||file.getName().contains(".m4a")){
                            continue;
                        }
                        LocalFileBean bean=new LocalFileBean(file.getName(),file.length(),file.lastModified(),file.getAbsolutePath());
                        localFileBeanList.add(bean);
                    }
                }
            }
        }
        Collections.sort(localFileBeanList, (o1, o2) -> o2.getFileName().compareTo(o1.getFileName()));
        localFileAdapter.notifyDataSetChanged();
    }

    private void requestPermission(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
        refreshData();
    }

    /****************
     *
     * 发起添加群流程。群号：IT技术交流群-二群(912159500) 的 key 为： YGr6qALm-Uccz7rTuZYHM46C0yNRESGi
     * 调用 joinQQGroup(YGr6qALm-Uccz7rTuZYHM46C0yNRESGi) 即可发起手Q客户端申请加群 IT技术交流群-二群(912159500)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    private void showHelp(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("帮助");
        builder.setMessage("本软件支持网易云音乐(听歌缓存)、QQ音乐、MOO音乐的加密和解密\n" +
                "文件路径：\n" +
                "1、blackkey/download\n" +
                "2、Android/data/com.tencent.blackkey/files/sd_card_migrated/audio_cache/\n"+
                "3、qqmusic/song\n" +
                "4、qqmusic/cache\n" +
                "5、netease/cloudmusic/Cache/Music1/\n"+
                "支持qmc、bkc、ele、mqcc、mflac、ogg、uc、1 文件解密，暂不支持ncm文件");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
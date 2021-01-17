package xyz.thewind.moounlock;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Date;
import java.util.List;

import xyz.thewind.moounlock.event.DecryptMsg;

public class LocalFileAdapter extends RecyclerView.Adapter<LocalFileAdapter.LocalFileViewHolder>{
    private List<LocalFileBean> localFileBeanList;
    private Context context;

    public LocalFileAdapter(List<LocalFileBean> localFileBeanList) {
        this.localFileBeanList = localFileBeanList;
    }

    @NonNull
    @Override
    public LocalFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(context==null){
            context=parent.getContext();
        }
        View view= LayoutInflater.from(context).inflate(R.layout.rv_item_local_file,parent,false);
        return new LocalFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalFileViewHolder holder, int position) {
        LocalFileBean fileBean=localFileBeanList.get(position);
        holder.tvFileName.setText(fileBean.getFileName());
//        if(fileBean.getFileName().endsWith("flac")){
//            holder.ivFileIcon.setImageResource(R.mipmap.flac);
//        }else if(fileBean.getFileName().endsWith("mp3")){
//            holder.ivFileIcon.setImageResource(R.mipmap.mp3);
//        }
        String time=new Date(fileBean.getCreateTime()).toLocaleString();

        holder.tvFileDesc.setText(formatFileSize(fileBean.getFileSize())+"\t"+time);

        if(fileBean.getFileSize()>0x5000&&!fileBean.getFileName().endsWith(".mp3")&&!fileBean.getFileName().endsWith(".flac")){
            holder.btnDecrypt.setVisibility(View.VISIBLE);
        }
        holder.btnDecrypt.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(FileDecrypt.decrypt(fileBean.getPath())){
                        String msg="解密成功,存放位置："+fileBean.getPath().substring(0,fileBean.getPath().lastIndexOf(File.separator));
                        EventBus.getDefault().post(new DecryptMsg(msg));
                    }else {
                        EventBus.getDefault().post(new DecryptMsg("解密失败"));
                    }
                    EventBus.getDefault().post("刷新："+fileBean.getFileName());
                }
            }).start();

        });
        holder.btnDelete.setOnClickListener(v -> {
            File file=new File(fileBean.getPath());
            file.delete();
            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post("删除文件："+fileBean.getFileName());
        });
    }

    @Override
    public void onViewRecycled(@NonNull LocalFileViewHolder holder) {
        super.onViewRecycled(holder);
        holder.btnDecrypt.setVisibility(View.GONE);
    }

    private String formatFileSize(long fileSize){
        if(fileSize<1024){
            return fileSize+"B";
        }else if(fileSize<1024*1024){
            return String.format("%.2f", (fileSize+0.0)/1024)+"KB";
        }else {
            return String.format("%.2f", (fileSize+0.0)/1024/1024)+"MB";
        }
    }

    @Override
    public int getItemCount() {
        return localFileBeanList.size();
    }

    static class LocalFileViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFileIcon;
        private TextView tvFileName;
        private TextView tvFileDesc;
        private Button btnDelete;
        private Button btnDecrypt;
        public LocalFileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileIcon=itemView.findViewById(R.id.iv_file_icon);
            tvFileName=itemView.findViewById(R.id.tv_file_name);
            tvFileDesc=itemView.findViewById(R.id.tv_file_desc);
            btnDelete=itemView.findViewById(R.id.btnDel);
            btnDecrypt=itemView.findViewById(R.id.btnDecrypt);

        }
    }
    private void openAssignFolder(String path){
        File file = new File(path);
        if(null==file || !file.exists()){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "file/*");
        try {
            context.startActivity(intent);
//            startActivity(Intent.createChooser(intent,"选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}

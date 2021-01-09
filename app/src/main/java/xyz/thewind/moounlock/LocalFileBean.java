package xyz.thewind.moounlock;

public class LocalFileBean {
    private String fileName;
    private long fileSize;
    private long createTime;
    private String path;

    public LocalFileBean() {
    }

    public LocalFileBean(String fileName, long fileSize, long createTime, String path) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.createTime = createTime;
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

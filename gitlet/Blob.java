package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.*;

/** Blob 文件类
 *  便于保存文件
 *  @author Skyss7
 */
public class Blob implements Serializable {

    /** 文件的唯一标识（SHA-1） */
    private final String uid;
    /** Blob 文件的内容 */
    private final String context;
    /** 绝对路径 */
    private final String FilePath;
    /** 保存文件的 File */
    private final File FileName;


    /** 初始化 Blob */
    public Blob(File f,String content) {
        context = content;
        uid = GetBlobName(f);
        FileName = GenerateBlobFile(f);
        FilePath = f.getAbsolutePath();
    }

    /** 返回任意文件的 SHA-1
     *  注意：静态 */
    public static String GetBlobName(File f) {
        return Utils.sha1(Utils.readContentsAsString(f) + f.getName());
    }

    /** 生成文件：生成 Blob 文件位置 */
    public File GenerateBlobFile(File file) {
        return Utils.join(Object_DIR, this.uid);
    }

    /** 保存文件 */
    public void SaveBlob() {
        Utils.writeObject(this.FileName, this);
    }

    public String getFilePath() {
        return FilePath;
    }

    public File getFileName() {
        return FileName;
    }

    public String getUid() {
        return uid;
    }

    public String getContext() {return context;}
}

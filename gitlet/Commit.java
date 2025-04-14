package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.Repository.*;

/** 提供一个 gitlet 提交对象
 *
 *  @author Skyss7
 */
public class Commit implements Serializable {
    /** List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.*/

    /** 这次提交的消息 */
    private String message;
    /** 提交的时间 */
    private Date TimeStamp;
    /** Blob 文件的 Map，key 为 track 文件的文件名，value 是其对应的 Blob 的 SHA-1 */
    private HashMap<String,String> BlobMap;
    /** 前一个 Commit 的指针（用文件id来找） */
    private String PreCommitID;
    /** 另一个父亲指针 */
    private String OtherPreCommitID;

    /** 创建一个Commit */
    public Commit(String m, Date t,String id) {
        message = m;
        TimeStamp = t;
        this.PreCommitID = id;
        BlobMap = new HashMap<String, String>();
    }

    public Commit(Commit parent) {
        this.message = parent.message;
        this.TimeStamp = parent.TimeStamp;
        this.PreCommitID = parent.PreCommitID;
        this.BlobMap = parent.BlobMap;
    }

    public void setMessage(String m) {
        this.message = m;
    }

    public void setTimeStamp(Date date) {
        this.TimeStamp = date;
    }

    public void setPreCommitID(String preCommitid) {
        this.PreCommitID = preCommitid;
    }

    public void setOtherPreCommitID(String otherPreCommitID) {
        this.OtherPreCommitID = otherPreCommitID;
    }

    public HashMap<String, String> getCommitBlobs() {
        return BlobMap;
    }

    public String getMessage() {
        return this.message;
    }

    public String getPreCommitID() {
        return this.PreCommitID;
    }

    public String getOtherPreCommitID() {
        return this.OtherPreCommitID;
    }

    /**往 BolbMap 中添加一对文件 */
    public void addBlobMap(String key, String value) {
        BlobMap.put(key, value);
    }


    /**往 BolbMap 中删除一对文件 */
    public void removeBlobMap(String key) {
        BlobMap.remove(key);
    }


    /**@return  Commit的SHA-1 */
    public String GetCommitSHA() {
        return sha1(PreCommitID, message, DateToString());
    }

    /** 保存 Commit，保存文件名为 GetCommitSHA() */
    public void SaveCommit() {
        String hash = GetCommitSHA();
        File FileCom = join(Commit_DIR, hash);
        writeObject(FileCom, this);
    }

    public String DateToString() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(TimeStamp);
    }


    /** 读取 HEAD 中的 Commit
     * @return Commit 类
     * */
    public static Commit GetHeadToCommit() {
        String[] temp = readContentsAsString(HEAD).split(":", 2);
        File com = join(Commit_DIR, temp[1]);
        Commit HeadCommit = readObject(com, Commit.class);
        return HeadCommit;
    }


    /** 对于给定的 6 位给定的SHA-1，查找有没有对应的 Commit
     *  @return 如果查找到的 Commit 文件，没有则 null
     *  @param sub 6 位 uid
     * */
    public static File CheckCommit(String sub) {
       List<String> commitFiles = plainFilenamesIn(Commit_DIR);

       if (commitFiles != null) {
           for (String filename : commitFiles) {
               if (sub.substring(0, 6).equals(filename.substring(0, 6))) {
                   return join(Commit_DIR, filename);
               }
           }
       }
       return null;
    }

    /** 迭代向上找祖先
     * @return 上一个 Commit 类
     * */
    public Commit GetpreCommit() {
        File temp = join(Commit_DIR, PreCommitID);
        Commit pre = readObject(temp, Commit.class);
        return pre;
    }

    public Commit GetpreOtherCommit() {
        File temp = join(Commit_DIR, OtherPreCommitID);
        Commit pre = readObject(temp, Commit.class);
        return pre;
    }

    /** 生成相应的文件*/
    public File SetCommitFile() {
        return Utils.join(Commit_DIR, this.GetCommitSHA());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Commit)) {
            return false;
        }

        Commit obj = (Commit) o;
        return this.SetCommitFile().equals(obj.SetCommitFile());
    }
}

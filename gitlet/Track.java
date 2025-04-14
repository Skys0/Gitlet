package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/**
 * 跟踪文件：暂存区与删除区，还有跟踪文件
 * @author Skyss7
 */
public class Track implements Serializable {

    /** 在追踪器的工作目录中记录文件 */
    private HashMap<String, String> Working;
    /** 记录文件的添加 */
    private HashMap<String, String> Added;
    /** 记录文件的删除  */
    private HashMap<String, String> remove;

    /**初始化*/
    public Track() {
        Added = new HashMap<String, String>();
        remove = new HashMap<String, String>();
        Working = new HashMap<String, String>();
    }


    /** 将当前状态下的文件副本添加到暂存区
     *  如果该文件当前在 Remove 中，则将其从 Remove 中删除
     * */
    public static void add(File file) {
        String filename = file.getName();
        String AddFileContent = Utils.readContentsAsString(file);
        Commit headCommit = Commit.GetHeadToCommit();

        // 读取 Head 中的 Commit，并比对
        HashMap<String, String> Blobs = headCommit.getCommitBlobs();
        List<String> stagingFiles = plainFilenamesIn(Staging_DIR);
        List<String> removingFiles = plainFilenamesIn(Remove_DIR);


        // 如果在 Blob 中这个文件被跟踪
        if (Blobs.containsKey(filename)) {
            String TrackerValue = Blobs.get(filename);
            String InsertValue = Blob.GetBlobName(file);
            // 两个文件（SHA-1相同=版本相同）
            if (TrackerValue.equals(InsertValue)) {
                if (removingFiles.contains(filename)) {
                    Utils.join(Remove_DIR, filename).delete();
                }
                return;
            }
        }

        Blob b = new Blob(file, AddFileContent);
        b.SaveBlob();

        // 写入文件到Staging_DIR：文件名为原本名，内容为 SHA-1(uid)
        File bolbfile = Utils.join(Staging_DIR, filename);
        Utils.writeContents(bolbfile, b.getUid());
    }

    /**  将该文件暂存为 Remove 状态
     * @return {@code true} 如果被追踪可以删除
     * @return {@code false} 如果既未暂存也未被 head 提交跟踪
     * */
    public static boolean remove(File file) {
        boolean flag = false;
        String filename = file.getName();
        Commit headCommit = Commit.GetHeadToCommit();
        HashMap<String, String> Blobs = headCommit.getCommitBlobs();

        File StageFile = Utils.join(Staging_DIR, filename);
        // 如果这个文件在暂存区内就删掉
        if (StageFile.exists()) {
            flag = true;
            StageFile.delete();
        }
        // 如果这个被跟踪了，我们去添加到暂存删除区中
        if (Blobs.containsKey(filename)) {
            File RemoveFile = join(Remove_DIR, filename);
            writeContents(RemoveFile, "");

            // 如果这文件还在工作区里，直接删除就行
            restrictedDelete(file);
            flag = true;
        }
        return flag;
    }

    /** 判断是否有未被追踪的文件
     *  @return {@code true}    有未被追踪的的文件
     *          {@code false}   没有未被追踪的文件
     * */
    public static boolean CheckUntrackFile() {
        List<String> workingFiles = plainFilenamesIn(CWD);
        HashMap<String, String> crackBlobs = Commit.GetHeadToCommit().getCommitBlobs();

        if (workingFiles != null) {
            for (String workFile : workingFiles) {
                if (!crackBlobs.containsKey(workFile)) {
                    return true;
                }
            }
        }

        return false;
    }
}
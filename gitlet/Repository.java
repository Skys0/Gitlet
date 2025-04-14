package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static gitlet.Utils.*;

/** 表示一个 Gitlet 版本控制系统仓库
 *  @author Skyss7
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** 工作目录 */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** .gitlet 目录. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Object 的目录 */
    public static final File Object_DIR = join(GITLET_DIR, "objects");
    /** 各种分支的目录 */
    public static final File Refs_DIR = join(GITLET_DIR, "refs");
    /** 其他分支的目录存放地 */
    public static final File Branch_DIR = join(Refs_DIR, "heads");
    /**Commit 中的目录 */
    public static final File Commit_DIR = join(Object_DIR, "commits");

    /** 分支 HEAD */
    public static final File HEAD = join(Refs_DIR, "HEAD");

    /** 索引对象，用于存储已添加文件、已移除文件和暂存文件的引用的目录 */
    public static final File Staging_DIR = join(GITLET_DIR, "staging");
    public static final File Remove_DIR = join(GITLET_DIR, "removing");


    /** 创建一个目录 */
    public static void CreateRepository() {
        ArrayList<File> Dirs = new ArrayList<File>(Arrays.asList(Refs_DIR, Object_DIR, Branch_DIR, Staging_DIR, Remove_DIR, Commit_DIR));
        for (File f : Dirs) {
            if(!f.exists()) {
                f.mkdir();
            }
        }

        // ToDo: 将 master 分支指向这个Commit
        Date temp = new Date(0);
        Commit Initize = new Commit("initial commit", temp, "");
        Initize.SaveCommit();
        // 将 HEAD 指向新的 Commit
        Branch.SaveHead("master", Initize.GetCommitSHA());
        Branch.saveBranch("master", Initize.GetCommitSHA());
    }

    /** 删除本地文件中的所有文件
     *  注意：必须慎重使用！！
     * */
    public static void removeCWDFiles() {
        List<String> workingFiles = plainFilenamesIn(CWD);

        if (workingFiles != null) {
            for (String workFile : workingFiles) {
                join(CWD, workFile).delete();
            }
        }
    }

    /** 查找两个暂存区有没有文件
     *  @return 是否有文件
     * */
    public static boolean ExistStageFile() {
        List<String> add = plainFilenamesIn(Staging_DIR);
        List<String> removing = plainFilenamesIn(Remove_DIR);
        return (!add.isEmpty() || !removing.isEmpty());
    }

    /** 给的字符串有没有空的
     *  @return 只要有一个为空，返回 true
     * */
    public static boolean CheckStringNull(String s1,String s2) {
        return (s1 == null || s2 == null);
    }

    /** 没查找到指令报错*/
    public static void NoCommand() {
        System.err.println("No command with that name exists.");
        System.exit(0);
    }

    /** 每个函数都要检查是否 init
     *  如果没有 .gitlet 就报错
     * */
    public static void CheckIfInit() {
        if (!GITLET_DIR.exists()) {
            System.err.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}

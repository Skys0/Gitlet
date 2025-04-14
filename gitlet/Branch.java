package gitlet;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** 提供一个分支，基本都是静态方法
 *  @author Skyss7
 */
public class Branch {


    /** 保存一个 Branch，内容为它所指的 Commit 的 id
     *  @param branchName 分支名
     *  @param commitId 保存的内容
     * */
    public static void saveBranch(String branchName, String commitId) {
        File f = join(Branch_DIR, branchName);
        writeContents(f, commitId);
    }


    /** 保存 HEAD 中有的分支和所指的 Commit,中间用 : 保存
     * @param HEADToCommitID HEAD 指向的 Commit 的文件名
     * @param branch 分支名
     * */
    public static void SaveHead(String branch, String HEADToCommitID) {
        writeContents(HEAD, branch + ":" +HEADToCommitID);
    }


    /** 从 HEAD 中获取分支名字
     *  @return 直接返回一个文件
     * */
    public static File getHeadBranch() {
        String[] temp = readContentsAsString(HEAD).split(":", 2);
        File branch = join(Branch_DIR, temp[0]);
        return branch;
    }


    /** 从这个分支里读取所指文件
     *  @return 返回 Commit 文件
     *  @param branch 分支文件
     * */
    public static File getBranchCommit(File branch) {
        if (!branch.exists())
            return null;
        String com = readContentsAsString(branch);
        File Point = join(Commit_DIR, com);
        return Point;
    }

    /**
     * 找到两个 Branch 的公共祖先
     * @param branch1 当前分支
     * @param branch2 给定分支
     * @return 公共祖先的文件
     */
    public static Commit GetBranchLCA(File branch1, File branch2) {
        Commit com1 = readObject(getBranchCommit(branch1), Commit.class);
        Commit com2 = readObject(getBranchCommit(branch2), Commit.class);
        Deque<Commit> deq1 = new ArrayDeque<Commit>();
        Deque<Commit> deq2 = new ArrayDeque<Commit>();
        Set<String> com1Ancestor = new HashSet<String>();
        deq1.add(com1);
        deq2.add(com2);
        com1Ancestor.add(com1.GetCommitSHA());

        while (!deq1.isEmpty()) {
            Commit temp1 = deq1.poll();
            if (temp1 != null) {
                if (!temp1.getPreCommitID().isEmpty()) {
                    com1Ancestor.add(temp1.GetpreCommit().GetCommitSHA());
                    deq1.add(temp1.GetpreCommit());
                }

                if (temp1.getOtherPreCommitID() != null) {
                    com1Ancestor.add(temp1.GetpreOtherCommit().GetCommitSHA());
                    deq1.add(temp1.GetpreOtherCommit());
                }
            }
        }

        while (!deq2.isEmpty()) {
            Commit temp2 = deq2.poll();
            if (temp2 != null) {
                if (com1Ancestor.contains(temp2.GetCommitSHA())) {
                    return temp2;
                }
                if (!temp2.getPreCommitID().isEmpty()) {
                    deq2.add(temp2.GetpreCommit());
                }

                if (temp2.getOtherPreCommitID() != null) {
                    deq2.add(temp2.GetpreOtherCommit());
                }
            }
        }
        return null;
    }
}

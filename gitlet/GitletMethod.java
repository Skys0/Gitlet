package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static java.util.Collections.sort;

/**
 * 表示 Gitlet 命令
 * @author Skyss7
 */
public class GitletMethod {


    /** 对应着 init 命令
     * Usage: java gitlet.Main init
     * */
    public static void init() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        } else{
            System.err.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        Repository.CreateRepository();
    }


    /** 对应着 add 命令
     * Usage: java gitlet.Main add [file name]
     * */
    public static void add(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect operands.");
            System.exit(0);
        }

        File infile = Utils.join(CWD, args[1]);
        if (!infile.exists()) {
            System.err.println("File does not exist.");
            System.exit(0);
        }

        Track.add(infile);
    }


    /** 对应 rm命令
     * Usage: java gitlet.Main rm [file name].
     * */
    public static void rm(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect operands.");
            System.exit(0);
        }

        File infile = Utils.join(CWD, args[1]);

        if (!Track.remove(infile)) {
            System.err.println("No reason to remove the file.");
            System.exit(0);
        }
    }


    /**对应 Commit 命令
     * Usage: java gitlet.Main commit [message]
     * */
    public static void commit(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect operands.");
            System.exit(0);
        }

        if (args[1].equals("")) {
            System.err.println("Please enter a commit message.");
            System.exit(0);
        }

        // 获取*现在*的 Commit 与现在要提交的 Commit 逐一比较再对比更改
        Commit oldCommit = Commit.GetHeadToCommit();
        Commit newCommit = new Commit(oldCommit);
        newCommit.setMessage(args[1]);
        newCommit.setTimeStamp(new Date());
        newCommit.setPreCommitID(oldCommit.GetCommitSHA());

        // 读取两个暂存区文件，并比对
        List<String> StagingFile = plainFilenamesIn(Staging_DIR);
        List<String> RemoveFile = plainFilenamesIn(Remove_DIR);

        if (StagingFile.isEmpty() && RemoveFile.isEmpty()) {
            System.err.println("No changes added to the commit.");
            System.exit(0);
        }

        int cnt = 0;
        for (String stag : StagingFile) {
            File temp = join(Staging_DIR, stag);
            String idFile = readContentsAsString(temp);
            newCommit.addBlobMap(stag, idFile);
            temp.delete();
            cnt += 1;
        }

        for (String rem : RemoveFile) {
            File temp = join(Remove_DIR, rem);
            String idFile = readContentsAsString(temp);
            newCommit.removeBlobMap(rem);
            temp.delete();
            cnt += 1;
        }

        if (cnt == 0) {
            System.err.println("No changes added to the commit.");
            System.exit(0);
        }

        // 保存至 HEAD
        newCommit.SaveCommit();
        Branch.SaveHead(Branch.getHeadBranch().getName(), newCommit.GetCommitSHA());
        Branch.saveBranch(Branch.getHeadBranch().getName(), newCommit.GetCommitSHA());
        // 这里两个都得保存，不然在移动分支的时候会回到 init 的时候
    }

    /**对应 Merge 中的自动 Commit
     * */
    public static void MergeCommit(String message, Commit headCommit,Commit otherCommit,Date times) {
        Commit newCommit = new Commit(headCommit);
        newCommit.setMessage(message);
        newCommit.setTimeStamp(times);
        newCommit.setPreCommitID(headCommit.GetCommitSHA());
        newCommit.setOtherPreCommitID(otherCommit.GetCommitSHA());

        List<String> stagingFile = plainFilenamesIn(Staging_DIR);
        List<String> rmFile = plainFilenamesIn(Remove_DIR);

        if (stagingFile != null) {
            for (String stag : stagingFile) {
                File temp = join(Staging_DIR, stag);
                String idFile = readContentsAsString(temp);
                newCommit.addBlobMap(stag, idFile);
                temp.delete();
            }
        }

        if (rmFile != null) {
            for (String rem : rmFile) {
                File temp = join(Remove_DIR, rem);
                String idFile = readContentsAsString(temp);
                newCommit.removeBlobMap(rem);
                temp.delete();
            }
        }
        newCommit.SaveCommit();
        Branch.SaveHead(Branch.getHeadBranch().getName(), newCommit.GetCommitSHA());
        Branch.saveBranch(Branch.getHeadBranch().getName(), newCommit.GetCommitSHA());
    }


    /** 对应着 log 命令
     * Usage: java gitlet.Main log
     * */
    public static void printLog() {
        Commit points = Commit.GetHeadToCommit();
        while (points != null) {
            System.out.println("===");
            System.out.println("commit " + points.GetCommitSHA());
            System.out.println("Date: " + points.DateToString());
            System.out.println(points.getMessage());
            System.out.print("\n");

            // 找下一个 Commit
            if (points.getPreCommitID().isEmpty())      break;

            File nextFile = join(Commit_DIR, points.getPreCommitID());
            points = readObject(nextFile, Commit.class);
        }
    }

    /** 对应着 global-log 命令
     * 区别于 log 命令，它是输出所有的 Commit
     * Usage: java gitlet.Main global-log
     * */
    public static void printGlobalLog() {
        List<String> allCommitFile = plainFilenamesIn(Commit_DIR);

        // 查找所有 Commit 文件
        if (allCommitFile != null) {
            for (String its : allCommitFile) {
                File temp = join(Commit_DIR, its);
                Commit t = readObject(temp, Commit.class);

                System.out.println("===");
                System.out.println("commit " + t.GetCommitSHA());
                System.out.println("Date: " + t.DateToString());
                System.out.println(t.getMessage());
                System.out.print("\n");
            }
        }
    }


    /** 对应 Find 命令
     *  找到相应 message 的 Commit，并且输出 id
     *  Usage: java gitlet.Main find [commit message]
     * */
    public static void Find(String[] args) {
        List<String> allCommit = plainFilenamesIn(Commit_DIR);
        String checkMessage = args[1];
        int cnt = 0;


        // 在文件夹中找有没有对应的
        if (allCommit != null) {
            for (String its : allCommit) {
                File temp = join(Commit_DIR, its);
                Commit c = readObject(temp, Commit.class);

                if(checkMessage.equals(c.getMessage())) {
                    System.out.println(c.GetCommitSHA());
                    cnt += 1;
                }
            }
        }

        // 如果没有，就报错
        if (cnt == 0) {
            System.err.println("Found no commit with that message.");
            System.exit(0);
        }
    }


    /** 对应 status 命令
     *  显示当前存在的分支，暂存添加，暂存删除的文件
     *  Usage: java gitlet.Main status
     * */
    public static void status() {
        System.out.println("=== Branches ===");
        File Head = Branch.getHeadBranch();
        System.out.println("*" + Head.getName());

        // 查找所有不为当前所在的分支
        List<String> checkBranch = plainFilenamesIn(Branch_DIR);
        if (checkBranch != null) {
            for (String s : checkBranch) {
                File temp = join(Branch_DIR, s);
                if (!temp.equals(Head)) {
                    System.out.println(temp.getName());
                }
            }
        }
        System.out.print("\n");
        System.out.println("=== Staged Files ===");

        // 输出所有被暂存添加的文件
        List<String> stagingFiles = plainFilenamesIn(Staging_DIR);
        if (stagingFiles != null) {
            for (String s : stagingFiles) {
                System.out.println(s);
            }
        }
        System.out.print("\n");
        System.out.println("=== Removed Files ===");

        // 输出所有被暂存删除的文件
        List<String> removeFiles = plainFilenamesIn(Remove_DIR);
        if (removeFiles != null) {
            for (String s : removeFiles) {
                System.out.println(s);
            }
        }
        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> outputFiles = new ArrayList<String>();
        HashMap<String,String> blobs = Commit.GetHeadToCommit().getCommitBlobs();
        List<String> CWDFile = plainFilenamesIn(CWD);

        // 在当前提交中跟踪，在工作目录中更改，但未暂存
        for (String f : CWDFile) {
            if (blobs.containsKey(f)) {
                File temp = join(CWD, f);
                File t = join(Staging_DIR, f);
                if (!Blob.GetBlobName(temp).equals(blobs.get(f)) && !t.exists()) {
                    outputFiles.add(f + "(modified)");
                }
            }
        }
        // 暂存以进行添加，但内容与工作目录中的内容不同
        // 暂存以进行添加，但在工作目录中删除
         for (String f : stagingFiles) {
             File temp = join(CWD, f);
             File t = join(Staging_DIR, f);
             if (!temp.exists()) {
                 outputFiles.add(f + "(deleted)");
             }
             if (!Blob.GetBlobName(temp).equals(readContentsAsString(t))) {
                 outputFiles.add(f + "(modified)");
             }
         }
         // 不是暂存以供删除，而是在当前提交中跟踪并从工作目录中删除
        for (String f : blobs.keySet()) {
            File temp = join(CWD, f);
            if (!temp.exists() && !removeFiles.contains(f)) {
                outputFiles.add(f + "(deleted)");
            }
        }
        // 排序后输出
        sort(outputFiles);
        for (String s : outputFiles) {
            System.out.println(s);
        }

        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        for (String f : CWDFile) {
            if (!blobs.containsKey(f) && !removeFiles.contains(f) && !stagingFiles.contains(f)) {
                System.out.println(f);
            }
        }
        System.out.print("\n");
    }

    /**对应着 checkout 命令
     * 一共有三种可能，分别是，直接有 --，中间接着一个 --，没有 --
     * Usages:
     * 1. java gitlet.Main checkout -- [file name]
     * 2. java gitlet.Main checkout [commit id] -- [file name]
     * 3. java gitlet.Main checkout [branch name]
     * */
    public static void checkout(String[] args) {
        // 对应第一种参数
        // 获取 head commit 中存在的文件版本，并将其放入工作目录中，覆盖已经存在的文件版本（如果有）。文件的新版本不会暂存。
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.err.println("Incorrect operands.");
                System.exit(0);
            }

            Commit headCommit = Commit.GetHeadToCommit();
            HashMap<String, String> perviousBlobs = headCommit.getCommitBlobs();
            File recoverFile = join(CWD, args[2]);
            // 如果我们恢复的文件存在
            if (perviousBlobs.containsKey(args[2])) {
                String id = perviousBlobs.get(args[2]);
                File remain = join(Object_DIR, id);
                Blob temp = readObject(remain, Blob.class);
                writeContents(recoverFile, temp.getContext());
            } else {
                System.err.println("File does not exist in that commit.");
                System.exit(0);
            }
        }

        // 对应第二种参数
        // 获取提交中具有给定 ID 的文件版本，并将其放入工作目录中，覆盖已经存在的文件版本（如果有）。文件的新版本不会暂存。
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.err.println("Incorrect operands.");
                System.exit(0);
            }

            // 首先找到 Commit 存不存在
            File checkFile = Commit.CheckCommit(args[1]);
            if (checkFile == null) {
                System.err.println("No commit with that id exists.");
                System.exit(0);
            }

            // 如果存在，那么查找对应文件恢复
            File recoverFile = join(CWD, args[3]);
            Commit checkCommit = readObject(checkFile, Commit.class);
            HashMap<String, String> Blobs = checkCommit.getCommitBlobs();

            if (Blobs.containsKey(args[3])) {
                File t = join(Object_DIR, Blobs.get(args[3]));
                Blob temp = readObject(t, Blob.class);
                writeContents(recoverFile, temp.getContext());
            } else {
                System.err.println("File does not exist in that commit.");
                System.exit(0);
            }
        }

        if (args.length == 2) {
            checkoutBranch(args[1]);
        }
    }

    /**针对 checkout branch 的处理
     * @param branchName 分支的名字
     * */
    public static void checkoutBranch(String branchName) {
        File branch = join(Branch_DIR, branchName);
        File branchFile = Branch.getBranchCommit(branch);

        // 如果不存在具有该名称的分支
        if (branchFile == null) {
            System.err.println("No such branch exists.");
            System.exit(0);
        }

        // 如果该分支是当前分支
        if (branch.equals(Branch.getHeadBranch())) {
            System.err.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit headCommit = readObject(branchFile, Commit.class);
        HashMap<String, String> Blobs = headCommit.getCommitBlobs();

        // 如果有文件没被追踪，报错
        if (Track.CheckUntrackFile()) {
            System.err.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        removeCWDFiles();

        // 每一个文件恢复
        for (String s : Blobs.keySet()) {
            File recoverFile = join(CWD, s);
            Blob temp = readObject(join(Object_DIR, Blobs.get(s)), Blob.class);
            writeContents(recoverFile, temp.getContext());
        }

        Branch.SaveHead(branchName, headCommit.GetCommitSHA());
    }


    /**对应 Branch 命令
     * 创建一个引用，创建一个新分支，指向新 HEAD
     * Usage: java gitlet.Main branch [branch name]
     * */
    public static void CreateBranch(String[] args) {
        String name = args[1];
        File newBranch = join(Branch_DIR, name);

        if (newBranch.exists()) {
            System.err.println("A branch with that name already exists.");
            System.exit(0);
        }

        Branch.saveBranch(name, Commit.GetHeadToCommit().GetCommitSHA());
    }

    /** 对应 rm-branch 命令
     *  删除具有给定名称的分支
     *  Usage: java gitlet.Main rm-branch [branch name]
     * */
    public static void rmBranch(String[] args) {
        File branch = join(Branch_DIR, args[1]);
        if (!branch.exists()) {
            System.err.println("A branch with that name does not exist.");
            System.exit(0);
        }

        if (branch.equals(Branch.getHeadBranch())) {
            System.err.println("Cannot remove the current branch.");
            System.exit(0);
        }

        branch.delete();
    }

    /** 对应 reset 命令
     *  Usage: java gitlet.Main reset [commit id]
     * */
    public static void reset(String[] args) {
        File f = Commit.CheckCommit(args[1]);
        if (f == null) {
            System.err.println("No commit with that id exists.");
            System.exit(0);
        }

        // 遍历整个 Commit 中的文件，一个个恢复
        Commit commit = readObject(f, Commit.class);
        HashMap<String, String> Blobs = commit.getCommitBlobs();
        HashMap<String, String> HeadBlobs = Commit.GetHeadToCommit().getCommitBlobs();

        // 总的查一遍有没有没被跟踪的，但是要被覆盖的
        for (String recoverFileName : Blobs.keySet()) {
            File cwdfile = join(CWD, recoverFileName);
            if (!HeadBlobs.containsKey(recoverFileName) && cwdfile.exists()) {
                System.err.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

        }
        removeCWDFiles();

        for (String recoverFileName : Blobs.keySet()) {
            File recoverFile = join(CWD, recoverFileName);
            Blob temp = readObject(join(Object_DIR, Blobs.get(recoverFileName)), Blob.class);

            writeContents(recoverFile, temp.getContext());
        }

        // 删除暂存区域
        List<String> stageFile = plainFilenamesIn(Staging_DIR);
        List<String> removeFile = plainFilenamesIn(Remove_DIR);
        for (String temp : stageFile) {
            join(Staging_DIR, temp).delete();
        }
        for (String temp : removeFile) {
            join(Remove_DIR, temp).delete();
        }

        // 同时将其 branchHEAD 指向 commit
        Branch.saveBranch(Branch.getHeadBranch().getName(), commit.GetCommitSHA());
        // 将目前给定的 HEAD 指针指向这个 commit
        Branch.SaveHead(Branch.getHeadBranch().getName(), commit.GetCommitSHA());
    }


    /** 对应 merge 命令
     *
     *  Usage: java gitlet.Main merge [branch name]
     * */
    public static void merge(String[] args) {
        if (ExistStageFile()) {
            System.err.println("You have uncommitted changes.");
            System.exit(0);
        }
        File branch1 = Branch.getHeadBranch();
        File branch2 = join(Branch_DIR, args[1]);

        if (!branch2.exists()) {
            System.err.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch1.equals(branch2)) {
            System.err.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (Track.CheckUntrackFile()) {
            System.err.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }


        // LCA 最近公共祖先（分歧点），currCommit 为当前分支，givenCommit 为给定分支
        Commit LCA = Branch.GetBranchLCA(branch1, branch2);
        Commit currCommit = Commit.GetHeadToCommit();
        Commit givenCommit = readObject(Branch.getBranchCommit(branch2), Commit.class);

        // 分歧点与给定分支相同
        if (LCA.equals(givenCommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        // 分歧点与当前分支相同
        if (LCA.equals(currCommit)) {
            // 模拟直接 checkout
            String[] simulationArgs = {"checkout", args[1]};
            checkout(simulationArgs);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        // 分歧点既不是当前分支也不是给定分支
        HashMap<String, String> LCABlobs = LCA.getCommitBlobs();
        HashMap<String, String> currBlobs = currCommit.getCommitBlobs();
        HashMap<String, String> givenBlobs = givenCommit.getCommitBlobs();

        // 先对 LCA 的文件进行遍历
        for (String lcaFile : LCABlobs.keySet()) {
            String lcaVersion = LCABlobs.get(lcaFile);
            String currVersion = currBlobs.get(lcaFile);
            String givenVersion = givenBlobs.get(lcaFile);

            // 存在于LCA，在给定分支中未修改且在当前分支中不存在的文件：不变
            // 两者均被修改，两个分支版本不同：冲突
            if (givenVersion != null && currVersion != null && !lcaVersion.equals(currVersion)
                    && !lcaVersion.equals(givenVersion) && !currVersion.equals(givenVersion)) {
                DealWithConflict(currCommit, givenCommit, lcaFile);
            }

            // 给定的分支修改过，当前分支未修改：先 checkout，在 add
            if (givenVersion != null && !lcaVersion.equals(givenVersion) && lcaVersion.equals(currVersion)) {
                String[] simulateArgs = {"checkout", givenCommit.GetCommitSHA(), "--", lcaFile};
                checkout(simulateArgs);
                simulateArgs = new String[]{"add", lcaFile};
                add(simulateArgs);
            }
            // 有一个被删除的情况我们下面来讨论
        }

        // 对所有当前分支的文件进行操作
        for (String currFile : currBlobs.keySet()) {
            String lcaVersion = LCABlobs.get(currFile);
            String currVersion = currBlobs.get(currFile);
            String givenVersion = givenBlobs.get(currFile);
            // 当前分支未修改，给定分支不存在
            if (givenVersion == null && currVersion.equals(lcaVersion)) {

                String[] simulateArgs = {"rm", currFile};
                rm(simulateArgs);
            }
            // 当前分支修改了，给定分支不存在：冲突
            if (givenVersion == null && lcaVersion != null && !currVersion.equals(lcaVersion)) {
                DealWithConflict(currCommit, givenCommit, currFile);
            }
            // 在拆分点不存在，但是两个版本不相同：冲突
            if (lcaVersion == null && givenVersion != null && !currVersion.equals(givenVersion)) {
                DealWithConflict(currCommit, givenCommit, currFile);
            }
        }

        for (String givenFile : givenBlobs.keySet()) {
            String lcaVersion = LCABlobs.get(givenFile);
            String currVersion = currBlobs.get(givenFile);
            String givenVersion = givenBlobs.get(givenFile);
            // 在拆分点不存在且仅存在于给定分支中的文件都应该被签出并暂存
            if (lcaVersion == null && currVersion == null) {
                String[] simulateArgs = {"checkout", givenCommit.GetCommitSHA(), "--", givenFile};
                checkout(simulateArgs);
                simulateArgs = new String[]{"add", givenFile};
                add(simulateArgs);
            }
            // 当前分支不存在，给定分支修改了：冲突
            if (currVersion == null && lcaVersion != null && !givenVersion.equals(lcaVersion)) {
                DealWithConflict(currCommit, givenCommit, givenVersion);
            }
        }
        Date times = new Date();
        String message = String.format("Merged %s into %s.", branch2.getName(), branch1.getName());
        MergeCommit(message, currCommit, givenCommit, times);
    }


    /** 处理文件冲突
     * @param headCommit 当前分支所指的提交
     * @param otherHeadCommit 给定分支所指的提交
     * @param trackConflictName 需要解决冲突的文件
     *  */
    public static void DealWithConflict(Commit headCommit, Commit otherHeadCommit,
                                        String trackConflictName) {
        HashMap<String, String> headBlobs = headCommit.getCommitBlobs();
        HashMap<String, String> otherBlobs = otherHeadCommit.getCommitBlobs();

        String headFileContext = "";
        String headBlob = "";
        String otherFileContext = "";
        String otherBlob = "";

        System.out.println("Encountered a merge conflict.");

        // 从 HEAD 中获取文件内容
        if (headBlobs.containsKey(trackConflictName)) {
            headBlob = headBlobs.get(trackConflictName);
            Blob temp = readObject(join(Object_DIR, headBlob), Blob.class);
            headFileContext = temp.getContext();
        }

        // 从其他文件中获取文件内容
        if (otherBlobs.containsKey(trackConflictName)) {
            otherBlob = otherBlobs.get(trackConflictName);
            Blob temp = readObject(join(Object_DIR, otherBlob), Blob.class);
            otherFileContext = temp.getContext();
        }

        StringBuilder inputString = new StringBuilder();
        inputString.append("<<<<<<< HEAD\n");
        inputString.append(headFileContext);
        inputString.append("=======\n");
        inputString.append(otherFileContext);
        inputString.append(">>>>>>>\n");

        // 将冲突写入文件，并暂存结果
        writeContents(join(CWD, trackConflictName), inputString.toString());
        String[] simulationArgs = new String[]{"add", trackConflictName};
        add(simulationArgs);
    }
}
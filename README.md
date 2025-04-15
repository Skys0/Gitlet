# Gitlet 

这是针对CS61B: Data Structures, Spring 2021版本的gitlet构建，Java 版本控制系统 Git 的独立实现。Gitlet 支持 Git 的大多数本地功能：添加、提交、签出、日志，以及分支操作，包括 merge 和 rebase

### **Name**: Skys0

### 文件储存

`.gitlet` 是gitlet 主文件，之后储存的所有东西都在里面

```
.gitlet
├─ refs	<------------- 所有的引用
│  ├─ HEAD <---------- 头部指针，也就是当前所在的分支
│  └─ heads <--------- 储存着所有分支，比如说 master...
│     ├─ master
│	  └─ ....other
└─ objects <---------- 储存着所有的对象文件
   └─ commits <------- 专门储存 Commit 对象的文件的
```

### 储存文件 : Blob 类

#### 参数

`uid`：文件的唯一表示，用 SHA-1 方法保证了不一样的文件内容

`context` ：跟踪的文件的内容

`FilePath`：文件的绝对路径

`FileName` ：文件的名字

#### 重要的方法

`SaveBlob`：保存文件

`GetBlobName`：**静态**，传入一个文件，给出文件的SHA-1

### 储存提交：Commit 类

#### 参数

`message`：这次提交的信息

`TimeStamp`：提交的时间

`BlobMap`：Blob 文件的字典，`key`为被跟踪文件的文件名，`value`是其对应的 Blob 的 SHA-1，也是对应的文件名

`PreCommitID`，`OtherPreCommitID`：父亲 Commit 的指针和另一个父亲指针，储存的文件名

#### 重要的方法

`SaveCommit`：保存 Commit 文件

`CheckCommit`：对于给定的 $6$ 位给定的SHA-1，查找有没有对应的 Commit，有就返回文件，没有就返回 null

### 操作分支：Branch 类

这个类里面的所有方法都是静态类。

对于 `HEAD` 文件，我们储存了例如 `master:129ef320fedb09fbj` 这样的内容，前面代表 HEAD 指针代表的分支，后面表示所指的提交为文件名。

`heads` 文件夹中各个分支文件中储存了所指提交的文件名。

#### 重要的方法

`saveBranch`，`SaveHead`：保存分支，以及保存HEAD

`GetBranchLCA`：找到两个提交的**最近**的公共祖先，整个提交有可能是一个图，可能会有多个祖先。

### Repository 类

定义了许多文件路径，这里不再说明

#### 重要的方法

`CreateRepository`：创建 .gitlet 目录

`removeCWDFiles`：删除本地文件中的所有文件，**注意：必须慎重使用**

`ExistStageFile`：查找两个暂存区有没有文件。如果有返回 true，没有返回 false

 `CheckIfInit`：检查是否 init

### GitletMethod：Gitlet 主要命令执行

`init()` 对应 init 命令

* `java gitlet.Main init`

`add(String[] args)` 对应 add 命令

* `java gitlet.Main add [file name]`

`rm(String[] args)` 对应着 rm 命令：

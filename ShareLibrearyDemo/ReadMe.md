### 共享库的目录结构:
```
(root)
+- src                     # Groovy source files
|   +- org
|       +- foo
|           +- Bar.groovy  # for org.foo.Bar class
+- vars
|   +- foo.groovy          # for global 'foo' variable
|   +- foo.txt             # help for 'foo' variable
+- resources               # resource files (external libraries only)
|   +- org
|       +- foo
|           +- bar.json    # static helper data for org.foo.Bar

```

- src 目录应该看起来像标准的 Java 源目录结构。当执行流水线时，该目录被添加到类路径下。

- vars目录承载了在管道中作为变量公开的脚本文件。文件名是管道中变量的名称。所以，如果你有一个名为vars/log.groovy的文件，它有一个类似def info（message）的函数…​ 在其中，您可以像Pipeline中的log.info“hello world”一样访问此函数。您可以在这个文件中放入任意多的函数。 Jenkins 共享库里的 var 目录下的文件有这些规矩和特点：
简单来说，这段话说的是 Jenkins 共享库里的 `var` 目录下的文件有这些规矩和特点：

1. **Groovy 文件命名**：每个 `.groovy` 文件的文件名得像 Java 或 Groovy 里的变量名那样（比如用驼峰式命名，像 `myTool.groovy`），不能随便起。

2. **配套的文档文件**：如果有和 `.groovy` 同名的 `.txt` 文件（比如 `myTool.txt`），这个文件可以写文档，支持 HTML、Markdown 等格式（但扩展名必须是 `.txt`）。

3. **文档啥时候能看到**：这些文档只有在使用了这个共享库的 Pipeline 任务里，通过侧边栏的“全局变量参考”页面才能看到。而且，这些任务必须成功运行过一次，文档才会生成出来。

4. **Groovy 文件的处理方式**：这个目录下的 Groovy 代码，会和“脚本式 Pipeline”里的代码一样，经过 Jenkins 的“CPS 转换”（一种特殊处理，让代码能在 Pipeline 里正常运行）。

- resources目录允许从外部库使用libraryResource步骤来加载相关的非Groovy文件。目前，内部库(可能指 Jenkins 内置的库或者特定场景下的内部库)不支持此功能。

- 根目录下的其他目录保留用于未来的增强。

---

### 定义共享库:
* 全局共享库：Manage Jenkins » Configure System » Global Pipeline Libraries, 这些库被认为是 "可信的:"他们可以在 Java，Groovy, Jenkins 内部 API, Jenkins 插件, 或第三方库中运行任何方法。 
* 文件夹级别的共享库： 创建的任何文件夹都可以有一个与之关联的共享库。该机制允许将特定库的范围扩展到文件夹或子文件夹的所有流水线。基于文件夹的库不被认为是 "可信的:" 他们在 Groovy 沙箱中运行，就像典型的流水线一样。
* 自动分享库：其他插件可能会提供一种 “临时定义共享库” 的方式。比如有个叫 “Pipeline: GitHub Groovy Libraries” 的插件，它能让你的 Pipeline 脚本直接使用类似 “github.com/someorg/somerepo” 这样的未经过信任配置的库，而且不需要做任何额外的配置。这种情况下，插件会自动加载指定的 GitHub 仓库，默认拉取 master 分支的内容，并且是以匿名（不登录）的方式去获取代码。

---

### 使用库:
* 配置共享库时如果勾选"Load implicitly", 这个共享库会被自动加载到所有 Pipeline 任务中，不需要在 Pipeline 脚本里手动用 @Library 注解去引入。

* 使用 @Library 注解, 该注解可以在脚本的 Groovy 允许注解的任何地方
```
@Library('my-shared-library') _

/* Using a version specifier, such as branch, tag, etc */
@Library('my-shared-library@1.0') _

/* Accessing multiple libraries with one statement (一条声明语句加载多个共享库)/
@Library(['my-shared-library', 'otherlib@abc1234']) _

/*  When referring to class libraries (with src/ directories), conventionally the annotation goes on an import statement: (当引用类库（使用src/目录）时，通常注释会出现在import语句上) */
@Library('somelib')
import com.mycorp.pipeline.somelib.UsefulClass


@Library('my-shared-library') _  和  @Library('my-shared-library')  有什么区别
1. 带 _ 的写法是 “加载整个库并让所有内容可用”，适合用共享库中的全局方法或变量。
2. 不带 _ 的写法需要配合 import 语句，适合精确导入库中的特定类。
3. 如果共享库只定义了 vars/ 目录下的全局变量（比如一些可直接调用的方法），或者你的 Pipeline 脚本只需要用这些全局变量，那么用带下划线 _ 的这种写法更简洁。这里的 _ 相当于一个 “占位符”，避免了写一个不必要的 import 语句，让代码更干净。
4. 不要去 “导入” 共享库里的全局变量或函数（比如用 import 语句显式引入）。
因为这样做会迫使编译器把这些本可以灵活定制的字段和方法当成 “静态的”（固定不变的）来处理，可能导致 Groovy 编译器报出一些让人 confusion 的错误信息
```


* 动态加载库: 
从Pipeline:Shared Groovy Libraries插件的2.7版本开始，有一个在脚本中加载（非隐式）库的新选项，在构建过程中的任何时候动态加载库的库步骤。
```
/* 如果你只对使用全局变量/方法 (从 vars/ 目录)感兴趣, 可以使用如下写法, 此后该脚本可以访问该库的任何全局变量。*/
library 'my-shared-library'


/* 如果想动态加载库中"src/" 目录里的类比较麻烦。不能像平常写代码那样，在脚本开头用 import 来直接使用这些类。因为脚本在运行到你的库步骤之前，就已经编译好了，它“不认识”你的库。
可以通过动态调用来使用它们。先通过 library  步骤加载你的库。然后使用这个库步骤返回的对象，通过完整的类名来访问里面的类和方法。简单总结：不能提前“引入”库里的类，但可以在运行时通过“全路径名”来“找到”并使用它们。 */
library('my-shared-library').com.mycorp.pipeline.Utils.someStaticMethod()

// 您还可以访问静态字段，并调用构造函数，就像它们是名为new的静态方法一样：
def useSomeLib(helper) { 		// 不能写成 Helper helper 形式, Helper 类通过 library 步骤动态加载, 由于脚本编译时还不认识 Helper 这个类. 只能用 def 这种动态类型声明参数，运行时再根据实际传入的对象来处理
    helper.prepare()
    return helper.count()
}

def lib = library('my-shared-library').com.mycorp.pipeline 		// src/com/mycorp/pipeline/ 里面放的才是具体的 .groovy 类文件

// Helper 是类名,对应的文件是 Helper.groovy , 在 Groovy 中，new 类名() 和 类名.new() 这两种写法效果完全相同. 类名.new() 是 Groovy 特有的语法糖(简化写法),Java中不支持
echo useSomeLib(lib.Helper.new(lib.Constants.SOME_TEXT))
```

---

### 库版本:
#### 当共享库配置了 “默认版本” 时，以下两种情况会自动使用这个版本：
- 勾选了 “Load implicitly（隐式加载）” 时（库会被自动加载）；
- Pipeline 脚本中只通过库名引用共享库时，比如 @Library('my-shared-library') _（没指定具体版本）。
- 如果没设置 “默认版本”，那 Pipeline 必须在引用时明确指定版本，比如 @Library('my-shared-library@master') _（@master 就是版本，通常是分支名）。

#### 如果在共享库配置中开启了 “Allow default version to be overridden（允许覆盖默认版本）”，那么：
- 即使设置了默认版本，Pipeline 也可以在 @Library 注解中指定其他版本来覆盖它，比如默认版本是 main，但脚本里可以写 @Library('my-shared-library@dev') _ 来用 dev 版本；
- 对于 “隐式加载” 的库，也能根据需要加载其他版本（原本隐式加载会用默认版本，开启后可以临时换版本）。
- 总结来说：默认版本是共享库的 “默认选项”，但可以通过配置和脚本指定来灵活替换，具体能否替换取决于是否开启了 “允许覆盖” 的开关。

#### 当使用 library 步骤你也可以指定一个版本:   library 'my-shared-library@master'

#### library 步骤支持动态版本（可以通过代码算出版本号），而 @Library 注解只能用固定的版本字符串。 如: library "my-shared-library@$BRANCH_NAME"

#### （使用 library 步骤时）可以加载与 “多分支流水线（multibranch）的 Jenkinsfile 所在的代码分支相同” 的共享库分支。
- 举个例子：如果你的多分支流水线中，当前运行的是 dev 分支的 Jenkinsfile，那么可以让共享库也自动加载它自己的 dev 分支（保持分支一致）
- 再举一个例子：你还可以通过 “参数” 来选择要加载的共享库版本，比如让用户在触发流水线时手动选择用哪个版本的共享库（而不是写死的固定版本）。
- 简单说：library 步骤很灵活，既多种方式指定版本 —— 可以和当前流水线分支保持一致，也可以通过参数动态选择，不用局限于写死的版本号。
- 注意，库步骤不能用于覆盖隐式加载的库的版本。在脚本启动时，它已经加载，给定名称的库可能不会加载两次。
```
properties([parameters([string(name: 'LIB_VERSION', defaultValue: 'master')])])
library "my-shared-library@${params.LIB_VERSION}"
```

---

### 库的检索方法:

* Modern SCM:(现代 SCM)支持大多数主流的源码管理工具(如 Git、Subversion 等).配置界面更简洁,与 Jenkins 整体的 SCM 配置风格一致;支持更多现代 SCM 功能(如分支过滤、凭据管理等);推荐此方式,兼容性和维护性更好
* Legacy SCM:(传统 SCM)早期 Jenkins 使用的 SCM 配置方式,主要为了兼容一些旧的插件或特殊场景。配置界面相对繁琐，不同 SCM 工具的配置入口可能分散；功能较少，可能不支持现代 SCM 的高级特性；仅在现代 SCM 无法满足需求时(如兼容旧系统)才考虑。
* 动态检索: 不需要在Jenkins中预定义库,但必须指定库版本。
```
library identifier: 'custom-lib@master', retriever: modernSCM(
  [$class: 'GitSCMSource',
   remote: 'git@git.mycorp.com:my-jenkins-utils.git',
   credentialsId: 'my-private-key'])
```

---

Jenkins 中**共享库（Library）变更的预测试方法**，主要围绕 `Replay`（重放）功能展开，具体含义如下：

### 核心内容解析：

1. **如何预测试非可信库（untrusted library）的修改**  
   如果你在使用非可信共享库的构建中发现错误，可以直接点击构建页面的 **Replay 链接**，尝试编辑该共享库的一个或多个源文件，然后重新运行构建，验证修改后的效果是否符合预期。  
   当你对修改结果满意后，可以从构建状态页点击 `diff` 链接查看修改差异，再将这些差异应用到共享库的代码仓库并提交。


2. **Replay 功能的特殊机制（即使库版本是分支）**  
   即使流水线请求的库版本是一个分支（而非标签等固定版本），**重放的构建仍会使用与原始构建完全相同的代码修订版本**：不会重新检出共享库的源代码。  
   （这意味着 Replay 仅修改当前重放流程中的代码，不会影响原始分支的代码，也不会拉取分支的最新变更，确保测试环境与原始构建一致。）


3. **Replay 功能的限制**  
   - 目前不支持对**可信库（trusted libraries）** 使用 Replay 功能。  
   - 目前 Replay 过程中不支持修改**资源文件（resource files）**（即 `resources` 目录下的文件）。


### 简单来说：
`Replay` 是 Jenkins 提供的一种便捷功能，允许你在发现非可信共享库有错误时，临时修改库代码并重新运行构建进行测试，而无需先提交修改到仓库。测试通过后，再将修改正式提交到库的代码仓库。  

但该功能有局限性：不能用于可信库，也不能修改资源文件，且重放时始终使用原始构建的库版本（确保测试环境一致）。

### 场景示例：
假设你使用了一个非可信共享库 `my-lib`，在构建时发现 `vars/build.groovy` 中有语法错误：  
1. 点击该构建的 `Replay` 链接；  
2. 在编辑界面直接修改 `build.groovy` 的错误代码；  
3. 重放构建，验证错误是否修复；  
4. 修复后，通过 `diff` 链接查看修改内容，将其手动提交到 `my-lib` 的代码仓库。  

这样就完成了共享库的“预测试-提交”流程，无需反复提交代码来测试修复效果。


---

Jenkins 中测试共享库的拉取请求(Pull Request)变更，核心是通过指定共享库的 PR 版本，在实际流水线中验证修改效果。具体含义和示例解析如下：

### 核心内容解析：
1. **测试共享库 PR 变更的方法**  
   如果你的共享库托管在 GitHub 上，且 Jenkins 中共享库的 SCM 配置使用 GitHub，可以在使用该共享库的 Jenkinsfile 顶部添加：  
   ```groovy
   @Library('my-shared-library@pull/<你的PR编号>/head') _
   ```  
   这样就能在实际流水线中直接测试共享库的 PR 变更(无需先合并 PR 到主分支)。

2. **其他代码托管平台的适配**  
   对于 GitHub 以外的平台(如 Assembla、Bitbucket、GitLab 等)，需遵循其特定的 PR 或合并请求(Merge Request)分支命名规范(例如 GitLab 可能使用 `merge-requests/<编号>/head`)。


### 示例场景详解：
以 Jenkins 官方的 `ci.jenkins.io` 共享库为例(源码存储在 `github.com/jenkins-infra/pipeline-library/`)：

1. **场景**：  
   你为这个共享库开发了新功能，并提交了编号为 `123` 的 PR。

2. **测试方法**：  
   在专门的测试仓库(如 `jenkins-infra-test-plugin`)中修改其 Jenkinsfile，通过指定共享库的 PR 版本来验证变更：  
   ```diff
   --- jenkins-infra-test-plugin/Jenkinsfile
   +++ jenkins-infra-test-plugin/Jenkinsfile
   @@ -1,3 +1,4 @@
   + @Library('pipeline-library@pull/123/head') _  // 引用 PR 123 的代码
    buildPlugin(
      useContainerAgent: true,
      configurations: [
        [platform: 'linux', jdk: 21],
        [platform: 'windows', jdk: 17],
    ])
   ```

3. **效果**：  
   当测试仓库的流水线运行时，会使用共享库 PR 123 中的代码(而非正式版本),从而验证你的新功能是否正常工作。

### 总结：
这段话的核心是教你如何在不合并 PR 的情况下，直接在实际流水线中测试共享库的 PR 变更——通过在 `@Library` 注解中指定 `@pull/<PR编号>/head` (或对应平台的格式)，让流水线临时使用 PR 中的代码，确保修改符合预期后再合并。这是一种安全高效的共享库测试方式。

---











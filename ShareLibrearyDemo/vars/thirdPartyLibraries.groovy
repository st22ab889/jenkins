
/*
// 此文件中的内容作为了解内容, 知道即可: Jenkins 流水线中使用第三方库, Third-party libraries are cached by default in ~/.groovy/grapes/ on the Jenkins controller.
@Grab('org.apache.commons:commons-math3:3.4.1')
import org.apache.commons.math3.primes.Primes
void parallelize(int count) {
  if (!Primes.isPrime(count)) {
    error "${count} was not prime"
  }
  // …
}
*/





/*
虽然可以通过 `@Grab` 注解从可信库中引入第三方库，但这种方式存在各种问题，因此不推荐。替代 `@Grab` 的建议做法是：用你选择的编程语言创建一个独立的可执行程序(可以自由使用任何第三方库)，将其安装到你的流水线所使用的 Jenkins 代理节点上，然后在流水线中通过 `bat` 或 `sh` 步骤调用这个可执行程序。


### 关键信息解析：
1. **不推荐使用 `@Grab`**：  
   `@Grab` 是 Groovy 语言的一个注解，用于动态下载并引入第三方库。但在 Jenkins 环境中使用它存在问题：  
   - 可能导致依赖冲突(不同库之间的版本不兼容)；  
   - 增加流水线启动时间(需要下载依赖)；  
   - 受 Jenkins 安全沙箱限制，可能需要复杂的权限配置；  
   - 稳定性差，依赖的远程仓库故障会直接导致流水线失败。

2. **推荐的替代方案**：  
   - **步骤 1**：用任意编程语言(如 Python、Java、Node.js 等)编写程序，在其中自由使用所需的第三方库；  
   - **步骤 2**：将程序打包为可执行文件(如 `.exe`、`jar`、脚本文件等)，并安装到所有 Jenkins 代理节点(执行流水线任务的机器)上；  
   - **步骤 3**：在 Jenkins 流水线中，通过 `sh`(Linux/macOS)或 `bat`(Windows)步骤直接调用这个可执行文件。

3. **替代方案的优势**：  
   - 避免依赖冲突和安全沙箱问题；  
   - 程序可以独立测试和维护，不影响 Jenkins 流水线；  
   - 执行速度更快，无需每次运行都下载依赖；  
   - 兼容性更好，不受 Groovy 或 Jenkins 插件版本的限制。


### 示例场景：
假设你需要在流水线中使用一个 Python 第三方库(如 `requests`)处理 HTTP 请求：

- **不推荐的方式**(使用 `@Grab` 类似的思路，在 Groovy 中直接调用)：  
  尝试在共享库中用 `@Grab` 引入 Python 相关库，或在 Groovy 中直接调用 Python 代码并依赖第三方库，可能会遇到各种依赖和权限问题。

- **推荐的方式**：  
  1. 编写一个独立的 Python 脚本(`http_handler.py`)，在其中使用 `requests` 库：  
     ```python
     # http_handler.py
     import requests
     response = requests.get("https://example.com")
     print(response.text)
     ```  
  2. 在所有 Jenkins 代理节点上安装 Python 和 `requests` 库(`pip install requests`)，并将脚本放在代理节点的固定路径(如 `/usr/local/scripts/`)；  
  3. 在流水线中通过 `sh` 步骤调用：  
     ```groovy
     pipeline {
         agent any
         stages {
             stage('Call Script') {
                 steps {
                     sh '/usr/local/scripts/http_handler.py'  // 调用独立脚本
                 }
             }
         }
     }
     ```


总结：这段话的核心是建议避免在 Jenkins 流水线中通过 `@Grab` 直接使用第三方库，而是采用“独立可执行程序 + 代理节点预安装 + `sh`/`bat` 调用”的方式，以提高稳定性和可维护性。
*/








/*
引用第三方库的代码可以放在 **Jenkins 共享库** 中(更推荐)，也可以直接放在 Pipeline 脚本中，但从最佳实践和可行性来看，**放在共享库中更合适**，原因如下：


### 1. 放在 Pipeline 脚本中可能遇到的问题
如果直接将这段代码写在 Jenkinsfile(Pipeline 脚本)中，可能会：
- **触发安全沙箱限制**：`@Grab` 注解用于动态下载第三方库，属于“动态代码执行”，在默认的 Jenkins 安全沙箱模式下会被拦截，需要管理员手动批准(安全性低且繁琐)。
- **增加流水线启动时间**：每次运行流水线都会触发 `@Grab` 下载依赖(即使已缓存)，拖慢执行速度。
- **依赖稳定性问题**：如果 `org.apache.commons:commons-math3` 的远程仓库不可用，流水线会直接失败。


### 2. 放在共享库中更合理
将这段代码放在共享库(如 `src` 目录的类中或 `vars` 目录的脚本中)：
- **减少安全审批成本**：共享库可以配置为“可信库”(Trusted)，跳过部分安全沙箱限制，`@Grab` 注解更可能被允许执行。
- **复用性更高**：如果多个 Pipeline 都需要 `parallelize` 方法，放在共享库中可以统一维护，避免重复代码。
- **更符合设计意图**：共享库的作用就是封装通用逻辑(包括依赖第三方库的代码)，而 Pipeline 脚本应专注于流程编排。


### 示例：放在共享库中
#### (1)在共享库的 `src` 目录创建类
```groovy
// src/com/example/ParallelUtils.groovy
@Grab('org.apache.commons:commons-math3:3.4.1')
import org.apache.commons.math3.primes.Primes

class ParallelUtils {
    static void parallelize(int count) {
        if (!Primes.isPrime(count)) {
            error "${count} was not prime"
        }
        // 其他并行处理逻辑...
    }
}
```

#### (2)在 Pipeline 中调用
```groovy
@Library('my-lib') import com.example.ParallelUtils

pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                script {
                    ParallelUtils.parallelize(5)  // 调用共享库中的方法
                }
            }
        }
    }
}
```


### 注意事项
即使放在共享库中，`@Grab` 仍有潜在问题(如依赖冲突、下载失败)，因此 **Jenkins 官方更推荐用“独立可执行程序 + sh/bat 调用”替代 `@Grab`**(如前文中提到的最佳实践)。如果必须使用第三方库，放在共享库中是相对更优的选择。
*/















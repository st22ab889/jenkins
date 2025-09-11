
def call(int buildNumber) {
  if (buildNumber % 2 == 0) {
    pipeline {
      agent any
      stages {
        stage('Even Stage') {
          steps {
            echo "The build number is even"
          }
        }
      }
    }
  } else {
    pipeline {
      agent any
      stages {
        stage('Odd Stage') {
          steps {
            echo "The build number is odd"
          }
        }
      }
    }
  }
}



/*

在 Jenkins 共享库中定义**声明式流水线（Declarative Pipeline）** 的相关规则和限制：

### 核心内容解析：

1. **支持版本与基本能力**  
   从 2017 年 9 月发布的 Declarative 1.2 版本开始，Jenkins 允许在共享库中定义声明式流水线。例如，可以实现一个根据构建编号(奇/偶)执行不同声明式流水线的逻辑。

2. **定义限制**  
   - **只能定义完整流水线**：目前，在共享库中只能定义**完整的声明式流水线**，不能定义部分片段(如单独的 `stage` 或 `steps`)。  
   - **存放位置与方法**：必须在共享库的 `vars` 目录下的 `.groovy` 文件中定义，且只能在 `call` 方法内实现(与自定义步骤的定义方式一致)。  
   - **单构建限制**：一次构建中只能执行**一个声明式流水线**。如果尝试执行第二个，构建会失败. 也就是说不能在同一个构建中再次调用其他声明式流水线（否则会失败）

### 总结：
共享库从 Declarative 1.2 版本开始支持定义声明式流水线,但有严格限制,必须是完整流水线、放在 `vars/*groovy` 的 `call` 方法中，且一次构建只能执行一个.这为复用相似的声明式流水线逻辑提供了可能

*/


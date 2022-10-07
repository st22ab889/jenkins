### 关于Jenkins ###
```
Jenkins的Pipeline通过Jenkinsfile进行描述
Pipeline是Jenkins的核心功能，提供一组可扩展的工具。
通过Pipeline 的DSL(DSL,Dynamic Script Language)语法可以完成从简单到复杂的交付流水线实现。
``` 
<br/>

### Jenkinsfile使用两种语法进行编写，分别是声明式和脚本式 ###
```
声明式是jenkins流水线更友好的特性。声明式流水线使编写和读取流水线代码更容易设计。
脚本式的流水线语法，提供更丰富的语法特性。(脚本式可以写 if else 语句，非常灵活)。
对jenkins来说声明式比脚本式更友好一些, 声明式功能更加强大些,所以直接用声明式.
一般会使用声明式中嵌入脚本的方法，因为这样足够灵活。
声明式Pipleine是官方推荐的语法，声明式语法更加简洁。
比如构建失败要做一些操作, 脚本式需要使用 try catch 语句捕获异常，使用声明式有现成的 DSL 用
```
<br/>

### pipeline演示，Jenkinsfile的组成及每个部分的功能含义 ###
```
使用agent{}，指定node节点/workspace（定义好此流水线在某节点运行）
指定options{}运行选项（定义好此流水线运行时的一些选项，例如输出日志的时间）
指定stages{}（stages包含多个stage，stage包含steps。是流水线的每个步骤)
指定post{}（定义好此流水线运行成功或者失败后，根据状态做一些任务）
```  

<br/>

### Jenkinsfile 示例  ###
```
#!groovy
@Library('jenkins@master') _    // 加载共享库
String workspace = "/opt/jenkins/workspace"

// Pipeline
pipeline {
    agent {
        node { 
            label "master"                  // 指定运行节点的标签或者名称                    
            customWorkspace "${workspace}"  // 指定运行工作目录(可选)
        }
    }

    options {
        timestamps()                // 日志会有时间
        skipDefaultCheckout()       // 删除隐式 checkout scm 语句
        disableConcurrentBuilds()   // 禁止并行
        timeout(time: 1, unit: 'HOURS')     // 流水线超时设置 1h
    }

    stages {
        stage("GetCode"){       // 阶段名称,比如这个阶段就是下载代码
            steps{              // 步骤
                timeout(time:5, unit:"MINUTES"){    // 步骤超时时间
                    script{
                        println('获取代码')        
                    }
                }        
            }
        }

        stage("Build"){     // 构建阶段
            steps{
                timeout(time:20, unit:"MINUTES"){
                    script{
                        println('应用打包')
                    }
                }
            }
        }

        stage("CodeScan"){  // 代码扫描阶段
            steps{
                timeout(time:30, unit:"MINUTES"){
                    script{
                        println('代码扫描')
                    }
                }
            }
        }
    }

    // 构建后操作
    post {
        always {
            script{
                println("always")
            }
        }

        success {
            script {
                currentBuild.description = "\n 构建成功!"
            }
        }

        failure {
            script {
                currentBuild.description = "\n 构建失败!"
            }
        }

        aborted {
            script {
                currentBuild.description = "\n 构建取消!"
            }
        }
    }
}
```
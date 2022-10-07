### Gradle简介 ###
```
同一个项目 Gradle 比 maven 编译快很多
```

<br/>

### jekins 集成 Gradle 步骤 ###
* 安装 Gradle (下载 --> 解压 --> 把解压后的 bin 目录路径配置到"/etc/profile" --> 使用 source 命令生效) , 如下: 
   ```
   export GRADLE_HOME=/usr/local/gradle-5.3
   export PATH=$PATH:$GRADLE_HOME/bin
   ```
* 在jeknis中下载 Gradle 插件
* 在jeknis中配置 Gradle 参数

<br/>

### Jenkins 脚本 ###
```
#!groovy

// 参数化构建, "env.buildShell"表示获取添加的参数
String buildShell = "${env.buildShell}"

pipeline{
    agent{node {label "master"}}
    stages{
        stage("maven build"){
            steps{
                script{
                    mvnHome = tool "M2"
                    sh "${mvnHome}/bin/mvn -v"
                    // sh "${mvnHome}/bin/mvn ${buildShell}"
                }
            }
        }

        // 集成 Ant,首先要在 Jenkins 中安装 Ant 插件
        stage("ant build"){
            steps{
                script{
                    try{
                        antHome = tool "ANT"
                        sh "${antHome}/bin/ant -v"
                        //sh "${antHome}/bin/ant ${buildShell}"
                    }catch(e){
                        println(e)
                    }
                }
            }
        }

        stage("Gradle Build"){
            steps{
               script{
                    gradleHome = tool "GRADLE"
                    sh "${gradleHome}/bin/gradle ${buildShell}"
               } 
            }
        }
    }
}
```

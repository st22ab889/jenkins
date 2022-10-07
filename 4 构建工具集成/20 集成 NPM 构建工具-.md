### NPM 简介 ###
```
NPM是前端工具,前端工具用的最多的是 nodejs 打包
```

<br/>

### jekins 集成 NPM 步骤 ###
* 安装 NPM (下载 --> 解压 --> 把解压后的 bin 目录路径配置到"/etc/profile" --> 使用 "source /etc/profile" 命令生效), 如下: 
   ```
   export NODE_HOME=/usr/local/node-v12.14.0-linux-x64
   export PATH=$PATH:$NODE_HOME/bin
   ```
* 在jeknis中下载 NPM 插件
* 在jeknis中配置 NPM 参数

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

        stage("NPM Build"){
            steps{
               script{
                    npmHome = tool "NPM"
                    sh "export NODE_HOME=${npmHome} && export PATH=\$NODE_HOME/bin:\$PATH && ${npmHome}/bin/npm ${buildShell}"
               } 
            }
        }
    }
}
```

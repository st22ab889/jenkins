### ant  ###
```
安装 ant
jenkins中配置下载 ant 插件
jenkins中配置 ant
```


### Jenkins 脚本 ###
```
#!groovy

// 参数化构建, "env.buildShell"表示获取添加的参数
// String buildShell = "${env.buildShell}"

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
                    antHome = tool "ANT"
                    sh "${antHome}/bin/ant -v"
                    //sh "${antHome}/bin/ant ${buildShell}"
                }
            }
        }
    }
}
```
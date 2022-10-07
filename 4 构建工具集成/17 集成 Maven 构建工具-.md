### maven  ###
```
安装maven
jenkins中配置下载maven插件
jenkins中配置maven
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
    }
}
```
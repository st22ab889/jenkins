## 上传制品的三种方式 ##
```
使用 "mvn deploy" 方式, 本节使用这种方式. // 在".../apache-maven-x.x.x/conf/setting.xml"配置nexus的用户名和密码以及"server id".
使用 jenkins 插件 "nexus artifact uploader", 使用插件后期不好维护, 比如插件升级等; Jenkins插件装多了也会臃肿
调用 nexus 的 api 上传.
```

<br/>

### gitlab.jenkinsfile ###
```
#!groovy

@Library('jenkinslibrary@master') _

// func from share library
def build = new org.devops.build()
def tools = new org.devops.tools()
def gitlab = new org.devops.gitlab()

// env
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String srcUrl = "${env.srcUrl}"

// branch 是通过解析 gitlab 的 webhook 请求传过来的 reqeust body 拿到;
String branchName = branch - "refs/heads/"
currentBuild.description = "Trigger by ${userName} ${branch}"
gitlab.ChangeCommitStatus(projectId,commitSha,"running")

pipeline{
    agent{node {label "master"}}
    stages{
        
        stage("CheckOut"){
            steps{
                script{
                    println("${branchName}")

                    tools.PrintMes("获取代码", "green")
                    // 下面的代码可以通过流水线语法生成
                    checkout([$class: 'GitSCM', branches: [[name: "${branchName}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'gitlab-admin-user', url: "${srcUrl}"]]])
                }
            }
        }

        stage("build"){
            steps{
                script{
                    tools.PrintMes("打包代码", "green")
                    build.Build(buildType, buildShell)

                    // 上传制品
                    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
                    jarName = jarName - "\n"

                    def pom = readMavenPom file: 'pom.xml'
                    pomVersion = "${pom.version}"
                    pomArtifact = "${pom.artifactId}"
                    pomPackaging = "${pom.packaging}"
                    pomGroupId = "${pom.groupId}"

                    println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")

                    def mvnHome = tool "M2"
                    sh  """ 
                    cd target/
                    ${mvnHome}/bin/mvn deploy:deploy-file -Dmaven.test.skip=true  \
                                                -Dfile=${jarName} -DgroupId=${pomGroupId} \
                                                -DartifactId=${pomArtifact} -Dversion=${pomVersion}  \
                                                -Dpackaging=${pomPackaging} -DrepositoryId=maven-hostd \
                                                -Durl=http://192.168.1.200:30083/repository/maven-hostd 
                    """
                }
            }
        }      
    }
}
```
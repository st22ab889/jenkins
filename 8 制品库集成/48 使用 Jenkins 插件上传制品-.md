## 需求: 使用 jenkins 插件 "nexus artifact uploader"上传制品 ##
```
步骤:
    a. 在 jenkins 中安装"nexus artifact uploader"插件.
    b. 创建nexus凭证.
    c. 在Jenkins的片段生成器中生成DSL,然后把DSL代码复制到流水线脚本中. 注意: 如果定义了变量,生成的脚本中变量是用单引号引起来的,这时要用双引号,否则解析不了.
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

                    //use nexus plugin
                    def repoName = "maven-hostd"
                    def filePath = "target/${jarName}"
                    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                                    classifier: '', 
                                    file: "${filePath}", 
                                    type: "${pomPackaging}"]], 
                        credentialsId: 'nexus-admin-user', 
                        groupId: "${pomGroupId}", 
                        nexusUrl: '192.168.1.200:30083', 
                        nexusVersion: 'nexus3', 
                        protocol: 'http', 
                        repository: "${repoName}", 
                        version: "${pomVersion}"
                }
            }
        }      
    }
}
```

<br/>

### gitlab.jenkinsfile 使用共享库上传制品 ###
```
#!groovy

@Library('jenkinslibrary@master') _

// func from share library
def build = new org.devops.build()
def tools = new org.devops.tools()
def gitlab = new org.devops.gitlab()
def nexus = new org.devops.nexus()

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

                    //nexus.main("maven")
                    nexus.main("nexus")
                }
            }
        }      
    }
}
```

<br/>

### ShareLibreay --> nexus.groovy ###
```
package org.devops

// 获取POM中的坐标
def GetGav() {
    // 上传制品
    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    env.jarName = jarName - "\n"

    def pom = readMavenPom file: 'pom.xml'
    //pomVersion = "${pom.version}"
    //pomArtifact = "${pom.artifactId}"
    //pomPackaging = "${pom.packaging}"
    //pomGroupId = "${pom.groupId}"

    // 如果把这些变量定义为全局变量,其实都不用return
    env.pomVersion = "${pom.version}"
    env.pomArtifact = "${pom.artifactId}"
    env.pomPackaging = "${pom.packaging}"
    env.pomGroupId = "${pom.groupId}"

    println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")

    return ["${pomGroupId}","${pomArtifact}","${pomVersion}","${pomPackaging}"]
}

// maven deploy
def MavenUpload() {
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

// nexus plugin deploy
def NexusUpload() {
    //use nexus plugin
    def repoName = "maven-hostd"
    def filePath = "target/${jarName}"
    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                    classifier: '', 
                    file: "${filePath}", 
                    type: "${pomPackaging}"]], 
        credentialsId: 'nexus-admin-user', 
        groupId: "${pomGroupId}", 
        nexusUrl: '192.168.1.200:30083', 
        nexusVersion: 'nexus3', 
        protocol: 'http', 
        repository: "${repoName}", 
        version: "${pomVersion}"
}

def main(uploadType) {
    
    GetGav();
    
    if("${uploadType}" == "maven"){
        MavenUpload()
    }else if("${uploadType}" == "nexus"){
        NexusUpload()
    }
}
```
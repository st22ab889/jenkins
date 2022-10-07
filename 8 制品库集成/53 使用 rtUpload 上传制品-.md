## 使用 rtUpload 上传制品 ##
```
(1) rtUpload 是个插件, 在 Jenkins 中可以安装, Artifactory 支持使用 rtUpload 上传单个文件.
(2) Artifactory 发布版本和版本晋级，Artifactory 没有类似"Maven Artifact ChoiceListProvider(Nexus)"这样的插件.
    2.1 发布版本的解决方案: 可以自己写项目做页面实现获取制品的所有版本,然后让用户去选.
    2.2 制品晋级: 可以通过"copy"和"mv"实现.
```

<br/><br/>

### jenkinsfile ###
```
#!groovy

@Library('jenkinslibrary@master') _

// func from share library
def build = new org.devops.build()
def tools = new org.devops.tools()
def gitlab = new org.devops.gitlab()
def nexus = new org.devops.nexus()
def artifactory = new org.devops.artifactory()

// env
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String srcUrl = "${env.srcUrl}"
String artifactUrl = "${env.artifactUrl}"

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
                    artifactory.main(buildType, buildShell)
                    artifactory.PushArtifact()
                }
            }
        }      
    }
}
```

<br/><br/>

### ShareLibrary --> artifactory.groovy ###
```
// 上传制品
def PushArtifact(){
    //重命名制品
    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    jarName = jarName - "\n"
    def pom = readMavenPom file: 'pom.xml'
    env.pomVersion = "${pom.version}" 
    env.serviceName = "${JOB_NAME}".split("_")[0]
    // BUILD_ID 就是构建编号
    env.buildTag =  "${BUILD_ID}"
    def newJarName = "${serviceName}-${pomVersion}-${buildTag}.jar"
    println("${jarName} -------->>> ${newJarName}")
    sh "mv target/${jarName} target/${newJarName}"

    //上传制品
    env.businessName = "${env.JOB_NAME}.split("-")[0]"
    env.repoName = "${businessName}-${JOB_NAME.split("_")[-1].toLowerCase()}"
    println("本次制品将要上传到${repoName}仓库中!")
    env.uploadDir = "${repoName}/${businessName}/${serviceName}/${pomVersion}"

    println("上传制品")
    rtUpload(
        serverId: "artifactory",
        spec:
            """{
            "files": [
                {
                    "pattern": "target/${newJarName}",
                    "target": "${uploadDir}/"
                }
            ]
            }"""
    )
}
```
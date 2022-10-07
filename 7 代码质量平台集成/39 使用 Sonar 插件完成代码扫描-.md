## 使用 Sonar 插件完成代码扫描 ##
```
1. Sonar 插件插件安装好后, 在 Jenkins 系统配置中找到 SonarQube installations , 配置相关参数, 如 name、sonar 服务器地址等.
2. 可以有多个 SonarQube 配置, 实现多 sonar 服务.
3. 在 Jenkins 中用 sonar 插件的好处是会在 pipeline 页面增加 sonar 衔接.
```

<br/>

### ShareLibrary --> sonarqube.groovy ### 
```
def SonarSacnForJenkinsSonarPlugin(sonarServer,projectName,projectDesc,projectPath){
    // "sonarqube-test"和"sonarqube-prod" 是Jenkins 安装 SonarQube Scanner 插件后, 在"Jenkins --> 系统配置 --> SonarQube installations --> Name"的值
    def servers=["test":"sonarqube-test", "prod":"sonarqube-prod"]

    withSonarQubeEnv("${servers[sonarServer]}"){
        def sonarDate = sh returnStdout: true, script: 'date + %Y%m%d%H%M%S'
        sonarDate = sonarDate - "\n"
        
        def scannerHome = "/usr/local/sonar-scanner-3.2.0.1227-linux"
        sh  """
            ${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName}  \
            -Dsonar.projectName=${projectName}  \
            -Dsonar.projectVersion=${sonarDate} \
            -Dsonar.ws.timeout=30 \
            -Dsonar.projectDescription=${projectDesc}  \
            -Dsonar.links.homepage=http://www.baidu.com \
            -Dsonar.sources=${projectPath} \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.java.surefire.report=target/surefire-reports
            """
    }
}
```

<br/>

### Jenkins 脚本 ### 
```
#!groovy

@Library('jenkinslibrary@master') _

// func from share library
def build = new org.devops.build()
def tools = new org.devops.tools()
def gitlab = new org.devops.gitlab()
def toemail = new org.devops.toemail()
def sonarqube = new org.devops.sonarqube()

// env
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String srcUrl = "${env.srcUrl}"

// 因为是通过解析 webhook 请求传过来的 reqeust body 拿到, 所以不用把分支名配置到环境变量中
// runOpts 是 webhook 请求中的requestParameter.
// branch、userName、projectId、commitSha 是通过解析  webhook 请求传过来的 reqeust body 拿到;
String branchName = ""
if("${runOpts}" == "GitlabPush"){
    branchName = branch - "refs/heads/"
    
    currentBuild.description = "Trigger by ${userName} ${branch}"
    gitlab.ChangeCommitStatus(projectId,commitSha,"running")

    // 睡眠15秒, 便于观察效果
    // sleep 15
}  

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
                }
            }
        }

        stage("Sonar Qube Scan"){
            steps{
                script{
                  tools.PrintMes("代码扫描", "green")
                  sonarqube.SonarSacnForJenkinsSonarPlugin("test", ${JOB_NAME},${JOB_NAME},"src")
                }
            }
        }        
    }

    post {
        always {
            script {
                println("always")
            }
        }

        success{
            script {
                println("success")
                gitlab.ChangeCommitStatus(projectId,commitSha,"success")
                // userEmail 通过解析  webhook 请求传过来的reqeust body 拿到, 所以在 GitLab 账户要配置上 email
                toemail.Email("流水线成功", userEmail)
            }            
        }

        failure {
             script {
                println("failure")
                gitlab.ChangeCommitStatus(projectId,commitSha,"failed")
                toemail.Email("流水线失败", userEmail)
            }              
        }
        
        aborted {
              script {
                println("cancel")
                gitlab.ChangeCommitStatus(projectId,commitSha,"canceled")
                toemail.Email("流水线被取消", userEmail)
            }             
        }
    }
}
```

<br/>

### Pipeline 效果 ### 
<img src="./readee-ref-resource/39/Jenkins-sonar-plugin.jpg"  />
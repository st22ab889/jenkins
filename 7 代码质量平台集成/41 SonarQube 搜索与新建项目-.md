
## 需求: 使用 sonar api 实现在一开始扫描之前就指定扫描规则和质量阈 ##
```
新建任务运行 pipeline 脚本, 可以手动触发, 因为有些经过 gitlab hook 传过来的变量已经定义在脚本中!
```

<br/><br/>

### ShareLibray --> sonarapi.groovy ###
```
//搜索Sonar项目
def SerarchProject(projectName){
    apiUrl = "projects/search?projects=${projectName}"
    response = HttpReq("GET",apiUrl,'')

    response = readJSON text: """${response.content}"""
    result = response["paging"]["total"]

    if(result.toString() == "0"){
       return "false"
    } else {
       return "true"
    }
}


//创建Sonar项目
def CreateProject(projectName){
    apiUrl =  "projects/create?name=${projectName}&project=${projectName}"
    response = HttpReq("POST",apiUrl,'')
    println(response)
}

```

<br/>

### pipeline 脚本 ###
```
#!groovy

@Library('jenkinslibrary@master') _

// func from share library
def build = new org.devops.build()
def tools = new org.devops.tools()
def gitlab = new org.devops.gitlab()
def toemail = new org.devops.toemail()
def sonarqube = new org.devops.sonarqube()
def sonarApi = new org.devops.sonarapi()

// env
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String srcUrl = "${env.srcUrl}"

// 因为是通过解析 webhook 请求传过来的 reqeust body 拿到, 所以不用把分支名配置到环境变量中
// runOpts 是 webhook 请求中的requestParameter.
// branch、userName、projectId、commitSha 是通过解析  webhook 请求传过来的 reqeust body 拿到;
String branchName = ""

// 定义 runOpts, 因为手动触发 pipeline 时没有这个参数, 防止 pipeline 因缺少这个参数而报错
def runOpts

if("${runOpts}" == "GitlabPush"){
    branchName = branch - "refs/heads/"
    
    currentBuild.description = "Trigger by ${userName} ${branch}"
    gitlab.ChangeCommitStatus(projectId,commitSha,"running")

    // 睡眠15秒, 便于观察效果
    // sleep 15
} else {
    userEmail = "example@163.com"
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
                  
                  tools.PrintMes("搜索项目", "green")
                  result = sonarapi.SerarchProject(${JOB_NAME})
                  println(result)
                  if(result == "false"){
                    println("${JOB_NAME} ===>> 项目不存在,准备创建项目!")
                    sonarapi.CreateProject(${JOB_NAME})
                  }else{
                     println("${JOB_NAME} ===>> 项目已存在!")
                  }

                  tools.PrintMes("代码扫描", "green")
                  sonarqube.SonarSacnForJenkinsSonarPlugin("test", ${JOB_NAME},${JOB_NAME},"src")
                  tools.PrintMes("获取扫描结果", "green")
                  result = sonarapi.GetProjectStatus("${JOB_NAME}")
                  println(result)

                  if(result.toString() == "ERROR"){
                    toemail.Email("代码质量阈错误!请及时修复!", userEmail)
                    error "代码质量阈错误!请及时修复!"
                  }else{
                    println(result)
                  }
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
                if("${runOpts}" == "GitlabPush"){
                    gitlab.ChangeCommitStatus(projectId,commitSha,"success")
                }
                // userEmail 通过解析  webhook 请求传过来的reqeust body 拿到, 所以在 GitLab 账户要配置上 email
                toemail.Email("流水线成功", userEmail)
            }            
        }

        failure {
             script {
                println("failure")
                if("${runOpts}" == "GitlabPush"){
                    gitlab.ChangeCommitStatus(projectId,commitSha,"failed")
                }
                toemail.Email("流水线失败", userEmail)
            }              
        }
        
        aborted {
              script {
                println("cancel")
                if("${runOpts}" == "GitlabPush"){
                    gitlab.ChangeCommitStatus(projectId,commitSha,"canceled")
                }
                toemail.Email("流水线被取消", userEmail)
            }             
        }
    }
}
```
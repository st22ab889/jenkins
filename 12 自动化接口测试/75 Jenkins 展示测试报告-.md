## 1. 在 build.xml 文件中定义报告的名称 ##
<img src="./readee-ref-resource/75/jemeter_1_buildxml.jpg"  />

<br/><br/>

## 2. 在 Jenkins 中生成展示测试报告的代码,这段代码加到 jenkinsfile 中, 跑完 pipeline 就会看到测试报告的衔接 ##
<img src="./readee-ref-resource/75/jemeter_2_Jenkins.jpg"  />
<img src="./readee-ref-resource/75/jemeter_3_report.jpg"  />
<img src="./readee-ref-resource/75/jemeter_4_report_issue.jpg"  />

<br/><br/>

## 3. 使用添加脚本命令行的方式解决 Jenkins 不能正确展示 html 样式, 添加脚本命令行后要重新跑pipeline ##
```
持续集成 解决 Jenkins 中无法展示 HTML 样式的问题: https://testerhome.com/topics/9476
```
<img src="./readee-ref-resource/75/jemeter_5_resolve.jpg"  />
<img src="./readee-ref-resource/75/jemeter_6_report_normal_1.jpg"  />
<img src="./readee-ref-resource/75/jemeter_6_report_normal_2.jpg"  />

<br/><br/>

## interfaceTest.jenkinsfile 脚本 ##
```
#!groovy

@Library('jenkinslibrary@master') _

// func from share library
def build = new org.devops.build()
def tools = new org.devops.tools()
def toemail = new org.devops.toemail()


// env
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String srcUrl = "${env.srcUrl}"
String branchName = "${env.branchName}"

userEmail = "xxxxxx@163.com"

// pipeline
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
                  // 展示测试报告
                  publishHTML([allowMissing: false,
                               alwaysLinkToLastBuild: false,
                               keepAll: false,
                               reportDir: 'result/htmlfile',
                               reportFiles: 'SummaryReport.html,DetailReport.html',
                               reportName: 'InterfaceTestReport',
                               reportTitles: ''])
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
                // userEmail 通过解析  webhook 请求传过来的reqeust body 拿到, 所以在 GitLab 账户要配置上 email
                toemail.Email("流水线成功", userEmail)
            }            
        }

        failure {
             script {
                println("failure")
                toemail.Email("流水线失败", userEmail)
            }              
        }
        
        aborted {
              script {
                println("cancel")
                toemail.Email("流水线被取消", userEmail)
            }             
        }
    }
}
```
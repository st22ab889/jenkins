## Jira 自动创建 Gitlab 分支思路 ##
```
(1). 使用 jekins 的 "Generic Webhook Trigger"触发器, 添加 webHookData 和 projectKey 以及 token 参数 
    (1.1) webHookData 和 projectKey 通过 Jira 调用Jenkins的hook衔接传递过来.
    (1.2) 定义token参数的同时定义好其值,这个值也需要通过enkins的hook衔接传递过来,token 参数用来做接口认证
(2). 配置Jira的 webHook(填上Jenkins回调地址等参数)
(3). 在脚本中解析 Jira 传递过来的相关参数
(4). 根据参数在 jenkinsfile 中实现逻辑, 然后通过调用 GitLab 相关的 API 实现对分支的操作
```

<br/>

### jira.jenkinsfile ### 
```
@Library('jenkinslibrary') _

def gitlab = new org.devops.gitlab()
def jira = new org.devops.jira()

pipeline {
    agent { node { label "master"}}


    stages{

        stage("FileterData"){
            steps{
                script{
                    response = readJSON text: """${webHookData}"""

                    println(response)

                    env.eventType = response["webhookEvent"]

                    switch(eventType) {
                        case "jira:version_created":
                            env.versionName = response['version']['name']
                            currentBuild.description = "Trigger by ${eventType} ${versionName}"
                            break                        
                        case "jira:issue_created":
                            env.issueName = response['issue']['key']
                            env.userName = response['user']['name']
                            env.moduleNames = response['issue']['fields']['components']
                            env.fixVersion = response['issue']['fields']['fixVersions']
                            currentBuild.description = " Trigger by ${userName} ${eventType} ${issueName} "
                            break

                        case "jira:issue_updated":
                            env.issueName = response['issue']['key']
                            env.userName = response['user']['name']
                            env.moduleNames = response['issue']['fields']['components']
                            env.fixVersion = response['issue']['fields']['fixVersions']
                            currentBuild.description = " Trigger by ${userName} ${eventType} ${issueName} "
                            break
                        case "jira:version_released":
                            env.versionName = response['version']['name']
                            currentBuild.description = "Trigger by ${eventType} ${versionName}"
                            break  
                        default:
                            println("hello")
                    }
                }
            }
        }

        stage("CreateBranchOrMR"){
            
            // 只有当 eventType 的值为 'jira:issue_created' 或 'jira:issue_updated' 才能运行这个阶段, "anyOf"表示两者之中的一个就可以
            when {
                anyOf {
                    environment name: 'eventType', value: 'jira:issue_created'   //issue 创建 /更新
                    environment name: 'eventType', value: 'jira:issue_updated' 
                }
            }

            steps{
                script{
                    def projectIds = []
                    println(issueName)
                    fixVersion = readJSON text: """${fixVersion}"""
                    println(fixVersion.size())

                    //获取项目Id
                    def projects = readJSON text: """${moduleNames}"""
                    for ( project in projects){
                        println(project["name"])
                        projectName = project["name"]
                        currentBuild.description += "\n project: ${projectName}"
                        repoName = projectName.split("-")[0]
                        
                        try {
                            projectId = gitlab.GetProjectID(repoName, projectName)
                            println(projectId)
                            projectIds.add(projectId)   
                        } catch(e){
                            println(e)
                            println("未获取到项目ID，请检查模块名称！")
                        }
                    } 

                    println(projectIds)  


                    if (fixVersion.size() == 0) {
                        for (id in projectIds){
                            println("新建特性分支--> ${id} --> ${issueName}")
                            currentBuild.description += "\n 新建特性分支--> ${id} --> ${issueName}"
                            gitlab.CreateBranch(id,"master","${issueName}")
                        }
                            
                        

                    } else {
                        fixVersion = fixVersion[0]['name']
                        println("Issue关联release操作,Jenkins创建合并请求")
                        currentBuild.description += "\n Issue关联release操作,Jenkins创建合并请求 \n ${issueName} --> RELEASE-${fixVersion}" 
                        
                        for (id in projectIds){

                            println("创建RELEASE-->${id} -->${fixVersion}分支")
                            gitlab.CreateBranch(id,"master","RELEASE-${fixVersion}")


                            
                            println("创建合并请求 ${issueName} ---> RELEASE-${fixVersion}")
                            gitlab.CreateMr(id,"${issueName}","RELEASE-${fixVersion}","${issueName}--->RELEASE-${fixVersion}")
                            
                        }
                    } 
                }
            }
        }
    }
}
```

<br/>

### ShareLibray -->  gitlab.groovy ### 
```
package org.devops

//封装HTTP请求
def HttpReq(reqType,reqUrl,reqBody){
    def gitServer = "http://192.168.1.200:30088/api/v4"
    withCredentials([string(credentialsId: 'gitlab-token', variable: 'gitlabToken')]) {
      result = httpRequest customHeaders: [[maskValue: true, name: 'PRIVATE-TOKEN', value: "${gitlabToken}"]], 
                httpMode: reqType, 
                contentType: "APPLICATION_JSON",
                consoleLogResponseBody: true,
                ignoreSslErrors: true, 
                requestBody: reqBody,
                url: "${gitServer}/${reqUrl}"
                //quiet: true
    }
    return result
}

//创建分支
def CreateBranch(projectId, refBranch, newBranch){
    try{
        branchApi = "projects/${projectId}/repository/branches?branch=${newBranch}&ref=${refBranch}"
        response = HttpReq('POST',branchApi,'').content
        branchInfo = readJSON text: """${response}"""
    }catch(e){
        println(e)
        //println(branchInfo)
    }
}
```
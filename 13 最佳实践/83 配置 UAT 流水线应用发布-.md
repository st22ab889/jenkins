## 部署 deployment YAML 文件到 kubernetes ##

### jenkins\13 最佳实践\jenkinslibrary-master\jenkinsfiles\java.jenkinsfile ###
```
......
       //发布
       stage("Deploy"){
            steps{
                script{
                    tools.PrintMes("发布应用","green")
                    
                    //下载版本库文件 
                    
                    releaseVersion = "${branchName}".split("-")[-1]
                    response = gitlab.GetRepoFile(7,"demo-uat%2f${releaseVersion}-uat.yaml")
                    //println(response)
                    
                    //替换文件中内容（镜像）
                    fileData = readYaml text: """${response}"""
                    println(fileData["spec"]["template"]["spec"]["containers"][0]["image"])
                    println(fileData["metadata"]["resourceVersion"])
                    oldImage = fileData["spec"]["template"]["spec"]["containers"][0]["image"] 
                    oldVersion = fileData["metadata"]["resourceVersion"]
                    oldUid = fileData["metadata"]["uid"]
                    response = response.replace(oldImage,dockerImage)
                    response = response.replace(oldVersion,"")
                    response = response.replace(oldUid,"")
                   
                    println(response)
                    
                    //更新gitlab文件内容
                    base64Content = response.bytes.encodeBase64().toString()
                    gitlab.UpdateRepoFile(7,"demo-uat%2f${releaseVersion}-uat.yaml",base64Content)
                    
                    //发布kubernetes
                    k8s.UpdateDeployment("demo-uat","demoapp",response)
                }
            }
        }
......
```

<br/>

## uat 流水线完整图示 ##
![jenkins pipeline](./readee-ref-resource/81/push_2_run.jpg)

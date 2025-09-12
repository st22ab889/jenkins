

### 1.内置文档(提供详细的帮助和信息) 和 代码生成器(有助于为单个步骤创建代码段，发现插件提供的新步骤，或为特定步骤尝试不同的参数)
* ${YOUR_JENKINS_URL}/pipeline-syntax
* ${YOUR_JENKINS_URL}/directive-generator
* ${YOUR_JENKINS_URL}/pipeline-syntax/globals 


### 2.命令行流水线 linter (用来校验 Jenkinsfile 语法)
```Jenkinsfile
pipeline {
  agent
  stages {
    stage ('Initialize') {
      steps {
        echo 'Placeholder.'
      }
    }
  }
}
```

#### 方式1: 通过SSH命令使用 Linting 

**如何通过 SSH 使用jenkins CLI:**
-	Jenkins 版本非常旧(2.200 之前),SSH 配置可能仍在"安全"页面，但目前主流版本（2.300+）均已迁移到"系统"页面.如何这两个页面都找不到,说明需要安装"SSH server"插件. 
-	安装后在"安全"或"系统"页面找到"SSH Server"配置.通常在"安全"页面, "SSH Server"插件主页有说明: Enable the built-in SSH server in Manage Jenkins » Configure Global Security.

```shell
# JENKINS_PORT=[sshd port on controller]
# JENKINS_HOST=[Jenkins controller hostname]
ssh -p $JENKINS_PORT $JENKINS_HOST declarative-linter < Jenkinsfile
```


#### 方式2: 通过HTTP POST命令使用 Linting 
```shell
# JENKINS_URL=[root URL of Jenkins controller]
# JENKINS_AUTH=[your Jenkins username and an API token in the following format: your_username:api_token]
curl -X POST --user "$JENKINS_AUTH" -F "jenkinsfile=<Jenkinsfile" "$JENKINS_URL/pipeline-model-converter/validate"
```


	
	
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	

* 首先需要下载 jenkins-cli.jar,参考内置文档: http://${YOUR_JENKINS_URL}/manage/cli/
* Jenkins CLI 用法官方文档: https://www.jenkins.io/doc/book/managing/cli/

(1) 在 .bashrc（或 .zshrc）中添加别名：
# 假设 Jenkins 无需认证（本地测试场景）
alias declarative-linter='java -jar /usr/local/bin/jenkins-cli.jar -s http://localhost:8675/ declarative-linter'
# 如果需要认证，添加 -auth 参数
# alias declarative-linter='java -jar /usr/local/bin/jenkins-cli.jar -s http://localhost:8675/ -auth 用户名:API_TOKEN declarative-linter'

(3) 测试. 官方文档是直接使用 declarative-linter 命令, 但这个命令只是 jenkins-cli.jar 的一个子命令, 不能直接使用, 需通过前面两步间接实现

(2) 刷新配置使配置生效
source ~/.bashrc 
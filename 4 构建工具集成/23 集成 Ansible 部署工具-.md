### Ansible 简介 ###
```
Ansible 是 CD 工具(发布工具)
```

<br/>

### 安装 Ansible ###
```
1. 安装 Ansible
yum -y install ansible

2.配置免密(就是SSH)

3.配置清单文件 "/etc/ansible/hosts"

4.测试
ansible -m ping servers         // servers就来自"/etc/ansible/hosts"
```

<br/>

### Jenkins 上配置 SaltStack ###
```
1. 不用下载任何插件, 直接将命令(代码)写在共享库中, 如（"src\org\devops\deploy.groovy"中的 AnsibleDeploy 方法） 
2. 可以参数化构建, 这里将主机名添加到参数中, 参数名为 deployHostsForAnsible
```
### Jenkins 脚本 ###
```
#!groovy

// "master"表示版本,"@master"表示使用master分支的共享库代码,共享库代码保存在远程仓库,可以在Jenkins中设置共享库代码仓库的地址
@Library('jenkinslibrary@master') _

def build = new org.devops.build()
def deploy = new org.devops.deploy()

// 参数化构建, "env.buildShell"表示获取添加的参数
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String deployHostsForSalt = "${env.deployHostsForSalt}"
String deployHostsForAnsible = "${env.deployHostsForAnsible}"

pipeline{
    agent{node {label "master"}}
    stages{
        stage("build"){
            steps{
                script{
                  build.Build(buildType, buildShell)
                  deploy.SaltDeploy("${deployHostsForSalt}", "test.ping")
                  deploy.AnsibleDeploy("${deployHostsForAnsible}", "-m ping")
                }
            }
        }
    }
}
```
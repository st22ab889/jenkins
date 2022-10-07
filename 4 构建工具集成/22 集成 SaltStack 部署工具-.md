### SaltStack 简介 ###
```
1. SaltStack 是 CD 工具(发布工具)
2. SaltStack是由 Salt-master 和 Salt-minion 以及 API (API这里不会涉及到) 组成
3. 需要在每台部署的机器上都要安装 Salt-master, 在每台应用服务器上安装 Salt-minion, 并且让他们建立连接, 这个时候才可以把包传到应用服务器.
```

<br/>

### 安装 SaltStack ###
```
1. 安装 RPM 包
yum install https://repo.saltstack.com/py3/redhat/salt-py3-repo-latest.e18.noarch.rpm
2. 安装 salt-master 和 salt-minion, 这里为了方便把 salt-master 和 salt-minion 都安装在同一台主机上
yum install salt-master
yum install salt-minion
3. 配置 master 和 minion 之间的通信

// 下面命令在安装 minion 的机器上运行
cd /etc/salt

vim minion
master: [这里指定master的IP地址]

service salt-minion start

// 下面命令在安装 master 的机器上运行
service salt-master start
salt-key -L                                               // 查看接收的key和未接收的key
salt-key -a [这里填上个命令打印出来的 Unaccepted Keys]
salt-key -L                                               // 这里会列出 Accepted Keys, Accepted Keys 是能连接的主机名称列表
salt [hostname] test.ping                                 // 测试一台主机, [hostname] 也就是"Accepted Keys"列表中其中之一的主机名称
salt -L [hostname_1],[hostname_2] test.ping               // 测试多台主机
```

<br/>

### Jenkins 上配置 SaltStack ###
```
1. 不用下载任何插件, 直接将命令(代码)写在共享库中, 如（"src\org\devops\deploy.groovy"中的 SaltDeploy 方法） 
2. 可以参数化构建, 这里将 Accepted Keys 添加到参数中, 参数名为 deployHostsForSalt
```
### Jenkins 脚本 ###
```
#!groovy

// "master"表示版本,"@master"表示使用master分支的共享库代码,共享库代码保存在远程仓库,可以在Jenkins中设置共享库代码仓库的地址
@Library('jenkinslibrary@master') _

def build = new org.devops.build()

// 参数化构建, "env.buildShell"表示获取添加的参数
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String deployHostsForSalt = "${env.deployHostsForSalt}"

pipeline{
    agent{node {label "master"}}
    stages{
        stage("build"){
            steps{
                script{
                  build.Build(buildType, buildShell)
                  deploy.SaltDeploy("${deployHostsForSalt}", "test.ping")
                }
            }
        }
    }
}
```
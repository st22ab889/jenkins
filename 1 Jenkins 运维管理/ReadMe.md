### jenkins 介绍 ###
```
1. jenkins 是最好的 CI (持续集成：编译、打包、扫描这个过程)工具，CD（持续部署：把软件发布到环境当中）这块比较弱, 因为做 CD 需要配置大量的工作, 比如可以使用 SaltStack 发布应用，虽然jenkins有插件，但是还不如 shell、command 这些命令更友好，因为jenkins上安装插件越来越多的时候，jenkins就越来越难以管理，而且 jenkins 越来越臃肿。其它 CD 工具还有: SpinnakerCD、jenkins-x 云原生等
2. jenkins 不但有插件支持，而且还可以调用 shell，python 等等脚本
3. 应该使用什么 CI/CD 工具？ https://www.jenkins-zh.cn/wechat/articles/2019/04/2019-04-30-what-cicd-tool-should-i-use/
```

<br/>

### 目前用来部署应用的机器一般有以下几种 ###
```
vim(虚拟机)
云主机
容器部署
公有云主机
```










### 一个项目一般有多个分支, 实现在任何一个分支提交代码都能触发流水线的构建 ###
```
有两种配置方法:
1. 使用构建触发器中的 GitLab 触发器(需要安装),不太灵活,看文档学习成本也比较高
2. 使用构建触发器中的"Generic Webhook Trigger",很灵活,可以设置自定义触发参数
```

### 配置"Generic Webhook Trigger"步骤  ###
```
1. 在 jenkins 中配置 "Generic Webhook Trigger"
2. 在 gitlab 上配置 "webhook"
```







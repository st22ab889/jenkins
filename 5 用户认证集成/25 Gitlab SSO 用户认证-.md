### Gitlab SSO 用户认证 ###
```
使用登录 Gitlab 的用户名和密码登录 jenkins。
```

<br/>

### 实现步骤 ###
```
1.登录到 GitLab , 添加相应配置
2.在jenkins中安装 "Gitlab Authentication" 插件
3.配置 jenkins。注意: 同样先将授权策略修改为 Anyone can do anything, 原因参考"24 Ldap 用户认证集成-.md"
```

<br/>

### 参考资料 ###
```
http://docs.idevops.site/jenkins/pipelineintegrated/chapter02/#gitlab%E5%8D%95%E7%82%B9%E7%99%BB%E5%BD%95
```
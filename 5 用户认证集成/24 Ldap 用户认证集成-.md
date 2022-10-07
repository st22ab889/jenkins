### LDAP概念 ###
```
LDAP简介: https://www.jianshu.com/p/4b3c89ce6ac3
Jenkins默认使用自带数据库模式存储用户，在企业中一般都会有统一的认证中心，例如 LDAP、ActiveDirectory中管理用户。可以配置Jenkins集成实现统一用户管理。
Jenkins 集成 LDAP 的目的是实现 jenkins 账号的统一管理, 如果要实现jenkins用户权限管理,需要下载"Role-based Authorization Strategy"插件。 
```

<br/>

### 安装 LDAP 服务器 ###
```
1. 使用手动安装 或 docker 
2. 安装后需要登录到 LDAP 后台进行一系列设置,参考资料如下:
    Jenkins和LDAP集成方法: https://blog.csdn.net/qq_38986854/article/details/121990079
    用户认证系统集成: http://docs.idevops.site/jenkins/pipelineintegrated/chapter02/#ldap%E7%B3%BB%E7%BB%9F%E9%9B%86%E6%88%90
```

<br/>

### jenkins 安装 LDAP 插件 ###
```
1.下载 LDAP 插件
2.配置 LADP 参数
3.注意：启用了LDAP，默认的admin用户，也即jenkins的本地用户数据就被禁用了，不能继续登录。所以应用前先将授权策略修改为 Anyone can do anything, 因为admin用户不能用了,万一ldap用户不能登录或者没权限问题也不大。或者不登出admin账号也可以注意不要超时。
```
<font face="仿宋" size=3> 修改授权策略的方法: "访问控制 -> 授权策略"中改为"登录用户可以做任何事", 勾选"匿名用户具有可读权限", 防止授权不成功导致不能登录,匿名用户可以后面再授权。</font>

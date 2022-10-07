## 基于 Jira 流水线的方案 ##
```
本节知识点:
(1) Jira 和 Jenkins 做集成, Jenkins 和 Gitlab 集成. 实际上是 Jira 和 Gitlab 集成,但是没有合适的插件,所以用jenkins来调度.
(2) 主要演示一个任务从开始到结束, Jira、Jenkins、Gitlab 都做了哪些操作。
(3) 本节的分支策略是: feature分支开发, 开发后自动创建 release 分支验证, 验证后发生产, 然后合并到master分支
```

<br/>

## Jira 上创建问题的步骤 ##
```
(1) 在 Jira 上创建一个模块, 这个模块就是 Gitlab 上 project 的名称 (在 Jira 上，模块就相当于 Gitlab 上的仓库)
(2) 然后在 Jira 上新建一个名为 test002 的问题(问题类型可以是任务或故事), 关联刚刚创建的模块，
```
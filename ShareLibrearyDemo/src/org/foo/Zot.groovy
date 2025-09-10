package org.foo


/**
* 库类不能直接调用sh或git等步骤。然而，它们可以在封闭类的范围之外实现方法，从而调用Pipeline步骤
* 注意: 这种方法存在局限性，例如，它会阻止声明父类(superclass),即使用这种方式定义的类难以实现类的继承
*/
def checkOutFrom(repo, credentialsId, branch = "master") {
	
	try {	
		// git url: "git@github.com:jenkinsci/${repo}"
		git( 
			url: "git@gitee.com:st22ab889/${repo}",
			credentialsId: credentialsId,
			branch: branch
		)
		echo "Successfully checked out repository: ${repo} (branch: ${branch})"
	} catch (Exception e) {
		error "Failed to checkout repository ${repo}: ${e.getMessage()}"
	}
}

return this



/*
在这个 Groovy 脚本中，`return this` 的作用与“类”无关，而是和 Jenkins 共享库的**脚本加载机制**直接相关。


### 具体含义：
在 Groovy 中，一个独立的 `.groovy` 脚本文件(没有定义类)本质上会被隐式处理为一个“脚本对象”(类似一个匿名类的实例)，其中定义的函数(如 `checkOutFrom`)会成为这个脚本对象的方法。

而 `return this` 表示：**返回当前脚本对象自身**。


### 在 Jenkins 共享库中的作用：
当你在 Jenkins Pipeline 中通过 `@Library` 加载这个共享库脚本时，Jenkins 会执行这个脚本并获取其返回值(即 `this` 代表的脚本对象)。这样一来：
- 脚本中定义的方法(如 `checkOutFrom`)会被“暴露”给 Pipeline。
- Pipeline 可以直接调用这些方法(例如 `checkOutFrom('repo', 'id')`)，无需实例化类。


### 举例理解：
假设这个脚本名为 `GitUtils.groovy`，在 Pipeline 中加载后：
```groovy
@Library('my-lib') import org.foo.GitUtils  // 加载脚本
GitUtils.checkOutFrom('my-repo', 'creds123')  // 直接调用方法
```
这里能直接调用 `checkOutFrom`，正是因为脚本通过 `return this` 把自身(包含该方法的脚本对象)返回给了 Jenkins，使得 Pipeline 可以访问其中的方法。


### 总结：
对于没有定义类的 Groovy 脚本：
- `this` 代表当前脚本对象本身(包含脚本中定义的所有方法和变量)。
- `return this` 是 Jenkins 共享库的一种约定：通过返回脚本对象，让 Pipeline 可以直接调用脚本中定义的方法，简化使用方式。

这也是为什么这种“无类脚本”能在 Pipeline 中直接使用的核心原因。

*/
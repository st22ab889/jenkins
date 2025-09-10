package org.foo



/**
* 传递步骤(steps)的方式：通过在类的构造函数中接收 steps 参数(通常用 this 传递当前流水线的上下文),使类内部能够使用 Jenkins 流水线的各种步骤(如 sh、tool 等)
* Serializable 接口的必要性：
*	Jenkins 流水线支持暂停和恢复(如遇到输入等待时)
*	实现 Serializable 接口能确保类的状态可以被正确序列化和反序列化
*	这是 Jenkins 流水线实现 "持久化" 和 "可恢复性" 的关键要求
*/

class Utilities implements Serializable {
	
	def steps
	
	Utilities(steps){
		this.steps = steps
	}
	
	def mvn(args) {
		// steps.sh "${steps.tool 'Maven'}/bin/mvn -o ${args}"
		steps.echo "/usr/bin/mvn -o ${args}"
	}
	
	/*
	* 如果共享库需要访问 env 等全局变量，应该上下文显式传递到库的类或方法中,不必从 Scripted Pipeline 向库中传递大量变量.
	* 使用静态方法可以避免创建类的实例，直接通过类名调用，简化了在流水线中的使用方式。
	*/
	static def mvn(script, args) {
		// script.sh "${script.tool 'Maven'}/bin/mvn -s ${script.env.HOME}/jenkins.xml -o ${args}"
		script.echo "usr/bin/mvn -s ${script.env.Home}/jenkins.xml  -o ${args}"
	}

}


/*
Zot.groovy 和 Utilities.groovy 的区别:

直接在 Groovy 脚本中定义函数并返回 `this`与**类实现方式**在 Jenkins 共享库中都可以工作，但两者存在本质区别，主要体现在设计模式、功能边界和扩展性上：


### 1. **实现方式的本质区别**
- **直接在 Groovy 脚本中定义函数并返回 `this`的方式**：  
  这是一种**"脚本式"实现**，本质上是在一个 Groovy 脚本中直接定义函数(方法)，通过 `return this` 暴露这些函数，让 Pipeline 可以直接调用。  
  这种方式下，脚本会隐式地拥有 Pipeline 上下文(`steps`)，因此可以直接使用 `git`、`echo`、`error` 等 Pipeline 步骤，无需显式传递上下文。

- **类实现方式**：  
  这是一种**"面向对象"实现**，通过定义一个类(如 `Utilities`)，并在类中显式接收 `steps` 参数(通常通过构造函数传入 `this`，即 Pipeline 上下文)，从而在类的方法中使用 Pipeline 步骤。  
  类必须实现 `Serializable` 接口以支持 Jenkins 流水线的暂停/恢复机制。


### 2. **核心差异对比**

| **维度**         | **脚本式**                              		   | **类实现方式**                                  |
|------------------|---------------------------------------------------|---------------------------------------------|
| **上下文获取**   | 隐式拥有 Pipeline 上下文，直接使用 `git`、`echo` 等步骤        | 需显式通过 `steps` 参数接收上下文(如 `steps.sh`)       |
| **代码组织**     | 适合简单逻辑，函数直接定义在脚本中，缺乏类的封装性               | 适合复杂逻辑，通过类的封装、继承等特性组织代码，结构更清晰      |
| **可扩展性**     | 函数之间耦合度高，难以复用和扩展(如继承、多态)               | 支持类的继承、接口实现，便于扩展和复用(如多个类共享基础方法)   |
| **序列化要求**   | 无需显式实现 `Serializable`(脚本本身会被 Jenkins 特殊处理)   | 必须实现 `Serializable` 接口，否则流水线无法正常暂停/恢复    |
| **状态管理**     | 不适合保存复杂状态(如跨步骤的变量)，容易因上下文问题导致异常    | 可通过类的成员变量安全保存状态(需确保变量可序列化)         |


### 3. **适用场景**
- **脚本式**：  
  适合简单的工具类函数(如你示例中的 `checkOutFrom`)，逻辑单一、无需复用或扩展，追求简洁性。  
  例如：单个共享库脚本中定义几个独立的工具方法( checkout、打包、部署等)，直接在 Pipeline 中调用。

- **类实现方式**：  
  适合复杂逻辑或需要复用的场景，例如：  
  - 多个方法需要共享状态(如全局配置、连接对象)；  
  - 需要通过继承扩展功能(如 `BaseUtil` 作为父类，`DeployUtil` 继承并扩展)；  
  - 团队协作中需要规范代码结构，避免函数混乱。


### 总结
脚本式更简单直接，适合快速实现独立功能；而类实现方式更规范、可扩展，适合复杂场景。两者在 Jenkins Pipeline 中都能调用 Pipeline 步骤，核心区别在于**是否通过面向对象的特性(类、继承、状态管理)来组织代码**。

在实际使用中，如果功能简单且无需扩展，脚本式足够；如果功能复杂或团队协作，类实现方式更易维护。
*/
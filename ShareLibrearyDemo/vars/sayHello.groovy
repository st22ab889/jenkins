/*
* 在Groovy中call方法是一种特殊的方法,它支持隐式调用.当对一个对象使用类似函数调用的语法时(如 obj() 或 obj(args)),Groovy 会自动调用该对象的call方法,这一特性是Groovy语言的设计特性
*	
* 	1.Groovy 规定：如果一个类或对象定义了 call 方法，那么可以直接通过 "对象名 + 括号" 的方式调用它. 如下:
* 		// 类定义
*		class MyStep {
*			def call(String msg) {
*				println "Message: ${msg}"
*			}
*		}
*
*		// 调用
*		def step = new MyStep()
*		step("Hello")  // 等价于 step.call("Hello")，隐式调用 call 方法	
*
*	2.在 Jenkins 共享库的 vars 目录下，每个脚本文件(如 sayHello.groovy)都会被 Jenkins 处理为一个单例对象,而脚本中定义的 call 方法会成为这个对象的方法
*   3.不管是在类中还是在 vars 目录下的脚本文件中, 如果将call方法定义为静态方法,将失去 Groovy 的隐式调用特性
*/




/*
* "使用call方法" 以及 "共享库可以定义全局变量" 实现"自定义步骤", 这些自定义步骤(全局)可以像 Jenkins 内置步骤(如 sh、git)一样在流水线中使用.
* 	自定义步骤的定义方式: 在 vars 目录下创建与步骤名同名的 .groovy 文件, 通过实现 call 方法定义步骤逻辑
* 	call 方法的特殊性: 它允许自定义步骤像内置步骤一样被直接调用(如 sayHello() 而非 sayHello.call())
* 	参数支持: 可以定义带参数(包括默认值) 的 call 方法，也可以接收代码块 (Closure), 实现类似 node { ... } 这样的结构化步骤。
* 	命名规范: 文件名(即步骤名)必须全小写或驼峰式(如 sayHello、myCustomStep),否则 Jenkins 可能无法正确识别
*/
def call(String name = 'jenkins') {
	echo "Hello, ${jenkins}"
}




/*
* Closure 对象:
* 	1.Closure是Groovy语言的核心特性,它是Groovy中表示"代码块"的一种对象类型，类似于其他语言中的"匿名函数" 或 "lambda 表达式",并广泛用于Pipeline脚本中(例如作为步骤的参数传递代码块)
*	2.在 Groovy 中, Closure 是一个可以包含代码块的对象,它可以被赋值给变量、作为参数传递给方法,或被直接调用。例如：
*		// 定义一个 Closure (也就是代码块)	
*		def myClosure = { String name ->	
*			println "Hello, ${name}"
*		}
*		// 调用 Closure, myClosure 就是 Closure 类型的对象
*		myClosure("Groovy")  // 输出：Hello, Groovy
*	3.Jenkins Pipeline 大量利用 Groovy 的 Closure 特性来实现"代码块传递",作为 Pipeline 脚本中传递代码块的核心方式,让用户可以像使用内置步骤一样通过代码块定义逻辑。 例如:
*		内置步骤如 node、stage、script 等都接收 Closure 作为参数，用于定义步骤内的逻辑：	
*			node('linux') {  			// 这里的 { ... } 就是一个 Closure
*				sh 'echo "Hello"'
*			}
*		自定义共享库步骤(如 vars 目录下的脚本)也可以接收 Closure 参数,实现类似内置步骤的用法( 如之前提到的 windows { ... } )
*	4.Jenkins 对 Closure 的扩展.虽然 Closure 本身是 Groovy 的类型，但 Jenkins 在 Pipeline 中对其进行了一些适配，例如：
*		在 Pipeline 上下文中，Closure 内可以调用 Jenkins 步骤(如 echo、sh), 这些步骤会被 Jenkins 引擎解析执行
*		支持 Pipeline 特有的语法(如 @NonCPS 注解标记非序列化的 Closure)
*
* call 方法同样支持方法重载
*/
def call(Closure body) {
	body()
}




def callCall(String level, String message){
	echo "${level}: ${message}"
}





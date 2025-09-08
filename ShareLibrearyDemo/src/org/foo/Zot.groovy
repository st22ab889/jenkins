package org.foo;


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
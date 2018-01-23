package jp.harujioh.gradle.daemon

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException

/**
 * Gradle Daemon Plugin
 * 
 * @author harujioh
 */
class DaemonPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.task('hello') {
			println project.extensions.create("daemonConfig", DaemonConfig)
			
			doLast {
				println "hoge=${project.daemonConfig.hoge}, fuga=${project.daemonConfig.fuga}"
			}
		}
	}
}

class DaemonConfig {
	def String hoge = "hoge"
	def int fuga = 5
}

package jp.harujioh.gradle.daemon

/**
 * Gradle Daemon Config
 * 
 * @author harujioh
 */
class DaemonConfig {
	def boolean env = true
	def String name
	def String configKey = 'config.yaml'
	def String config = 'config.yaml'
	def String log4j2 = 'log4j2.xml'
	def option = '-Dapple.awt.UIElement=true'
}
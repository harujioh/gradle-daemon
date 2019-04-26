package jp.harujioh.gradle.daemon

import org.gradle.api.Project
import org.gradle.api.GradleException

/**
 * Gradle Daemon Config
 * 
 * @author harujioh
 */
class DaemonConfig {
	def String configKey = 'config.yaml'
	def String config = 'config.yaml'
	def logFiles = [ 'log4j2.xml' : { p -> "-Dlog4j.configurationFile=${p}" }, 'logback.xml': { p -> "-Dlogback.configurationFile=${p}" } ]
	def option = '-Dapple.awt.UIElement=true'

	/**
	 * デーモンインスタンスを取得します。
	 * @param project プロジェクト
	 * @return デーモンインスタンス
	 */
	public EnvDaemon getDaemon(Project project){
		def os = project.ant.properties['os.name']
		EnvDaemonType daemonType = EnvDaemonType.getDaemonType(os).orElseThrow({ new GradleException("Unsupported OS : $os") })
		return daemonType.newDaemonInstance(project)
	}

	/**
	 * 起動ディレクトリを取得します。
	 * @param project プロジェクト
	 * @return 起動ディレクトリ
	 */
	public File getLaunchDirectory(Project project){
		def launchDir = new File(project.projectDir, 'launch')
		if(project.hasProperty('launch')){
			launchDir = new File(launchDir, project.launch)
		} else {
			def os = project.ant.properties['os.name']
			def EnvDaemonType daemonType = EnvDaemonType.getDaemonType(os).orElseThrow({ new GradleException("Unsupported OS : $os") })
			launchDir = new File(launchDir, daemonType.getDirectoryName())
		}

		if(!launchDir.isDirectory()){
			return null;
		}
		return launchDir;
	}
}
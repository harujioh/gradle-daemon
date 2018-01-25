package jp.harujioh.gradle.daemon

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.GradleException
import jp.harujioh.gradle.daemon.env.*

/**
 * Gradle Daemon Plugin
 * 
 * @author harujioh
 */
class DaemonPlugin implements Plugin<Project> {

	/**
	 * 実行
	 * @param project プロジェクト
	 */
	void apply(Project project) {
		project.extensions.create("daemon", DaemonConfig)

		def os = project.ant.properties['os.name']
		EnvDaemonType daemonType = EnvDaemonType.getDaemonType(os).orElseThrow({ new GradleException("Unsupported OS : $os") })
		EnvDaemon daemon = daemonType.newDaemonInstance(project)
		File launchDir = getLaunchDirectory(project, daemonType)
		
		project.task('loadDaemon', group: 'Daemon', description: 'Launch Running Daemon.', dependsOn: ['jar']) {
			mustRunAfter(['jar'])
			
			doLast {
				daemon.load(launchDir)
			}
		}
		
		project.task('unloadDaemon', group: 'Daemon', description: 'Shutdown Running Daemon.') {
			doLast {
				daemon.unload(launchDir)
			}
		}
		
		project.task('rebootDaemon', group: 'Daemon', description: 'Reboot Running Daemon.') {
			doLast {
				daemon.reboot(launchDir)
			}
		}

		project.extensions.launchDir = launchDir
	}

	/**
	 * 起動ディレクトリを取得します。
	 * @param project プロジェクト
	 * @param daemonType デーモン種類
	 */
	private File getLaunchDirectory(Project project, EnvDaemonType daemonType){
		def launchDir = new File(project.projectDir, 'launch')
		if(project.hasProperty('launch')){
			launchDir = new File(launchDir, project.launch)
		} else if(project.daemon.env) {
			launchDir = new File(launchDir, daemonType.getDirectoryName())
		}

		if(!launchDir.isDirectory()){
			throw new GradleException("Not found launchDir: $launchDir")
		}
		return launchDir;
	}
}

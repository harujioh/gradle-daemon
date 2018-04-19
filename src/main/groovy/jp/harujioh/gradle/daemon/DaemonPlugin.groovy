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
				checkLaunchDirectory(launchDir);
				daemon.load(launchDir, getArguments(project, launchDir))
			}
		}
		
		project.task('unloadDaemon', group: 'Daemon', description: 'Shutdown Running Daemon.') {
			doLast {
				daemon.unload()
			}
		}
		
		project.task('rebootDaemon', group: 'Daemon', description: 'Reboot Running Daemon.') {
			doLast {
				checkLaunchDirectory(launchDir);
				daemon.reboot(launchDir)
			}
		}

		if(launchDir != null){
			project.extensions.launchDir = launchDir
		}
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
			return null;
		}
		return launchDir;
	}

	/**
	 * 起動ディレクトリが設定されているかチェックします。
	 * @param launchDir 起動ディレクトリ
	 */
	private void checkLaunchDirectory(File launchDir){
		if(launchDir == null){
			throw new GradleException("Not found launchDir: $launchDir")
		}
	}

	/**
	 * 起動コマンド引数を取得します。
	 * @param project プロジェクト
	 * @param launchDir 起動ディレクトリ
	 */
	private def getArguments(Project project, File launchDir){
        def configFile = new File(launchDir, project.daemon.config)
        def log4j2File = new File(launchDir, project.daemon.log4j2)

		return [
            project.daemon.option,
            "-D${project.daemon.configKey}=${configFile}",
            "-Dlog4j.configurationFile=${log4j2File}",
            "-jar",
            "${project.jar.archivePath}",
            project.hasProperty('daemonArgs') ? project.daemonArgs.split(' ') : []
        ];
	}
}

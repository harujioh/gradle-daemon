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

		EnvDaemon daemon = project.daemon.getDaemon(project)
		File launchDir = project.daemon.getLaunchDirectory(project)
		
		project.task('exe', group: 'Application', description: 'Create launch application.', dependsOn: ['jar']) {
			mustRunAfter(['jar'])
			
			doLast {
				checkLaunchDirectory(launchDir);
				daemon.exe(project.hasProperty('wakeup'), launchDir, getArguments(project, launchDir))
			}
		}
		
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
        def logbackFile = new File(launchDir, project.daemon.logback)

		return [
            project.daemon.option,
            "-D${project.daemon.configKey}=${configFile}",
            log4j2File.exists() ? "-Dlog4j.configurationFile=${log4j2File}" : [],
            logbackFile.exists() ? "-Dlogback.configurationFile=${logbackFile}" : [],
            "-jar",
            "${project.jar.archivePath}",
            project.hasProperty('daemonArgs') ? project.daemonArgs.split(' ') : []
        ];
	}
}

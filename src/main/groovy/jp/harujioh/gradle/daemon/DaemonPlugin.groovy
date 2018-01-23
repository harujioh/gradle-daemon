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
	void apply(Project project) {
		project.extensions.create("daemon", DaemonConfig)
		if(!project.daemon.name){
			project.daemon.name project.rootProject.name
		}

		def EnvDaemon daemon
		def os = project.ant.properties['os.name']
		if(os == 'Mac OS X') {
			daemon = new MacDaemon(project)
		} else {
			throw new GradleException("Unsupported OS : $os")
		}
		
		project.task('loadDaemon', group: 'Daemon', description: 'Launch Running Daemon.', dependsOn: ['jar']) {
			mustRunAfter(['jar'])
			
			doLast {
				def launchDir = getLaunchDirectory(project, daemon)

				daemon.load(launchDir)
			}
		}
		
		project.task('unloadDaemon', group: 'Daemon', description: 'Shutdown Running Daemon.') {
			doLast {
				def launchDir = getLaunchDirectory(project, daemon)

				daemon.unload(launchDir)
			}
		}
	}

	private File getLaunchDirectory(Project project, EnvDaemon daemon){
		def launchDir = new File(project.projectDir, 'launch')
		if(project.hasProperty('launch')){
			launchDir = new File(launchDir, project.launch)
		} else if(project.daemon.env) {
			launchDir = new File(launchDir, daemon.getLaunchDirectoryName())
		}

		if(!launchDir.isDirectory()){
			throw new GradleException("Not found launchDir: $launchDir")
		}
		return launchDir;
	}
}

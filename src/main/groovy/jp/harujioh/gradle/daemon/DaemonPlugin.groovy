package jp.harujioh.gradle.daemon

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.GradleException

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
		
		project.task('loadDaemon', group: 'Daemon', description: 'Launch Running Daemon.', dependsOn: ['jar']) {
			mustRunAfter(['jar'])
			
			doLast {
				def launchDir = new File(project.projectDir, 'launch')
				if(project.hasProperty('launch')){
					launchDir = new File(launchDir, project.launch)
				} else if(project.daemon.env) {
					if(ant.properties['os.name'] == 'Mac OS X') {
						launchDir = new File(launchDir, 'macos')
					} else if (ant.properties['os.name'] == 'Linux') {
						launchDir = new File(launchDir, 'linux')
					}
				}
				if(!launchDir.isDirectory()){
					throw new GradleException("Not found launchDir: $launchDir")
				}

				if(ant.properties['os.name'] == 'Mac OS X'){
					loadDaemonOnMac(project, launchDir)
				}
			}
		}
		
		project.task('unloadDaemon', group: 'Daemon', description: 'Shutdown Running Daemon.') {
			doLast {
				def launchDir = new File(project.projectDir, 'launch')
				if(project.hasProperty('launch')){
					launchDir = new File(launchDir, project.launch)
				} else if(project.daemon.env) {
					if(ant.properties['os.name'] == 'Mac OS X') {
						launchDir = new File(launchDir, 'macos')
					} else if (ant.properties['os.name'] == 'Linux') {
						launchDir = new File(launchDir, 'linux')
					}
				}
				if(!launchDir.isDirectory()){
					throw new GradleException("Not found launchDir: $launchDir")
				}

				if(ant.properties['os.name'] == 'Mac OS X'){
					unloadDaemonOnMac(project, launchDir)
				}
			}
		}
	}

	private static void loadDaemonOnMac(Project project, File launchDir){
        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = project.group + '.' + project.daemon.name;
        def plistFile = new File(plistDir, plistName + '.plist');

        def configFile = new File(launchDir, project.daemon.config)
        def log4j2File = new File(launchDir, project.daemon.log4j2)

        if(!plistDir.isDirectory()){
            plistDir.mkdir()
        }

        if(plistFile.isFile()){
            ['launchctl', 'unload', plistFile].execute()
        }

        plistFile.text = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
<dict>
<key>Label</key>
<string>$plistName</string>
<key>ProgramArguments</key>
<array>
    <string>/usr/bin/java</string>
    <string>-Dapple.awt.UIElement=true</string>
    <string>-D${project.daemon.configKey}=${configFile}</string>
    <string>-Dlog4j.configurationFile=${log4j2File}</string>
    <string>-jar</string>
    <string>${project.jar.archivePath}</string>
</array>
<key>RunAtLoad</key>
<true/>
<key>KeepAlive</key>
<true/>
</dict>
</plist>"""

        ['launchctl', 'load', plistFile].execute()
	}

	private static void unloadDaemonOnMac(Project project, File launchDir){
        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = project.group + '.' + project.daemon.name;
        def plistFile = new File(plistDir, plistName + '.plist');

        if(plistFile.isFile()){
            ['launchctl', 'unload', plistFile].execute()
        }
	}
}

class DaemonConfig {
	def boolean env = true
	def String name
	def String configKey = 'config.yaml'
	def String config = 'config.yaml'
	def String log4j2 = 'log4j2.xml'
}

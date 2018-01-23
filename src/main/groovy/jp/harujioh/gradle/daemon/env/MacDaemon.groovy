package jp.harujioh.gradle.daemon.env

import org.gradle.api.Project
import jp.harujioh.gradle.daemon.EnvDaemon

/**
 * Gradle EnvDaemon(Mac)
 * 
 * @author harujioh
 */
class MacDaemon implements EnvDaemon {

	/**
	 * プロジェクト
	 */
	private final Project project

	/**
	 * {@inheritDoc}
	 */
	public MacDaemon(Project project){
		this.project = project
	}

    /**
     * {@inheritDoc}
     */
    public String getDaemonName(){
        if(project.properties['appDaemonName']){
            return project.appDaemonName.replaceAll(' ', '')
        }
        return project.rootProject.name.replaceAll(' ', '');
    }

	/**
	 * {@inheritDoc}
	 */
	public void load(File launchDir){
        unload(launchDir)

        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = project.group + '.' + getDaemonName();
        def plistFile = new File(plistDir, plistName + '.plist');

        def configFile = new File(launchDir, project.daemon.config)
        def log4j2File = new File(launchDir, project.daemon.log4j2)

        if(!plistDir.isDirectory()){
            plistDir.mkdir()
        }

        def option = ([
            project.daemon.option,
            "-D${project.daemon.configKey}=${configFile}",
            "-Dlog4j.configurationFile=${log4j2File}",
            "-jar",
            "${project.jar.archivePath}"
        ].flatten().collect{ return "\n    <string>$it</string>" }.join())

        plistFile.text = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
<dict>
<key>Label</key>
<string>$plistName</string>
<key>ProgramArguments</key>
<array>
    <string>/usr/bin/java</string>$option
</array>
<key>RunAtLoad</key>
<true/>
<key>KeepAlive</key>
<true/>
</dict>
</plist>"""

        ['launchctl', 'load', plistFile].execute()
	}

	/**
	 * {@inheritDoc}
	 */
	public void unload(File launchDir){
        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = project.group + '.' + getDaemonName();
        def plistFile = new File(plistDir, plistName + '.plist');

        if(plistFile.isFile()){
            ['launchctl', 'unload', plistFile].execute()

            sleep 2000

            plistFile.delete()
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public void reboot(File launchDir){
        unload(launchDir)
        sleep 2000
        load(launchDir)
	}
}

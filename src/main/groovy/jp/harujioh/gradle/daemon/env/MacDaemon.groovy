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
	public void load(File launchDir){
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

        def option = ([project.daemon.option].flatten().collect{ return "    <string>$it</string>\n" }.join())

        plistFile.text = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
<dict>
<key>Label</key>
<string>$plistName</string>
<key>ProgramArguments</key>
<array>
    <string>/usr/bin/java</string>
$option    <string>-D${project.daemon.configKey}=${configFile}</string>
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

	/**
	 * {@inheritDoc}
	 */
	public void unload(File launchDir){
        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = project.group + '.' + project.daemon.name;
        def plistFile = new File(plistDir, plistName + '.plist');

        if(plistFile.isFile()){
            ['launchctl', 'unload', plistFile].execute()
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public void reboot(File launchDir){
        load(launchDir)
        sleep 2000
        unload(launchDir)
	}
}

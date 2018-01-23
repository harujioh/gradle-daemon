package jp.harujioh.gradle.daemon.env

import org.gradle.api.Project
import jp.harujioh.gradle.daemon.EnvDaemon

/**
 * Gradle EnvDaemon(Mac)
 * 
 * @author harujioh
 */
class MacDaemon implements EnvDaemon {

	private final Project _project

	public MacDaemon(Project project){
		_project = project
	}

	public String getLaunchDirectoryName(){
		return 'macos'
	}

	public void load(File launchDir){
        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = _project.group + '.' + _project.daemon.name;
        def plistFile = new File(plistDir, plistName + '.plist');

        def configFile = new File(launchDir, _project.daemon.config)
        def log4j2File = new File(launchDir, _project.daemon.log4j2)

        if(!plistDir.isDirectory()){
            plistDir.mkdir()
        }

        if(plistFile.isFile()){
            ['launchctl', 'unload', plistFile].execute()
        }

        def option = ([_project.daemon.option].flatten().collect{ return "    <string>$it</string>\n" }.join())

        plistFile.text = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
<dict>
<key>Label</key>
<string>$plistName</string>
<key>ProgramArguments</key>
<array>
    <string>/usr/bin/java</string>
$option    <string>-D${_project.daemon.configKey}=${configFile}</string>
    <string>-Dlog4j.configurationFile=${log4j2File}</string>
    <string>-jar</string>
    <string>${_project.jar.archivePath}</string>
</array>
<key>RunAtLoad</key>
<true/>
<key>KeepAlive</key>
<true/>
</dict>
</plist>"""

        ['launchctl', 'load', plistFile].execute()
	}

	public void unload(File launchDir){
        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = _project.group + '.' + _project.daemon.name;
        def plistFile = new File(plistDir, plistName + '.plist');

        if(plistFile.isFile()){
            ['launchctl', 'unload', plistFile].execute()
        }
	}

	public void start(){

	}

	public void stop(){

	}

	public void restart(){

	}
}

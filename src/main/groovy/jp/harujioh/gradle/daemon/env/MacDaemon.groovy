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
    public void exe(File launchDir, Object[] arguments){
        def exeDir = new File(System.properties['user.home'], 'Desktop')
        def exeFile = new File(exeDir, 'launch.command');

        def option = arguments.flatten().collect{ return " \\\n$it" }.join()

        if(!exeDir.isDirectory()){
            exeDir.mkdir()
        }

        exeFile.text = """#!/bin/sh

/usr/bin/java$option"""

        ['chmod', 'a+x', exeFile].execute()
        ['/bin/bash', '-c', 'osascript -e "tell application \\"System Events\\" to make login item at end with properties {path:\\"' + exeFile + '\\"}"'].execute()
    }

	/**
	 * {@inheritDoc}
	 */
	public void load(File launchDir, Object[] arguments){
        unload()

        def plistDir = new File(System.properties['user.home'], '/Library/LaunchAgents')
        def plistName = project.group + '.' + getDaemonName();
        def plistFile = new File(plistDir, plistName + '.plist');

        def option = arguments.flatten().collect{ return "\n    <string>$it</string>" }.join()

        if(!plistDir.isDirectory()){
            plistDir.mkdir()
        }

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
	public void unload(){
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
        unload()
        sleep 2000
        load(launchDir)
	}
}

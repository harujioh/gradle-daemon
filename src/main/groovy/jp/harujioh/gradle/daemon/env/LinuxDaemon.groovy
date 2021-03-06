package jp.harujioh.gradle.daemon.env

import org.gradle.api.Project
import jp.harujioh.gradle.daemon.EnvDaemon

/**
 * Gradle EnvDaemon(Linux)
 * 
 * @author harujioh
 */
class LinuxDaemon implements EnvDaemon {

	/**
	 * プロジェクト
	 */
	private final Project project

	/**
	 * {@inheritDoc}
	 */
	public LinuxDaemon(Project project){
		this.project = project
	}

    /**
     * {@inheritDoc}
     */
    public String getDaemonName(){
        if(project.properties['appLinuxDaemonName']){
            return project.appLinuxDaemonName.replaceAll(' ', '')
        }
        return project.rootProject.name.replaceAll(' ', '').toLowerCase() + 'd';
    }

    /**
     * {@inheritDoc}
     */
    public void exe(boolean wakeup, File launchDir, Object[] arguments){
        def exeDir = new File(System.properties['user.home'])
        def exeFile = new File(exeDir, 'launch.sh');

        def option = arguments.flatten().collect{ return " \\\n$it" }.join()

        if(!exeDir.isDirectory()){
            exeDir.mkdir()
        }

        exeFile.text = """#!/bin/sh

/usr/bin/java$option"""

        ['chmod', 'a+x', exeFile].execute()
    }

	/**
	 * {@inheritDoc}
	 */
	public void load(File launchDir, Object[] arguments){
        unload()

        def daemonName = getDaemonName();
        def daemonFile = new File(launchDir, daemonName)
        def daemonLinkFile = new File('/etc/init.d/', daemonName)

        def option = arguments.flatten().collect{ return " \\\n${it}" }.join()

        daemonFile.text = """#!/bin/sh
#/etc/init.d/${daemonName}

### BEGIN INIT INFO
# Provides: ${daemonName}
# Required-Start:
# Required-Stop:
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Start ${project.rootProject.name}
# Description: Start ${project.rootProject.name}
### END INIT INFO

PID_FILE=/var/run/${daemonName}.pid
PROG=/usr/bin/java
PROG_ARG=\"${option}\"

start(){
  echo \"Starting ${project.rootProject.name}\"
  start-stop-daemon --start --pidfile \$PID_FILE --make-pidfile --background --exec \$PROG -- \$PROG_ARG
}

stop(){
  echo \"Stopping ${project.rootProject.name}\"
  start-stop-daemon --stop --oknodo --pidfile \$PID_FILE
}

case \"\$1\" in
    start)
        start
        ;;

    stop)
        stop
        ;;

    restart)
        stop
        sleep 1
        start
        ;;

    *)
        echo \"Usage: /etc/init.d/${daemonName} {start|stop|restart}\"
        exit 1
        ;;
esac

exit 0"""

        ['chmod', '755', daemonFile.absolutePath].execute()
        ['sudo', 'mv', daemonFile.absolutePath, daemonLinkFile.absolutePath].execute()
        ['sudo', daemonLinkFile.absolutePath, 'start'].execute()
        ['sudo', 'insserv', getDaemonName()].execute()
	}

	/**
	 * {@inheritDoc}
	 */
	public void unload(){
        def daemonName = getDaemonName();
        def daemonLinkFile = new File('/etc/init.d/', daemonName)

        ['sudo', 'insserv', '-r', getDaemonName()]

        if(daemonLinkFile.isFile()){
            ['sudo', daemonLinkFile.absolutePath, 'stop'].execute()

            sleep 2000
            
            ['sudo', 'rm', daemonLinkFile].execute()
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

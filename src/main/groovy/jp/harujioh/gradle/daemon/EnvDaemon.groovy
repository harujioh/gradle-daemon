package jp.harujioh.gradle.daemon

import org.gradle.api.Project

/**
 * Gradle EnvDaemon
 * 
 * @author harujioh
 */
interface EnvDaemon {

	String getLaunchDirectoryName()

	void load(File launchDir)

	void unload(File launchDir)

	void start()

	void stop()

	void restart()
}

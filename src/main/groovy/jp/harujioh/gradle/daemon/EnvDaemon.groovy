package jp.harujioh.gradle.daemon

import org.gradle.api.Project

/**
 * Gradle EnvDaemon
 * 
 * @author harujioh
 */
interface EnvDaemon {

	/**
	 * デーモン起動を開始します。
	 * @param launchDir 起動ディレクトリ
	 */
	void load(File launchDir)

	/**
	 * デーモン起動を停止します。
	 * @param launchDir 起動ディレクトリ
	 */
	void unload(File launchDir)

	/**
	 * デーモンを再起動します。
	 */
	void reboot(File launchDir)
}

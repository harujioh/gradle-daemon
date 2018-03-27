package jp.harujioh.gradle.daemon

import org.gradle.api.Project

/**
 * Gradle EnvDaemon
 * 
 * @author harujioh
 */
interface EnvDaemon {

	/**
	 * デーモン名を取得します。
	 * @return デーモン名
	 */
	String getDaemonName()

	/**
	 * デーモン起動を開始します。
	 * @param launchDir 起動ディレクトリ
	 * @param arguments 起動コマンド引数
	 */
	void load(File launchDir, Object[] arguments)

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

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
	 * 起動できるアプリケーションを作成します。
	 * @param launchDir 起動ディレクトリ
	 * @param arguments 起動コマンド引数
	 * @param wakeup 起動時にアプリを実行するフラグ
	 */
	void exe(File launchDir, Object[] arguments, boolean wakeup)

	/**
	 * デーモン起動を開始します。
	 * @param launchDir 起動ディレクトリ
	 * @param arguments 起動コマンド引数
	 */
	void load(File launchDir, Object[] arguments)

	/**
	 * デーモン起動を停止します。
	 */
	void unload()

	/**
	 * デーモンを再起動します。
	 */
	void reboot(File launchDir)
}

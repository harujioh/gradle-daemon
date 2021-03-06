package jp.harujioh.gradle.daemon

import org.gradle.api.Project
import java.util.function.Predicate
import java.util.function.Function
import jp.harujioh.gradle.daemon.env.*

/**
 * Gradle EnvDaemon Type
 * 
 * @author harujioh
 */
enum EnvDaemonType {

	/**
	 * ant.properties['os.name'] = 'Mac OS X'
	 */
	MAC({ s -> s.equals('Mac OS X') }, 'macos', { p -> new MacDaemon(p) }),

	/**
	 * ant.properties['os.name'] = 'Linux'
	 */
	LINUX({ s -> s.equals('Linux') }, 'linux', { p -> new LinuxDaemon(p) }),

	/**
	 * ant.properties['os.name'] = 'Windows*'
	 */
	WINDOWS({ s -> s.startsWith('Windows') }, 'linux', { p -> new MacDaemon(p) }),

	;

	/**
	 * OS名のフィルタ
	 */
	private final Predicate<String> osNameFilter;

	/**
	 * デフォルトの起動ディレクトリ名
	 */
	private final String directoryName;

	/**
	 * デーモンインスタンスを作成するコンストラクタ
	 */
	private final Function<Project, EnvDaemon> daemonConstructor;

	/**
	 * デーモンの種類を初期化します。
	 * @param osNameFilter ANTで取得できるOS名
	 * @param directoryName デフォルトの起動ディレクトリ名
	 * @param daemonConstructor デーモンインスタンスを作成するコンストラクタ
	 */
	private EnvDaemonType(Predicate<String> osNameFilter, String directoryName, Function<Project, EnvDaemon> daemonConstructor) {
		this.osNameFilter = osNameFilter;
		this.directoryName = directoryName;
		this.daemonConstructor = daemonConstructor;
	}

	/**
	 * OS名からEnvDaemonTypeを取得します。
	 * @param osName OS名
	 * @return EnvDaemonType
	 */
	public static Optional<EnvDaemonType> getDaemonType(String osName){
		return Arrays.stream(EnvDaemonType.values()) //
			.filter({ t -> t.osNameFilter.test(osName) }) //
			.findAny();
	}

	/**
	 * デフォルトの起動ディレクトリ名を取得します。
	 * @return デフォルトの起動ディレクトリ名
	 */
	public String getDirectoryName(){
		return this.directoryName;
	}

	/**
	 * デーモンインスタンスを作成します。
	 * @return デーモンインスタンス
	 */
	public EnvDaemon newDaemonInstance(Project project){
		return this.daemonConstructor.apply(project);
	}
}

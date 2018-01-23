# gradle-daemon

Javaアプリケーションをデーモン登録するGradleプラグイン


## Javaバージョン

* Java Platform version 8以上


## ビルド

ビルドツールに Gradleを使っている。Gradlewを使うことで、JDKが入った環境であればビルドできる。
以下のコマンドを実行すると、Jarでパッケージングされて `/build/libs/gradle-daemon-{version}.jar` に配置される。

```
./gradlew build
```

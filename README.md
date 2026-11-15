# Chest to Vault (`chest2vault`)

構造物の未開封ルートチェストを、ルートテーブルを引き継いだままバニラの Vault に変換する Mod。
設計は [docs/plan.md](docs/plan.md) を参照。

- Minecraft 1.21.1 / NeoForge 21.1.265 以上

## 必要なもの

- JDK 21 以上（ビルドに使う Java 21 は Gradle が自動で探す。見つからなければダウンロードする）

## ビルド

```sh
./gradlew build
```

成果物：`build/libs/chest2vault-<version>.jar`。Minecraft の `mods/` に入れて使う。

## 開発用の起動

| コマンド | 内容 |
|---|---|
| `./gradlew runClient` | クライアントを起動する |
| `./gradlew runServer` | サーバーを起動する（`--nogui`）。初回は `run/eula.txt` を `eula=true` にする |

開発用の起動は `run/` を作業フォルダにする（git では除外）。

## 確認

| コマンド | 内容 |
|---|---|
| `./gradlew check` | format（`spotlessCheck`）・lint（`-Xlint:all -Werror`）・JUnit（`src/test`） |
| `./gradlew spotlessApply` | 書式を直す |
| `./gradlew runGameTestServer` | GameTest（`src/gametest`）。サーバーを起動して変換を判定する。`check` には含めない |

`src/test` と `src/gametest` は成果物の jar に入らない。

## 設定の場所

| 設定 | 場所 |
|---|---|
| ブラックリスト（from） | `config/chest2vault-common.toml` の `blacklist` |
| 変換の規則（to） | データパックの `data/<名前空間>/chest2vault/conversion_rule/*.json` |

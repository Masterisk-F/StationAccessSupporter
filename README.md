# StationAccessSupporter

現在地の最寄り駅をリアルタイムで通知するAndroidアプリです。「駅メモ！」などの位置情報ゲームでの使用を想定して開発しました。



## 必須要件
本アプリを使用するためには、以下の権限の許可が必要です。
*   位置情報
*   通知
*   使用状況へのアクセス (使用履歴にアクセスできるアプリ)
    *   「駅メモ！」アプリが起動中（フォアグラウンド）かどうかを判定し、起動中の場合に通知の振動を抑制するために使用します。
    *   この機能はオプションであり、設定画面から権限を許可した場合のみ有効になります。

## 開発の背景
もともと作者が個人的に使用するために作成していたアプリです。
類似アプリである「最寄り駅サーチ」が更新を停止しているため、代替手段として公開することにしました。

## 現状と予定
現在、アプリ公開先を検討中です（F-Droidなどを想定しています）。

## クレジット
本アプリでは、以下の駅データを使用しています。
*   [Seo-4d696b75/station_database](https://github.com/Seo-4d696b75/station_database)

## ライセンス
本ソフトウェアは、以下のライセンスの下で提供されています。

*   **ソースコード**: [GNU General Public License v3.0 (GPLv3)](./LICENSE)
*   **駅データ**: [クリエイティブ・コモンズ 表示 - 継承 4.0 国際 ライセンス (CC BY-SA 4.0)](http://creativecommons.org/licenses/by-sa/4.0/)
    <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="クリエイティブ・コモンズ・ライセンス" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png" /></a>


---

### English Introduction
**StationAccessSupporter** is an Android application that provides real-time notifications of the nearest railway station in Japan. It is primarily designed as a support tool for location-based games such as "Station Memories!" (Eki-memo!).

[NOTICE] This app covers ONLY railway stations in JAPAN. The user interface is available ONLY in JAPANESE.

---
# 家計簿アプリケーション

---

## 1. システムアーキテクチャ & セキュリティ機構

本システムは、未認証ユーザーからリソースを保護するための堅牢なセッション認証と、データベースの安全性を担保する設計が組み込まれています。

### セキュリティと認証の仕組み
- **`AuthInterceptor` と `SessionUtil`**: 
  すべてのリクエストはインターセプターによって監視されます。セッション操作は `SessionUtil` に集約（`LOGIN_USER_ID_KEY` の一元管理）されており、セッションが存在しない場合は強制的に `/login` へリダイレクトされます（`/login`, `/register`, 静的ファイルは除外）。
- **パスワードのハッシュ化 (`BCryptPasswordEncoder`)**:
  データベースに生のパスワードを保存せず、`UserService` 内で登録時にハッシュ化（`encode`）、ログイン時に照合（`matches`）を行う実践的なセキュリティ要件を満たしています。

### 画面遷移と主要機能フロー
1. **未認証アクセス** -> `/login` へ自動リダイレクト
2. **アカウント作成 (`/register`)** - 重複チェック（`DuplicateEmailException`）
   - **[重要]** ユーザー登録成功時、同時にそのユーザー専用の**デフォルトカテゴリ（食費、交通費、光熱費、給与、その他）が自動生成**されます。
   - 即時ログインされ `/transactions` へ遷移。
3. **メイン機能（認証済）**:
   - `/transactions` (収支データ一覧・ダッシュボード：収入/支出の合計表示)
   - `/transactions/new` または `/{id}/edit` (入力バリデーション付き新規・編集フォーム)
   - `/categories` (カテゴリマスタ管理：制約付き削除機能)
   - `/summary` (年月ごとの動的集計レポート)
   - `/logout` (POST送信による安全なセッション破棄)

---

## 2. 実技試験で加点される「実装のハイライト」

コントローラーを肥大化させず（ファットコントローラーの回避）、Service層にビジネスロジックを適切に分離している点が本テンプレートの最大の強みです。

### ① トランザクション制御と初期データの確実な生成 (`UserService`)
- 新規ユーザー登録時、単にユーザーを保存するだけでなく、初期マスタデータ（カテゴリ）を同時作成します。
- **JPAの罠の回避**: Cascade（連動保存）に頼らず、まず `userRepository.save(user)` で親のIDを確定させてから、子であるカテゴリに明示的にセットして `categoryRepository.saveAll()` を実行しています。これにより、試験本番での原因不明の「IDがnull」エラーを完全に防ぎます（`@Transactional` によって途中失敗時は全ロールバックされます）。

### ② Stream API を用いたスマートな集計処理 (`TransactionService`)
- 収支一覧画面の上部に表示する「収入合計」「支出合計」の計算は、データベースに都度SUMクエリを投げるのではなく、取得済みのリストに対して Java 8 の Stream API を使用して高速に算出しています。
  ```java
  public int calcTotalIncome(List<Transaction> list) {
      return list.stream()
                 .filter(t -> "income".equals(t.getType()))
                 .mapToInt(Transaction::getAmount).sum();
  }
  ```
- これによりコントローラー側の記述が極めてシンプルになり、可読性が向上しています。

### ③ LinkedHashMap による順序保持グループ化 (`SummaryService`)
- 月次集計画面（`/summary`）では、SQLの `GROUP BY` を使わず、Service層でデータを構築しています。
- 日付降順で取得したデータを `LinkedHashMap` に格納していくことで、最新の月が常に上に表示される順序をメモリ上で保証しています（通常の `HashMap` では順序が崩れてしまう問題を回避）。

### ④ 整合性を守るマスタ削除ブロック (`CategoryService`)
- 対象カテゴリが1件でも `Transaction`（収支データ）で使われている場合、`transactionRepository.existsByCategoryId()` で事前に検知し、`CategoryInUseException` をスローします。外部キー制約違反によるシステムクラッシュを未然に防ぎ、ユーザーに優しいエラーメッセージを画面に返します。

---

## 3. ディレクトリ・ファイル構成

```text
src/main/
 ├── java/com/example/demo/
 │    ├── config/          # WebMvcConfig (Interceptor登録)
 │    ├── Controller/      # Auth, Category, Summary, Transaction 各コントローラー
 │    ├── Entity/          # User (1), Category (多/1), Transaction (多)
 │    ├── Repository/      # Spring Data JPA インターフェース
 │    ├── Service/
 │    │    ├── UserService.java           # 認証・ハッシュ化・初期マスタ自動生成
 │    │    ├── CategoryService.java       # マスタ管理・削除制約チェック
 │    │    ├── SummaryService.java        # LinkedHashMapによる月次集計レポート
 │    │    └── TransactionService.java    # CRUD・StreamAPIによる合計金額計算
 │    ├── Exception/       # 業務例外 (Authentication, DuplicateEmail, CategoryInUse)
 │    ├── interceptor/     # AuthInterceptor (未認証アクセスのガード)
 │    └── Util/            # SessionUtil (LOGIN_USER_ID_KEY の一元管理)
 │
 └── resources/
      └── templates/       # Thymeleaf + Bootstrap 5 ビュー (layout, auth, transaction 等)
```

---

## 4. データモデル・ER図

- **User** : 利用者。パスワードはBcryptで暗号化され、メールアドレスは一意（Unique）。
- **Category** : ユーザー専用のマスタ。登録時にデフォルト5件が自動生成。
- **Transaction** : 履歴データ。外部キーの重複マッピングを避けるため `insertable = false, updatable = false` を活用し、安全に保存・更新を行います。

```text
  +----------------------+             +--------------------------+
  |        User          |             |         Category         |
  +----------------------+             +--------------------------+
  | id (PK)              | 1         多| id (PK)                  |
  | email (Unique)       |-------------| name                     |
  | password (Hashed)    |             | user_id (FK)             |
  +----------------------+             +--------------------------+
             |                                       |
             | 1                                     | 1
             |                                       |
             |                  多                   | 多
             +---------------------------------------+
             |
      +--------------------------+
      |       Transaction        |
      +--------------------------+
      | id (PK)                  |
      | user_id (FK:ReadOnly)    |
      | category_id (FK:ReadOnly)|
      | amount (>= 1)            |
      | type (income / expense)  |
      +--------------------------+
```

---

## 5. 実技試験 お題別・高速マッピングガイド（カスタマイズ手順）

お題が「家計簿」以外だった場合、以下の表に従ってクラス名・変数名をリネームするだけで、数十分で完成度の高いシステムが組み上がります。

| レイヤー / 本ひな形 | 備品発注システム | 社員・部署管理システム | 会議室・施設予約システム |
| :--- | :--- | :--- | :--- |
| **User (利用者)** | `Employee` (発注社員) | `Admin` (人事管理者) | `Member` (予約会員) |
| **Category (マスタ)** | `Equipment` (備品マスタ) | `Department` (部署マスタ) | `Room` (会議室マスタ) |
| **Transaction (履歴)**| `Order` (発注申請データ) | `EmployeeDetail` (所属社員データ)| `Reservation` (予約申込データ) |
| **初期マスタ自動生成**<br>(`UserService`内) | "PC", "文房具", "書籍" などを<br>登録時に初期生成 | "営業部", "開発部", "総務部" などを<br>登録時に初期生成 | "会議室A", "大ホール" などを<br>登録時に初期生成 |
| **集計Stream API**<br>(`TransactionService`) | `calcTotalQuantity`<br>(発注総数のSUM) | `calcAverageSalary`<br>(基本給のAVERAGE) | `calcTotalUsers`<br>(利用総人数のSUM) |
| **マスタ削除制約例外** | `EquipmentInUseException` | `DepartmentInUseException` | `RoomInUseException` |

### 試験開始直後のアクションプラン
1. **エンティティの置換**: 上記表をもとに `User`, `Category`, `Transaction` をお題に合わせてRename（一括置換）する。
2. **初期データ生成の書き換え**: `UserService.java` 内の `defaultCategoryNames` リストを、お題に合った初期マスタ名（例：「営業部」「開発部」）に変更する。
3. **集計ロジックの書き換え**: `TransactionService.java` の Stream API で計算している箇所を、お題の「合計数量」などに修正する。
4. **テンプレートの微修正**: 既に汎用化されているThymeleafテンプレートの `th:field` や文言をお題に合わせる。

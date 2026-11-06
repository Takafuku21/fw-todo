# DB設計書

## 1. この設計書について

この文書では、ToDoアプリのデータを保存するデータベースの形を定めます。画面の配置や見た目は、別の画面設計書で決めます。

- データベース：MySQL
- テーブル：`todos` の1つ
- 1行：やること1件
- 文字コード：`utf8mb4`（日本語など幅広い文字を保存できる設定）

**用語：** テーブルはデータを表の形で保存する場所、列（カラム）は表の項目、行は登録データ1件です。

## 2. テーブル一覧

| テーブル名 | 保存するもの |
|---|---|
| `todos` | やること、メモ、ジャンル、優先度、期限、完了状態、および登録・更新日時 |

## 3. `todos` テーブル定義

| 列名 | 型 | NULL（空） | 初期値・自動設定 | 内容・ルール |
|---|---|---|---|---|
| `id` | `BIGINT` | 不可 | `AUTO_INCREMENT`（自動で番号を増やす） | 1件を識別する番号。主キー（各行を一意に見分ける列）。 |
| `title` | `VARCHAR(255)` | 不可 | なし | やること。255文字以内。半角・全角の空白だけは登録時に空として扱う。 |
| `detail` | `VARCHAR(255)` | 可 | なし | メモ。任意、255文字以内。 |
| `category` | `VARCHAR(255)` | 不可 | なし | ジャンル。指定された5種類のいずれか。 |
| `priority` | `INT` | 不可 | `2` | 優先度。`1`＝高、`2`＝中、`3`＝低。登録時は中。 |
| `due_date` | `DATE` | 可 | なし | 期限。未設定可。 |
| `completed` | `BOOLEAN` | 不可 | `0` | 完了状態。`0`＝未完了、`1`＝完了。 |
| `created_at` | `DATETIME` | 不可 | 登録時の日時を自動設定 | 登録日時。利用者は入力しない。 |
| `updated_at` | `DATETIME` | 不可 | 登録時に自動設定し、更新時も自動更新 | 最終更新日時。利用者は入力しない。 |

**用語：** `NULL` は「値が入っていない」状態です。`VARCHAR(255)` は最大255文字の文字列、`DATE` は日付、`DATETIME` は日付と時刻を表します。

### 3.1 ジャンルの値

`category` に保存できる値は次の5つです。自由入力はせず、アプリ側でこの中から選びます。

- デザイン
- マーケティング
- プログラミング
- 資格
- 就職活動

### 3.2 優先度と完了状態

| 列 | 保存値 | 画面に表示する言葉 |
|---|---:|---|
| `priority` | `1` | 高 |
| `priority` | `2` | 中 |
| `priority` | `3` | 低 |
| `completed` | `0` | 未完了 |
| `completed` | `1` | 完了 |

## 4. 要件と列の対応

| 要件・扱う情報 | 対応する列 | DBでの扱い |
|---|---|---|
| 1件を見分ける番号、URLの`{id}` | `id` | 自動採番。編集・削除の対象指定に使う。 |
| やることの入力、一覧表示、名前での部分一致検索 | `title` | 必須、最大255文字。検索語を含む行に絞る。 |
| メモの入力・表示 | `detail` | 任意、最大255文字。 |
| ジャンルの入力・表示、完全一致の絞り込み | `category` | 必須。5種類に限定する。 |
| 優先度の入力・表示 | `priority` | 必須。1〜3。初期値は2。 |
| 期限の入力・表示、期限順の並び替え | `due_date` | 任意。近い順を初期表示に使う。 |
| 完了状態の編集・表示 | `completed` | 必須。初期値は未完了（0）。 |
| 登録日時 | `created_at` | MySQLが登録時に自動設定する。 |
| 更新日時 | `updated_at` | MySQLが登録時と更新時に自動設定する。 |
| 登録・編集・削除の操作名と対象番号の記録 | `todos`の列には保存しない | 要件に含まれる記録はアプリケーションログに残す。記録するのは操作名と`id`のみで、`title`と`detail`は含めない。追加テーブルは作らない。 |

## 5. 制約とインデックス

- `id`を主キーにし、各行の番号が重複しないようにします。
- `title`、`category`、`priority`、`completed`、`created_at`、`updated_at`は空にできません。
- `detail`と`due_date`は空にできます。
- ジャンル、優先度、完了状態は、下記DDLの`CHECK`制約（保存できる値を制限するルール）でも許可値を確認します。
- 期限順の一覧表示に備えて`due_date`にインデックス（検索や並べ替えを助ける目次）を設定します。ジャンルで絞り込んで期限順にする場合に備え、`category`と`due_date`の組にも設定します。
- `title`は「入力語を一部に含む」検索です。一般的なB-treeインデックス（値を探すための索引）はこの検索方法には有効でないため、追加しません。

## 6. CREATE TABLE 文（DDL）

以下はMySQL用です。`CHECK`制約を適用するMySQL 8.0.16以降を想定しています。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category
        CHECK (category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')),
    CONSTRAINT chk_todos_priority
        CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todos_completed
        CHECK (completed IN (0, 1)),
    INDEX idx_todos_due_date (due_date),
    INDEX idx_todos_category_due_date (category, due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**用語：** DDLはテーブルを作るSQL文です。`NOT NULL`は空を禁止し、`DEFAULT`は値を指定せずに登録したときの初期値を決めます。`INDEX`は検索・並び替えを助ける索引です。

## 7. この設計で扱わないデータ

利用者は1人でログイン機能がなく、添付ファイル、削除データの復元、完了日時の記録も今回の対象外です。そのため、それらを保存する列やテーブルは作りません。操作結果の一時表示（「登録しました」など）は画面に表示する情報であり、`todos`には保存しません。

#!/bin/sh
set -e

mysql_utf8() {
  mysql --default-character-set=utf8mb4 -uroot -p"$MYSQL_ROOT_PASSWORD" "$@"
}

mysql_utf8 --execute "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DATABASE\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
mysql_utf8 "$MYSQL_DATABASE" < /schema/product.sql
mysql_utf8 "$MYSQL_DATABASE" < /schema/order_main.sql
mysql_utf8 "$MYSQL_DATABASE" < /schema/order_detail.sql
mysql_utf8 "$MYSQL_DATABASE" < /schema/update_proc.sql
mysql_utf8 "$MYSQL_DATABASE" < /schema/db_test_data.sql

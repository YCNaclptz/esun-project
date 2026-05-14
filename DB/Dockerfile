FROM mysql:8.0

COPY product.sql /schema/product.sql
COPY order_main.sql /schema/order_main.sql
COPY order_detail.sql /schema/order_detail.sql
COPY update_proc.sql /schema/update_proc.sql
COPY db_test_data.sql /schema/db_test_data.sql
COPY docker-entrypoint-initdb.d/ /docker-entrypoint-initdb.d/

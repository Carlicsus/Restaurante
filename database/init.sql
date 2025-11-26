CREATE DATABASE IF NOT EXISTS restaurante_dev;
CREATE DATABASE IF NOT EXISTS restaurante_test;
CREATE DATABASE IF NOT EXISTS restaurante_prod;

GRANT ALL PRIVILEGES ON restaurante_test.* TO 'carlicsus'@'%';
GRANT ALL PRIVILEGES ON restaurante_prod.* TO 'carlicsus'@'%';
GRANT ALL PRIVILEGES ON restaurante_dev.* TO 'carlicsus'@'%';
FLUSH PRIVILEGES;
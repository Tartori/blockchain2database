CREATE TABLE IF NOT EXISTS unlock_script_coinbase(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  information VARCHAR(512),
    PRIMARY Key(tx_id,tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES input(tx_id, tx_index) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE = InnoDB;

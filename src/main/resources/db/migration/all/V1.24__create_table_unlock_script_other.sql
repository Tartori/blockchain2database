CREATE TABLE IF NOT EXISTS unlock_script_other(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  script_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES input(tx_id, tx_index),
    FOREIGN KEY(script_id) REFERENCES script(script_id)
)ENGINE = InnoDB;


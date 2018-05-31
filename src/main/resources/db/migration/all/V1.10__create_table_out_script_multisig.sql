CREATE TABLE IF NOT EXISTS out_script_multisig(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  min_keys INT,
  max_keys INT,
    PRIMARY KEY(tx_id, tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES output(tx_id, tx_index) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE = InnoDB;


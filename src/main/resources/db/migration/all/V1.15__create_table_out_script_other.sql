CREATE TABLE IF NOT EXISTS out_script_other(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  script_id BIGINT,
    PRIMARY KEY(tx_id, tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES output(tx_id, tx_index) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY(script_id) REFERENCES script(script_id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS unlock_script_p2raw_pub_key(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  signature_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES input(tx_id, tx_index),
    FOREIGN KEY(signature_id) REFERENCES signature(id)
)ENGINE = InnoDB;


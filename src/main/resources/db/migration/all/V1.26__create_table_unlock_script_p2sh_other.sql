CREATE TABLE IF NOT EXISTS unlock_script_p2sh_other(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  redeem_script_size INT,
  script_id BIGINT,
  redeem_script_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES input(tx_id, tx_index),
    FOREIGN KEY(script_id) REFERENCES script(script_id),
    FOREIGN KEY(redeem_script_id) REFERENCES script(script_id)
)ENGINE = InnoDB;



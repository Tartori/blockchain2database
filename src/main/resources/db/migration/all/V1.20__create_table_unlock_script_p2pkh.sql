CREATE TABLE IF NOT EXISTS unlock_script_p2pkh(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  pubkey_id BIGINT,
  signature_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id, tx_index) REFERENCES input(tx_id, tx_index) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY(pubkey_id) REFERENCES public_key(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY(signature_id) REFERENCES signature(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE = InnoDB;


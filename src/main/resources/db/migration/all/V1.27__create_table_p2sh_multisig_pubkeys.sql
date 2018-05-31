CREATE TABLE IF NOT EXISTS p2sh_multisig_pubkeys(
  tx_id BIGINT,
  tx_index INT,
  idx INT,
  public_key_id BIGINT,
  PRIMARY KEY(tx_id,tx_index,idx,public_key_id),
  FOREIGN KEY(tx_id,tx_index) REFERENCES unlock_script_p2sh_multisig(tx_id,tx_index) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY(public_key_id) REFERENCES public_key(id) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE = InnoDB;

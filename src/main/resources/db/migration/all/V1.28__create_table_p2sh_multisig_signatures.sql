CREATE TABLE IF NOT EXISTS p2sh_multisig_signatures(
   tx_id BIGINT,
   tx_index INT,
   idx INT,
   signature_id BIGINT,
   PRIMARY KEY(tx_id,tx_index,idx,signature_id),
   FOREIGN KEY(signature_id) REFERENCES signature(id) ON UPDATE CASCADE ON DELETE CASCADE,
   FOREIGN KEY(tx_id,tx_index) REFERENCES unlock_script_p2sh_multisig(tx_id,tx_index) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE = InnoDB;
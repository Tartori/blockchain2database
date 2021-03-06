package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

/**
 * Used to turn output scripts into the correct OutputScript class. 
 * 
 * @author niklaus
 */
public class OuputScriptCreator {

	private static final Logger logger = LogManager.getLogger("OutputScriptCreator");
	private static final int MAX_OP_RETURN_SIZE = 80;

	/**
	 * From a passed output, returns a matching OutputScript for the output's script. Whenever possible, a
	 * specific type is chosen, based on the type of the output. If no matching type can be found, a 
	 * OtherScript is returned, if the script cannot be parsed, an InalidScript is returned.
	 * 
	 * @param out the transaction output holding the scrip
	 * @param txId The id of the transaction which the output is part of in the database
	 * @param txIndex the index of the transaction which the output is part of within the block
	 * @return OutputScript, based on the type of script
	 */
	public static OutputScript parseScript(TransactionOutput out, long txId, int txIndex) {
		try {
			byte[] outputBytes = out.getScriptBytes();
			int scriptSize = outputBytes.length;
			Script script = new Script(outputBytes);

			try {
				if (script.isSentToAddress())
					return new P2PKHashScript(script, scriptSize, txId, txIndex);

				if (script.isPayToScriptHash())
					return new P2SHScript(script, scriptSize, txId, txIndex);

				if (script.isSentToRawPubKey()){
					OutputScript outputScript;
					try{
						outputScript = new P2RawPubKeyScript(script, scriptSize, txId, txIndex);
					} catch (IllegalArgumentException e){
						logger.debug("Non standard SENT TO RAW PUB KEY output: " + e.toString() );
						logger.debug("The script looks like so: " + script.toString());
						outputScript = new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_RAWPUBKEY_SPEC);
					}
					
					return outputScript;
				}

				if (script.isSentToMultiSig()){
					OutputScript outputScript;
					try{
						outputScript = new MultiSigScript(script, scriptSize, txId, txIndex);
					} catch (IllegalArgumentException e){
						logger.debug("Non standard SENT TO MULTI SIG output: " + e.toString() );
						logger.debug("The script looks like so: " + script.toString());
						outputScript = new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_MULTISIG_SPEC);
					}
					
					return outputScript;
				}

				// OP_RETURN w/ data >80 Byte is not relayed by core client, thus not a standard transaction...
				if (script.isOpReturn() ) {
						if (script.getChunks().size() == 2) {
							if (script.getChunks().get(1).data != null && script.getChunks().get(1).data.length <= MAX_OP_RETURN_SIZE)
								return new OPReturnScript(script, scriptSize, txId, txIndex);
							else
								return new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_OP_RETURN_SPEC);
						} else
								return new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_OP_RETURN_SPEC);

				}

			} catch (IllegalArgumentException e) {
				logger.debug("There was an error when trying to detect the script type for the following scrip: "
						+ script.toString()
						+ " It will be saved as OtherScript");
				return new OtherScript(script, scriptSize, txId, txIndex);
			}

			return new OtherScript(script, scriptSize, txId, txIndex);

		} catch (ScriptException e) {
			return new InvalidScript();
		}
	}

}

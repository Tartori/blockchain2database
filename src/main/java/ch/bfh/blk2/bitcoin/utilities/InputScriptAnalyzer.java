package ch.bfh.blk2.bitcoin.utilities;

import java.util.List;

import ch.bfh.blk2.bitcoin.producer.BlockFileLoader;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.Block;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;

public class InputScriptAnalyzer {
	
	private static Context context;
	
	public static void main(String[] args) {
		context = new Context(new MainNetParams());
		BlockFileLoader bfl = new BlockFileLoader(new MainNetParams(), BlockFileLoader.getReferenceClientBlockFileList());
		
		int fakeCounter = 0;
		int realCounter = 0;
		int myCounter = 0;
		
		for( Block block: bfl)
			for( Transaction transaction: block.getTransactions())
				for( TransactionInput input: transaction.getInputs()){
					try{
						byte[] scriptBytes = input.getScriptBytes();
						Script script = new Script( scriptBytes );
						if( script.isSentToMultiSig() )
							fakeCounter++;
						for( ScriptChunk scriptChunk: script.getChunks()){
							if( scriptChunk.isOpCode())
								if( scriptChunk.equalsOpCode(174) || scriptChunk.equals(175)){	//OP_CHECKMULTISIG, OP_CHECKMULTISIGVERIFY
									realCounter++;
									continue;
								}
						}
						try{
							List<ScriptChunk> chunks = script.getChunks();
							Script redeemscript = new Script(chunks.get(chunks.size()-1).data);
							if( redeemscript.isSentToMultiSig()){
								myCounter++;
							}
						} catch (Exception e ){
							
						}
					} catch (Exception e ){
						
					}

				}
		
		System.out.println("fakeCounter " + fakeCounter );
		System.out.println("realCounter " + realCounter );
		System.out.println("myCounter " + myCounter );
	}

}

package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

/**
 * Manages the public_key table in the database. If makes sure no duplicate entries are written and makes
 * it easy to add and / or update entries without risk of damaging the data.
 * 
 * @author niklaus
 */
public class PubKeyManager {

	private static final Logger logger = LogManager.getLogger("PubKeyManager");

	private static final String INSERT_PUBKEY = "INSERT INTO public_key (pubkey_hash,pubkey, valid_pubkey) Values (?,?,?)",

	GET_PK_ID = "SELECT id FROM public_key WHERE pubkey_hash = ?",

	UPDATE_PK = "UPDATE public_key" + " SET pubkey = ?" + " WHERE id = ?",

	INSERT_IGNORE_KEY_HASH = "INSERT INTO public_key (pubkey_hash, valid_pubkey) VALUES(?,?)";
	
	private static PubKeyManager instance = null;
	
	private PubKeyManager(){};
	
	public static PubKeyManager getInstance(){
		if( instance == null )
			instance = new PubKeyManager();
		return instance;
	}
	
	private long getPKIdFromPubKeyHash(DatabaseConnection connection, String pkHash) {

		long pkId = -1;

		try {

			PreparedStatement getPKId = connection.getPreparedStatement(GET_PK_ID);
			getPKId.setString(1, pkHash);
			ResultSet resultPkId = getPKId.executeQuery();
			if (resultPkId.next())
				pkId = resultPkId.getLong(1);

			resultPkId.close();

		} catch (SQLException e) {
			logger.fatal("Failed to query adresse [" + pkHash + "]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}

		return pkId;
	}

	/**
	 * This inserts a raw public key. If the key is already inserted, you will just get it's ID back.
	 * If the key's Hash is in the database already but not the key itself, the entry will be updated 
	 * accordingly. If the public key's hash is not in the database yet, it will be formed and also 
	 * inserted together with the public key. If the passed byte sequence is not a valid public key, it
	 * will be written into the database just like that.
	 * 
	 * @param connection DB Connection to be used
	 * @param pkBytes Byte representation of the public key, as read from the Blockchain by Bitcoinj
	 * @return The public key's database id
	 */
	public long insertRawPK(DatabaseConnection connection, byte[] pkBytes) {
		String pkHash = null;
		String pkHex = null;
		boolean valid = true;

		try {
			// ECKey fails to create a key from an empty char array. It throws some other
			// Exception, but I don't trust that is a stable condition.
			if( pkBytes.length == 0 )
				throw new IllegalArgumentException();

			ECKey publicKey = ECKey.fromPublicOnly(pkBytes);
			pkHash = LegacyAddress.fromKey(Utility.PARAMS, publicKey).toString();
			pkHex = publicKey.getPublicKeyAsHex();
		} catch (IllegalArgumentException e) {
			//String keyPrint = new String(pkBytes);
			pkHex = Utils.HEX.encode(pkBytes);

			logger.debug("Unable to create an ECKey from public key " + pkHex + " : " + e.toString() );
			logger.debug("Will write in a hex represenattion of the keybytes instead and mark the key as invalid.");

			byte[] pubKeyHash = Utils.sha256hash160(pkBytes);
			pkHash = LegacyAddress.fromPubKeyHash(Utility.PARAMS, pubKeyHash).toString();
			valid = false;
		}

		if (pkHash == null || pkHex == null) {
			logger.fatal("Despite our best efforts, we were unable to create a public key and address from ["
					+ Utils.HEX.encode(pkBytes)
					+ "]");
			System.exit(1);
		}

		long pkId = -1;

		try {

			pkId = getPKIdFromPubKeyHash(connection, pkHash);

			if (pkId != -1) {

				PreparedStatement updatePK = connection.getPreparedStatement(UPDATE_PK);
				updatePK.setString(1, pkHex);
				updatePK.setLong(2, pkId);
				updatePK.executeUpdate();
			} else {
				PreparedStatement insertPK = connection.getPreparedStatement(INSERT_PUBKEY);
				insertPK.setString(1, pkHash);
				insertPK.setString(2, pkHex);
				insertPK.setBoolean(3, valid);
				insertPK.executeUpdate();

				ResultSet generatedKeys = insertPK.getGeneratedKeys();
				if (generatedKeys.next())
					pkId = generatedKeys.getLong(1);
				else {
					logger.fatal("Bad generatedKeySet from Address [" + pkHash + "]");
					connection.commit();
					connection.closeConnection();
					System.exit(1);
				}

				generatedKeys.close();
			}

		} catch (SQLException e) {
			logger.fatal("Failed to Insert pubkey and adresse [" + pkHash + "]", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		return pkId;
	}

	/**
	 * Inserts the public key hash (aka Bitcoin Address) into the public key table.
	 * If an entry for that address already exists, it's ID will be returned and the
	 * data in the database will not be change.
	 * 
	 * @param connection Connection to be used
	 * @param pkHash The Base58 encoded Hash of the public key, including the network specific prefix - Aka the Bitcoin address
	 * @return the publickeyhash's database id
	 */
	public long insertPubkeyHash(DatabaseConnection connection, String pkHash) {
		long id = -1;
		id = getPKIdFromPubKeyHash(connection, pkHash);
		if( id == -1 ){
			try {
				PreparedStatement insertAddr = connection.getPreparedStatement(INSERT_IGNORE_KEY_HASH);
				insertAddr.setString(1, pkHash);
				insertAddr.setBoolean(2, true);
				insertAddr.executeUpdate();
				ResultSet generatedKeys = insertAddr.getGeneratedKeys();

				if (generatedKeys.next())
					id = generatedKeys.getLong(1);
				else {
					logger.fatal("Bad generatedKeySet from Address [" + pkHash + "]");
					connection.commit();
					connection.closeConnection();
					System.exit(1);
				}

				generatedKeys.close();
			} catch (SQLException e) {
				logger.fatal("Failed to Insert adresse [" + pkHash + "]", e);
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
		}
		
		return id;
	}
}

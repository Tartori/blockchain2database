package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BlockTransactionOutputWriter {

    private String insertBlockQuery = "INSERT INTO block (blk_id, time) VALUES (?, ?);";
    private String insertTransactionQuery = "INSERT INTO transaction (tx_id, blk_time) VALUES (?, ?);";
    private String insertOutputQuery = "INSERT INTO output (tx_id, tx_index) VALUES (?, ?);";

    private DatabaseConnection connection;
    private long blk_id, tx_id;
    private int tx_index;
    private java.util.Date timestamp;

    public BlockTransactionOutputWriter(DatabaseConnection connection) {
        this.connection = connection;
        blk_id = 1;
        tx_id = 1;
        tx_index = 1;
        timestamp = new java.util.Date(System.currentTimeMillis());
    }

    public void write() {
        try {
            PreparedStatement statementBlock = (PreparedStatement) connection.getPreparedStatement(insertBlockQuery);
            statementBlock.setLong(1, blk_id);
            statementBlock.setTimestamp(2, new java.sql.Timestamp(timestamp.getTime()));
            statementBlock.executeUpdate();
            statementBlock.close();

            PreparedStatement statementTx = (PreparedStatement) connection.getPreparedStatement(insertTransactionQuery);
            statementTx.setLong(1, tx_id);
            statementTx.setTimestamp(2, new java.sql.Timestamp(timestamp.getTime()));
            statementTx.executeUpdate();
            statementTx.close();

            PreparedStatement statementOut = (PreparedStatement) connection.getPreparedStatement(insertOutputQuery);
            statementOut.setLong(1, tx_id);
            statementOut.setInt(2, tx_index);
            statementOut.executeUpdate();
            statementOut.close();

            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
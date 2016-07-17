package whetselsapps.spendingtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.*;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mikey on 6/5/2016.
 */
public class DbConnector {

    //names of tables and columns
    private static final String DB_NAME = "spending",
                                ACT_TABLE = "accounts",
                                ACT_ID = "act_id",
                                ACT_NAME = "name",
                                ACT_TYPE = "type",
                                ACT_BAL = "balance",
                                TRANS_TABLE = "transactions",
                                TRANS_ID = "trans_id",
                                TRANS_DESC = "description",
                                TRANS_AMT = "amount";
    // types of accounts -- refer to as TYPE[*]
    private static final String[] TYPES = {"Debit", "Credit"};
    // call when querying database
    private SQLiteDatabase database;
    // only use to create/update db
    private SQLiteOpenHelper dbOpenHelper;

    // refers to the user specified account and last transaction made
    // for new account or transaction increment relative id
    private int actId, transId;

    /**
     * Constructor. Sets transaction id and open helper for the database.
     * Use when account information is not needed.
     * @param context
     */
    protected DbConnector(Context context) {
        this(context, null);
    }

    /**
     * Constructor. Calls
     * @param context
     * @param accountName
     */
    protected DbConnector(Context context, String accountName) {
        this.dbOpenHelper = new DbOpenHelper(context, DB_NAME, null, 1);
        this.setTransId();

        // check for null account name;
        if ( accountName == null )
            this.actId = 0;
        else
            this.setActId(accountName);

        // Temporary data insertion
        ContentValues actInfo = new ContentValues();
        actInfo.put(ACT_ID, this.actId);
        actInfo.put(ACT_NAME, "Bank of America");
        actInfo.put(ACT_TYPE, TYPES[0]);
        actInfo.put(ACT_BAL, 400.00);

        ContentValues transInfo = new ContentValues();
        transInfo.put(ACT_ID, this.actId);
        transInfo.put(TRANS_ID, this.transId);
        transInfo.put(TRANS_DESC, "first");
        transInfo.put(TRANS_AMT, 0.0);

        this.open();
        this.database.insert(ACT_TABLE, null, actInfo);
        this.database.insert(TRANS_TABLE, null, transInfo);
        this.close();
    }

    /**
     * Get a writable and readable version of the database.
     * @throws SQLException
     */
    private void open() throws SQLException {
        this.database = this.dbOpenHelper.getWritableDatabase();
    }

    /**
     * Close the database to prevent reading or writing.
     */
    private void close() {
        if ( this.database != null )
            this.database.close();
    }

    /**
     * Set the max ids for adding new accounts and transactions to the database.
     */
    private void setTransId() {
        String getTransIdQuery = "SELECT MAX(" + TRANS_ID + ")" +
                " FROM " + TRANS_TABLE + ";";

        this.open();
        Cursor transMax =
                this.database.rawQuery(getTransIdQuery, null);
        this.close();

        if ( transMax.moveToFirst() ) {
            this.transId =
                    transMax.getInt(transMax.getColumnIndex(TRANS_ID));
        }
        else
            this.transId = 0;
    }

    /**
     * Sets the account id to the selected account by the user.
     * @param accountName
     */
    protected void setActId(String accountName) {
        Integer id = this.getAccountByName(accountName);
        if ( id != null )
            this.actId = id;
    }

    /**
     * Increment the account id for a new account.
     */
    private void incrementActId() {
        this.actId += 1;
    }

    /**
     * Increment the transaction table id for a new transaction.
     */
    private void incrementTransactionId() {
        this.transId += 1;
    }

    /**
     * Get the final balance recorded and add the new amount to it.
     * @return the new balance
     */
    private void updateBalance( double transAmount ) {
        //TODO update account table with new balance info
        double balance = 0.0;
        String getCurrentBalanceQuery =
                "SELECT " + ACT_BAL + ", " + ACT_TYPE +
                " FROM " + ACT_TABLE +
                " WHERE " + ACT_ID + " = " + this.actId + ";";

        this.open();

        Cursor getCurrentBalance =
                this.database.rawQuery(getCurrentBalanceQuery, null);

        if ( getCurrentBalance.moveToFirst() )
            balance =
                getCurrentBalance.getDouble(
                        getCurrentBalance.getColumnIndex(ACT_BAL));

        balance += transAmount;

        String updateQuery = "UPDATE " + ACT_TABLE +
                " SET " + ACT_BAL + " = " + balance +
                " WHERE " + ACT_ID + " = " + this.actId + ";";

        this.database.rawQuery(updateQuery, null);
        getCurrentBalance.close();
        this.close();
    }

    /**
     * Adds a new account to the accounts table in the database.
     * TODO check for existing account names and/or ids
     * @param name
     * @param type
     * @param balance
     */
    protected void addNewAccount( String name, Integer type, Double balance ) {
        this.incrementActId();

        ContentValues account = new ContentValues();

        account.put(ACT_ID, this.actId);
        account.put(ACT_NAME, name);
        account.put(ACT_TYPE, this.TYPES[type]);
        account.put(ACT_BAL, balance);

        this.open();
        this.database.insert(ACT_TABLE, null, account);
        this.close();
    }

    /**
     * First gets the new balance according to the amount given (increments
     * max id in in the update balance class) and inputs the new transaction
     * as the last row in the table.
     * @param amount
     * @param description
     */
    protected void addNewTransaction( double amount, String description ) {
        this.incrementTransactionId();

        ContentValues trans = new ContentValues();

        trans.put(ACT_ID, this.actId);
        trans.put(TRANS_ID, this.transId);
        trans.put(TRANS_DESC, description);
        trans.put(TRANS_AMT, amount);

        this.open();
        this.database.insert(TRANS_TABLE, null, trans);
        this.updateBalance(amount);
        this.close();
    }

    /**
     * Deletes the selected transaction by the user
     * @param rowId
     */
    protected void deleteTrans( int rowId ) {
        String deleteQuery = "DELETE FROM " + TRANS_TABLE +
                " WHERE " + ACT_ID + " = " + rowId + ";";
        this.open();
        this.database.execSQL(deleteQuery);
        this.close();
    }

    /**
     * Returns the integer id associated with an account. All id's are unique.
     * @param accountName
     * @return
     */
    private Integer getAccountByName(String accountName) {
        String query = "SELECT " + ACT_ID +
                " FROM " + ACT_TABLE +
                " WHERE " + ACT_NAME +
                    " LIKE " + accountName + ";";

        this.open();
        SQLiteCursor cursor = (SQLiteCursor) this.database.rawQuery(query, null);
        this.close();

        if ( cursor.moveToFirst() )
            return cursor.getInt(cursor.getColumnIndex(ACT_ID));
        return null;
    }

    /**
     * Returns a list containing all account names to be displayed for the user.
     * @return
     */
    protected List<String> getAllAccounts() {
        String query = "SELECT DISTINCT(" + ACT_NAME + ")" +
                " FROM " + ACT_TABLE + ";";
        List<String> list = new ArrayList<String>();
        this.open();
        SQLiteCursor cursor = (SQLiteCursor) this.database.rawQuery(query, null);
        this.close();

        if ( cursor.moveToFirst() ) {
            do {
                list.add(cursor.getString(cursor.getPosition()));
            } while ( cursor.moveToNext() );
        }

        return list;
    }

    /**
     * private inner class used to create the database and open
     * readable and writable versions of the db.
     */
    private class DbOpenHelper extends SQLiteOpenHelper {

        /**
         * Constructor
         * @param context
         * @param dbName
         * @param factory
         * @param version
         */
        public DbOpenHelper(Context context, String dbName,
                            CursorFactory factory, int version) {
            super(context, dbName, factory, version);
        }

        /**
         * Creates the db if the tables don't exist and initializes the first values in the db
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            String createAccountsTable =
                    "CREATE TABLE IF NOT EXISTS " + ACT_TABLE +
                    " (" + ACT_ID + " INT PRIMARY KEY NOT NULL, " +
                        ACT_NAME + " TEXT NOT NULL, " +
                        ACT_TYPE + " TEXT NOT NULL, " +
                        ACT_BAL + " REAL) WITHOUT ROWID;";
            String createTransactionTable =
                    "CREATE TABLE IF NOT EXISTS " + TRANS_TABLE +
                    " (" + ACT_ID + " INT PRIMARY KEY NOT NULL, " +
                        TRANS_ID + " INT PRIMARY KEY NOT NULL, " +
                        TRANS_DESC + " TEXT, " +
                        TRANS_AMT + " REAL) WITHOUT ROWID;";


            db.execSQL(createAccountsTable);
            db.execSQL(createTransactionTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {

        }
    }
}

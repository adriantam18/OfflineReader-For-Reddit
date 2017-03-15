package atamayo.offlinereddit.RedditDAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import atamayo.offlinereddit.RedditAPI.Subreddit;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SUBREDDITS_LIST".
*/
public class SubredditDao extends AbstractDao<Subreddit, Long> {

    public static final String TABLENAME = "SUBREDDITS_LIST";

    /**
     * Properties of entity Subreddit.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DisplayName = new Property(1, String.class, "displayName", false, "DISPLAY_NAME");
        public final static Property Susbscribers = new Property(2, int.class, "susbscribers", false, "SUSBSCRIBERS");
    }


    public SubredditDao(DaoConfig config) {
        super(config);
    }
    
    public SubredditDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SUBREDDITS_LIST\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"DISPLAY_NAME\" TEXT," + // 1: displayName
                "\"SUSBSCRIBERS\" INTEGER NOT NULL );"); // 2: susbscribers
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_SUBREDDITS_LIST_DISPLAY_NAME ON SUBREDDITS_LIST" +
                " (\"DISPLAY_NAME\" ASC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SUBREDDITS_LIST\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Subreddit entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String displayName = entity.getDisplayName();
        if (displayName != null) {
            stmt.bindString(2, displayName);
        }
        stmt.bindLong(3, entity.getSusbscribers());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Subreddit entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String displayName = entity.getDisplayName();
        if (displayName != null) {
            stmt.bindString(2, displayName);
        }
        stmt.bindLong(3, entity.getSusbscribers());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Subreddit readEntity(Cursor cursor, int offset) {
        Subreddit entity = new Subreddit( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // displayName
            cursor.getInt(offset + 2) // susbscribers
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Subreddit entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDisplayName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSusbscribers(cursor.getInt(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Subreddit entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Subreddit entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Subreddit entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
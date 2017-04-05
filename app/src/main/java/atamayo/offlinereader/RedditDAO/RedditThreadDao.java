package atamayo.offlinereader.RedditDAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "REDDIT_THREADS".
*/
public class RedditThreadDao extends AbstractDao<RedditThread, Long> {

    public static final String TABLENAME = "REDDIT_THREADS";

    /**
     * Properties of entity RedditThread.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property WasClicked = new Property(1, boolean.class, "wasClicked", false, "WAS_CLICKED");
        public final static Property FullName = new Property(2, String.class, "fullName", false, "FULL_NAME");
        public final static Property ThreadId = new Property(3, String.class, "threadId", false, "THREAD_ID");
        public final static Property Subreddit = new Property(4, String.class, "subreddit", false, "SUBREDDIT");
        public final static Property Title = new Property(5, String.class, "title", false, "TITLE");
        public final static Property Author = new Property(6, String.class, "author", false, "AUTHOR");
        public final static Property Score = new Property(7, int.class, "score", false, "SCORE");
        public final static Property Ups = new Property(8, int.class, "ups", false, "UPS");
        public final static Property Downs = new Property(9, int.class, "downs", false, "DOWNS");
        public final static Property Selftext = new Property(10, String.class, "selftext", false, "SELFTEXT");
        public final static Property Permalink = new Property(11, String.class, "permalink", false, "PERMALINK");
        public final static Property NumComments = new Property(12, int.class, "numComments", false, "NUM_COMMENTS");
        public final static Property CreatedUTC = new Property(13, long.class, "createdUTC", false, "CREATED_UTC");
        public final static Property Over18 = new Property(14, boolean.class, "over18", false, "OVER18");
    }


    public RedditThreadDao(DaoConfig config) {
        super(config);
    }
    
    public RedditThreadDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"REDDIT_THREADS\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"WAS_CLICKED\" INTEGER NOT NULL ," + // 1: wasClicked
                "\"FULL_NAME\" TEXT," + // 2: fullName
                "\"THREAD_ID\" TEXT," + // 3: threadId
                "\"SUBREDDIT\" TEXT," + // 4: subreddit
                "\"TITLE\" TEXT," + // 5: title
                "\"AUTHOR\" TEXT," + // 6: author
                "\"SCORE\" INTEGER NOT NULL ," + // 7: score
                "\"UPS\" INTEGER NOT NULL ," + // 8: ups
                "\"DOWNS\" INTEGER NOT NULL ," + // 9: downs
                "\"SELFTEXT\" TEXT," + // 10: selftext
                "\"PERMALINK\" TEXT," + // 11: permalink
                "\"NUM_COMMENTS\" INTEGER NOT NULL ," + // 12: numComments
                "\"CREATED_UTC\" INTEGER NOT NULL ," + // 13: createdUTC
                "\"OVER18\" INTEGER NOT NULL );"); // 14: over18
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_REDDIT_THREADS_FULL_NAME ON REDDIT_THREADS" +
                " (\"FULL_NAME\" ASC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"REDDIT_THREADS\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, RedditThread entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getWasClicked() ? 1L: 0L);
 
        String fullName = entity.getFullName();
        if (fullName != null) {
            stmt.bindString(3, fullName);
        }
 
        String threadId = entity.getThreadId();
        if (threadId != null) {
            stmt.bindString(4, threadId);
        }
 
        String subreddit = entity.getSubreddit();
        if (subreddit != null) {
            stmt.bindString(5, subreddit);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(6, title);
        }
 
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(7, author);
        }
        stmt.bindLong(8, entity.getScore());
        stmt.bindLong(9, entity.getUps());
        stmt.bindLong(10, entity.getDowns());
 
        String selftext = entity.getSelftext();
        if (selftext != null) {
            stmt.bindString(11, selftext);
        }
 
        String permalink = entity.getPermalink();
        if (permalink != null) {
            stmt.bindString(12, permalink);
        }
        stmt.bindLong(13, entity.getNumComments());
        stmt.bindLong(14, entity.getCreatedUTC());
        stmt.bindLong(15, entity.getOver18() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, RedditThread entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getWasClicked() ? 1L: 0L);
 
        String fullName = entity.getFullName();
        if (fullName != null) {
            stmt.bindString(3, fullName);
        }
 
        String threadId = entity.getThreadId();
        if (threadId != null) {
            stmt.bindString(4, threadId);
        }
 
        String subreddit = entity.getSubreddit();
        if (subreddit != null) {
            stmt.bindString(5, subreddit);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(6, title);
        }
 
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(7, author);
        }
        stmt.bindLong(8, entity.getScore());
        stmt.bindLong(9, entity.getUps());
        stmt.bindLong(10, entity.getDowns());
 
        String selftext = entity.getSelftext();
        if (selftext != null) {
            stmt.bindString(11, selftext);
        }
 
        String permalink = entity.getPermalink();
        if (permalink != null) {
            stmt.bindString(12, permalink);
        }
        stmt.bindLong(13, entity.getNumComments());
        stmt.bindLong(14, entity.getCreatedUTC());
        stmt.bindLong(15, entity.getOver18() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public RedditThread readEntity(Cursor cursor, int offset) {
        RedditThread entity = new RedditThread( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getShort(offset + 1) != 0, // wasClicked
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // fullName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // threadId
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // subreddit
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // title
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // author
            cursor.getInt(offset + 7), // score
            cursor.getInt(offset + 8), // ups
            cursor.getInt(offset + 9), // downs
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // selftext
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // permalink
            cursor.getInt(offset + 12), // numComments
            cursor.getLong(offset + 13), // createdUTC
            cursor.getShort(offset + 14) != 0 // over18
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, RedditThread entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setWasClicked(cursor.getShort(offset + 1) != 0);
        entity.setFullName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setThreadId(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSubreddit(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setTitle(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAuthor(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setScore(cursor.getInt(offset + 7));
        entity.setUps(cursor.getInt(offset + 8));
        entity.setDowns(cursor.getInt(offset + 9));
        entity.setSelftext(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setPermalink(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setNumComments(cursor.getInt(offset + 12));
        entity.setCreatedUTC(cursor.getLong(offset + 13));
        entity.setOver18(cursor.getShort(offset + 14) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(RedditThread entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(RedditThread entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(RedditThread entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
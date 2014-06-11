package info.guardianproject.mrapp.model;


	import net.sqlcipher.database.SQLiteDatabase;
	import info.guardianproject.mrapp.db.ProjectsProvider;
	import info.guardianproject.mrapp.db.StoryMakerDB;
	import android.net.Uri;

	public class ReportTable extends Table{
	    private final static String TAG = "ReportTable";
	    
	    public ReportTable() {
	        
	    }
	    
	    public ReportTable(SQLiteDatabase db) {
	        super(db);
	    }

	    @Override
	    protected String getTableName() {
	        return StoryMakerDB.Schema.Reports.NAME;
	    }
	    
	    @Override
	    protected String getIDColumnName() {
	        return StoryMakerDB.Schema.Reports.ID;
	    }

	    @Override
	    protected Uri getURI() {
	        return ProjectsProvider.REPORTS_CONTENT_URI;
	    }

	    @Override
	    protected String getProviderBasePath() {
	        return ProjectsProvider.REPORTS_BASE_PATH;
	    }
	}

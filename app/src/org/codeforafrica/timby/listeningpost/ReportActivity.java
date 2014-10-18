package org.codeforafrica.timby.listeningpost;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Cipher;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.api.SyncService;
import org.codeforafrica.timby.listeningpost.encryption.EncryptionService;
import org.codeforafrica.timby.listeningpost.location.GPSTracker;
import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.Project;
import org.codeforafrica.timby.listeningpost.model.Report;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.*;
import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class ReportActivity extends BaseActivity implements OnClickListener,
OnItemLongClickListener{
	
	//private RadioGroup rGroup;
	//private TextView txtNewStoryDesc;
	private EditText editTextStoryName;
	private Spinner spinnerSector;
	private Spinner spinnerIssue;
	private EditText editTextDesc;
	
	private TextView picture_label;
	private TextView video_label;
	private TextView audio_label;
	private TextView gallery_label;
	
	int pics = 0;
	int vids = 0;
	int auds = 0;
	
	int rid;
	String title;
	String issue;
	String sector;
	String description;
	String location;
	String entity;
	Button done;
	Button addEntity;
	String[] allEntities;
	ListView entitiesLV;
    
	ImageView setLocation;
	ImageView view;
	int story_mode;
	TextView gpsInfo;
	GPSTracker gpsT; 
	
	RelativeLayout images;
	RelativeLayout video;
	RelativeLayout audio;
	RelativeLayout gallery;
	int resultMode;
	
    private ArrayList<String> datasource;
    private MyAdapter adapter;
    private Dialog dialog;
    private Dialog dialog_save;
    private Dialog dialog_publish;
    private ToggleButton toggleGPS;
    private Button btnImport;

    public boolean new_report = false;
    
    public RelativeLayout capture_now;
    public RelativeLayout importfromgallery;
    private boolean importing = false;
    private boolean addscreen = false;
    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent i = getIntent();
        rid = i.getIntExtra("rid", -1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        if(rid==-1){
        	
            setContentView(R.layout.activity_report_add);            
            new_report = true;
        	getSupportActionBar().setTitle("Add Media");
        	addscreen = true;
        	
        }else{
        	
        	addscreen = false;
            setContentView(R.layout.activity_report_edit);
        
	        editTextStoryName = (EditText)findViewById(R.id.editTextStoryName);
	        
	        
	        addEntity = (Button)findViewById(R.id.AddEntity);
	        entitiesLV = (ListView)findViewById(R.id.EntitiesList);
	        
	        spinnerSector = (Spinner)findViewById(R.id.spinnerSector);
	        setSectors();        
	        
	        spinnerIssue = (Spinner)findViewById(R.id.spinnerIssue);
	        setCategories();
	        
	        editTextDesc = (EditText)findViewById(R.id.editTextDescription);
	        
	        done = (Button)findViewById(R.id.done);
	        
	        setLocation = (ImageView)findViewById(R.id.imageView4);
	        gpsInfo = (TextView)findViewById(R.id.textViewLocation);
	        
	        //entity
	        datasource = new ArrayList<String>();
	        adapter = new MyAdapter();
	        
	        entitiesLV.setAdapter(adapter);
	        entitiesLV.setOnItemLongClickListener(this);
	        
	        setEntities();
	        addEntity.setOnClickListener(new OnClickListener() {
	
	            @Override
	            public void onClick(View v) {
	            	 dialog = new Dialog(ReportActivity.this);
	            	 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	                 dialog.setContentView(R.layout.dialog_entities);
	                 //Entities autocomplete 
	                 ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
	                         android.R.layout.simple_dropdown_item_1line, allEntities);
	                 AutoCompleteTextView textView = (AutoCompleteTextView)dialog.findViewById(R.id.edit_box);
	                 textView.setAdapter(adapter);
	                dialog.findViewById(R.id.button_cancel).setOnClickListener(
	                        ReportActivity.this);
	                dialog.findViewById(R.id.button_ok).setOnClickListener(
	                        ReportActivity.this);
	                dialog.setTitle("Add Entity");
	                dialog.show();
	            }
	        });        
	        
	        done.setText("Submit");
	       
	        	getSupportActionBar().setTitle("Edit");
	        	Report r = Report.get(this, rid);
	        	
	        	location = r.getLocation();
	        	title = r.getTitle();
	            sector = r.getSector();
	            issue = r.getIssue();
	            entity = r.getEntity();
	            description = r.getDescription();
	            
	            if(!r.getServerId().equals("0")){
	            	done.setText("Update");
	            }
	            
	            if(location.equals("0, 0")){
	        		location = "Location not set";
	        	}
	            editTextStoryName.setText(title);
	            spinnerSector.setSelection(Integer.parseInt(sector));
	           
	            spinnerIssue.setSelection(Integer.parseInt(issue));
	            editTextDesc.setText(description);
	            
	            
	            String[] mListEntities = entity.split(",");
	            
	    	 	for (int j = 0; j < mListEntities.length; j++) {
	    	 		datasource.add(mListEntities[j]);
	    	 	}
	    	 	
	    	 	entitiesLV.setAdapter(adapter);
	            
	            gpsInfo.setText(location);
	        
	        
	        if (datasource.size()==0){
	        	entitiesLV.setVisibility(View.GONE);
	        }
	     
	        
	        done.setOnClickListener(new OnClickListener() {
	            
	            @Override
	            public void onClick(View v) {
	            	
	            	report_save();
	            	
	            }
	        });
	        
			setLocation.setOnClickListener(new OnClickListener(){
				@Override
	            public void onClick(View v) {		
					setLocation();
				}
			});
			
			toggleGPS = (ToggleButton) findViewById(R.id.toggleButton1);
	        toggleGPS.setOnClickListener(new OnClickListener() {
	
	            @Override
	            public void onClick(View arg0) {
	            	                	
	                if(toggleGPS.isChecked()){
	                	setLocation();
	                }
	                else{
	                	gpsInfo.setText("0, 0");
	                }
	            }
	        });
        }

        capture_now = (RelativeLayout)findViewById(R.id.capturenow);
        capture_now.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				importing = false;
				captureDialog();
				
			}
		});
        importfromgallery = (RelativeLayout)findViewById(R.id.importfromgallery);
        importfromgallery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				importing = true;
				captureDialog();
				
			}
		});
    }


    public void captureDialog(){
    	dialog = new Dialog(ReportActivity.this);
	    if(importing==true){
	    	dialog.setTitle(getResources().getString(R.string.choose_media_import));
	    }else{
	    	dialog.setTitle(getResources().getString(R.string.choose_media_capture));
	    }
	    dialog.setContentView(R.layout.dialog_media_types);
	    
	    images = (RelativeLayout)dialog.findViewById(R.id.add_picture);
        video = (RelativeLayout)dialog.findViewById(R.id.add_video);
        audio = (RelativeLayout)dialog.findViewById(R.id.add_audio);
        
        
        images.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				story_mode = 2;
				resultMode = Project.STORY_TYPE_PHOTO;
				if(addscreen==true){
					launchProject(null, 0,0,null,null,null, false, importing);		
				}else{
					launchProject(editTextStoryName.getText().toString(), spinnerIssue.getSelectedItemPosition(),spinnerSector.getSelectedItemPosition(),datasource.toString(),editTextDesc.getText().toString(),gpsInfo.getText().toString(), false, importing);		
				}
				
			}
		});
        video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				story_mode = 2;
				resultMode = Project.STORY_TYPE_VIDEO;
				if(addscreen==true){
					launchProject(null, 0,0,null,null,null, false, importing);		
				}else{
					launchProject(editTextStoryName.getText().toString(), spinnerIssue.getSelectedItemPosition(),spinnerSector.getSelectedItemPosition(),datasource.toString(),editTextDesc.getText().toString(),gpsInfo.getText().toString(), false, importing);							
				}
			}
		});
        audio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				story_mode = 2;
				resultMode = Project.STORY_TYPE_AUDIO;
				if(addscreen==true){
					launchProject(null, 0,0,null,null,null, false, importing);		
				}else{
					launchProject(editTextStoryName.getText().toString(), spinnerIssue.getSelectedItemPosition(),spinnerSector.getSelectedItemPosition(),datasource.toString(),editTextDesc.getText().toString(),gpsInfo.getText().toString(), false, importing);							
				}
			}
		});
        
	    dialog.show();
    }
    public int getMediaCount(){
    	
    	pics = 0;
    	vids = 0;
    	auds = 0;
    	
    	ArrayList<Project> mListProjects = Project.getAllAsList(getApplicationContext(), rid);
	 	for (int j = 0; j < mListProjects.size(); j++) {
	 		Project project = mListProjects.get(j);
	 		Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
		 	for (Media media: mediaList){
		 		if(media!=null){
			 		String ptype = media.getMimeType();
			 		if(ptype.contains("image")){
				 		pics++;
				 	}else if(ptype.contains("video")){
				 		vids++;
				 	}else if(ptype.contains("audio")){
				 		auds++;
				 	}
		 		}
		 	}
	 	}
	 	
    	int total = pics + vids + auds;    	
    	
    	return total;
    }
    public void setMediaCount(){
    	
    	pics = 0;
    	vids = 0;
    	auds = 0;
    	
    	ArrayList<Project> mListProjects = Project.getAllAsList(getApplicationContext(), rid);
	 	for (int j = 0; j < mListProjects.size(); j++) {
	 		Project project = mListProjects.get(j);
	 		Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
		 	for (Media media: mediaList){
		 		if(media!=null){
			 		String ptype = media.getMimeType();
			 		if(ptype.contains("image")){
				 		pics++;
				 	}else if(ptype.contains("video")){
				 		vids++;
				 	}else if(ptype.contains("audio")){
				 		auds++;
				 	}
		 		}
		 	}
	 	}
	 	
    	picture_label.setText("Picture ("+String.valueOf(pics)+")");
    	video_label.setText("Video ("+String.valueOf(vids)+")");
    	audio_label.setText("Audio ("+String.valueOf(auds)+")");
    	int total = pics + vids + auds;    	
    	gallery_label.setText("Gallery ("+String.valueOf(total)+")");
    }
    
    public void setLocation(){
		gpsT = new GPSTracker(ReportActivity.this); 
		  
        // check if GPS enabled 
        if(gpsT.canGetLocation()){ 

            double latitude = gpsT.getLatitude(); 
            double longitude = gpsT.getLongitude(); 

            // \n is for new line 
            gpsInfo.setText(latitude+", "+longitude); 
           /* GeoPoint myGeoPoint = new GeoPoint( 
                  (int)(latitude*1000000), 
                  (int)(longitude*1000000)); 
          	CenterLocatio(myGeoPoint); */
            if(String.valueOf(latitude).equals("0")){
                gpsT.showSettingsAlert(); 
            }
        }else{ 
            // can't get location 
            // GPS or Network is not enabled 
            // Ask user to enable GPS/network in settings 
            gpsT.showSettingsAlert(); 
        } 
    }
    public void report_save(){
    	
    	if (formValid()) {
			launchProject(editTextStoryName.getText().toString(), spinnerIssue.getSelectedItemPosition(),spinnerSector.getSelectedItemPosition(),datasource.toString(),editTextDesc.getText().toString(),gpsInfo.getText().toString(), true, false);		
    	}
    	
    }
    public void report_close(){
    	dialog_publish = new Dialog(ReportActivity.this);
    	dialog_publish.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog_publish.setContentView(R.layout.dialog_publish);
    	dialog_publish.findViewById(R.id.button_publish).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//TODO: check if sync or encrypt is running
				Intent i = new Intent(ReportActivity.this,SyncService.class);
				i.putExtra("rid", rid);
  	        	startService(i);
  	        	
  	        	
            	do_report_close();
				dialog_publish.dismiss();
			}        	
        });
    	dialog_publish.findViewById(R.id.button_skip).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
            	do_report_close();
				dialog_publish.dismiss();
			}        	
        });
    	dialog_publish.show();
    }

   public void do_report_close(){ 	
    	//Hide keyboard
        InputMethodManager inputManager = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE); 
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),      
        		    InputMethodManager.HIDE_NOT_ALWAYS);
        //NavUtils.navigateUpFromSameTask(this);
        Intent i = new Intent(getBaseContext(), HomePanelsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
    public void setEntities(){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	try {
    	    JSONArray jsonArray2 = new JSONArray(prefs.getString("entities", "[]"));
    	    
    	    allEntities = new String[jsonArray2.length()];
			for(int i=0;i<jsonArray2.length();i++)
			{
				allEntities[i]=jsonArray2.getString(i);
				Log.d("entity"+String.valueOf(i), allEntities[i]);
			}
			
    	}catch (Exception e) {
    	    e.printStackTrace();
    	}
	}
    
    public void setCategories(){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	try {
    	    JSONArray jsonArray2 = new JSONArray(prefs.getString("categories", "[]"));
    	    ArrayList<String> list=new ArrayList<String>();
    	    list.add("Select Category");
			for(int i=0;i<jsonArray2.length();i++)
			{
				list.add(jsonArray2.getString(i));
			}
			
			ArrayAdapter<String> spinnerMenu = new ArrayAdapter<String>(getApplicationContext(),  R.layout.spinner_report_new, list);
			spinnerIssue.setAdapter(spinnerMenu);

			
    	}catch (Exception e) {
    	    e.printStackTrace();
    	}
	}
    public void setSectors(){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    	try {
	    	    JSONArray jsonArray2 = new JSONArray(prefs.getString("sectors", "[]"));
	    	    ArrayList<String> list=new ArrayList<String>();
	    	    list.add("Select Sector");
				for(int i=0;i<jsonArray2.length();i++){
					list.add(jsonArray2.getString(i));
				}
				ArrayAdapter<String> spinnerMenu = new ArrayAdapter<String>(getApplicationContext(),  R.layout.spinner_report_new, list);
				spinnerSector.setAdapter(spinnerMenu);
	    	} catch (Exception e) {
	    	    e.printStackTrace();
	    	}
    }
    public void setSelectedItem(Spinner spinner,String string){
		int index = 0;
		for (int i = 0; i < spinner.getAdapter().getCount(); i++){
			if (spinner.getItemAtPosition(i).equals(string)){
				index = i;
			}
		}
			spinner.setSelection(index);
	}
    
    private boolean formValid ()
    {
    	return true;
    }

    private void launchProject(String title, int pIssue, int pSector, String pEntity, String pDesc, String pLocation, boolean update, boolean importing) {
    	
    	new_report = false;
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String currentdate = dateFormat.format(new Date());
    	
    	if(pLocation==null){
    		pLocation = "0, 0";
    	}
    	
    	if(pLocation.equals("Location not set")){
    		pLocation = "0, 0";
    	}
    	
    	if (title == null || title.length() == 0)
    	{
    		title = "Captured at "+currentdate;
    	}
    	
    	if(pEntity==null){
    		pEntity = "";
    	}
    	
    	if(pDesc==null){
    		pDesc = "";
    	}
    	    	
    	pEntity = pEntity.replace("[", "");
    	pEntity = pEntity.replace("]", "");
    	
    	Report report;
        if(rid==-1){
        	report = new Report (this, 0, title, String.valueOf(pSector), String.valueOf(pIssue), pEntity, pDesc, pLocation, "0", currentdate, "0", "0");
         }else{
        	report = Report.get(this, rid);
        	report.setTitle(title);
        	report.setDescription(pDesc);
        	report.setEntity(pEntity);
        	report.setIssue(String.valueOf(pIssue));
        	report.setSector(String.valueOf(pSector));
        	report.setLocation(pLocation);        	
        }
        report.save();
        
        rid = report.getId();
                
        if(update == false){
	        Intent intent = new Intent(getBaseContext(), StoryNewActivity.class);
	        intent.putExtra("storymode", resultMode);
	        intent.putExtra("importing", importing);
	        intent.putExtra("rid", report.getId());
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(intent);
	        //setMediaCount();
        }else{
        	if(pLocation.equals("0, 0")){
        		Toast.makeText(getApplicationContext(), "Trouble finding location. Try again later!", Toast.LENGTH_LONG).show();
        	}else{
	    		
	        	Toast.makeText(getBaseContext(), String.valueOf(rid)+" Updated successfully!", Toast.LENGTH_LONG).show();
	        	report_close();
        	}      	
        }
         
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_report, menu);
        //menu.getItem(0).setTitle("test").setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        MenuItem galleryItem = menu.findItem(R.id.gallery);
        galleryItem.setTitle("Gallery (" + getMediaCount() + ")");
        if(rid==-1){
        	galleryItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home){
            	if(new_report){
            		if(something_changed()){
            			showSaveAlert();
            		}else{
            			do_report_close();
            		}
            	}else{
            		//old report
            		if(something_changed_db()){
            			showSaveAlert();
            		}else{
            			do_report_close();
            		}
            	}
            	
            return true;
        }else if (item.getItemId() == R.id.gallery){
	        	ArrayList<Project> mListProjects;
	    		mListProjects = Project.getAllAsList(getApplicationContext(), rid);
	    	 	
	    		if(mListProjects.size()>0){
					Intent p = new Intent(getBaseContext(), ProjectsActivity.class);
	            	p.putExtra("rid", rid);
	            	p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            	startActivity(p);
	    		}else{
	    			Toast.makeText(getApplicationContext(), "No media added on this report yet!", Toast.LENGTH_LONG).show();
	    		}
	    		
            return true;
        }
    	
        return super.onOptionsItemSelected(item);
    }
    public void showSaveAlert(){
    	dialog_save = new Dialog(ReportActivity.this);
    	dialog_save.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog_save.setContentView(R.layout.dialog_save);
    	dialog_save.findViewById(R.id.button_save).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				report_save();  
            	report_close();
				dialog_save.dismiss();
			}        	
        });
    	dialog_save.findViewById(R.id.button_discard).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
            	report_close();
				dialog_save.dismiss();
			}        	
        });
    	dialog_save.show();
    }
    
    public boolean something_changed_db(){
    	Report report = Report.get(this, rid);
    	
    	if((editTextStoryName.getText().toString().equals(report.getTitle()))&&
				(spinnerIssue.getSelectedItemPosition()==Integer.parseInt(report.getIssue()))&&
				(spinnerSector.getSelectedItemPosition()==Integer.parseInt(report.getSector()))&&
				(datasource.toString().equals("["+report.getEntity()+"]"))&&
				(editTextDesc.getText().toString().equals(report.getDescription()))
				){
    		return false;
    	}else{
    		return true;
    	}
    }
    public boolean something_changed(){
			if((editTextStoryName.getText().toString().equals(""))&&
				(spinnerIssue.getSelectedItemPosition()==0)&&
				(spinnerSector.getSelectedItemPosition()==0)&&
				(datasource.size()==0)&&
				(editTextDesc.getText().toString().equals(""))
				){
			//nothing changed; ignore location change
			
			return false;
		}else{
				
	    	return true;
		}
    }
    
    //entities
    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return datasource.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return datasource.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) convertView;
            if (null == view) {
                view = new TextView(ReportActivity.this);
                view.setPadding(15, 15, 15, 15);
            }
            view.setText(datasource.get(position));
            view.setTextAppearance(ReportActivity.this, android.R.style.TextAppearance_Medium);
            view.setTextColor(getResources().getColor(R.color.grey));
            return view;
        }
    }

    @SuppressLint("NewApi")
	@Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_cancel:
            dialog.dismiss();
            break;

        case R.id.button_ok:
            String text = ((AutoCompleteTextView) dialog.findViewById(R.id.edit_box))
                    .getText().toString();
            if (null != text && 0 != text.compareTo("")) {
                datasource.add(text);
                
                int minHeight = datasource.size()*80;
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, minHeight);
                entitiesLV.setLayoutParams(params);
                                
                dialog.dismiss();
                adapter.notifyDataSetChanged();
            }
            if (datasource.size()>0){
            	entitiesLV.setVisibility(View.VISIBLE);
            }
            break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> listView, View view,
            int position, long column) {
    	int minHeight = datasource.size()*80;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, minHeight);
        entitiesLV.setLayoutParams(params);
        
        datasource.remove(position);
        adapter.notifyDataSetChanged();
        
        if (datasource.size()==0){
        	entitiesLV.setVisibility(View.GONE);
        }
        
        return true;
    }
}

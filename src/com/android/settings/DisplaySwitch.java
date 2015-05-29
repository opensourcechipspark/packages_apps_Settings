package com.android.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter.ViewBinder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import android.os.SystemProperties;

public class DisplaySwitch extends Activity implements OnClickListener{
	
	
	private Button buttonPick, buttonRestore;
	private RadioButton radio1,radio2,radio3;
	private RadioGroup radioGroup;
	private ImageView imageBg;
	private static final int IMAGE_REQUEST_CODE = 0;
	private String SOFT_VALUE;
	private String STANDARD_VALUE;
	private String DYNAMIC_VALUE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_switch);
		buttonPick=(Button)findViewById(R.id.button_pick);
		buttonRestore=(Button)findViewById(R.id.button_restore);
		radio1=(RadioButton) findViewById(R.id.soft_button);
		radio2=(RadioButton) findViewById(R.id.standard_button);
		radio3=(RadioButton) findViewById(R.id.dynamic_button);
		radioGroup=(RadioGroup)findViewById(R.id.radio_group);
		imageBg=(ImageView)findViewById(R.id.image_bg);
		
		buttonPick.setOnClickListener(this);
		buttonRestore.setOnClickListener(this);
		radioGroup.setOnCheckedChangeListener(radioListener);
		
		setBcsh("open");
		initChecked();
		
	}

	private void initChecked(){
		String str=SystemProperties.get("ro.rk.display_array");
		Log.d("dzy","str="+str);
		String[] array=str.split(",");
		SOFT_VALUE=array[0];
		STANDARD_VALUE=array[1];
		DYNAMIC_VALUE=array[2];
		String value=SystemProperties.get("persist.sys.display_value",STANDARD_VALUE);
		if(value.equals(SOFT_VALUE)){
			radio1.setChecked(true);
		}else if(value.equals(STANDARD_VALUE)){
			radio2.setChecked(true);
		}else if(value.equals(DYNAMIC_VALUE)){
			radio3.setChecked(true);
		}
	}
	
	private OnCheckedChangeListener radioListener=new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			switch(checkedId){
			case R.id.soft_button:
				setBcsh("sat_con "+SOFT_VALUE);
				SystemProperties.set("persist.sys.display_value", SOFT_VALUE);
				break;
			case R.id.standard_button:
				setBcsh("sat_con "+STANDARD_VALUE);
				SystemProperties.set("persist.sys.display_value", STANDARD_VALUE);
				break;
			case R.id.dynamic_button:
				setBcsh("sat_con "+DYNAMIC_VALUE);
				SystemProperties.set("persist.sys.display_value", DYNAMIC_VALUE);
				break;
			
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.button_restore){
			imageBg.setImageResource(R.drawable.still_fruit_color);
		}else{
		  Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		  galleryIntent.setComponent(new ComponentName("com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity"));
		  galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
		  galleryIntent.setType("image/*");
		  startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
		}
	}

	
	private void setBcsh(String strbuf){
        File f = new File("/sys/class/graphics/fb0/bcsh");
        OutputStream output = null;     
        OutputStreamWriter outputWrite = null;
        PrintWriter  print = null;
        try{ 
                output = new FileOutputStream(f);
                outputWrite = new OutputStreamWriter(output);
                print = new PrintWriter(outputWrite);
                print.print(strbuf.toString());
                print.flush();
                output.close();
        } catch (Exception e) { 
                e.printStackTrace();
        }    
    }    
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode != RESULT_OK) {
			return;
		} else {
			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
					Uri selectedImage = data.getData();
		            String[] filePathColumn = { MediaStore.Images.Media.DATA };
		            Cursor cursor = getContentResolver().query(selectedImage,
		                    filePathColumn, null, null, null);
		            cursor.moveToFirst();
		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            String picturePath = cursor.getString(columnIndex);
		            cursor.close();
		            imageBg.setImageBitmap(BitmapFactory.decodeFile(picturePath));
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}

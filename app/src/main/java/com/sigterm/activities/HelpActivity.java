package com.sigterm.activities;

import com.sigterm.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
 

public class HelpActivity  extends Activity{
	private final String LOG_TAG = getClass().getSimpleName();
    
    
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
		    setContentView(R.layout.help);
		    ListView lvHelp = (ListView) findViewById(R.id.lvHelp);
		    lvHelp.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(id == 0){
						 Intent intent = new Intent(Intent.ACTION_SEND); 
						 intent.setType("text/html"); 
						 intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@sigterm.biz"}); 
						 intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.supportTitle)); 
						 intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.supportBody)+"<br>")); 
						 startActivity(intent); 
					}
					else if(id == 1){
						 Intent intent = new Intent(Intent.ACTION_SEND); 
						 intent.setType("text/html"); 
						 intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@sigterm.biz"}); 
						 intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.featureTitle)); 
						 intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.featureBody)+"<br>")); 
						 startActivity(intent); 
					}
					else if(id == 2 ){
						 Intent intent = new Intent(Intent.ACTION_SEND); 
						 intent.setType("text/html"); 
						  
						 intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.tellAFriendTitle)); 
						 intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.tellAFriendBody)+ Uri.parse("market://details?id=" + getPackageName()))); 
						 startActivity(intent); 
					}
					else if(id == 3 ){
						 Intent intent = new Intent(getApplicationContext(), FAQActivity.class);
						 startActivity(intent); 
					}
				}});
	    }
	 
	 
}

package tw.instartit.app.autowificgu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ReadmeActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.readme);
		
		((ImageView) findViewById(R.id.imageView1)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=tw.instartit.app.autowificgu");
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		});
	}
	
}

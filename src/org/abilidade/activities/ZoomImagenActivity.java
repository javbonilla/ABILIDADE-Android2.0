package org.abilidade.activities;

import org.abilidade.application.AbilidadeApplication;
import org.abilidade.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class ZoomImagenActivity extends Activity {
	
	// Definicion de atributos
	private Bitmap imagen;
	private String imagenPath;
	private String imagenCod;
	
	private ImageView imageView;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.zoom_imagen);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Recibimos los parametros del Intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		imagenPath = extras.getString(AbilidadeApplication.ZoomImagenActivityImagenPath);
		imagenCod = extras.getString(AbilidadeApplication.ZoomImagenActivityImagenCod);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		imageView = (ImageView) findViewById(R.id.imagenZoom);
		
		if(imagenCod != null) {
			imagen = AbilidadeApplication.descodificarImagen(imagenCod);
		} else {
			imagen = BitmapFactory.decodeFile(imagenPath);
			imagen = AbilidadeApplication.rotarImagen(imagenPath, imagen);
		}
		
		imageView.setImageBitmap(imagen);
	}
}
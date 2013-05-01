package org.abilidadev2.activities;

import org.abilidadev2.application.AbilidadeApplication;
import org.abilidadev2.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
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
			imagen = decodeSampledBitmapFromResource(imagenPath, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
			imagen = AbilidadeApplication.rotarImagen(imagenPath, imagen);
		}
		
		imageView.setImageBitmap(imagen);
	}
	
	public Bitmap decodeSampledBitmapFromResource(String filePath, int iDesiredWidth, int iDesiredHeight) {
		 
		// 1. Primero se descodifica la imagen con inJustDecodeBounds=true para comprobar las propiedades de la imagen
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filePath, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, iDesiredWidth, iDesiredHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath, options);
	}
	
	public int calculateInSampleSize(Options options, int iDesiredWidth, int iDesiredHeight) {
		
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > iDesiredWidth || width > iDesiredHeight) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) iDesiredHeight);
	        final int widthRatio = Math.round((float) width / (float) iDesiredWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}
}
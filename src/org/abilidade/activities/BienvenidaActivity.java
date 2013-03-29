package org.abilidade.activities;

import org.abilidade.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class BienvenidaActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.bienvenida);
        
        if (comprobarConexion()) {
        	// Hacemos que se muestre la pantalla por un tiempo corto
            Thread splashThread = new Thread() {
                @Override
                public void run() {
                   try {
                      int waited = 0;
                      while (waited < 5000) {
                         sleep(100);
                         waited += 100;
                      }
                   } catch (InterruptedException e) {
                      // do nothing
                   } finally {
                      finish();
                      Intent i = new Intent();
                      i.setClass(BienvenidaActivity.this, AccederActivity.class);
                      startActivity(i);
                   }
                }
             };
             splashThread.start();
       } else {
    	Toast.makeText(getApplicationContext(), "Necesita conexión a Internet para poder ejecutar la aplicación", Toast.LENGTH_LONG).show();
    	finish();
	   }
    }
	
	private boolean comprobarConexion() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			return false;
		} else {
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
	}
}

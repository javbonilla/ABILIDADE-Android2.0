package org.abilidadev2.activities;

import org.abilidadev2.R;
import org.abilidadev2.application.AbilidadeApplication;
import org.abilidadev2.mapa.MapaActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class BienvenidaActivity extends Activity {
	
	// ************************** DEFINICION DE ATRIBUTOS DE LA CLASE  ************************** //
	
	private ProgressDialog pDialog;
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.bienvenida);
        
        if (comprobarConexion()) {
        	
        	// Se muestra la pantalla de bienvenida por un espacio corto de tiempo
            /*Thread splashThread = new Thread() {
                @Override
                public void run() {
                   try {
                      int waited = 0;
                      while (waited < 1000) {
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
             splashThread.start();*/
             
             // Se usa una tarea asincrona para poder mostrar mientras tanto un ProgessDialog
 			new asynclogin().execute();
       } else {
    	Toast.makeText(getApplicationContext(), getString(R.string.bienvenidaConexionInternet), Toast.LENGTH_LONG).show();
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
	
	/*		CLASE ASYNCTASK
	 * Se usa esta clase para poder mostrar el dialogo de progreso mientras se configuran los datos de acceso a la aplicacion     
	 */

	class asynclogin extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialog = new ProgressDialog(BienvenidaActivity.this);
	        pDialog.setMessage(getString(R.string.cargandoEsperePorFavor));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();
	    }

		protected String doInBackground(String... params) {
			
			boolean bPrimeraVez;
			Editor editor;
			boolean bLogin;
			
			// 1. Lo primero que se va a hacer es comprobar si es la primera vez que se arranca la aplicacion
			//    Para ello, se recuperan las SharedPreferences y se comprueba el parametro "primeraVez"
			//    - Si es la primera vez, se cargan los parametros y se pone "primeraVez" a false
			//    - Si no es la primera vez, se cargan los parametros de la sesion anterior
			SharedPreferences pref = getApplicationContext().getSharedPreferences(AbilidadeApplication.SHARED_PREFERENCES, 0); // 0 - for private mode
			editor = pref.edit();
			
			bPrimeraVez = pref.getBoolean(AbilidadeApplication.SHPF_PRIMERA_VEZ, true);
			
			Log.d("bienvenida","bPrimeraVez: "+bPrimeraVez);
			
			if (bPrimeraVez) {
				
				Log.d("bienvenida","primera vez que se arranca la aplicacion");
				
				// Pongo el ShPf "usuario" a espacios y el "login" a false y accedo a la pantalla AccederActivity
				editor.putString(AbilidadeApplication.SHPF_USUARIO, "");
				editor.putBoolean(AbilidadeApplication.SHPF_LOGIN, false);
				
				// Indico que ya se ha ejecutado la aplicacion por primera vez
				editor.putBoolean(AbilidadeApplication.SHPF_PRIMERA_VEZ, false);
				
				// Salvo los cambios en las ShPf
				editor.commit();
				
				return AbilidadeApplication.RETORNO_BIENVENIDA_ACCEDER;
				
			} else {
				
				// Recupero el ShPf "login" y lo compruebo:
				// - Si esta a true, accedo a la pantalla MainActivity sin pasar por AccederActivity
				// - Si no lo esta, accedo a la pantalla AccederActivity
				bLogin = pref.getBoolean(AbilidadeApplication.SHPF_LOGIN, false);
				
				Log.d("bienvenida", "Valor de bLogin: "+bLogin);
				
				if (bLogin) {
					Log.d("bienvenida","Si me llega login, voy a MainActivity");
					return AbilidadeApplication.RETORNO_BIENVENIDA_MAIN;
				} else {
					Log.d("bienvenida","No me llega login, voy a AccederActivity");
					return AbilidadeApplication.RETORNO_BIENVENIDA_ACCEDER;
				}
			}
		}
	   
		protected void onPostExecute(String result) {

	       pDialog.dismiss();//ocultamos progess dialog.
	       Log.d("onPostExecute=",""+result);
	       
	       if (result.equals(AbilidadeApplication.RETORNO_BIENVENIDA_MAIN)){

				// Lanzo la MainActivity
	    	    Intent i=new Intent(BienvenidaActivity.this, MapaActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Para cerrar todas las demas Activities
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Para comenzar la nueva Activity
				startActivity(i); 
				finish();
				
	        }else{
	        	
	        	// Lanzo la AccederActivity
	        	Intent i=new Intent(BienvenidaActivity.this, AccederActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Para cerrar todas las demas Activities
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Para comenzar la nueva Activity
				startActivity(i); 
				finish();
	        	
	        }
	     }
	}
}

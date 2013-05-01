package org.abilidadev2.activities;

import java.util.ArrayList;

import org.abilidadev2.R;
import org.abilidadev2.application.AbilidadeApplication;
import org.abilidadev2.mapa.MapaActivity;
import org.abilidadev2.network.Httppostaux;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccederActivity extends Activity {
	
	// ************************** DEFINICION DE ATRIBUTOS DE LA CLASE  ************************** //
	
	// Elementos de pantalla de la Activity
	
	private EditText editTextEmail;
	private EditText editTextPassword;
	
	private Button buttonAcceder;
	private Button buttonCrearCuenta;
	
	private TextView textViewOlvidastePass;
	private TextView textViewAyuda;
	
	// Atributos de la clase
	
	private String sUsuario;
	private String sPassword;
	
	private boolean bValidacionOk;
	private boolean bMantenerAplicacion;
	
	private ProgressDialog pDialog;
	
	private Httppostaux post;
	
	// Constantes necesarias para el funcionamiento del acceso
	
	private final String sURLConnect = "http://abilidade.eu/r/loginmovil/acces.php";
	private final String sURLForgotPassword = "http://abilidade.eu/r/loginmovil/recordar.html";
	private final String sURLAyuda = "http://abilidade.eu/r/androidhelp/help_menu_login.htm";
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acceso_usuarios);
		
		post = new Httppostaux();
		
		// Localizacion de los elementos de pantalla
		editTextEmail         = (EditText) findViewById(R.id.loginTextoEmail);
		editTextPassword      = (EditText) findViewById(R.id.loginTextoPassword);
		buttonAcceder         = (Button)   findViewById(R.id.loginBotonAcceder);
		buttonCrearCuenta     = (Button)   findViewById(R.id.loginBotonCrearCuenta);
		textViewOlvidastePass = (TextView) findViewById(R.id.loginTextoOlvidarPassword);
		textViewAyuda         = (TextView) findViewById(R.id.loginTextoAyuda);
		
		// Formato para el texto de olvidaste password
		SpannableString content = new SpannableString(textViewOlvidastePass.getText().toString()); 
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0); 
		textViewOlvidastePass.setText(content); 
		
		// Formato para el texto de olvidaste password
		SpannableString content2 = new SpannableString(textViewAyuda.getText().toString()); 
		content2.setSpan(new UnderlineSpan(), 0, content2.length(), 0); 
		textViewAyuda.setText(content2);
		
		bMantenerAplicacion = false;
		Log.d("AccederActivity","onCreate");
		
		// Accion en caso de pulsar el boton de login
		buttonAcceder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// 1. Se extraen los datos de los EditText
				
				sUsuario  = editTextEmail.getText().toString();
				sPassword = editTextPassword.getText().toString();
				
				// 2. Se valida que los datos introducidos por el usuario no esten en blanco
				
				validarDatosEnBlanco();
				
				// 3. Si los datos validan ok, ahora se comprueban que pertenezcan a un usuario de verdad
				
				if (bValidacionOk) {
					
					// Se usa una tarea asincrona para poder mostrar mientras tanto un ProgessDialog
					new asynclogin().execute();
					
				}
			}
		});
		
		// Accion en caso de pulsar el boton de crear cuenta
		buttonCrearCuenta.setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) {
				
				// 1. Llamar a la Activity de crear cuenta
				bMantenerAplicacion = true;
				Log.d("AccederActivity","buttonCrearCuenta. bMantenerAplicacion: " + bMantenerAplicacion);
				
				Intent i=new Intent(AccederActivity.this, CrearCuentaActivity.class);
				startActivity(i); 
				
			}
		});
		
		// Accion en caso de pulsar el texto de Olvidaste tu password
		textViewOlvidastePass.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				bMantenerAplicacion = true;
				Log.d("AccederActivity","textViewOlvidastePass. bMantenerAplicacion: " + bMantenerAplicacion);
				
				// Lo que se hace es llamar al navegador con la pagina para recordar la password
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(sURLForgotPassword));
				startActivity(i);
			}
		});
		
		// Accion en caso de pulsar el texto de Olvidaste tu password
		textViewAyuda.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				bMantenerAplicacion = true;
				Log.d("AccederActivity","textViewAyuda. bMantenerAplicacion: " + bMantenerAplicacion);
				
				// Lo que se hace es llamar al navegador con la pagina para la ayuda
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(sURLAyuda));
				startActivity(i);
			}
		});
	}

	public void validarDatosEnBlanco() {

		bValidacionOk = true;
		
		if (sUsuario.equals("")) {
			Log.e("Login usuario", "Usuario introducido en blanco");
			errLogin(getString(R.string.accederUsuarioBlanco));
			bValidacionOk = false;
		}
		else {
			// 3. Segunda validacion --> Que el password no este en blanco
			if (sPassword.equals("")) {
				Log.e("Login usuario", "Password introducido en blanco");
				errLogin(getString(R.string.accederPasswordBlanco)); 
				bValidacionOk = false;
			}
		}
	}
	
	public int validarDatosBD() {
		
		int iRetorno = 0;
		
		// Se crea un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
   	    // y enviarlo mediante POST al sistema para realizar la validacion
		ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
		
		postparameters2send.add(new BasicNameValuePair("usuario",sUsuario));
		postparameters2send.add(new BasicNameValuePair("password",sPassword));
		
		// Se realiza una peticion y como respuesta se obtiene un array JSON
		JSONArray jdata = post.getserverdata(postparameters2send, sURLConnect);
		
		// Si lo que se obtiene de la peticion no es NULL
    	if (jdata!=null && jdata.length() > 0){

    		JSONObject jsonData; 
			int iLogStatus = -1;
			
			try {
				jsonData = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
				iLogStatus = jsonData.getInt("logstatus");//accedemos al valor 
				Log.e("loginstatus","iLogStatus= "+iLogStatus);//muestro por log que obtuvimos
			} catch (JSONException e) {
				e.printStackTrace();
			}		            
             
			// Se valida el valor obtenido
			
			Log.d("iLogStatus",""+iLogStatus);
			
			switch (iLogStatus) {
				case 0:
					Log.d("loginstatus", "usuario valido");
					break;
				case 1:
					Log.d("loginstatus", "e-mail no encontrado");
					break;
				case 2:
					Log.d("loginstatus", "password erroneo");
					break;
				case 3:
					Log.d("loginstatus", "usuario no activo");
					break;					
			}
			
			iRetorno = iLogStatus;
    		 
	  }else{	// JSON obtenido invalido, revisar parte web
    			 Log.e("Login usuario (JSON)", "JSON obtenido invalido, revisar parte web");
	    		iRetorno = -1;
	  }
		
      return iRetorno;
	}
	
	public void errLogin(String mensajeError) {
		Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    vibrator.vibrate(200);
	    Toast.makeText(getApplicationContext(),mensajeError, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onPause() {
		// Se fuerza a la aplicacion a salir 
		
		Log.d("AccederActivity","onPause. bMantenerAplicacion: " + bMantenerAplicacion);
		
		if(!bMantenerAplicacion) {
			Log.d("AccederActivity","onPause. finish()");
			finish();
		}
		
		super.onPause();
	}
	
	/*		CLASE ASYNCTASK
	 * Se usa esta clase para poder mostrar el dialogo de progreso mientras se envian y obtienen los datos.     
	 */

	class asynclogin extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialog = new ProgressDialog(AccederActivity.this);
	        pDialog.setMessage(getString(R.string.accediendo));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();
	    }

		protected String doInBackground(String... params) {
			// Se validan los datos contra la BD
			int iRetornoValidacion = validarDatosBD();
			
			switch (iRetornoValidacion) {
				case 0:
					return AbilidadeApplication.RETORNO_OK; //login valido
				case 1:
					return AbilidadeApplication.RETORNO_KO_USUARIO; //login invalido - e-mail no encontrado
				case 2:
					return AbilidadeApplication.RETORNO_KO_PASSWORD; //login invalido - password erroneo
				case 3:
					return AbilidadeApplication.RETORNO_KO_USUACTIVO; //login invalido - usuario no activo
				default:
					return AbilidadeApplication.RETORNO_KO_ERROR; //login invalido - error de acceso
			}
	    	
		}
	   
		protected void onPostExecute(String result) {

		   Editor editor;
	       
		   pDialog.dismiss();//ocultamos progess dialog.
	       Log.e("onPostExecute=",""+result);
	       
	       if (result.equals(AbilidadeApplication.RETORNO_OK)){

				// 1. Se guardan las SharedPreferences
				SharedPreferences pref = getApplicationContext().getSharedPreferences(AbilidadeApplication.SHARED_PREFERENCES, 0); // 0 - for private mode
				editor = pref.edit();
				
				editor.putString(AbilidadeApplication.SHPF_USUARIO, sUsuario);
				editor.putBoolean(AbilidadeApplication.SHPF_LOGIN, true);
				
				editor.commit();
				
				Log.d("AccederActivity","Coloco "+sUsuario+" como usuario logueado en el sistema");
				
				// 2. Y se arranca la MainActivity
				Intent i=new Intent(AccederActivity.this, MapaActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Para cerrar todas las demas Activities
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Para comenzar la nueva Activity
				startActivity(i); 
				finish();
				
	        }else{
	        	
	        	if (result.equals(AbilidadeApplication.RETORNO_KO_USUARIO)) {
	        		errLogin(getString(R.string.accesoIncorrectoUsuario));
	        	} else {
	        		if (result.equals(AbilidadeApplication.RETORNO_KO_PASSWORD)) {
	        			errLogin(getString(R.string.accesoIncorrectoPassword));
	        		} else {
	        			if (result.equals(AbilidadeApplication.RETORNO_KO_USUACTIVO)) {
	        				errLogin(getString(R.string.accesoIncorrectoUsuarioNoActivo));
	        			} else {
	        				errLogin(getString(R.string.accesoIncorrectoError));
	        			}
	        		}
	        	}
	        }
	     }
	}
}
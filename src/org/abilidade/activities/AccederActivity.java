package org.abilidade.activities;

import java.util.ArrayList;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.network.Httppostaux;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
	
	// Atributos de la clase
	
	private String sUsuario;
	private String sPassword;
	
	private boolean bValidacionOk;
	
	private ProgressDialog pDialog;
	
	private Httppostaux post;
	
	// Constantes necesarias para el funcionamiento del acceso
	
	private String sURLConnect = "http://abilidade.eu/r/loginmovil/acces.php";
	
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
				
				Intent i=new Intent(AccederActivity.this, CrearCuentaActivity.class);
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

	       pDialog.dismiss();//ocultamos progess dialog.
	       Log.e("onPostExecute=",""+result);
	       
	       if (result.equals(AbilidadeApplication.RETORNO_OK)){

				Intent i=new Intent(AccederActivity.this, MainActivity.class);
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
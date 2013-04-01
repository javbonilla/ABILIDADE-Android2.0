package org.abilidade.activities;

import java.util.ArrayList;

import org.abilidade.R;
import org.abilidade.activities.AccederActivity.asynclogin;
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

public class CrearCuentaActivity extends Activity {
	
	// ************************** DEFINICION DE ATRIBUTOS DE LA CLASE  ************************** //
	
	// Elementos de pantalla de la Activity
	
	private EditText editTextEmail;
	private EditText editTextPassword;
	private EditText editTextConfirmarPassword;
	
	private Button botonRegistrar;
	private Button botonVolver;
	
	// Atributos de la clase
	
	private String sUsuario;
	private String sPassword;
	private String sConfirmarPassword;
	
	private boolean bValidacionOk;
	
	private ProgressDialog pDialog;
	
	private Httppostaux post;
	
	// Constantes necesarias para el funcionamiento del acceso
	
	private final String sURLConnect = "http://abilidade.eu/r/loginmovil/adduser.php";
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crear_cuenta);
		
		post = new Httppostaux();
		
		// Localizar los elementos de pantalla
		editTextEmail             = (EditText) findViewById(R.id.crearCuentaTextoEmail);
		editTextPassword          = (EditText) findViewById(R.id.crearCuentaTextoPassword);
		editTextConfirmarPassword = (EditText) findViewById(R.id.crearCuentaTextoConfirmarPassword);
		botonRegistrar            = (Button)   findViewById(R.id.crearCuentaBotonRegistrar);
		botonVolver               = (Button)   findViewById(R.id.crearCuentaBotonVolver);
		
		// Accion en caso de pulsar el boton de crear cuenta
	    botonRegistrar.setOnClickListener(new View.OnClickListener() {
					
				@Override
				public void onClick(View v) {
						
					// 1. Se extraen los datos de los EditText
						
					sUsuario           = editTextEmail.getText().toString();
					sPassword          = editTextPassword.getText().toString();
					sConfirmarPassword = editTextConfirmarPassword.getText().toString(); 
					
					// 2. Se valida que los datos introducidos por el usuario no esten en blanco
					
					validarDatosEnBlanco();
					
					// 3. Se valida que las contraseñas introducidas coincidan
					
					if (bValidacionOk) {
						validarPassword();	
					}
					
					
					// 4. Si los datos validan ok, se envian a la BD
					
					if (bValidacionOk) {
						
						// Se usa una tarea asincrona para poder mostrar mientras tanto un ProgessDialog
						new asynclogin().execute();
						
					}
				}
	     });
	    
	    // Accion en caso de pulsar el boton de volver
	    botonVolver.setOnClickListener(new View.OnClickListener() {
					
				@Override
				public void onClick(View v) {
					finish();
				}
	     });
  }
	
	
   public void validarDatosEnBlanco() {

		bValidacionOk = true;
		
		if (sUsuario.equals("")) {
			Log.e("Crear cuenta", "Usuario introducido en blanco");
			errLogin(getString(R.string.accederUsuarioBlanco));
			bValidacionOk = false;
		}
		else {
			// 3. Segunda validacion --> Que el password no este en blanco
			if (sPassword.equals("")) {
				Log.e("Crear cuenta", "Password introducido en blanco");
				errLogin(getString(R.string.accederPasswordBlanco)); 
				bValidacionOk = false;
			}
			else {
				// Tercera validacion --> Que la confirmacion de password no este en blanco
				if (sConfirmarPassword.equals("")) {
					Log.e("Crear cuenta", "Confirmar password introducido en blanco");
					errLogin(getString(R.string.accederConfirmarPasswordBlanco)); 
					bValidacionOk = false;
				}
			}
		}
	}
   
    public void validarPassword() {
    	
    	if (sPassword.equals(sConfirmarPassword)) {
    		Log.d("Crear cuenta", "Las passwords coinciden, se procede a registrar usuario");
    	}
    	else {
			Log.e("Crear cuenta", "Las passwords no coinciden");
			errLogin(getString(R.string.accederPasswordsNoCoinciden)); 
			bValidacionOk = false;
    	}
    }
  
	public void errLogin(String mensajeError) {
		Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    vibrator.vibrate(200);
	    Toast.makeText(getApplicationContext(),mensajeError, Toast.LENGTH_LONG).show();
	}
	
	public int enviarDatos() {
		
		int iRetorno = 0;
		
		// Se crea un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
   	    // y enviarlo mediante POST al sistema para realizar la validacion
		ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
		
		Log.d("Crear usuario","Creo el ArrayList");
		
		postparameters2send.add(new BasicNameValuePair("usuario",sUsuario));
		postparameters2send.add(new BasicNameValuePair("password",sPassword));
		
		Log.d("Crear usuario","Creo los parametros usuario y password");
		
		// Se realiza una peticion y como respuesta se obtiene un array JSON
		JSONArray jdata = post.getserverdata(postparameters2send, sURLConnect);
		
		Log.d("Crear usuario","Acabo de recibir el JSONArray");
		
		// Si lo que se obtiene de la peticion no es NULL
    	if (jdata!=null && jdata.length() > 0){

    		JSONObject jsonData; 
			int iCreateAccountStatus = -1;
			
			try {
				jsonData = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
				iCreateAccountStatus = jsonData.getInt("createaccountstatus");//accedemos al valor 
				Log.d("createaccountstatus","iCreateAccountStatus= "+iCreateAccountStatus);//muestro por log que obtuvimos
			} catch (JSONException e) {
				e.printStackTrace();
			}		            
             
			// Se valida el valor obtenido
			
			Log.d("iCreateAccountStatus",""+iCreateAccountStatus);
			
			switch (iCreateAccountStatus) {
				case 0:
					Log.d("loginstatus", "alta de usuario realizada correctamente");
					break;
				case 1:
					Log.d("loginstatus", "el usuario ya existia en la BD");
					break;
				default:
					Log.d("loginstatus", "error sql al insertar el usuario en la BD");
					break;					
			}
			
			iRetorno = iCreateAccountStatus;
    		 
    	}else{	// JSON obtenido invalido, revisar parte web
    		Log.e("Login usuario (JSON)", "JSON obtenido invalido, revisar parte web");
	    	iRetorno = -1;
    	}
		
		return iRetorno;
	}
	
	/*		CLASE ASYNCTASK
	 * Se usa esta clase para poder mostrar el dialogo de progreso mientras se envian los datos de usuario a la BD     
	 */

	class asynclogin extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialog = new ProgressDialog(CrearCuentaActivity.this);
	        pDialog.setMessage(getString(R.string.enviandoDatos));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();
	    }

		protected String doInBackground(String... params) {
			// Se crea el usuario en la BD
			int iRetornoValidacion = enviarDatos();
						
			switch (iRetornoValidacion) {
				case 0:
					return AbilidadeApplication.RETORNO_OK; //alta realizada correctamente
				case 1:
					return AbilidadeApplication.RETORNO_KO_USUARIO; //alta invalida - el usuario ya existia
				default:
					return AbilidadeApplication.RETORNO_KO_ERROR; //alta invalida - error SQL
			}
	    	
		}
	   
		protected void onPostExecute(String result) {

	       pDialog.dismiss();//ocultamos progess dialog.
	       Log.e("onPostExecute=",""+result);
	       
	       if (result.equals(AbilidadeApplication.RETORNO_OK)){

	    	   Intent i=new Intent(CrearCuentaActivity.this, ConfirmarUsuarioActivity.class);
			   startActivity(i);
			   finish();
				
	        }else{
	        	if (result.equals(AbilidadeApplication.RETORNO_KO_USUARIO)) {
        			errLogin(getString(R.string.altaIncorrectaUsuarioExiste));
        		} else {
        			errLogin(getString(R.string.altaIncorrectaError));
        		}
	        }
	     }
	}
}
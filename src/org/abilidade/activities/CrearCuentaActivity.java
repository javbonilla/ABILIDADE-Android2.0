package org.abilidade.activities;

import org.abilidade.R;
import org.abilidade.activities.AccederActivity.asynclogin;
import org.abilidade.application.AbilidadeApplication;

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
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crear_cuenta);
		
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
	
	public boolean enviarDatos() {
		//TODO Aqui es donde tendre que enviar los datos, por ahora retorno true siempre
		return true;
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
			// Se validan los datos contra la BD
			if (enviarDatos() == true) {    		    		
				return AbilidadeApplication.RETORNO_OK; //login valido
			} else {    		
				return AbilidadeApplication.RETORNO_KO; //login invalido     	          	  
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
	        	errLogin(getString(R.string.accesoIncorrecto));
	        }
	     }
	}
}
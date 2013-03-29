package org.abilidade.activities;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

public class webViewActivity extends GDActivity {
	
	// Definicion de los componentes de pantalla
	private WebView webView;
	
	// Definicion de atributos
	private int rutaAccesible;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.webview_ruta);
		
		// Elementos en la barra de accion --> Boton para localizar al usuario y boton de ayuda
		addActionBarItem(Type.List, R.id.action_bar_list);
		
		// Localizacion de los componentes de pantalla
		webView = (WebView) findViewById(R.id.webView);
		
		// Recibimos el intent y sus parametros, y los almacenamos en los atributos correspondientes
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			rutaAccesible = extras.getInt(AbilidadeApplication.RutaAccesibleParametro);
		}
		
		switch (rutaAccesible) {
			case 0:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle01.php");
				break;
			case 1:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle02.php");
				break;
			case 2:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle03.php");
				break;
			case 3:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle04.php");
				break;
			case 4:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle05.php");
				break;
			case 5:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle06.php");
				break;
			case 6:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle07.php");
				break;
			case 7:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle08.php");
				break;
			case 8:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle09.php");
				break;
			case 9:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle10.php");
				break;
			default:
				webView.loadUrl("http://abilidade.eu/r/rutadetalle01.php");
				break;
		}
	}
	
	@Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        if (item.getItemId() == R.id.action_bar_list) {
        	finish();
		} else {
			return super.onHandleActionBarItemClick(item, position);
		}
        return true;
    }
}

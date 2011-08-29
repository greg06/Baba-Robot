/*
 * Copyright (C) 2011 Gr�gory Fromain <gregoryfromain@gmail.com>
 *
 * This work is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License.
 * See the following website for more information: 
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 * 
 */

package com.robot.baba;

import com.robot.baba.R;
import com.robot.baba.capteur_actionneur.Capteur_actionneur;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class blu_car extends Activity
{ 
	private static final int REQUEST_ENABLE_BT = 2;
	
    // Variable du programme
    private boolean ledStat;
    private boolean connectStat = false;
    private Button led_button;
    private Button forward_button;
    private Button reverse_button;
    private Button connect_button;
    protected static final int MOVE_TIME = 80;
    private AlertDialog aboutAlert;
    private View VueAPropos;
    private View controlView;
    OnClickListener myClickListener;
    ProgressDialog myProgressDialog;
    
    long lastWrite = System.currentTimeMillis();
    
    public Capteur_actionneur action_baba = null;
    public RepetAction timer_action = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    
     /** Called when the activity is first created. */ 
     @Override 
     public void onCreate(Bundle savedInstanceState) 
     { 
    	 super.onCreate(savedInstanceState);
    	 
 		// On regarde si un module bluetooth existe sur le t�l�phone
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
         if (mBluetoothAdapter == null) { 
              Toast.makeText(this, R.string.no_bt_device, Toast.LENGTH_LONG).show(); 
              finish(); 
              return; 
         } 
         
         // Si le module bluetooth n'est pas activ�, on propose l'activation.
         if (!mBluetoothAdapter.isEnabled()) {
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         }
    	 
    	 action_baba = new Capteur_actionneur(this);
    	 timer_action = new RepetAction();
    	 // Bouton Cr�er la vue principale
    	 LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	 VueAPropos = inflater.inflate(R.layout.vue_a_propos, null);
    	 controlView = inflater.inflate(R.layout.main, null);
    	 controlView.setKeepScreenOn(true);
    	 setContentView(controlView);
    	 
         // R�cuperation des boutons du layout Main
    	 led_button = (Button) findViewById(R.id.led_button);
         forward_button = (Button) findViewById(R.id.forward_button);
         reverse_button = (Button) findViewById(R.id.reverse_button);
         connect_button = (Button) findViewById(R.id.connect_button);

         
         // Gestion des evenements dans la fenetre de dialogue d'aide et d'information
         myClickListener = new DialogInterface.OnClickListener() 
         {
    			@Override
    			public void onClick(DialogInterface dialog, int which) 
    			{
    				switch (which) {
    				case DialogInterface.BUTTON_POSITIVE:		//clique sur Ok.
    					dialog.dismiss();						//On ferme la fenetre de dialogue.
    				break;
    				case DialogInterface.BUTTON_NEUTRAL:		//clique sur : Acc�s au site web
    					//R�cuperation de l'URL.
    					Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(getResources().getString(R.string.website_url)));
    					startActivity(browserIntent);			// Lancement du navigateur internet.
    				break;
    				default: dialog.dismiss();					// Si aucun des cas valide on ferme le dialogue.
    				}
    			}
         };
         
         myProgressDialog = new ProgressDialog(this);
         
         // Creation du dialogue "A propos"
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setView(VueAPropos).setCancelable(true).setTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.appVersion)).setIcon(R.drawable.blu_car_icon).setPositiveButton(getResources().getString(R.string.okButton), myClickListener).setNeutralButton(getResources().getString(R.string.websiteButton), myClickListener);
         aboutAlert = builder.create();
         
         
          /**********************************************************************
           * Button de controle de Baba
           */
          
         // Clique sur le boutton de connexion a baba
         connect_button.setOnClickListener(new View.OnClickListener() 
         {
 			@Override
 			public void onClick(View v) 
 			{
				if (connectStat) 
				{									// Si on est connect�, On se deconnect.
					action_baba.bluetooth_close();
				}
				else
				{												// sinon on ce connect.				
					action_baba.bluetooth_connect();
				}
				connectStat = !connectStat;
 			}
 		});
       
         // Clique sur le boutton d'eclairage
         led_button.setOnClickListener(new View.OnClickListener() 
         {
			@Override
			public void onClick(View v) 
			{
				if (ledStat) {
					action_baba.eclairage(true);					
					led_button.setText(R.string.ledON);		//On change le texte sur le bouton
				}
				else{
					action_baba.eclairage(false);
					led_button.setText(R.string.ledOFF);	
				}
				ledStat = !ledStat;							// On inverse l'etat de la variable
			}
		});
                 
         // Drive forward
         forward_button.setOnTouchListener(new View.OnTouchListener() {
			boolean nouvelle_reference = true;
			float angle_reference = 0;
			int angle_diff = 0;
        	 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_DOWN) | (event.getAction() == MotionEvent.ACTION_MOVE)) 
				{
					if(nouvelle_reference)
					{
						angle_reference = action_baba.position_tel('a');
						nouvelle_reference = false;
					}
					angle_diff = (int) (angle_reference - action_baba.position_tel('a'));
					
					angle_diff = angle_diff * 2;
					
					int vitesse_droite = 80 + angle_diff;
					int vitesse_gauche = 80 - angle_diff;
					
					action_baba.moteur_droite(vitesse_droite);
					action_baba.moteur_gauche(vitesse_gauche);
					
					forward_button.setPressed(true);
					//action_baba.avant();
					return true;
					
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) 
				{
					nouvelle_reference = true;
					forward_button.setPressed(false);
					action_baba.stop();
					return true;
				}
				forward_button.setPressed(false);
				return false;
			}
		});
         
         // Back up
         reverse_button.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if ((event.getAction() == MotionEvent.ACTION_DOWN) | (event.getAction() == MotionEvent.ACTION_MOVE)) 
				{
					reverse_button.setPressed(true);
					action_baba.arriere();					
					return true;
					
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) 
				{
					reverse_button.setPressed(false);
					action_baba.stop();
					return true;
				}
				
				reverse_button.setPressed(false);
				return false;
			}
		});
        
     }
     
     public class RepetAction
     {
    	 Timer tempo;
    	 public RepetAction()
    	 {
    		 tempo = new Timer();
    		 tempo.schedule(new MonAction(), 0, 100); // tempo de 100 ms
    	 }

    	 class MonAction extends TimerTask 
    	 {
    		 public void run()
    		 {
    			 int pos_X = (int) ((action_baba.position_tel('x') * -4.5) + 90);
    			 int pos_Y = (int) ((action_baba.position_tel('y') * 4.5) + 90);
    			 action_baba.tourelle((char) (pos_X), (char) (pos_Y));
    		 }
    	 }
     }
   /*  
     private final SensorEventListener mSensorListener = new SensorEventListener() {
    	 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 		
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			
 			
 			// Checks whether to send steering command or not
 			long date = System.currentTimeMillis();
 			if (date - lastWrite > 100) {
  			float axe_X = event.values[0];
 			float axe_Y = event.values[1];
			int pos_X = (int) ((axe_X * -4.5) + 90);
			int pos_Y = (int) ((axe_Y * 4.5) + 90);
			action_baba.tourelle((char) (pos_X), (char) (pos_Y));			
 				lastWrite = date;
 			}
 		}
      };
 */     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.option_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
         switch (item.getItemId()) 
         {
         case R.id.about:
             // Show info about the author (that's me!)
        	 aboutAlert.show();
             return true;
         }
         return false;
     }

     @Override 
     public void onResume() 
     { 
          super.onResume();
     } 

     @Override 
     public void onDestroy() 
     {
    	 action_baba.bluetooth_close();
         super.onDestroy(); 
     } 
}
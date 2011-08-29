package com.robot.baba.capteur_actionneur;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;			// Classe qui repr�sente le module bluetooth de votre Android, elle permet de scanner les p�riph�riques pr�sents, d'activer / d�sactiver le bluetooth, ...
import android.bluetooth.BluetoothDevice;			// Classe qui repr�sente un p�riph�rique bluetooth
import android.bluetooth.BluetoothSocket;			// Classe qui permet d'obtenir les canaux d'�criture (�mission) et de lecture (r�ception) du p�riph�rique
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Communication_bluetooth {
    public static final char OCTET_START = '0';
    public static final char OCTET_FIN_1 = '8';
    public static final char OCTET_FIN_2 = '9';
    
	private BluetoothDevice device = null;			// le p�riph�rique (le module bluetooth)
	private BluetoothSocket socket = null; 
	private InputStream receiveStream = null;		// Canal de r�ception
	private OutputStream sendStream = null;			// Canal d'�mission
	
	private ReceiverThread receiverThread;			// On cr�er le thread de r�ception des donn�es avec l'Handler venant du thread UI

	Handler handler;

	public Communication_bluetooth(Handler hstatus, Handler h) {
		
		// On r�cup�re la liste des p�riph�riques associ�s
		Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
				
		for(int i=0;i<pairedDevices.length;i++) {					// On parcours la liste pour trouver notre module bluetooth
			 // On teste si ce p�riph�rique contient le nom du module bluetooth connect� au microcontr�leur
			if(pairedDevices[i].getName().contains("Bluetooth_V3")) {
				device = pairedDevices[i];
				try {												// On r�cup�re le socket de notre p�riph�rique					
					socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					receiveStream = socket.getInputStream();		// Canal de r�ception (valide uniquement apr�s la connexion)
					sendStream = socket.getOutputStream();			// Canal d'�mission (valide uniquement apr�s la connexion)
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}

		handler = hstatus;
		
		receiverThread = new ReceiverThread(h);
	}
	
	public void sendData(String data) {
		sendData(data, false);
	}
	
	// Pour envoyer des donn�es, on va tout simplement utiliser les fonctions write(...) de OutputStream
	public void sendData(String data, boolean deleteScheduledData) {
		try {									
			sendStream.write(data.getBytes());						// On �crit les donn�es dans le buffer d'envoi
	        sendStream.flush();										// On s'assure qu'elles soient bien envoy�s
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void trame_envoi(char adresse, char fonction, char donnee)
    { 	 
    	/*
		 StringBuffer trame = new StringBuffer();
		 trame = trame.append(OCTET_START).append(adresse).append(fonction).append(donnee).append(OCTET_FIN_1).append(OCTET_FIN_2);
		 sendData( trame.toString());
		 */
		 String trame = Character.toString(OCTET_START);
		 trame = trame.concat(Character.toString(adresse));
		 trame = trame.concat(Character.toString(fonction));
		 trame = trame.concat(Character.toString(donnee));
		 trame = trame.concat(Character.toString(OCTET_FIN_1));
		 trame = trame.concat(Character.toString(OCTET_FIN_2));
		 sendData(trame);
		 Log.v("Baba", trame);
    }
	
	// Pour se connecter, on utilise la fonction connect(); de BluetoothSocket. 
	// La connexion pouvant prendre du temps, il est n�cessaire de la lancer dans un autre thread pour �viter de bloquer l'application.
	public void connect() {
		new Thread() {
			@Override 
			public void run() {
				try {
					socket.connect();								// Tentative de connexion
																	// Connexion r�ussie
					Message msg = handler.obtainMessage();
					msg.arg1 = 1;
	                handler.sendMessage(msg);
	                
					receiverThread.start();
					
				} catch (IOException e) {							// Echec de la connexion
					Log.v("Baba", "Connection Failed : "+e.getMessage());
					e.printStackTrace();
				}
			}
		}.start();
	}

	// Enfin, pour fermer la connexion, il y a la fonction close() de BluetoothSocket
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BluetoothDevice getDevice() {
		return device;
	}
	
	
	// Pour recevoir des donn�es, le plus simple est de faire un Thread qui va v�rifier l'arriv� de donn�es en permanence. 
	// On utilise les fonctions read(...) et available() de InputStream pour lire ces donn�es et v�rifier si de nouvelles sont disponibles.
	
	// Probl�me : on ne peut afficher les donn�es depuis ce nouveau thread : android impose de les envoy�es dans le thread de l'UI. 
	// On utilise donc la classe Handler. On doit alors modifier le constructeur pour r�cup�rer un Handler du thread de l'UI.
	private class ReceiverThread extends Thread {
		Handler handler;
		
		ReceiverThread(Handler h) {
			handler = h;
		}
		
		@Override public void run() {
			while(true) {
				try {
					if(receiveStream.available() > 0) {				// On teste si des donn�es sont disponibles

						byte buffer[] = new byte[100];
						int k = receiveStream.read(buffer, 0, 100);	// On lit les donn�es, k repr�sente le nombre de bytes lu

						if(k > 0) {
							byte rawdata[] = new byte[k];
							for(int i=0;i<k;i++)
								rawdata[i] = buffer[i];
							
							String data = new String(rawdata);		// On convertit les donn�es en String

							Message msg = handler.obtainMessage();	// On envoie les donn�es dans le thread de l'UI pour les affich�es
							Bundle b = new Bundle();
							b.putString("receivedData", data);
			                msg.setData(b);
			                handler.sendMessage(msg);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

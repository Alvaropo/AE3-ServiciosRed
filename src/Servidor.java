//package es.florida.servidorHTTP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

public class Servidor {

    public static void main(String[] args) throws Exception {
    	
    	
    	
    	System.out.println("Arranca el servidor");
    	
    	String host = "localhost"; //127.0.0.1
		int puerto = 5000;
    	
    	
    	InetSocketAddress direccionTCPIP = new InetSocketAddress(host,puerto);
    	int backlog = 0; //Numero de conexiones pendientes que el servidor puede mantener en cola
    	HttpServer servidor = HttpServer.create(direccionTCPIP, backlog);
    	
    	GestorHTTP gestorHTTP = new GestorHTTP();   //Clase que gestionara los GETs, POSTs, etc.
    	String rutaRespuesta = "/test";
    	servidor.createContext(rutaRespuesta, gestorHTTP);   //Crea un contexto, asocia la ruta al gestor HTTP
    	
    	
    	ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
    	servidor.setExecutor(threadPoolExecutor); 
    	
    	servidor.start();
    	System.out.println("Servidor HTTP arranca en el puerto " + puerto);
    	
    	
    }

}
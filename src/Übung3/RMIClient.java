// Datei: RMIClient.java
// Autor: Christian Mehns
// Thema: RMI Server und Client
// -------------------------------------------------------------
package Übung3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.StringTokenizer;

//import Übung3.PhoneBook3;


public class RMIClient implements Remote{
	
	private static int port = 9876;
	
	private static Socket clientSocket;   

	
	
	
	
	public static void main(String[] args) throws Exception {
		
		RMIClient server = new RMIClient();

	    // Vereinbarungen
	    // ---------------------------------------------------------
	    ServerSocket serverSocket       = null;  // Fuer das accept()
	    InputStream is        = null;  // Aus dem Socket lesen
	    InputStreamReader isr = null;
	    BufferedReader br     = null;
	    OutputStream os       = null;  // In den Socket schreiben
	    PrintWriter pw        = null;
	    String requestMessageLine          = null;  // Eine Zeile aus dem Socket
	    String host           = null;  // Der Hostname
	    boolean active = true;

	    // Programmstart
	    host = InetAddress.getLocalHost().getHostName();
	    System.out.println("Server startet auf "+host+" an Port"+port);
	    serverSocket = new ServerSocket(port);
	    
	    
	    // init RMIServer
	    RMIServerInterface rmiServer = (RMIServerInterface) Naming.lookup("RMIServer");
	    
	    // loop that wait for request
	    while(active) {
	      System.out.println("\nWait in accept()");
	      clientSocket = serverSocket.accept();              

	      os = clientSocket.getOutputStream();
		  pw = new PrintWriter(os, true);
	     
	      
	      // Read request
	      is    = clientSocket.getInputStream();
	      isr   = new InputStreamReader(is);
	      br    = new BufferedReader(isr);
	      requestMessageLine = br.readLine();
	      System.out.println("Kontrollausgabe: "+requestMessageLine);
	      
	      StringTokenizer tokenizer = new StringTokenizer(requestMessageLine);
	      String method = tokenizer.nextToken();
	      String query = tokenizer.nextToken();		
	      
	      
	      // Favicon-Requests denied
	      if(query.startsWith("/favicon")) {
	        System.out.println("Favicon-Request");
	        br.close();
	        continue;                       // Zum naechsten Request
	      }
	      
	      
	      // process request
	      if (method.equals("GET") && query.contains("?")) { 
	    	  StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine, "&");
	    	  
	    	  String name = tokenizedLine.nextToken();
	    	  name = name.substring(name.indexOf("=")+1);
	    	  name = URLDecoder.decode(name, "UTF-8"); // decode URL
	    	  
	    	  String number = tokenizedLine.nextToken();
	    	  number = number.substring(number.indexOf("=")+1);
	    	  
	    	  String action = tokenizedLine.nextToken();
	    	  action = action.substring(action.indexOf("=")+1, action.indexOf(" "));
	    	  
	    	  
	    	  System.out.printf("Name: %s, Number: %s, Action: %s \n", name, number, action);

	    	  
	    	  // shutdown Server when user presses button quit
	    	  if(action.equals("quit")){	    		  
	    		  System.out.println("RMIClient wird herunter gefahren");	    		  
	    		  server.printWebsite("Server wurde beendet");
	    		  serverSocket.close();	    		  
	    		  rmiServer.quit();
	    		  active = false;
	    		  System.out.println("Shutdown RMIClient");	    		  
//	    		  break;
	    		  continue;         
	    	  }
	    	  
	    	  // error if empty name or number
	    	  if(name.isEmpty() && number.isEmpty()){
	    		  System.out.println("User sent empty input");	    		  
	    		  server.printWebsite("Leere Eingaben sind ungültig!");
	    	  } 
	    	  
	    	  // run PhoneBook
	    	  else {
	    		  System.out.println("Request RMIServer");	    		  
	    		  String result = rmiServer.search(name, number);
	    		  System.out.println("Send Results to user");	    		  
	    		  server.printWebsite(result);
	    	  }	 
	      }
	      
	      // send Formular
	      else{
	    	  System.out.println("Send formular page to client");
	    	  server.sendFormuler();
	      }
	       
	      
	    }  // end while

	  }  // end main()
	
	
	
	private void sendFormuler() throws Exception{
		// send formular
        OutputStream output;
        PrintWriter printer = null;
        BufferedReader in = null;
        
		try {
			output = clientSocket.getOutputStream();
			printer  = new PrintWriter(output, true);
	        
	        // read textfile for html form
	        in = new BufferedReader(new FileReader("form.txt"));
	        String line = null;
	        while ((line = in.readLine()) != null) {
	        	printer.println(line);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Output failed");
			e.printStackTrace();
		}
		finally{
	        //close all streams
	        printer.close();
	        in.close();
		}  
	}
	
	
	private void printWebsite(String message) throws Exception{

        OutputStream output;
        PrintWriter printer = null;
        PrintStream printStream = null;
        
        
		try {
			output = clientSocket.getOutputStream();
			printer  = new PrintWriter(output);
			printStream = new PrintStream(output);
	        
			printer.println("HTTP/1.1 200 OK");               // Der Header
			printer.println("Content-Type: text/html");
			printer.println();
			printer.println();
			printer.println("<html lang='de'>");                    // Die HTML-Seite
			printer.println("<head>");
			printer.println("<meta charset='utf-8'>");
			printer.println("<html>");// Die HTML-Seite
			printer.println("<body>");
			printer.println("<h1>Ergebnis</h1>");
			printer.println(message);
			
			printer.println("<a href='http://localhost:9876'>");
			printer.println("<br><br><button type='submit'>Zurück</button>");
			printer.println("</a>");
			
			printer.println("</body>");
			printer.println("</html>");
			printer.println();
			
			printer.flush();		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Output failed");
			e.printStackTrace();
		}
		finally{
	        //close all streams
	        printer.close();
	        printStream.close();
		}  
	}

}

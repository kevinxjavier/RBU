package com.entel.arquitectura;

import java.util.Properties;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class Backup {
		
	private Properties propiedades;
	private String hostname; 
	private String username;
	private String password;
	private String command;
	private String outputType;
	
	public Backup(String filename) throws IOException {
		// Loading the properties
		propiedades = new Properties();
		propiedades.load(new FileInputStream(currentPath() + "RBU\\" + filename));
		
		hostname = propiedades.getProperty("hostname");
		username = propiedades.getProperty("username");
		password = propiedades.getProperty("password");
		command = propiedades.getProperty("command");
		outputType = propiedades.getProperty("output_type");	
	}
	
	public void conectSession() {
		String output = "";
		try	{
			Connection conn = new Connection(hostname);

			conn.connect();

			boolean isAuthenticated = conn.authenticateWithPassword(username, password);

			if (isAuthenticated == false) {
				throw new IOException("Authentication failed.");
			}
			
			Session sess = conn.openSession();

			
			sess.execCommand(command);

			InputStream stdout = new StreamGobbler(sess.getStdout());
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
  			
			String line = "";
			while (true) {		
				if ((line = br.readLine()) == null){
					break;
				}
				output += line + "\n";
			}
						
			sess.close();

			conn.close();
		
			displayOutput(output);
			
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.exit(2);
		}
		
	}
	
	public void displayOutput(String output) throws IOException {
		if (outputType.equals("file")) {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream("./" + propiedades.getProperty("output_file_name")));
			dos.writeBytes(output);
			dos.close();
		} else if (outputType.equals("console")) {
			System.out.println(output);
		}
	}
	
	public String currentPath(){

		String[] path = System.getProperty("user.dir").split("\\\\");
		String newPath = "";
		for(int i = 0; i < path.length - 1; i++){
			newPath += path[i] + "\\";
		}		
		return newPath;
		
	}	
	
	public static void main(String[] args) throws IOException {

		new Backup(args[0]).conectSession();
		
	}
	
}
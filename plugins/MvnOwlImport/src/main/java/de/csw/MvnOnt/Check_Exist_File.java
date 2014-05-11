/*******************************************************************************
 * Copyright 2013 Corporate Semantic Web, Freie Universitaet Berlin
 * 
 * This file is part of the Coporate Semantic Web Project.
 * 
 * This work has been partially supported by the "InnoProfile-Corporate Semantic Web" project funded by the German Federal
 * Ministry of Education and Research (BMBF) and the BMBF Innovation Initiative for the New German Laender - Entrepreneurial Regions.
 * 
 * http://www.corporate-semantic-web.de/
 * 
 * 
 * Freie Universitaet Berlin
 * Copyright (c) 2007-2013
 * 
 * 
 * Institut fuer Informatik
 * Working Group Coporate Semantic Web
 * Koenigin-Luise-Strasse 24-26
 * 14195 Berlin
 * 
 * http://www.mi.fu-berlin.de/en/inf/groups/ag-csw/
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.csw.MvnOnt;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class Check_Exist_File {
	
	private String location; 
	private boolean status;
	private URL url;
	
	public Check_Exist_File(String location) {
		
		this.location = location;
		
	}
	
	boolean exist_File(){
		
	
		HttpURLConnection conn = null;
		
		try {
			
			
			url = new URL(location);
			
			conn = (HttpURLConnection) url.openConnection();
			
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
				
				status = true;
			}else{
				
				status = false;
			}
			
		}catch(UnknownHostException e){
			
			status=false;
			
		}catch (MalformedURLException e) {
			
			status=false;
			
		} catch (IOException e) {
			
			status=false;
		}
		
		return status;
	}
}

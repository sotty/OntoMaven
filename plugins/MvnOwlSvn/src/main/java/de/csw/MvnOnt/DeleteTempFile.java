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

import java.io.File;

public class DeleteTempFile {
	
	private FileFinder finder;
	
	
	public DeleteTempFile(){
		
		finder = new FileFinder();
	}
	
	
	public void delTemp(String temp_directory){
		
		File file = new File(temp_directory);
		
		
		if(file.exists()){
			
			if(file.isDirectory()){
				
				for(File fl : finder.find(temp_directory)){
					
					//System.out.println(fl.getName());
					fl.delete();
				}
				
			}
				
			file.delete();
			
		}
		
	}

}

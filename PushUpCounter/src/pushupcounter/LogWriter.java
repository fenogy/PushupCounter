/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushupcounter;

/**
 *
 * @author prabashk
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*Helper class to handle log file writing task*/
public class LogWriter {
    
    
    private static final String COMMA_DELIMITER = ",";                                                                                                                          
    private static final String NEW_LINE_SEPARATOR = "\n";                                  
                                                                                                                                                    
    private static final String FILE_HEADER = "time,distance";           
                                                                                        
    FileWriter fileWriter = null;   

/*Create a new file and keep it open*/    
public void createCsvFile(String fileName) {                                      
                                                                                        
    try {                                                                               
                                                                                        
        fileWriter = new FileWriter(fileName);                                          
                                                    
        //Write the CSV file header                                                                            
        fileWriter.append(FILE_HEADER.toString());                                      
                                    
        //Add a new line separator after the header                                                                                
        fileWriter.append(NEW_LINE_SEPARATOR); 
        
        System.out.println("CSV file was created successfully !!!");                    
                                                                               
    } catch (Exception e) {                                                             
                                                                                        
        System.out.println("Error in CsvFileWriter !!!");                                                                                  
        e.printStackTrace();                                                            
                                                                                        
    } finally {                                                                         
                                                                             
        try {
            
            fileWriter.flush();                                                         
            //fileWriter.close();                                                         
                                                                                        
        } catch (IOException e) {                                                       
                                                                                        
            System.out.println("Error while flushing/closing fileWriter !!!");          
            e.printStackTrace();                                                                               
        }                                                                               
                                                                                 
    }                                                                                   
                                                                                        
}

/*Append a text n keep it open */
public void appendLogFile(String str)
{
    if(fileWriter != null)
    {
        try {                                                                               
                                                                            
            fileWriter.append(str); 
                                                                      
        } catch (Exception e) {                                                             
                                                                                        
            System.out.println("Error in CsvFileWriter !!!");                                                                                  
            e.printStackTrace();                                                            
                                                                                        
        } finally {                                                                         
                                                                             
            try {
            
                fileWriter.flush();                                                         
                //fileWriter.close();                                                         
                                                                                        
            } catch (IOException e) {                                                       
                                                                                        
                System.out.println("Error while flushing/closing fileWriter !!!");          
                e.printStackTrace();                                                                               
            }                                                                               
                                                                                 
        }          
    }
}

/*Close the file here */
public void closeCsvFile()
{
    try {
            
        fileWriter.flush();                                                         
        fileWriter.close();                                                         
                                                                                        
    } catch (IOException e) {                                                       
                                                                                        
        System.out.println("Error while flushing/closing fileWriter !!!");          
                e.printStackTrace();                                                                               
    }            
    
}

}

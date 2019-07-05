/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushupcounter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Math.abs;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Chathura
 */
public class Find_Min_Max {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    GUI window = null;
    String fileName = "";
    int Limit = 0;
    
    public Find_Min_Max(String name, int limit,GUI window){
        
        this.window = window;
        
//        try{
//           
//            window.communicator.mLpf = new LowPassFilter(0.5);
//        }
//        catch(Exception error)
//        {
//             System.out.println(error.getCause());       
//        }
        fileName = name;
        Limit = limit;
    }
    
   // public static void main(String[] args) throws IOException {
    public int CountPushUps(boolean chart) throws IOException{
        String line;
        
        // Information collection variables        
        int window_size = 5;
        int pading_size = 0;
        int count = 0;
        int marks = 0;
        boolean same_max = false;
        boolean same_min = false;
        boolean start = false;
        
        int maxima = 0;
        int minima = 1000;
        int maxLocation = 0;
        int minLocation = 0;
        int maxValue = 0;
        int minValue = 1000;
        
        
        int xAxis = 0;
        int movAverage = 0;
        int movTotal = 0;
        int movSamples = 0;
        int fullScaleCounts = 0;
        boolean isAveraging = false;
        
        //int xAxisFiltered = 0;
        
        String preState = "";
        DecimalFormat df = new DecimalFormat("####0.00");
        
        List<Integer> list = new ArrayList<Integer>();
        List<Integer> locationlist = new ArrayList<Integer>();
        
        try (
            InputStream fis = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            br.readLine();
            
            while ((line = br.readLine()) != null) {
                List<String> elements = Arrays.asList(line.split(","));
                int time = Integer.parseInt(elements.get(0));
                int value = Integer.parseInt(elements.get(1));

                if(list.size() < window_size) {
                    list.add(value);
                    locationlist.add(time);
                    //window.series.add(xAxis++, value, true);
                    //window.seriesFiltered.add(xAxis++, window.communicator.mLpf.filter(value), true);
                }
                else {
                    //list.add(value);
                    Integer intVal = 0;
                    Double doubleVal = 0.0;
                    doubleVal = window.communicator.mLpf.filter(value);
                    intVal = doubleVal.intValue(); 
                    
                    list.add(intVal);
                    locationlist.add(time);
                    
                    if(isAveraging)
                    {
                        if(intVal >799)
                        {
                           fullScaleCounts++; 
                           
                        }
                        if(fullScaleCounts >5)
                        {
                            isAveraging =false;
                            window.txtLog.append("Averaging is Finished\n\r");
                            
                        }
                        movTotal = movTotal + intVal;
                        movSamples++;
                        
                        movAverage = movTotal/movSamples;
                        
                        
                    }
                    window.series.add(xAxis, value, true);
                    window.seriesFiltered.add(xAxis, window.communicator.mLpf.filter(value), true);
                    xAxis++;
                    //window.txtLog.append(String.valueOf(xAxis) + ", " + String.valueOf(value) + ", " + String.valueOf(value) + "\n");
                    list.remove(list.get(0));
                    locationlist.remove(locationlist.get(0));
                    
                    marks = 0;
                    same_max = false;
                    same_min = false;
               
                    // for maxima
                    //System.out.println(list.get(0)+" "+list.get(1)+" "+list.get(2)+" "+list.get(3)+" "+list.get(4));
                    if((list.get(2) - list.get(1)) > pading_size)
                        marks++;
                    
                    if((list.get(2) - list.get(3)) > pading_size)
                        marks++;
                    
                    if((list.get(1) - list.get(0)) > pading_size)
                        marks++;
                    
                    if((list.get(3) - list.get(4)) > pading_size)
                        marks++;
                    
                    // for minima
                    if((list.get(1) - list.get(2)) > pading_size)
                        marks--;
                    
                    if((list.get(3) - list.get(2)) > pading_size)
                        marks--;
                    
                    if((list.get(0) - list.get(1)) > pading_size)
                        marks--;
                    
                    if((list.get(4) - list.get(3)) > pading_size)
                        marks--;
            
                    //for both
                    if((list.get(2) - list.get(1)) == pading_size && list.get(2)!=800)
                        same_max = true;
                    
                    if((list.get(2) - list.get(3)) == pading_size && list.get(2)!=800)
                        same_max = true;
                    if((list.get(1) - list.get(2)) == pading_size && list.get(2)!=800)
                        same_min = true;
                    
                    if((list.get(3) - list.get(2)) == pading_size && list.get(2)!=800)
                        same_min = true;
                   // System.out.println(marks);
                    if(marks >= 2 || (marks==3 && (same_min || same_max)) || (marks==2 && (same_min && same_max)))
                    {
                        if((maxLocation == 0 || ((locationlist.get(2)-minLocation)>333 && minLocation != 0)) && start)
                        {
                            //System.out.println(preState+" "+list.get(2)+" "+ minValue+" "+maxValue+" "+locationlist.get(2));
                            if(!(("max".equals(preState) && list.get(2)<= maxValue) || ("min".equals(preState) && list.get(2)<= minValue)))
                            {
                                //System.out.println(abs(list.get(2)-minValue));
                                if(abs((list.get(2)-minValue)) <= Limit)
                                    System.out.println("There is not a not valid pushup at with pushup heinght of "+abs((list.get(2)-minValue))); //+" at "+locationlist.get(2));
                                else{
                                   /* System.out.println("~ identified: maxima");
                                    System.out.println("\t location: " + locationlist.get(2));
                                    System.out.println("\t value: " + list.get(2));*/
                                    
                                    maxLocation = locationlist.get(2);
                                    maxValue = list.get(2);
                                    
                                    if("min".equals(preState)){
                                        
                                        count++;
                                        if(count == 1)
                                        {
                                            isAveraging = true;
                                            window.txtLog.append("Averaging Started\n\r");
                                        }
                                        window.txtLog.append(String.valueOf(xAxis) + ", "+String.valueOf(time) + "\n\r");
                                        window.txtLog.append( String.valueOf(movAverage)+ "\n\r");
                                        System.out.println("Pushup Number "+count+" with rate of "+df.format((double)(1000)/abs((list.get(2)-minValue)))+"Hz at "+locationlist.get(2));
                                    }
                                    
                                    preState = "max";
                                }
                            }
                            
                            if(maxima < list.get(2))
                                maxima = list.get(2);
                        }
                    }
                    
                    if(marks <= -2 || (marks==-3 && (same_min || same_max)) || (marks==-2 && (same_min && same_max)))
                    {
                        if(minLocation == 0 || ((locationlist.get(2)-maxLocation)>333 && maxLocation != 0) || maxLocation == 0)
                        {
                            //System.out.println(preState+" "+list.get(2)+" "+ minValue+" "+maxValue+" "+locationlist.get(2));
                            if(!(("min".equals(preState) && list.get(2)>= minValue) || ("max".equals(preState) && list.get(2)>= maxValue)))
                            {
                                //System.out.println(abs(maxValue-list.get(2)));
                                if(abs((maxValue-list.get(2))) <= Limit && maxValue !=0)
                                    System.out.println("This is an unacceptable lower point with "+abs((maxValue-list.get(2)))+" from previous push up"); //+"at "+locationlist.get(2));
                                else{
                                  /*  System.out.println("~ identified: minima");
                                    System.out.println("\t location: " + locationlist.get(2));
                                    System.out.println("\t value: " + list.get(2));*/
                                  //  System.out.println("There is a minimum point at "+locationlist.get(2));
                                    if(minLocation == 0)
                                        start = true;
                                    
                                    minLocation = locationlist.get(2);
                                    minValue = list.get(2);
                                    preState = "min";
                                }
                            }

                            if(minima > list.get(2))
                                minima = list.get(2);
                        }
                    }
                }
            }
        }
        
        // print all results after the operation
        System.out.println("");
       /* System.out.println("Maxima : " + maxima);
        System.out.println("Minima : " + minima);*/
        System.out.println("No of Push ups : " + count);
        
        return count;
    }
    
}

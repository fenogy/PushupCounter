/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pushupcounter;

import gnu.io.*;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

//public class Communicator implements SerialPortEventListener
public class Communicator 
{
    //passed from main GUI
    GUI window = null;

    //for containing the ports that will be found
    private Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    private HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;
    private int timeSeconds = 0;
    private int rcvdDataDuration = 0;
    private boolean firstValueRcvd = false;
    private boolean newLineRcvd = false;
    String lineText = "";
    String lineTextCopy = "";
    public boolean isExtraThreadStarted = false;
    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";
    String strY = null;
    String strYcopy = "";
    int xAxis = 0;
    int yAxis = 0;
    int dataTime = 0;
    LogWriter logWriter = null;
    public LowPassFilter mLpf;
    
    public Communicator(GUI window)
    {
        this.window = window;
        
        try{
           
            mLpf = new LowPassFilter(0.2);
        }
        catch(Exception error)
        {
             System.out.println(error.getCause());       
        }
        
    }

    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    private class SearchPorts extends Thread {

        
        @Override
        public void run(){
            
            while(!bConnected){
                if(!bConnected)
                {
                    System.out.println("Searching ports...");
                    searchForPorts();
                
                    try {
                        System.out.println("Sleeping...");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                
                }
            }
        }
    }
    
    //Used for measuring the missing commands. Now can be uses just as a macro timer
    private class TimeUpdate extends Thread {

        
        @Override
        public void run(){
            
            
            while(bConnected){
                
                timeSeconds++;
                dataTime = rcvdDataDuration/20;
                if(dataTime == 1)
                {
                    timeSeconds = 1;
                }
                window.jLabel8.setText(String.valueOf(timeSeconds));
                window.jLabel9.setText(String.valueOf(dataTime));
                
                try {
                    
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                
            }
        }
    }
    
    
    public void timeKeep()
    {
        TimeUpdate timer = new TimeUpdate();
        timer.start();
    }
    
    //keep searching indefinitly
    public void keepSearhForPorts()
    {
        SearchPorts portSeek = new SearchPorts();
        portSeek.start();
    }
   
    //Search for a port once
    public void searchForPorts()
    {
        String tmpPortName = "";
        String tmpAvailablePortName = "";
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                tmpPortName = curPort.getName();
                
                for (int i = 0; i < window.cboxPorts.getItemCount();i++)
                {
                    tmpAvailablePortName = window.cboxPorts.getItemAt(i).toString();
                    if( !tmpAvailablePortName.equals(tmpPortName))
                    {
                        window.cboxPorts.addItem(curPort.getName());
                        portMap.put(curPort.getName(), curPort); 
                    }
                }
                
                if(window.cboxPorts.getItemCount() == 0)
                {
                    window.cboxPorts.addItem(curPort.getName());
                    portMap.put(curPort.getName(), curPort); 
                    
                }
                
                
            }
        }
    }

    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public void connect()
    {
        String selectedPort = (String)window.cboxPorts.getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
            
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
            
            
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;
            setSerialPortParameters();
            //for controlling GUI elements
            setConnected(true);

            //logging
            logText = selectedPort + " opened successfully.";
            window.txtLog.setForeground(Color.black);
            window.txtLog.append(logText + "\n");

            //CODE ON SETTING BAUD RATE ETC OMITTED
            //XBEE PAIR ASSUMED TO HAVE SAME SETTINGS ALREADY

            //enables the controls on the GUI if a successful connection is made
            window.keybindingController.toggleControls();
        }
        catch (PortInUseException e)
        {
            logText = selectedPort + " is in use. (" + e.toString() + ")";
            
            window.txtLog.setForeground(Color.RED);
            window.txtLog.append(logText + "\n");
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            window.txtLog.append(logText + "\n");
            window.txtLog.setForeground(Color.RED);
        }
    }
    
    private void setSerialPortParameters() throws IOException {
        int baudRate = 115200; // 57600bps
 
        try {
            // Set serial port to 57600bps-8N1..my favourite
            serialPort.setSerialPortParams(
                    baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
 
            serialPort.setFlowControlMode(
                    SerialPort.FLOWCONTROL_NONE);
        } catch (UnsupportedCommOperationException ex) {
            throw new IOException("Unsupported serial port parameter");
        }
    }

    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream()
    {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            writeData(0, 0);
            
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
            return successful;
        }
    }

    ////////////////////////
    //Data is read over this thred
    private class ReadThread implements Runnable {
       public void run() {
            while(true) {
               readSerial();
            }
        }
    }
   
    private byte[] readBuffer = new byte[40];
 
    private void readSerial() {
        
        try {
            
            int availableBytes = input.available();
            
            if (availableBytes > 0) {
                
                // Read the serial port
                input.read(readBuffer, 0, 40);

                //Parse the byte buffer to an array
                String s = new String(readBuffer, Charset.forName("utf-8"));
                               
                //System.out.println(s);
                
                //Check for the required pattern and extract the data
                byte[] lineByte = new byte[40];
                for(int i =0; i < 40; i++)
                {
                    lineByte[i] = readBuffer[i];
                    

                    if(readBuffer[i] == 0x0A )
                    {
                        newLineRcvd = true;
                        
                        lineText = new String(lineByte, Charset.forName("utf-8"));
                        lineText = lineText.substring(0, lineText.indexOf(0));
                        

                        //debug out
                        textLogUpdate();
                        //System.out.print(lineText);
                        
                        //Update the chart in the same thread
                        chartDataFeed2();
                        
                        //Cleaout the arrays to eliminate cross talk with old data
                        for(i =0; i < 40; i++)
                        {
                            readBuffer[i] = 0;
                            lineByte[i] = 0;
                        }
                    }  

            }
            }
        } catch (IOException e) {
        }
    }
    
    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener()
    {
        //Will go with continuos read instead of event lisener due to delayed response
        new Thread(new ReadThread()).start();

//        try
//        {
//            serialPort.addEventListener(this);
//            serialPort.notifyOnDataAvailable(true);
//            //new Thread(new ReadThread()).start();
//        }
//        catch (TooManyListenersException e)
//        {
//            logText = "Too many listeners. (" + e.toString() + ")";
//            window.txtLog.setForeground(Color.red);
//            window.txtLog.append(logText + "\n");
//        }
    }

    //disconnect the serial port
    //pre: an open serial port
    //post: clsoed serial port
    public void disconnect()
    {
        firstValueRcvd = false;
        timeSeconds = 0;
        //close the serial port
        try
        {
            writeData(0, 0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
            window.keybindingController.toggleControls();

            logText = "Disconnected.";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }

    final public boolean getConnected()
    {
        return bConnected;
    }

    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
//    public void serialEvent(SerialPortEvent evt) {
//        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
//        {
//            try
//            {
//                byte singleData = (byte)input.read();
//
//                if (singleData != NEW_LINE_ASCII)
//                {
//                    logText = new String(new byte[] {singleData});
//                    
//                    //append data until a newline recived;
//                    //lineText = logText + lineText;
////                    if(!isExtraThreadStarted)
////                    {
////                        fastUpdate();
////                    }
//                    
//                    chartDataFeed();
//                    //window.txtLog.append(logText);
//                    //window.txtLog.append(String.valueOf(yAxis));
//                    
//                }
//                else
//                {
//                    //gather all line copy it and let other thread to update ui
//                    //lineText = lineText + "\n";
//                    //lineTextCopy = lineText;
//                    //lineText = "";
//                    //window.txtLog.append("\n");
//                    //newLineRcvd = true;
//                }
//            }
//            catch (Exception e)
//            {
//                logText = "Failed to read data. (" + e.toString() + ")";
//                window.txtLog.setForeground(Color.red);
//                window.txtLog.append(logText + "\n");
//            }
//        }
//    }
    
    
//    private class UpdateUI extends Thread {
//
//        
//        @Override
//        public void run(){
//            
//            while(bConnected){
//                if(newLineRcvd)
//                {
//                    chartDataFeed();
//                    window.txtLog.append(logText);
//                
//                }
//            }
//        }
//    }
    
//    public void fastUpdate()
//    {
//        UpdateUI updater = new UpdateUI();
//        updater.start();
//    }

    public void textLogUpdate()
    {
        window.txtLog.append(lineText);
    }
    
    public void chartDataFeed2() 
    {
        
        String strX = null;
        
        String comma = ",";
        String closeBracket = ")";
        String openBracket = "(";
        String newLine = "\n";
        
        strY = strY + lineText;
        
        if(strY.contains(comma) && strY.contains(closeBracket))
        //if(strY.contains(comma) && strY.contains(closeBracket) && strY.contains(openBracket))
        {
            strYcopy = strY;
            strY = strY.substring(strY.indexOf(",") + 1);
            strY = strY.substring(0, strY.indexOf(")"));
       
            yAxis = tryParseInt(strY);
            rcvdDataDuration++;
            if(yAxis > 800)
            {
                yAxis = 800;
            }
            //window.txtLog.append(strY);
            strY = "";
            
            strYcopy = strYcopy.substring(strYcopy.indexOf("(") + 1);
            strYcopy = strYcopy.substring(0, strYcopy.indexOf(")"));
            writeLogFile(strYcopy);
            System.out.println(strYcopy);
            //Enable Tiem comparison using separate thread

            strYcopy = "";
            
            if(rcvdDataDuration % 2 == 0)
            {
                window.series.add(xAxis++, yAxis, true);
                window.seriesFiltered.add(xAxis++, mLpf.filter(yAxis), true);
            }
            else
            {
                window.series.add(xAxis++, yAxis, false);
                window.seriesFiltered.add(xAxis++, mLpf.filter(yAxis), true);
            }
            

            //window.series.
        }
        
    }
//    public void chartDataFeed() 
//    {
//        
//        String strX = null;
//        
//        String comma = ",";
//        String closeBracket = ")";
//        String openBracket = "(";
//        String newLine = "\n";
//        
//        strY = strY + logText;
//        
//        
//        if(strY.contains(comma) && strY.contains(closeBracket) && strY.contains(openBracket))
//        {
//            strYcopy = strY;
//            strY = strY.substring(strY.indexOf(",") + 1);
//            strY = strY.substring(0, strY.indexOf(")"));
//       
//            yAxis = tryParseInt(strY);
//            rcvdDataDuration++;
//            if(yAxis > 800)
//            {
//                yAxis = 800;
//            }
//            window.txtLog.append(strY);
//            strY = "";
//            
//            strYcopy = strYcopy.substring(strYcopy.indexOf("(") + 1);
//            strYcopy = strYcopy.substring(0, strYcopy.indexOf(")"));
//            writeLogFile(strYcopy);
//            System.out.println(strYcopy);
//            //Enable Tiem comparison using separate thread
//
//            strYcopy = "";
//            window.series.add(xAxis++, yAxis);
//
//            //window.series.
//        }
//        
//    }
    
    public static Integer tryParseInt(String text) {
        try {
                return Integer.parseInt(text);
        } catch (NumberFormatException e) {
                return null;
        }
    }

    public void writeLogFile(String line)
    {
        if(logWriter != null){
            line = line + "\n";
            logWriter.appendLogFile(line);
        }
    }
    
    public void newLogFile(String name)
    {
        
        logWriter = new LogWriter();
        logWriter.createCsvFile(name);
        
        
    }
    
    public void closeLogFile()
    {
        if(logWriter != null)
            logWriter.closeCsvFile();
        
    }
    //method that can be called to send data
    //pre: open serial port
    //post: data sent to the other device
    public void writeData(int leftThrottle, int rightThrottle)
    {
        try
        {
            output.write(leftThrottle);
            output.flush();
            //this is a delimiter for the data
            output.write(DASH_ASCII);
            output.flush();
            
            output.write(rightThrottle);
            output.flush();
            //will be read as a byte so it is a space key
            output.write(SPACE_ASCII);
            output.flush();
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }
}

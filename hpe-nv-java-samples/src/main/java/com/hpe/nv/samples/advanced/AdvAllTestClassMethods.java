/*************************************************************************
 (c) Copyright [2016] Hewlett Packard Enterprise Development LP

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*************************************************************************/

package com.hpe.nv.samples.advanced;

import com.hpe.nv.analysis.dtos.document.DocumentProperties;
import com.hpe.nv.analysis.dtos.document.ExportableResult;
import com.hpe.nv.analysis.dtos.document.ExportableSummary;
import com.hpe.nv.api.*;
import com.hpe.nv.samples.Spinner;
import com.hpe.nv.samples.SpinnerStatus;
import com.hpe.nv.statistics.dtos.MultiFlowStatistics;
import com.hpe.nv.statistics.dtos.MultiFlowStatisticsResponse;
import com.shunra.dto.emulation.EmulationResponse;
import com.shunra.dto.emulation.EmulationStopResponse;
import com.shunra.dto.emulation.PacketListInfo;
import com.shunra.dto.emulation.PacketListInfoResponse;
import com.shunra.dto.transactionmanager.ConnectResponse;
import com.shunra.dto.transactionmanager.TransactionEntity;
import com.shunra.dto.transactionmanager.TransactionResponse;
import org.apache.commons.cli.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
  This sample demonstrates all of the Test class APIs except for the real-time update API, which is demonstrated in AdvRealtimeUpdate.java.
  You can start the test in this sample using either the NTX or Custom modes.

  AdvAllTestClassMethods.java steps:
  1. Create and initialize the TestManager object.
  2. Set the active adapter.
  3. Start the NV test with the "3G Good" network scenario.
  4. Connect the NV test to the transaction manager.
  5. Start packet list capture.
  6. Get packet list information and print it to the console (displayed only if the --debug argument is set to true).
  7. Start the "Home Page" NV transaction.
  8. Build the Selenium WebDriver.
  9. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
  10. Get the NV test statistics.
  11. Print the Client-in statistics retrieved in the previous step to the console (displayed only if the --debug argument is set to true).
  12. Stop the "Home Page" NV transaction.
  13. Stop the packet list capture.
  14. Get packet list information and print it to the console (displayed only if the --debug argument is set to true).
  15. Disconnect from the transaction manager.
  16. Stop the NV test.
  17. Analyze the NV test and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
  18. Print the NV transaction's network time or the path of the .zip file, if the --zip-result-file-path argument is specified.
  19. Download the specified packet list.
  20. Download the .shunra file.
  21. Close and quit the Selenium WebDriver.
*/
public class AdvAllTestClassMethods {

    static TestManager testManager;
    static Test siteTest;
    static Transaction siteTransaction;
    static String browser, mode, siteUrl, xpath, proxySetting, activeAdapterIp, serverIp, username, password, ntxFilePath, zipResultFilePath, packetListId, packetListFilePath, shunraFilePath, packetListIdFromInfo;
    static WebDriver driver;
    static boolean ssl = false, debug = false, testRunning = false, transactionInProgress = false;
    static int serverPort;
    static String[] analysisPorts;
    static Thread spinner;

    public static void main(String[] args) throws Exception {
         try {
             // program arguments
             Options options = new Options();
             options.addOption( "i", "server-ip", true, "[mandatory] NV Test Manager IP" );
             options.addOption( "o", "server-port", true, "[mandatory] NV Test Manager port" );
             options.addOption( "u", "username", true, "[mandatory] NV username");
             options.addOption( "w", "password", true, "[mandatory] NV password");
             options.addOption( "e", "ssl", true, "[optional] Pass true to use SSL. Default: false");
             options.addOption( "y", "proxy", true, "[optional] Proxy server host:port");
             options.addOption( "t", "site-url", true, "[optional] Site under test URL. Default: Default: HPE Network Virtualization site URL. If you change this value, make sure to change the --xpath argument too");
             options.addOption( "x", "xpath", true, "[optional] Parameter for ExpectedConditions.visibilityOfElementLocated(By.xpath(...)) method. Use an xpath expression of some element in the site. Default: //div[@id='content']");
             options.addOption( "a", "active-adapter-ip", true, "[optional] Active adapter IP. Default: --server-ip argument");
             options.addOption( "m", "mode", true, "[mandatory] Test mode - ntx or custom");
             options.addOption( "n", "ntx-file-path", true, "[mandatory in ntx mode] File path of an .ntx/.ntxx file - used to start the test in ntx mode");
             options.addOption( "z", "zip-result-file-path", true, "[optional] File path to store the analysis results as a .zip file");
             options.addOption( "k", "analysis-ports", true, "[optional] A comma-separated list of ports for test analysis");
             options.addOption( "p", "packet-list-id", true, "[optional] A packet list ID used for capturing a specific packet list and downloading its corresponding .shunra file");
             options.addOption( "c", "packet-list-file-path", true, "[optional] .pcap file path - used to store all captured packet lists");
             options.addOption( "s", "shunra-file-path", true, "[optional] .shunra file path for download");
             options.addOption( "b", "browser", true, "[optional] The browser for which the Selenium WebDriver is built. Possible values: Chrome and Firefox. Default: Firefox");
             options.addOption( "d", "debug", true, "[optional] Pass true to view console debug messages during execution. Default: false");
             options.addOption( "h", "help", false, "[optional] Generates and prints help information");

             // parse and validate the command line arguments
             CommandLineParser parser = new DefaultParser();
             CommandLine line = parser.parse( options, args );

             if (line.hasOption("help")) {
                 // print help if help argument is passed
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp( "AdvAllTestClassMethods.java", options );
                 return;
             }

             if (line.hasOption("mode")) {
                 mode =  ((String)line.getOptionValue("mode")).toLowerCase();
             }
             else {
                 throw new Exception("Missing argument -m/--mode <mode>");
             }

             if (!mode.equals("custom") && !mode.equals("ntx")) {
                 throw new Exception("Mode not supported. Supported modes are: ntx or custom");
             }

             if (mode.equals("ntx")) {
                 if (line.hasOption("ntx-file-path")) {
                     ntxFilePath = line.getOptionValue("ntx-file-path");
                 }
                 else {
                     throw new Exception("Missing argument -n/--ntx-file-path <ntxFilePath>");
                 }
             }

             if (line.hasOption("server-ip")) {
                 serverIp =  line.getOptionValue("server-ip");
                 if (serverIp.equals("0.0.0.0")) {
                     throw new Exception("Please replace the server IP argument value (0.0.0.0) with your NV Test Manager IP");
                 }
             }
             else {
                 throw new Exception("Missing argument -i/--server-ip <serverIp>");
             }

             if (line.hasOption("server-port")) {
                 serverPort =  Integer.parseInt(line.getOptionValue("server-port"));
             }
             else {
                 throw new Exception("Missing argument -o/--server-port <serverPort>");
             }

             if (line.hasOption("username")) {
                 username =  line.getOptionValue("username");
             }
             else {
                 throw new Exception("Missing argument -u/--username <username>");
             }

             if (line.hasOption("password")) {
                 password =  line.getOptionValue("password");
             }
             else {
                 throw new Exception("Missing argument -w/--password <password>");
             }

             if (line.hasOption("ssl")) {
                 ssl =  Boolean.parseBoolean(line.getOptionValue("ssl"));
             }

             if (line.hasOption("zip-result-file-path")) {
                 zipResultFilePath =  line.getOptionValue("zip-result-file-path");
             }

             if (line.hasOption("packet-list-id")) {
                 packetListId =  line.getOptionValue("packet-list-id");
             }

             if (line.hasOption("packet-list-file-path")) {
                 packetListFilePath =  line.getOptionValue("packet-list-file-path");
             }

             if (line.hasOption("shunra-file-path")) {
                 shunraFilePath =  line.getOptionValue("shunra-file-path");
             }

             if (line.hasOption("site-url")) {
                 siteUrl =  line.getOptionValue("site-url");
             }
             else {
                 siteUrl = "http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html";
             }

             if (line.hasOption("xpath")) {
                 xpath =  line.getOptionValue("xpath");
             }
             else {
                 xpath = "//div[@id='content']";
             }

             if (line.hasOption("proxy")) {
                 proxySetting =  line.getOptionValue("proxy");
             }

             if (line.hasOption("active-adapter-ip")) {
                 activeAdapterIp =  line.getOptionValue("active-adapter-ip");
             }
             else {
                 activeAdapterIp = serverIp;
             }

             if (line.hasOption("analysis-ports")) {
                 String analysisPortsStr =  line.getOptionValue("analysis-ports");
                 analysisPorts = analysisPortsStr.split(",");
             }
             else {
                 analysisPorts = new String[]{"80", "8080"};
             }

             if (line.hasOption("browser")) {
                 browser = line.getOptionValue("browser");
             }
             else {
                 browser = "Firefox";
             }

             if (line.hasOption("debug")) {
                 debug = Boolean.parseBoolean(line.getOptionValue("debug"));
             }

             String newLine = System.getProperty("line.separator");
             String testDescription = "***   This sample demonstrates all of the Test class APIs except for the                      ***" + newLine +
                                      "***   real-time update API, which is demonstrated in AdvRealtimeUpdate.java.                  ***" + newLine +
                                      "***   You can start the test in this sample using either the NTX or Custom modes.             ***" + newLine +
                                      "***                                                                                           ***" + newLine +
                                      "***   You can view the actual steps of this sample in the AdvAllTestClassMethods.java file.   ***" + newLine;

             // print the sample's description
             System.out.println(testDescription);

             // start console spinner
             if (!debug) {
                 spinner = new Thread(new Spinner());
                 spinner.start();
             }

            // sample execution steps
            /*****    Part 1 - Initialize the TestManager object to pass logon credentials, the NV Test Manager IP, the port, and so on                                                      *****/
            printPartDescription("\b------    Part 1 - Initialize the TestManager object to pass logon credentials, the NV Test Manager IP, the port, and so on");
            initTestManager();
            printPartSeparator();
            /*****    Part 2 - Set the active adapter and start the NV test                                                                                                                  *****/
            printPartDescription("------    Part 2 - Set the active adapter and start the NV test");
            setActiveAdapter();
            startTest();
            testRunning = true;
            connectToTransactionManager();
            printPartSeparator();
            /*****    Part 3 - Start packet list capture, get the packet list information and print it to the console (if the --debug argument is set to true)                               *****/
            printPartDescription("------    Part 3 - Start packet list capture, get the packet list information and print it to the console (if the --debug argument is set to true)");
            startPacketListCapture();
            getPacketListInfo();
            printPartSeparator();
            /*****    Part 4 - Start the NV transaction and navigate to the site                                                                                                             *****/
            printPartDescription("------    Part 4 - Start the NV transaction and navigate to the site");
            startTransaction();
            transactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            printPartSeparator();
            /*****    Part 5 - Get the NV test statistics and print the Client-in statistics to the console (if the --debug argument is set to true)                                         *****/
            printPartDescription("------    Part 5 - Get the NV test statistics and print the Client-in statistics to the console (if the --debug argument is set to true)");
            getTestStatistics();
            printPartSeparator();
            /*****    Part 6 - Stop the NV transaction and the packet list capture, get the packet list information and print it to the console (if the --debug argument is set to true)     *****/
            printPartDescription("------    Part 6 - Stop the NV transaction and the packet list capture, get the packet list information and print it to the console (if the --debug argument is set to true)");
            stopTransaction();
            transactionInProgress = false;
            stopPacketListCapture();
            getPacketListInfo();
            printPartSeparator();
            /*****    Part 7 - Disconnect from the transaction manager and stop the NV test                                                                                                  *****/
            printPartDescription("------    Part 7 - Disconnect from the transaction manager and stop the NV test");
            disconnectFromTransactionManager();
            stopTest();
            testRunning = false;
            printPartSeparator();
            /*****    Part 8 - Analyze the NV test and print the results to the console                                                                                                      *****/
            printPartDescription("------    Part 8 - Analyze the NV test and print the results to the console");
            analyzeTest();
            printPartSeparator();
            /*****    Part 9 - Download the specified packet list and the .shunra file                                                                                                      *****/
            printPartDescription("------    Part 9 - Download the specified packet list and the .shunra file");
            downloadPacketList();
            downloadShunra();
            printPartSeparator();
            doneCallback();
         } catch(Exception e) {
             try {
                 handleError(e.getMessage());
             } catch(Exception e2) {
                 System.out.println("Error occurred: " + e2.getMessage());
             }
         }
    }

    // log debug messages
    public static void logDebugMessage(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    // handle errors and stop currently running tests and transactions
    public static void handleError(String errorMessage) throws Exception {
        if (!debug && spinner != null) {
            // stop spinner
            spinner.interrupt();
            // wait for the spinner thread to terminate
            spinner.join();
            System.out.print("\b");
        }

        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }

        if (transactionInProgress) {
            stopTransaction();
            transactionInProgress = false;
            if (testRunning) {
                stopTest();
                testRunning = false;
            }
        }

        if (!transactionInProgress && testRunning) {
            stopTest();
            testRunning = false;
        }

        System.out.println("Error occurred: " + errorMessage);
    }

    // print done message to console and stop console spinner
    public static void doneCallback() throws InterruptedException {
        if (!debug && spinner != null) {
            // stop spinner
            spinner.interrupt();
            // wait for the spinner thread to terminate
            spinner.join();
            System.out.print("\b");
        }

        System.out.println("Sample execution ended successfully");
        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    // Create and initialize the TestManager object
    public static void initTestManager() throws Exception {
        // create a TestManager object
        logDebugMessage("Creating and initializing the TestManager object");
        testManager = new TestManager(serverIp, serverPort, username, password);
        testManager.setUseSSL(ssl);
        // call the init method
        testManager.init();
    }

    // set the active adapter (the active adapter determines the NIC through which impairments are applied to the traffic and packets are captured)
    public static void setActiveAdapter()  throws Exception {
        logDebugMessage("Setting the active adapter");
        testManager.setActiveAdapter(activeAdapterIp);
    }

    // start an NV test with the "3G Good" network scenario
    public static void startTest() throws Exception {
        // create an NV test
        logDebugMessage("Creating the NV test object");
        Test.Mode testMode = null;
        if (mode.equals("custom")) {
            testMode = Test.Mode.CUSTOM;
        }
        if (mode.equals("ntx")) {
            testMode = Test.Mode.NTX;
        }

        String testName =  "AdvAllTestClassMethods Sample Test";
        String networkScenario = "3G Good";
        siteTest = new Test(testManager, testName, networkScenario);
        siteTest.setTestMode(testMode);

        if (mode.equals("custom")) {
            // add a flow to the test
            logDebugMessage("Adding a flow to the NV test");
            Flow flow = new Flow("Flow1", 80, 0, 2000, 512);
            siteTest.addFlow(flow);
        }
        if (mode.equals("ntx")) {
            siteTest.setNtxxFilePath(ntxFilePath);
        }

        // start the test
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the NV test in \"" + mode + "\" mode with the \"3G Good\" network scenario");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        EmulationResponse emulationResponse = siteTest.start();
        logDebugMessage("New test started. Test token: \"" + emulationResponse.getTestToken() + "\"");
    }

    // connect the NV test to the transaction manager
    public static void connectToTransactionManager() throws Exception {
        logDebugMessage("Connecting the NV test to the transaction manager");
        ConnectResponse connectResult = siteTest.connectToTransactionManager();
        logDebugMessage("Connected to the transaction manager with session ID: \"" + connectResult.getTransactionManagerSessionIdentifier() + "\"");
    }

    // start capturing the specified packet list, or all of the packet lists in the NV test if no packet list ID is specified
    public static void startPacketListCapture() throws Exception {
        if (packetListId != null) {
            // start capturing the packet list with the specified packet list ID
            logDebugMessage("Starting to capture the following packet list: \"" + packetListId + "\"");
            siteTest.startPacketListCapture(packetListId);
        }
        else {
            // start capturing all of the packet lists in the NV test
            logDebugMessage("Starting to capture all packet lists in the NV test");
            siteTest.startPacketListCapture();
        }
    }

    // get packet lists information
    public static void getPacketListInfo() throws Exception {
        PacketListInfoResponse packetListInfo;
        if (packetListId != null) {
            // get packet list information for the specified packet list
            logDebugMessage("Getting information for the following packet list: \"" + packetListId + "\"");
            packetListInfo = siteTest.getPacketListInfo(packetListId);
        }
        else {
            // get all packet lists information
            logDebugMessage("Getting information for all packet lists in the NV test");
            packetListInfo = siteTest.getPacketListInfo();
        }
        printPacketListInfo(packetListInfo);
    }

    // print the packet list information to the console
    public static void printPacketListInfo(PacketListInfoResponse packetListInfo) {
        ArrayList<PacketListInfo> packetListsInfoArray = packetListInfo.getPacketListsInfo();

        PacketListInfo packetListInfoObject;
        for (int i = 0; i < packetListsInfoArray.size(); i++) {
            packetListInfoObject = packetListsInfoArray.get(i);
            if (i == 0) {
                logDebugMessage("Packet list information:");
                packetListIdFromInfo = packetListInfoObject.getPlId();
            }
            logDebugMessage("---- Flow ID: '" +  packetListInfoObject.getFlowId() + "', " +
                    "Packet List ID: '" + packetListInfoObject.getPlId() + "', " +
                    "Capture Status: '" + packetListInfoObject.getCaptureStatus() + "'");
        }
    }

    // start the "Home Page" NV transaction
    public static void startTransaction() throws Exception {
        // create an NV transaction
        logDebugMessage("Creating the \"Home Page\" transaction");
        String transactionName = "Home Page";
        siteTransaction = new Transaction(transactionName);

        // add the NV transaction to the NV test
        logDebugMessage("Adding the \"Home Page\" transaction to the NV test");
        TransactionEntity transactionEntity = siteTransaction.addToTest(siteTest);
        logDebugMessage("New transaction added with ID \"" + transactionEntity.getId() + "\"");

        // start the NV transaction
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the \"Home Page\" transaction");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        TransactionResponse startTransactionResult = siteTransaction.start();
        logDebugMessage("Started transaction named \"" + siteTransaction.getName() + "\" with ID \"" + startTransactionResult.getTransactionIdentifier() + "\"");
    }

    // build the Selenium WebDriver
    public static void buildSeleniumWebDriver() {
        logDebugMessage("Building the Selenium WebDriver for " + browser);

        if (proxySetting != null) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxySetting).setFtpProxy(proxySetting).setSslProxy(proxySetting).setSocksProxy(proxySetting);
            DesiredCapabilities cap = new DesiredCapabilities();
            cap.setCapability(CapabilityType.PROXY, proxy);

            if (browser.equalsIgnoreCase("Firefox")) {
                driver = new FirefoxDriver(cap);
            }
            else {
                driver = new ChromeDriver(cap);
            }
        }
        else {
            if (browser.equalsIgnoreCase("Firefox")) {
                driver = new FirefoxDriver();
            }
            else {
                driver = new ChromeDriver();
            }
        }
    }

    // navigate to the specified site and wait for the specified element to display
    public static void seleniumNavigateToPage() throws InterruptedException {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Navigating to the specified site using the Selenium WebDriver");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        // navigate to the site
        driver.get(siteUrl);
        driver.manage().timeouts().pageLoadTimeout(2000000, TimeUnit.MILLISECONDS);

        // wait for the specified element to display
        WebDriverWait wait = new WebDriverWait(driver, 60*2);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    // get the NV test statistics
    public static void getTestStatistics() throws Exception {
        logDebugMessage("Getting the NV test statistics");
        List<String> flows = new ArrayList<String>();
        flows.add("Flow1");
        MultiFlowStatisticsResponse statsResponse = siteTest.getFlowStatistics(flows);
        printClientInStats(statsResponse);
    }

    // print the Client-in statistics retrieved in the previous step to the console
    public static void printClientInStats(MultiFlowStatisticsResponse statsResponse) {

        MultiFlowStatistics.List statsArray = statsResponse.getStatistics();

        if (statsArray.size() == 0) {
            logDebugMessage("No statistics available. Try again later.");
        }
        else {
            logDebugMessage("Client-in statistics:");
            MultiFlowStatistics statsInTimestamp;
            ArrayList<MultiFlowStatistics.FlowStats> flowStatsArray;
            MultiFlowStatistics.FlowStats flowStatsObject;
            MultiFlowStatistics.NICStats clientDownStatsObject;
            for (int i = 0; i < statsArray.size(); i++) {
                statsInTimestamp = statsArray.get(i);
                logDebugMessage("---- Statistics collected for the following timestamp: " + statsInTimestamp.getTimeStamp() + " ----");
                flowStatsArray = statsInTimestamp.getFlowStats();

                for (int j = 0; j < flowStatsArray.size(); j++) {
                    flowStatsObject = flowStatsArray.get(j);
                    clientDownStatsObject = flowStatsObject.getClientDownStats();
                    logDebugMessage("-------- Flow \"" + flowStatsObject.getId() + "\" client-in statistics: throughput - " + clientDownStatsObject.getBps() +
                                    ", bandwidth utilization - " + clientDownStatsObject.getBwUtil() + ", total throughput - " + clientDownStatsObject.getTotal());
                }
            }
            logDebugMessage("Last statistics timestamp: " + siteTest.getLastStatsTimestamp());
        }
    }

    // stop the "Home Page" NV transaction
    public static void stopTransaction() throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Stopping the \"Home Page\" transaction");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        TransactionResponse stopTransactionResult = siteTransaction.stop();
        logDebugMessage("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
    }

    // disconnect from the transaction manager
    public static void disconnectFromTransactionManager() throws Exception {
        logDebugMessage("Disconnecting from the transaction manager");
        siteTest.disconnectFromTransactionManager();
    }

    // stop capturing packet lists
    public static void stopPacketListCapture() throws Exception {
        if (packetListId != null) {
            // stop capturing the packet list with the specified packet list ID
            logDebugMessage("Stopping packet list capture for the following packet list: \"" + packetListId + "\"");
            siteTest.stopPacketListCapture(packetListId);
        }
        else {
            // stop capturing all packet lists of the NV test
            logDebugMessage("Stopping packet list capture for all of the packet lists in the NV test");
            siteTest.stopPacketListCapture();
        }
    }

    // stop the NV test
    public static void stopTest() throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Stopping the NV test");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        EmulationStopResponse stopTestResult = siteTest.stop();
        logDebugMessage("Test with token \"" + siteTest.getTestToken() + "\" stopped. Path to .shunra file: " + stopTestResult.getAnalysisResourcesLocation().get(siteTest.getTestToken()));
    }

    // analyze the NV test
    public static void analyzeTest() throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Analyzing the NV test");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }

        Object analyzeResult;
        if (zipResultFilePath != null) {
            analyzeResult =  siteTest.analyze(analysisPorts, zipResultFilePath);
            printZipLocation((File)analyzeResult);
        }
        else {
            analyzeResult =  siteTest.analyze(analysisPorts);
            printNetworkTime((ExportableResult)analyzeResult);
        }
    }

    // print the path of the .zip file, if the --zip-result-file-path argument is specified
    public static void printZipLocation(File analyzeResult) throws InterruptedException {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Analysis result .zip file path: " + analyzeResult.getAbsolutePath());
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
    }

    // print the transaction's network time
    public static void printNetworkTime(ExportableResult analyzeResult) throws InterruptedException {
        ArrayList<ExportableSummary> transactionSummaries = analyzeResult.getTransactionSummaries();

        ExportableSummary firstTransactionSummary = transactionSummaries.get(0);
        DocumentProperties firstTransactionProperties = firstTransactionSummary.getProperties();

        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("\"Home Page\" transaction network time: " + (firstTransactionProperties.getNetworkTime()/1000)  + "s");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
    }

    // download the specified packet list
    public static void downloadPacketList() throws Exception {
        String plId;
        if (packetListId != null) {
            plId =  packetListId;
        }
        else {
            plId = packetListIdFromInfo;
        }

        logDebugMessage("Downloading the following packet list: \"" +  plId + "\"");

        File downloadPacketListResult =  siteTest.downloadPacketList(plId, false, packetListFilePath);
        logDebugMessage("Downloaded packet list file absolute path: " + ((File) downloadPacketListResult).getAbsolutePath());
    }

    // download a .shunra file that contains the specified packet list or all of the captured packet lists, if no packet list ID is specified
    public static void downloadShunra() throws Exception {
        if (packetListId != null) {
            logDebugMessage("Downloading a .shunra file for the following packet list: '" +  packetListId + "'");
        }
        else {
            logDebugMessage("Downloading a .shunra file for all of the packet lists captured during the test");
        }

        File downloadShunraResult =  siteTest.downloadShunra(packetListId, shunraFilePath);
        logDebugMessage("Downloaded .shunra file absolute path: " + ((File) downloadShunraResult).getAbsolutePath());
    }

    // print a description of the next part of the sample
    public static void printPartDescription(String description) throws InterruptedException {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println(description);
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
     }

    // print a newline to separate between parts
    public static void printPartSeparator() throws InterruptedException {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }

        System.out.println(" ");

        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
    }
}

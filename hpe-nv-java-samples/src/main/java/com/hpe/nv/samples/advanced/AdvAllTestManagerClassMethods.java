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
import com.shunra.dto.configuration.ActiveAdapter;
import com.shunra.dto.configuration.ConfigurationSettings;
import com.shunra.dto.emulation.*;
import com.shunra.dto.emulation.multiengine.MultiUserEmulationTokensResponse;
import com.shunra.dto.emulation.multiengine.UserResource;
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
   This sample demonstrates all of the TestManager class APIs. These APIs let you:
   * initialize the TestManager object to pass logon credentials, the NV Test Manager IP, the port, and so on
   * set/get the NV configuration and active adapter
   * get the running tests tokens
   * start/stop packet list capture
   * get packet list information
   * stop a specified array of tests or all of the running tests
   * analyze a .shunra file, which is a compressed file that includes an events file, metadata, and packet lists

   AdvAllTestManagerClassMethods.java steps:
   1. Create and initialize the TestManager object.
   2. Set the active adapter.
   3. Get the active adapter and print its properties to the console (displayed only if the --debug argument is set to true).
   4. Set the NV configuration.
   5. Get the NV configuration and print its properties to the console (displayed only if the --debug argument is set to true).
   6. Start the first NV test with "Flow1" - view the sample's code to see the flow's properties.
   7. Connect the first NV test to the transaction manager.
   8. Start the second NV test with "Flow2" - view the sample's code to see the flow's properties.
   9. Connect the second NV test to the transaction manager.
   10. Get the running tests tokens and print them to the console (displayed only if the --debug argument is set to true).
   11. Start the "Home Page" NV transaction in the first test.
   12. Start the "Home Page" NV transaction in the second test.
   13. Start capturing all packet lists in all running tests.
   14. Build the Selenium WebDriver.
   15. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
   16. Get packet list information and print it to the console (displayed only if the --debug argument is set to true).
   17. Stop capturing all packet lists in all running tests.
   18. Stop the "Home Page" NV transaction in the first test.
   19. Stop the "Home Page" NV transaction in the second test.
   20. Stop the first NV test using the "stopTests" TestManager module API.
   21. Stop all tests using the "stopAllTests" TestManager module API.
   22. Analyze the specified .shunra file and get the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
   23. Print the network times of the transactions in the .shunra file, or the path of the .zip file, if the --zip-result-file-path argument is specified.
   24. Close and quit the Selenium WebDriver.
   */

 public class AdvAllTestManagerClassMethods {
     static TestManager testManager;
     static Test siteTest1, siteTest2;
     static Transaction siteTransaction1, siteTransaction2;
     static String browser, siteUrl, xpath, proxySetting, activeAdapterIp, serverIp, username, password, zipResultFilePath, shunraFilePath;
     static WebDriver driver;
     static boolean ssl = false, debug = false;
     static int serverPort, firstTestFlowTcpPort, secondTestFlowTcpPort;
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
              options.addOption( "t", "site-url", true, "[optional] Site under test URL. Default: HPE Network Virtualization site URL. If you change this value, make sure to change the --xpath argument too");
              options.addOption( "x", "xpath", true, "[optional] Parameter for ExpectedConditions.visibilityOfElementLocated(By.xpath(...)) method. Use an xpath expression of some element in the site. Default: //div[@id='content']");
              options.addOption( "a", "active-adapter-ip", true, "[optional] Active adapter IP. Default: --server-ip argument");
              options.addOption( "s", "shunra-file-path", true, "[optional] File path for the .shunra file to analyze");
              options.addOption( "z", "zip-result-file-path", true, "[optional] File path to store the analysis results as a .zip file");
              options.addOption( "k", "analysis-ports", true, "[optional] A comma-separated list of ports for test analysis");
              options.addOption( "f", "first-test-flow-tcp-port", true, "[optional] TCP port for the flow of the first test");
              options.addOption( "g", "second-test-flow-tcp-port", true, "[optional] TCP port for the flow of the second test");
              options.addOption( "b", "browser", true, "[optional] The browser for which the Selenium WebDriver is built. Possible values: Chrome and Firefox. Default: Firefox");
              options.addOption( "d", "debug", true, "[optional] Pass true to view console debug messages during execution. Default: false");
              options.addOption( "h", "help", false, "[optional] Generates and prints help information");

              // parse and validate the command line arguments
              CommandLineParser parser = new DefaultParser();
              CommandLine line = parser.parse( options, args );

              if (line.hasOption("help")) {
                  // print help if help argument is passed
                  HelpFormatter formatter = new HelpFormatter();
                  formatter.printHelp( "AdvAllTestManagerClassMethods.java", options );
                  return;
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

              if (line.hasOption("zip-result-file-path")) {
                  zipResultFilePath =  line.getOptionValue("zip-result-file-path");
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

              if (line.hasOption("shunra-file-path")) {
                  shunraFilePath =  line.getOptionValue("shunra-file-path");
              }

              if( line.hasOption( "firstTestFlowTcpPort" ) ) {
                  firstTestFlowTcpPort = Integer.parseInt(line.getOptionValue("firstTestFlowTcpPort"));
              }
              else {
                  firstTestFlowTcpPort = 8080;
              }

              if( line.hasOption( "secondTestFlowTcpPort" ) ) {
                  secondTestFlowTcpPort = Integer.parseInt(line.getOptionValue("secondTestFlowTcpPort"));
              }
              else {
                  secondTestFlowTcpPort = 80;
              }

              if (line.hasOption("browser")) {
                  browser = line.getOptionValue("browser");
              }
              else {
                  browser = "Firefox";
              }

              if( line.hasOption( "debug" ) ) {
                  debug = Boolean.parseBoolean(line.getOptionValue("debug"));
              }

              String newLine = System.getProperty("line.separator");
              String testDescription = "***   This sample demonstrates all of the TestManager class APIs. These APIs let you:                                 ***" + newLine +
                                       "***   * initialize the TestManager object to pass logon credentials, the NV Test Manager IP, the port, and so on      ***" + newLine +
                                       "***   * set/get the NV configuration and active adapter                                                               ***" + newLine +
                                       "***   * get the running tests tokens                                                                                  ***" + newLine +
                                       "***   * start/stop packet list capture                                                                                ***" + newLine +
                                       "***   * get packet list information                                                                                   ***" + newLine +
                                       "***   * stop a specified array of tests or all of the running tests                                                   ***" + newLine +
                                       "***   * analyze a .shunra file, which is a compressed file that includes an events file, metadata, and packet lists   ***" + newLine +
                                       "***                                                                                                                   ***" + newLine +
                                       "***   You can view the actual steps of this sample in the AdvAllTestManagerClassMethods.java file.                    ***" + newLine;

              // print the sample's description
              System.out.println(testDescription);

              // start console spinner
              if (!debug) {
                  spinner = new Thread(new Spinner());
                  spinner.start();
              }

             // sample execution steps
             /*****    Part 1 - Initialize the TestManager object to pass logon credentials, the NV Test Manager IP, the port, and so on            *****/
             printPartDescription("\b------    Part 1 - Initialize the TestManager object to pass logon credentials, the NV Test Manager IP, the port, and so on");
             initTestManager();
             printPartSeparator();
             /*****    Part 2 - Set/get the NV configuration and active adapter                                                                     *****/
             printPartDescription("------    Part 2 - Set/get the NV configuration and active adapter");
             setActiveAdapter();
             getActiveAdapter();
             setConfiguration();
             getConfiguration();
             printPartSeparator();
             /*****    Part 3 - Start tests and get the NV tests' tokens                                                                             *****/
             printPartDescription("------    Part 3 - Start tests and get the NV tests' tokens");
             startTest1();
             connectTest1ToTransactionManager();
             startTest2();
             connectTest2ToTransactionManager();
             getTestTokens();
             printPartSeparator();
             /*****    Part 4 - Start NV transactions, navigate to the site and start capturing the packet lists                                     *****/
             printPartDescription("------    Part 4 - Start NV transactions, navigate to the site and start capturing the packet lists");
             startTransaction1();
             startTransaction2();
             startPacketListCapture();
             buildSeleniumWebDriver();
             seleniumNavigateToPage();
             printPartSeparator();
             /*****    Part 5 - Get the packet list information and print it to the console (if the --debug argument is set to true)                 *****/
             printPartDescription("------    Part 5 - Get the packet list information and print it to the console (if the --debug argument is set to true)");
             getPacketListInfo();
             printPartSeparator();
             /*****    Part 6 - Stop capturing packet lists and stop the NV transactions                                                             *****/
             printPartDescription("------    Part 6 - Stop capturing packet lists and stop the NV transactions");
             stopPacketListCapture();
             stopTransaction1();
             siteTransaction1 = null;
             stopTransaction2();
             siteTransaction2 = null;
             printPartSeparator();
             /*****    Part 7 - Stop the first NV test using the "stopTests" method and then stop the second test using the "stopAllTests" method    *****/
             printPartDescription("------    Part 7 - Stop the first NV test using the \"stopTests\" method and then stop the second test using the \"stopAllTests\" method");
             stopTest1();
             siteTest1 = null;
             driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
             stopAllTests();
             siteTest2 = null;
             printPartSeparator();
             /*****    Part 8 - Analyze the specified .shunra file and print the results to the console                                              *****/
             printPartDescription("------    Part 8 - Analyze the specified .shunra file and print the results to the console");
             analyzeShunra();
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

         if (siteTransaction2 != null) {
             stopTransaction2();
             siteTransaction2 = null;
             if (siteTransaction1 != null) {
                 stopTransaction1();
                 siteTransaction1 = null;
                     if (siteTest1 != null || siteTest2 != null) {
                         testManager.stopAllTests();
                         siteTest1 = null;
                         siteTest2 = null;
                     }
             }
         }

         if (siteTransaction2 == null && (siteTest1 != null || siteTest2 != null)) {
             testManager.stopAllTests();
             siteTest1 = null;
             siteTest2 = null;
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
     public static void setActiveAdapter() throws Exception {
         logDebugMessage("Setting the active adapter");
         testManager.setActiveAdapter(activeAdapterIp);
     }

     // get the active adapter
     public static void getActiveAdapter() throws Exception {
         logDebugMessage("Getting the active adapter");
         ActiveAdapter activeAdapter = testManager.getActiveAdapter();
         printActiveAdapter(activeAdapter);
     }

     // print the active adapter properties to the console
     public static void printActiveAdapter(ActiveAdapter activeAdapter) {
         String ip =  activeAdapter.getIp();
         boolean reverseDirection =  activeAdapter.getReverseDirection();
         logDebugMessage("Active adapter IP: " + ip + ", Active adapter reverseDirection Boolean value: " + reverseDirection);
     }

     // set the NV configuration
     public static void setConfiguration() throws Exception {
         logDebugMessage("Setting the NV configuration");

         boolean isPacketListCaptureCyclic = false;
         int packetListMaxSizeMB = 350;
         double packetListServerClientRatio = 10;

         testManager.setConfiguration(isPacketListCaptureCyclic, packetListMaxSizeMB, packetListServerClientRatio);
     }

     // get the NV configuration
     public static void getConfiguration() throws Exception {
         logDebugMessage("Getting the NV configuration");
         ConfigurationSettings configurationSettings = testManager.getConfiguration();
         printConfiguration(configurationSettings);
     }

     // print the NV configuration to the console
     public static void printConfiguration(ConfigurationSettings configurationSettings) {
         logDebugMessage("Configuration - isPacketListCaptureCyclic:" + configurationSettings.getIsPacketListCaptureCyclic() + ", packetListMaxSizeMB: " + configurationSettings.getPacketListMaxSizeMB() +
                 ", packetListServerClientRatio: " + configurationSettings.getPacketListServerClientRatio());

     }

     // start the first NV test with the "3G Good" network scenario
     public static void startTest1() throws Exception {
         // create an NV test
         logDebugMessage("Creating the NV test object");
         String testName =  "AdvAllTestManagerClassMethods Sample Test 1";
         String networkScenario = "3G Good";
         siteTest1 = new Test(testManager, testName, networkScenario);
         siteTest1.setTestMode(Test.Mode.CUSTOM);

         // add a flow to the test
         logDebugMessage("Adding a flow to the NV test");
         Flow flow = new Flow("Flow1", 80, 0, 2000, 512);
         flow.setSrcIp(activeAdapterIp);

         IPRange ipRange = new IPRange(null, null, firstTestFlowTcpPort, IPRange.Protocol.TCP.getId());
         flow.includeDestIPRange(ipRange);

         ipRange = new IPRange(activeAdapterIp, activeAdapterIp, 0, IPRange.Protocol.ALL.getId());
         flow.excludeDestIPRange(ipRange);
         siteTest1.addFlow(flow);

         // start the test
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Starting the \"" + siteTest1.getTestMetadata().getTestName() + "\" test with \"Flow1\" flow");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         EmulationResponse emulationResponse = siteTest1.start();
         logDebugMessage("New test started. Test token: \"" + emulationResponse.getTestToken() + "\"");
     }

     // clone the first NV test to create the second NV test and
     // start the second NV test with the "3G Good" network scenario
     public static void startTest2() throws Exception {
         // create an NV test
         logDebugMessage("Creating the NV test object using the clone method");
         String testName =  "AdvAllTestManagerClassMethods Sample Test 2";
         String networkScenario = "3G Good";
         siteTest2 = new Test(testManager, testName, networkScenario);
         siteTest2.setTestMode(Test.Mode.CUSTOM);

         // add a flow to the test
         logDebugMessage("Adding a flow to the NV test");
         Flow flow = new Flow("Flow2", 80, 0, 2000, 512);
         flow.setSrcIp(activeAdapterIp);

         IPRange ipRange = new IPRange(null, null, secondTestFlowTcpPort, IPRange.Protocol.TCP.getId());
         flow.includeDestIPRange(ipRange);

         ipRange = new IPRange(activeAdapterIp, activeAdapterIp, 0, IPRange.Protocol.ALL.getId());
         flow.excludeDestIPRange(ipRange);
         siteTest2.addFlow(flow);

         // start the test
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Starting the \"" + siteTest2.getTestMetadata().getTestName() + "\" test with \"Flow2\" flow");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         EmulationResponse emulationResponse = siteTest2.start();
         logDebugMessage("New test started. Test token: \"" + emulationResponse.getTestToken() + "\"");
     }

     // get the tokens of the running tests
     public static void getTestTokens() throws Exception {
         logDebugMessage("Getting the tokens of the running tests");
         MultiUserEmulationTokensResponse emulationTokens = testManager.getTestTokens(false);
         printTestTokens(emulationTokens);
     }

     // print the tokens of the running tests to the console
     public static void printTestTokens(MultiUserEmulationTokensResponse emulationTokens) {
         logDebugMessage("Test tokens:");
         for (UserResource<EmulationTokens> test: emulationTokens.getTests()) {
            for (String emulationEngine: test.getEmulationEngine().keySet()) {
                logDebugMessage(test.getEmulationEngine().get(emulationEngine).getTestTokens().toString());
            }
         }
     }

     // connect the first NV test to the transaction manager
     public static void connectTest1ToTransactionManager() throws Exception {
         logDebugMessage("Connecting the \"" +  siteTest1.getTestMetadata().getTestName() + "\" test to the transaction manager");
         ConnectResponse connectResult = siteTest1.connectToTransactionManager();
         logDebugMessage("Test \"" + siteTest1.getTestMetadata().getTestName() + "\" is now connected to the transaction manager with session ID: \"" + connectResult.getTransactionManagerSessionIdentifier() + "\"");
     }

     // connect the second NV test to the transaction manager
     public static void connectTest2ToTransactionManager() throws Exception {
         logDebugMessage("Connecting the \"" +  siteTest2.getTestMetadata().getTestName() + "\" test to the transaction manager");
         ConnectResponse connectResult = siteTest2.connectToTransactionManager();
         logDebugMessage("Test \"" + siteTest2.getTestMetadata().getTestName() + "\" is now connected to the transaction manager with session ID: \"" + connectResult.getTransactionManagerSessionIdentifier() + "\"");
     }

     // start the "Home Page" NV transaction in the first test
     public static void startTransaction1() throws Exception {
         // create an NV transaction
         logDebugMessage("Creating the \"Home Page\" transaction for the \"" + siteTest1.getTestMetadata().getTestName() + "\" test");
         String transactionName = "Home Page";
         siteTransaction1 = new Transaction(transactionName);

         // add the NV transaction to the NV test
         logDebugMessage("Adding the \"Home Page\" transaction to the \"" +  siteTest1.getTestMetadata().getTestName() + "\" test");
         TransactionEntity transactionEntity = siteTransaction1.addToTest(siteTest1);
         logDebugMessage("New transaction added with ID \"" + transactionEntity.getId() + "\"");

         // start the NV transaction
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Starting the \"Home Page\" transaction in the \"" + siteTest1.getTestMetadata().getTestName() + "\" test");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         TransactionResponse transactionResponse = siteTransaction1.start();
         logDebugMessage("Started transaction named \"" + siteTransaction1.getName() + "\" with ID \"" + transactionResponse.getTransactionIdentifier() + "\"");
     }

     // start the "Home Page" NV transaction in the second test
     public static void startTransaction2() throws Exception {
         // create an NV transaction
         logDebugMessage("Creating the \"Home Page\" transaction for the \"" + siteTest2.getTestMetadata().getTestName() + "\" test");
         String transactionName = "Home Page";
         siteTransaction2 = new Transaction(transactionName);

         // add the NV transaction to the NV test
         logDebugMessage("Adding the \"Home Page\" transaction to the \"" +  siteTest2.getTestMetadata().getTestName() + "\" test");
         TransactionEntity transactionEntity = siteTransaction2.addToTest(siteTest2);
         logDebugMessage("New transaction added with ID \"" + transactionEntity.getId() + "\"");

         // start the NV transaction
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Starting the \"Home Page\" transaction in the \"" + siteTest2.getTestMetadata().getTestName() + "\" test");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         TransactionResponse transactionResponse = siteTransaction2.start();
         logDebugMessage("Started transaction named \"" + siteTransaction2.getName() + "\" with ID \"" + transactionResponse.getTransactionIdentifier() + "\"");
     }

    // start capturing all packet lists in all running tests
     public static void startPacketListCapture() throws Exception {
         logDebugMessage("Starting the packet list capture in all running tests");
         testManager.startPacketListCapture();
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

     // stop capturing all packet lists in all running tests
     public static void stopPacketListCapture() throws Exception {
         logDebugMessage("Stopping the packet list capture in all running tests");
         testManager.stopPacketListCapture();
     }

     // get the packet list information
     public static void getPacketListInfo() throws Exception {
         logDebugMessage("Getting the packet list information for all packet lists in all of the tests");
         PacketListInfoResponse packetListInfo = testManager.getPacketListInfo();
         printPacketListInfo(packetListInfo);
     }

     // print the packet list information to the console
     public static void printPacketListInfo(PacketListInfoResponse packetListInfo) throws Exception {
         ArrayList<PacketListInfo> packetListsInfoArray = packetListInfo.getPacketListsInfo();

         if (packetListsInfoArray.size() == 0) {
             logDebugMessage("No packet list information is available.");
         }

         PacketListInfo packetListsInfoObject = null;
         for (int i = 0; i < packetListsInfoArray.size(); i++) {
             if (i == 0) {
                 logDebugMessage("Packet list information:");
             }
             packetListsInfoObject = packetListsInfoArray.get(i);
             logDebugMessage("---- Flow ID: \"" +  packetListsInfoObject.getFlowId() + "\", " +
                     "Packet List ID: \"" + packetListsInfoObject.getPlId() + "\", " +
                     "Capture Status: \"" + packetListsInfoObject.getCaptureStatus() + "\"");
         }
     }

    // stop the "Home Page" NV transaction in the first NV test
     public static void stopTransaction1() throws Exception {
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Stopping the \"Home Page\" transaction of the \"" +  siteTest1.getTestMetadata().getTestName() + "\" test");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         TransactionResponse stopTransactionResult = siteTransaction1.stop();
         logDebugMessage("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
     }

     // stop the "Home Page" NV transaction in the second NV test
     public static void stopTransaction2() throws Exception {
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Stopping the \"Home Page\" transaction of the \"" +  siteTest2.getTestMetadata().getTestName() + "\" test");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         TransactionResponse stopTransactionResult = siteTransaction2.stop();
         logDebugMessage("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
     }

     // stop the NV test
     public static void stopTest1() throws Exception {
         logDebugMessage("Stopping tests: [" + siteTest1.getTestMetadata().getTestName() + "]");
         List<Test> tests = new ArrayList<Test>();
         tests.add(siteTest1);
         EmulationStopResponse stopTestResult = testManager.stopTests(tests);
         logDebugMessage("Test with token \"" + siteTest1.getTestToken() + "\" stopped. Path to .shunra file: " + stopTestResult.getAnalysisResourcesLocation().get(siteTest1.getTestToken()));
     }

    // stop all running tests
     public static void stopAllTests() throws Exception {
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Stop all running tests");
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
         EmulationStopResponse stopTestResult = testManager.stopAllTests();
         logDebugMessage("All tests stopped successfully. Paths to .shunra files: ");

         for (String testToken: stopTestResult.getAnalysisResourcesLocation().keySet()) {
             logDebugMessage("-- Test token: " + testToken);
             logDebugMessage("-- File path: " + stopTestResult.getAnalysisResourcesLocation().get(testToken));
         }
     }

    // analyze the specified .shunra file
     public static void analyzeShunra() throws Exception {
         if (shunraFilePath != null) {
             if (!debug) {
                 SpinnerStatus.getInstance().pauseThread();
                 System.out.print("\b");
             }
             System.out.println("Analyzing the specified .shunra file");
             if (!debug) {
                 SpinnerStatus.getInstance().resumeThread();
             }

             Object analyzeShunraResult;
             if (zipResultFilePath != null) {
                 analyzeShunraResult = testManager.analyzeShunraFile(analysisPorts, zipResultFilePath, shunraFilePath);
                 printZipLocation((File)analyzeShunraResult);
             }
             else {
                 analyzeShunraResult = testManager.analyzeShunraFile(analysisPorts, shunraFilePath);
                 printNetworkTime((ExportableResult)analyzeShunraResult);
             }
         }
     }

     // print the path of the .zip file, if the --zip-result-file-path argument is specified
     public static void printZipLocation(File analyzeShunraResult) throws InterruptedException {
         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }
         System.out.println("Analysis result .zip file path: " + analyzeShunraResult.getAbsolutePath());
         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
     }

    // print the network times of the transactions in the .shunra file
     public static void printNetworkTime(ExportableResult analyzeResult) throws InterruptedException {
         ArrayList<ExportableSummary> transactionSummaries = analyzeResult.getTransactionSummaries();

         if (!debug) {
             SpinnerStatus.getInstance().pauseThread();
             System.out.print("\b");
         }

         System.out.println("\n");
         System.out.println("Network times for all transactions in the specified .shunra file in seconds:");

         ExportableSummary transactionSummary;
         DocumentProperties transactionProperties;
         for (int i = 0; i < transactionSummaries.size(); i++) {
             transactionSummary = transactionSummaries.get(i);
             transactionProperties = transactionSummary.getProperties();
             System.out.println("--- \"" + transactionProperties.getTransactionName() + "\" transaction network time: " + (transactionProperties.getNetworkTime()/1000) + "s");
         }

         if (!debug) {
             SpinnerStatus.getInstance().resumeThread();
         }
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

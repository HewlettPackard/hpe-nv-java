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

package com.hpe.nv.samples.basic;

import com.google.gson.*;
import com.hpe.nv.analysis.dtos.document.DocumentProperties;
import com.hpe.nv.analysis.dtos.document.ExportableResult;
import com.hpe.nv.analysis.dtos.document.ExportableSummary;
import com.hpe.nv.api.*;
import com.hpe.nv.samples.Spinner;
import com.hpe.nv.samples.SpinnerStatus;
import com.shunra.dto.emulation.EmulationResponse;
import com.shunra.dto.emulation.EmulationStopResponse;
import com.shunra.dto.emulation.multiengine.RealTimeUpdateResponse;
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
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

 /*
    This sample demonstrates a comparison between two network scenarios - "WiFi" and "3G Busy".

    In this sample, the NV test starts with the "WiFi" network scenario, running three transactions (see below).
    Then, the sample updates the NV test's network scenario to "3G Busy" using the real-time update API and runs the same
    transactions again.

    After the sample analyzes the NV test and extracts the transaction times from the analysis results, it prints a
    summary to the console. The summary displays the comparative network times for each transaction in both
    network scenarios.

    This sample runs three identical NV transactions before and after the real-time update:
    1. "Home Page" transaction: Navigates to the home page in the HPE Network Virtualization website
    2. "Get Started" transaction: Navigates to the Get Started Now page in the HPE Network Virtualization website
    3. "Overview" transaction: Navigates back to the home page in the HPE Network Virtualization website

    BasicAnalyze2Scenarios.java steps:
    1. Create a TestManager object and initialize it.
    2. Set the active adapter.
    3. Start the NV test with the "WiFi" network scenario.
    4. Connect the NV test to the transaction manager.
    5. Start the "Home Page" NV transaction.
    6. Build the Selenium WebDriver.
    7. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
    8. Stop the "Home Page" NV transaction.
    9. Start the "Get Started" NV transaction.
    10. Click the "Get Started Now" button using the Selenium WebDriver.
    11. Stop the "Get Started" NV transaction.
    12. Start the "Overview" NV transaction.
    13. Click the "Overview" button using the Selenium WebDriver.
    14. Stop the "Overview" NV transaction.
    15. Close and quit the Selenium WebDriver.
    16. Update the NV test in real time - update the network scenario to "3G Busy".
    17. Rerun the transactions (repeat steps 5-11).
    18. Stop the NV test.
    19. Analyze the NV test and extract the network times for the NV transactions.
    20. Print the network time comparison summary to the console.
 */

public class BasicAnalyze2Scenarios {
    static TestManager testManager;
    static Test siteTest;
    static Transaction site1Transaction, site2Transaction, site3Transaction;
    static String browser, proxySetting, activeAdapterIp, serverIp, username, password, zipResultFilePath;
    static WebDriver driver;
    static boolean ssl = false, debug = false, testRunning = false, transactionInProgress = false;
    static int serverPort;
    static String[] analysisPorts;
    static int transactionInProgressIndex = -1;
    static Thread spinner;

    public static void main(String[] args) {
        try {
             // program arguments
             Options options = new Options();
             options.addOption( "i", "server-ip", true, "[mandatory] NV Test Manager IP" );
             options.addOption( "o", "server-port", true, "[mandatory] NV Test Manager port" );
             options.addOption( "u", "username", true, "[mandatory] NV username");
             options.addOption( "w", "password", true, "[mandatory] NV password");
             options.addOption( "e", "ssl", true, "[optional] Pass true to use SSL. Default: false");
             options.addOption( "y", "proxy", true, "[optional] Proxy server host:port");
             options.addOption( "a", "active-adapter-ip", true, "[optional] Active adapter IP. Default: --server-ip argument");
             options.addOption( "z", "zip-result-file-path", true, "[optional] File path to store the analysis results as a .zip file");
             options.addOption( "k", "analysis-ports", true, "[optional] A comma-separated list of ports for test analysis");
             options.addOption( "b", "browser", true, "[optional] The browser for which the Selenium WebDriver is built. Possible values: Chrome and Firefox. Default: Firefox");
             options.addOption( "d", "debug", true, "[optional] Pass true to view console debug messages during execution. Default: false");
             options.addOption( "h", "help", false, "[optional] Generates and prints help information");

             // parse and validate the command line arguments
             CommandLineParser parser = new DefaultParser();
             CommandLine line = parser.parse( options, args );

             if (line.hasOption("help")) {
                 // print help if help argument is passed
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp( "BasicAnalyze2Scenarios.java", options );
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
             String testDescription = "***   This sample demonstrates a comparison between two network scenarios - \"WiFi\" and \"3G Busy\".                                 ***" + newLine +
                                      "***                                                                                                                               ***" + newLine +
                                      "***   In this sample, the NV test starts with the \"WiFi\" network scenario, running three transactions (see below).                ***" + newLine +
                                      "***   Then, the sample updates the NV test's network scenario to \"3G Busy\" using the real-time update API and runs the same       ***" + newLine +
                                      "***   transactions again.                                                                                                         ***" + newLine +
                                      "***                                                                                                                               ***" + newLine +
                                      "***   After the sample analyzes the NV test and extracts the transaction times from the analysis results, it prints a             ***" + newLine +
                                      "***   summary to the console. The summary displays the comparative network times for each transaction in both                     ***" + newLine +
                                      "***   network scenarios.                                                                                                          ***" + newLine +
                                      "***                                                                                                                               ***" + newLine +
                                      "***   This sample runs three identical NV transactions before and after the real-time update:                                     ***" + newLine +
                                      "***   1. \"Home Page\" transaction: Navigates to the home page in the HPE Network Virtualization website                            ***" + newLine +
                                      "***   2. \"Get Started\" transaction: Navigates to the Get Started Now page in the HPE Network Virtualization website               ***" + newLine +
                                      "***   3. \"Overview\" transaction: Navigates back to the home page in the HPE Network Virtualization website                        ***" + newLine +
                                      "***                                                                                                                               ***" + newLine +
                                      "***   You can view the actual steps of this sample in the BasicAnalyze2Scenarios.java file.                                       ***" + newLine;

            // print the sample's description
            System.out.println(testDescription);

            // start console spinner
            if (!debug) {
                spinner = new Thread(new Spinner());
                spinner.start();
            }

            // sample execution steps
            /*****    Part 1 - Start the NV test with the "WiFi" network scenario                                                                      *****/
            printPartDescription("\b------    Part 1 - Start the NV test with the \"WiFi\" network scenario");
            initTestManager();
            setActiveAdapter();
            startTest();
            testRunning = true;
            printPartSeparator();
            /*****    Part 2 - Run three transactions - "Home Page", "Get Started" and "Overview"                                                      *****/
            printPartDescription("------    Part 2 - Run three transactions - \"Home Page\", \"Get Started\" and \"Overview\"");
            connectToTransactionManager();
            startTransaction(1);
            transactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            stopTransaction(1);
            transactionInProgress = false;
            startTransaction(2);
            transactionInProgress = true;
            seleniumGetStartedClick();
            stopTransaction(2);
            transactionInProgress = false;
            startTransaction(3);
            transactionInProgress = true;
            seleniumOverviewClick();
            stopTransaction(3);
            transactionInProgress = false;
            driverCloseAndQuit();
            printPartSeparator();
            /*****    Part 3 - Update the NV test in real time to the "3G Busy" network scenario                                                     *****/
            printPartDescription("------    Part 3 - Update the NV test in real time to the \"3G Busy\" network scenario");
            realTimeUpdateTest();
            printPartSeparator();
            /*****    Part 4 - Rerun the transactions                                                                                                *****/
            printPartDescription("------    Part 4 - Rerun the transactions");
            startTransactionAfterRTU(1);
            transactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            stopTransaction(1);
            transactionInProgress = false;
            startTransactionAfterRTU(2);
            transactionInProgress = true;
            seleniumGetStartedClick();
            stopTransaction(2);
            transactionInProgress = false;
            startTransactionAfterRTU(3);
            transactionInProgress = true;
            seleniumOverviewClick();
            stopTransaction(3);
            transactionInProgress = false;
            driverCloseAndQuit();
            printPartSeparator();
            /*****    Part 5 - Stop the NV test, analyze it and print the results to the console                                                                *****/
            printPartDescription("------    Part 5 - Stop the NV test, analyze it and print the results to the console");
            stopTest();
            testRunning = false;
            analyzeTestZip();
            analyzeTestJson();
            printPartSeparator();
            doneCallback();
        } catch(Exception e) {
            try {
                handleError(e.getMessage());
            }catch(Exception e2) {
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

        if (transactionInProgress && transactionInProgressIndex != -1) {
            stopTransaction(transactionInProgressIndex);
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
    }

    // close and quit the Selenium WebDriver
    public static void driverCloseAndQuit() {
        logDebugMessage("Closing and quitting the Selenium WebDriver");
        driver.close();
        driver.quit();
        driver = null;
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

    // start an NV test with the "WiFi" network scenario
    public static void startTest() throws Exception {
        // create an NV test
        logDebugMessage("Creating the NV test object");
        String testName =  "BasicAnalyze2Scenarios Sample Test";
        String networkScenario = "WiFi";
        siteTest = new Test(testManager, testName, networkScenario);
        siteTest.setTestMode(Test.Mode.CUSTOM);

        // add a flow to the test
        logDebugMessage("Adding a flow to the NV test");
        Flow flow = new Flow("Flow1", 0, 0, 0, 0);
        siteTest.addFlow(flow);

        // start the test
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the NV test with the \"WiFi\" network scenario");
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

    // start an NV transaction according to parameter i:
    // i = 1: start the "Home Page" transaction
    // i = 2: start the "Get Started" transaction
    // i = 3: start the "Overview" transaction
    public static void startTransaction(int i) throws Exception {
        String transactionName = (i == 1 ? "Home Page" : (i == 2 ? "Get Started": "Overview"));

        // create an NV transaction
        logDebugMessage("Creating the \"" + transactionName + "\" transaction");
        Transaction pageTransaction = new Transaction(transactionName);

        // add the NV transaction to the NV test
        logDebugMessage("Adding the \"" + transactionName+ "\" transaction to the NV test");
        TransactionEntity transactionEntity = pageTransaction.addToTest(siteTest);
        logDebugMessage("New transaction added with ID \"" + transactionEntity.getId() + "\"");

        // start the NV transaction
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the \"" + transactionName + "\" transaction");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        TransactionResponse startTransactionResult = pageTransaction.start();
        logDebugMessage("Started transaction named \"" + pageTransaction.getName() + "\" with ID \"" + startTransactionResult.getTransactionIdentifier() + "\"");

        if (i == 1) {
            site1Transaction = pageTransaction;
        }
        else if (i == 2) {
            site2Transaction = pageTransaction;
        }
        else {
            site3Transaction = pageTransaction;
        }

        transactionInProgressIndex = i;
    }

    // rerun the NV transaction after real-time update according to parameter i:
    // i = 1: rerun the "Home Page" transaction
    // i = 2: rerun the "Get Started" transaction
    // i = 3: rerun the "Overview" transaction
    public static void startTransactionAfterRTU(int i) throws Exception {
        // start NV transaction again
        Transaction transaction = (i == 1 ? site1Transaction: (i == 2 ? site2Transaction: site3Transaction));
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the \"" + transaction.getName() + "\" transaction after real-time update");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        TransactionResponse startTransactionResult = transaction.start();
        logDebugMessage("Started transaction named \"" + transaction.getName() + "\" with ID \"" + startTransactionResult.getTransactionIdentifier() + "\"");
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
        System.out.println("Navigating to the NV site using the Selenium WebDriver");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        // navigate to the site
        driver.get("http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html");
        driver.manage().timeouts().pageLoadTimeout(2000000, TimeUnit.MILLISECONDS);

        // wait for the element to display
        WebDriverWait wait = new WebDriverWait(driver, 60*2);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//nav//span[contains(text(), 'Get Started')]")));
    }

    // navigate to the "Get Started Now" page using the Selenium WebDriver
    public static void seleniumGetStartedClick() throws InterruptedException {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Navigating to the \"Get Started Now\" page using the Selenium WebDriver");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        driver.findElement(By.xpath("//nav//span[contains(text(), 'Get Started')]")).click();

        // wait for the element to display
        WebDriverWait wait = new WebDriverWait(driver, 60*2);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Network Virtualization Downloads')]")));
    }

    // navigate to the "Overview" page using the Selenium WebDriver
    public static void seleniumOverviewClick() throws InterruptedException {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Navigating to the \"Overview\" page using the Selenium WebDriver");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        driver.findElement(By.xpath("//nav//span[contains(text(), 'Overview')]")).click();

        // wait for the element to display
        WebDriverWait wait = new WebDriverWait(driver, 60*2);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Key Capabilities')]")));
    }

    // stop an NV transaction according to parameter i:
    // i = 1: stop the "Home Page" transaction
    // i = 2: stop the "Get Started" transaction
    // i = 3: stop the "Overview" transaction
    public static void stopTransaction(int i) throws Exception {
        Transaction transaction = (i == 1 ? site1Transaction: (i == 2 ? site2Transaction: site3Transaction));
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Stopping the \"" + transaction.getName()  + "\" transaction");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        TransactionResponse stopTransactionResult = transaction.stop();
        logDebugMessage("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
    }

    // update the test in real time to the "3G Busy" network scenario
    public static void realTimeUpdateTest() throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Updating the test in real time to the \"3G Busy\" network scenario");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        List<Flow> flows = new ArrayList<Flow>();
        Flow flow = new Flow("Flow1", 200, 0.5, 384, 128);
        flows.add(flow);

        RealTimeUpdateResponse realTimeUpdateResult = siteTest.realTimeUpdate("3G Busy", null, flows);
        logDebugMessage("Test \"" + realTimeUpdateResult.getEmulationTest().getMetadata().getTestName() + "\" updated successfully.");
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

    // analyze the NV test and retrieve the result as an object
    public static void analyzeTestJson() throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Analyzing the NV test and getting the result as an object");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        ExportableResult analyzeResult = (ExportableResult) siteTest.analyze(analysisPorts);

        printNetworkTime(analyzeResult);
    }

    // analyze the NV test and get the result as a .zip file
    public static void analyzeTestZip() throws Exception {
        if (zipResultFilePath != null) {
            if (!debug) {
                SpinnerStatus.getInstance().pauseThread();
                System.out.print("\b");
            }
            System.out.println("Analyzing the NV test and getting the result as a .zip file");
            if (!debug) {
                SpinnerStatus.getInstance().resumeThread();
            }
            File analyzeResult = (File) siteTest.analyze(analysisPorts, zipResultFilePath);
            printZipLocation(analyzeResult);
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

    // print the network times of the transaction runs
    public static void printNetworkTime(ExportableResult analyzeResult) throws InterruptedException {
        ArrayList<ExportableSummary> transactionSummaries = analyzeResult.getTransactionSummaries();

        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b ");
        }

        System.out.println("");
        System.out.println("Network times for transactions using the \"WiFi\" network scenario in seconds:");

        ExportableSummary transactionSummary;
        DocumentProperties transactionProperties;
        for (int i = 0; i < 3; i++) {
            transactionSummary = transactionSummaries.get(i);
            transactionProperties = transactionSummary.getProperties();
            System.out.println("--- \"" + transactionProperties.getTransactionName() + "\" transaction network time: " + (transactionProperties.getNetworkTime()/1000) + "s");
        }

        System.out.println("Network times for transactions using the \"3G Busy\" network scenario in seconds:");
        for (int i = 3; i < 6; i++) {
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

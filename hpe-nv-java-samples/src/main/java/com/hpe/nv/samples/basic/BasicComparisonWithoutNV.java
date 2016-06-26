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

import com.hpe.nv.analysis.dtos.document.DocumentProperties;
import com.hpe.nv.analysis.dtos.document.ExportableResult;
import com.hpe.nv.analysis.dtos.document.ExportableSummary;
import com.hpe.nv.api.*;
import com.hpe.nv.samples.Spinner;
import com.hpe.nv.samples.SpinnerStatus;
import com.shunra.dto.emulation.EmulationResponse;
import com.shunra.dto.emulation.EmulationStopResponse;
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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
 This sample demonstrates how NV helps you test your application under various network conditions.

 This test starts by navigating to the home page in the HPE Network Virtualization website using the Selenium WebDriver.
 This initial step runs without NV emulation and provides a basis for comparison.

 Next, the sample starts an NV test configured with a "3G Busy" network scenario.
 The same step runs as before - navigating to the home page in the HPE Network Virtualization website - but this time,
 it does so over an emulated "3G Busy" network as part of an NV transaction.

 When the sample finishes running, it prints a summary to the console. This summary displays a comparison of the time
 it took to navigate to the site both with and without NV's network emulation. The results show that the slow "3G Busy"
 network increases the time it takes to navigate to the site, as you would expect.

 BasicComparisonWithoutNV.java steps:
 1. Build the Selenium WebDriver.
 2. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
 3. Close and quit the Selenium WebDriver.
 4. Create and initialize the TestManager object.
 5. Set the active adapter.
 6. Start the NV test with the "3G Busy" network scenario.
 7. Connect to the transaction manager.
 8. Start the "Home Page" NV transaction.
 9. Rebuild the Selenium WebDriver.
 10. Navigate to the site again.
 11. Stop the "Home Page" NV transaction.
 12. Close and quit the Selenium WebDriver.
 13. Stop the NV test.
 14. Analyze the NV test and extract the network time for the NV transaction.
 15. Print the network time comparison summary to the console.
 */
public class BasicComparisonWithoutNV {

    static TestManager testManager;
    static Test siteTest;
    static Transaction pageTransaction;
    static String browser, siteUrl, xpath, proxySetting, activeAdapterIp, serverIp, username, password;
    static WebDriver driver;
    static boolean ssl = false, debug = false, testRunning = false, transactionInProgress = false;
    static int serverPort;
    static String[] analysisPorts;
    static long startNoNV, stopNoNV;
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
            options.addOption( "a", "active-adapter-ip", true, "[optional] Active adapter IP. Default: --server-ip argument");
            options.addOption( "t", "site-url", true, "[optional] Site under test URL. Default: HPE Network Virtualization site URL. If you change this value, make sure to change the --xpath argument too");
            options.addOption( "x", "xpath", true, "[optional] Parameter for ExpectedConditions.visibilityOfElementLocated(By.xpath(...)) method. Use an xpath expression of some element in the site. Default: //div[@id='content']");
            options.addOption( "k", "analysis-ports", true, "[optional] A comma-separated list of ports for test analysis");
            options.addOption( "b", "browser", true, "[optional] The browser for which the Selenium WebDriver is built. Possible values: Chrome, Firefox. Default: Firefox");
            options.addOption( "d", "debug", true, "[optional] Pass true to view console debug messages during execution. Default: false");
            options.addOption( "h", "help", false, "[optional] Generates and prints help information");

            // parse and validate the command line arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse( options, args );

            if (line.hasOption("help")) {
                // print help if help argument is passed
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "BasicComparisonWithoutNV.java", options );
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

            if (line.hasOption( "debug")) {
                debug = Boolean.parseBoolean(line.getOptionValue("debug"));
            }

            String newLine = System.getProperty("line.separator");
            String testDescription = "***    This sample demonstrates how NV helps you test your application under various network conditions.                          ***" + newLine +
                                     "***    This test starts by navigating to the home page in the HPE Network Virtualization website using the Selenium WebDriver.    ***" + newLine +
                                     "***    This initial step runs without NV emulation and provides a basis for comparison.                                           ***" + newLine +
                                     "***                                                                                                                               ***" + newLine +
                                     "***    Next, the sample starts an NV test configured with a \"3G Busy\" network scenario.                                           ***" + newLine +
                                     "***    The same step runs as before - navigating to the home page in the HPE Network Virtualization website - but this time,      ***" + newLine +
                                     "***    it does so over an emulated \"3G Busy\" network as part of an NV transaction.                                                ***" + newLine +
                                     "***                                                                                                                               ***" + newLine +
                                     "***    When the sample finishes running, it prints a summary to the console. This summary displays a comparison of the time       ***" + newLine +
                                     "***    it took to navigate to the site both with and without NV's network emulation. The results show that the slow \"3G Busy\"     ***" + newLine +
                                     "***    network increases the time it takes to navigate to the site, as you would expect.                                          ***" + newLine +
                                     "***                                                                                                                               ***" + newLine +
                                     "***    You can view the actual steps of this sample in the BasicComparisonWithoutNV.java file.                                    ***" + newLine;

            // print the sample's description
            System.out.println(testDescription);

            // start console spinner
            if (!debug) {
                spinner = new Thread(new Spinner());
                spinner.start();
            }

            // sample execution steps
            /*****    Part 1 - Navigate to the site without using NV emulation                       *****/
            printPartDescription("\b------    Part 1 - Navigate to the site without using NV emulation");
            buildSeleniumWebDriver();
            startNoNV = System.currentTimeMillis();
            seleniumNavigateToPage();
            stopNoNV = System.currentTimeMillis();
            driverCloseAndQuit();
            printPartSeparator();
            /*****    Part 2 - Navigate to the site using NV "3G Busy" network scenario emulation    *****/
            printPartDescription("------    Part 2 - Navigate to the site using NV \"3G Busy\" network scenario emulation");
            initTestManager();
            setActiveAdapter();
            startBusyTest();
            testRunning = true;
            connectToTransactionManager();
            startTransaction();
            transactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            stopTransaction();
            transactionInProgress = false;
            driverCloseAndQuit();
            stopTest();
            testRunning = false;
            printPartSeparator();
            /*****    Part 3 - Analyze the NV test and print the results to the console              *****/
            printPartDescription("------    Part 3 - Analyze the NV test and print the results to the console");
            analyzeTestJson();
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

    // start an NV test with the "3G Busy" network scenario
    public static void startBusyTest() throws Exception {
        // create an NV test
        logDebugMessage("Creating the NV test object");
        String testName =  "BasicComparisonWithoutNV Sample Test";
        String networkScenario = "3G Busy";
        siteTest = new Test(testManager, testName, networkScenario);
        siteTest.setTestMode(Test.Mode.CUSTOM);

        // add a flow to the test
        logDebugMessage("Adding a flow to the NV test");
        Flow flow = new Flow("Flow1", 200, 0.5, 384, 128);
        siteTest.addFlow(flow);

        // start the test
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the NV test");
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

    // start the "Home Page" NV transaction
    public static void startTransaction() throws Exception {
        // create an NV transaction
        logDebugMessage("Creating the \"Home Page\" transaction");
        String transactionName = "Home Page";
        pageTransaction = new Transaction(transactionName);

        // add the NV transaction to the NV test
        logDebugMessage("Adding the \"Home Page\" transaction to the NV test");
        TransactionEntity transactionEntity = pageTransaction.addToTest(siteTest);
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
        TransactionResponse startTransactionResult = pageTransaction.start();
        logDebugMessage("Started transaction named \"" + pageTransaction.getName() + "\" with ID \"" + startTransactionResult.getTransactionIdentifier() + "\"");
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
        TransactionResponse stopTransactionResult = pageTransaction.stop();
        logDebugMessage("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
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
        printTimes(analyzeResult);
    }

    // print the site navigation time summary
    public static void printTimes(ExportableResult analyzeResult) throws InterruptedException {
        ArrayList<ExportableSummary> transactionSummaries = analyzeResult.getTransactionSummaries();

        ExportableSummary firstTransactionSummary = transactionSummaries.get(0);
        DocumentProperties firstTransactionProperties = firstTransactionSummary.getProperties();

        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b ");
        }

        System.out.println("");
        System.out.println("Site navigation time in seconds:");

        double timeWithoutNV = (stopNoNV - startNoNV)/1000;
        double timeWithNV = (firstTransactionProperties.getNetworkTime()/1000);

        System.out.println("--- Time to navigate to the site without NV emulation: " + timeWithoutNV + "s");
        System.out.println("--- Time to navigate to the site with the NV \"3G Busy\" network scenario: " + timeWithNV + "s");
        System.out.println("--------- (Running this transaction with network emulation increased the time by: " + (timeWithNV - timeWithoutNV) + "s)");

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

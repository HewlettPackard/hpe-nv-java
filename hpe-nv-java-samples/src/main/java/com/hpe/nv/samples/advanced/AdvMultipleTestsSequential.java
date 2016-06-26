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

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
 This sample shows how to run several tests sequentially with different network scenarios.

 AdvMultipleTestsSequential.java steps:
 1. Create and initialize the TestManager object.
 2. Set the active adapter.
 3. Start the first NV test with the "3G Busy" network scenario.
 4. Connect the first NV test to the transaction manager.
 5. Start the "Home Page" NV transaction in the first NV test.
 6. Build the Selenium WebDriver.
 7. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
 8. Stop the "Home Page" NV transaction in the first NV test.
 9. Close and quit the Selenium WebDriver.
 10. Stop the first NV test.
 11. Analyze the first NV test and get the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
 12. Print the NV transaction's network time or the location of the .zip file for the first test, if the --zip-result-file-path argument is specified.
 13. Start the second NV test with the "3G Good" network scenario.
 14. Connect the second NV test to the transaction manager.
 15. Run the same transaction in the second test (repeat steps 5-9).
 16. Stop the second NV test.
 17. Analyze the second NV test and get the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
 18. Print the NV transaction's network time or the location of the .zip file for the second test, if the --zip-result-file-path argument is specified.
 */

public class AdvMultipleTestsSequential {
    static TestManager testManager;
    static Test siteTest;
    static Transaction pageTransaction;
    static String browser, siteUrl, xpath, proxySetting, activeAdapterIp, serverIp, username, password, firstZipResultFilePath, secondZipResultFilePath;
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
            options.addOption( "t", "site-url", true, "[optional] Site under test URL. Default: HPE Network Virtualization site URL. If you change this value, make sure to change the --xpath argument too");
            options.addOption( "x", "xpath", true, "[optional] Parameter for ExpectedConditions.visibilityOfElementLocated(By.xpath(...)) method. Use an xpath expression of some element in the site. Default: //div[@id='content']");
            options.addOption( "a", "active-adapter-ip", true, "[optional] Active adapter IP. Default: --server-ip argument");
            options.addOption( "f", "first-zip-result-file-path", true, "[optional] File path to store the first test analysis results as a .zip file");
            options.addOption( "s", "second-zip-result-file-path", true, "[optional] File path to store the second test analysis results as a .zip file");
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
                formatter.printHelp( "AdvMultipleTestsSequential.java", options );
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

            if (line.hasOption("first-zip-result-file-path")) {
                firstZipResultFilePath =  line.getOptionValue("first-zip-result-file-path");
            }

            if (line.hasOption("second-zip-result-file-path")) {
                secondZipResultFilePath =  line.getOptionValue("second-zip-result-file-path");
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

            if( line.hasOption( "debug" ) ) {
                debug = Boolean.parseBoolean(line.getOptionValue("debug"));
            }

            String newLine = System.getProperty("line.separator");
            String testDescription = "***   This sample shows how to run several tests sequentially with different network scenarios.       ***" + newLine +
                                     "***                                                                                                   ***" + newLine +
                                     "***   You can view the actual steps of this sample in the AdvMultipleTestsSequential.java file.       ***" + newLine;

            // print the sample's description
            System.out.println(testDescription);

            // start console spinner
            if (!debug) {
                spinner = new Thread(new Spinner());
                spinner.start();
            }

            // sample execution steps
            /*****    Part 1 - Initialize the TestManager object and set the active adapter                                                 *****/
            printPartDescription("\b------    Part 1 - Initialize the TestManager object and set the active adapter");
            initTestManager();
            setActiveAdapter();
            printPartSeparator();
            /*****    Part 2 - Start the first NV test with the "3G Busy" network scenario and run the "Home Page" transaction              *****/
            printPartDescription("------    Part 2 - Start the first NV test with the \"3G Busy\" network scenario and run the \"Home Page\" transaction");
            startTest("3G Busy");
            testRunning = true;
            connectToTransactionManager();
            startTransaction();
            transactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            stopTransaction();
            transactionInProgress = false;
            driverCloseAndQuit();
            printPartSeparator();
            /*****    Part 3 - Stop the first NV test, analyze it and print the results to the console                                      *****/
            printPartDescription("------    Part 3 - Stop the first NV test, analyze it and print the results to the console");
            stopTest();
            testRunning = false;
            analyzeTest();
            printPartSeparator();
            /*****    Part 4 - Start the second NV test with the "3G Good" network scenario and run the "Home Page" transaction                                                                                                                 *****/
            printPartDescription("------    Part 4 - Start the second NV test with the \"3G Good\" network scenario and run the \"Home Page\" transaction");
            startTest("3G Good");
            testRunning = true;
            connectToTransactionManager();
            startTransaction();
            transactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            stopTransaction();
            transactionInProgress = false;
            driverCloseAndQuit();
            printPartSeparator();
            /*****    Part 5 - Stop the second NV test, analyze it and print the results to the console                                      *****/
            printPartDescription("------    Part 3 - Stop the second NV test, analyze it and print the results to the console");
            stopTest();
            testRunning = false;
            analyzeTest();
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
    public static void  handleError(String errorMessage) throws Exception {
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
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Creating and initializing the TestManager object");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
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

    // start an NV test with the specified network scenario
    public static void startTest(String networkScenario) throws Exception {
        // create an NV test
        logDebugMessage("Creating the NV test object");

        String testName =  "AdvMultipleTestsSequential Sample Test - " + networkScenario;
        siteTest = new Test(testManager, testName, networkScenario);
        siteTest.setTestMode(Test.Mode.CUSTOM);

        // add a flow to the test
        logDebugMessage("Adding a flow to the NV test");
        Flow flow = null;
        if (networkScenario.equals("3G Busy")) {
            flow = new Flow("Flow1", 200, 0.5, 384, 128);
        }
        else {
            flow = new Flow("Flow1", 80, 0, 2000, 512);
        }
        siteTest.addFlow(flow);

        // start the test
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the NV test with the \"" + networkScenario + "\" network scenario");
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
        logDebugMessage("Adding the \"Home Page\" transaction to the following NV test: \"" + siteTest.getTestMetadata().getTestName() + "\"");
        TransactionEntity transactionEntity = pageTransaction.addToTest(siteTest);
        logDebugMessage("New transaction added with ID \"" + transactionEntity.getId() + "\"");

        // start the NV transaction
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the \"Home Page\" transaction in the following NV test: \"" + siteTest.getTestMetadata().getTestName() + "\"");
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
        String zipResultFilePath = null;
        if (siteTest.getTestMetadata().getNetworkScenario().equals("3G Busy") && firstZipResultFilePath != null) {
            zipResultFilePath = firstZipResultFilePath;
        }
        if (siteTest.getTestMetadata().getNetworkScenario().equals("3G Good") && secondZipResultFilePath != null) {
            zipResultFilePath = secondZipResultFilePath;
        }

        if (zipResultFilePath != null) {
            analyzeResult = siteTest.analyze(analysisPorts, zipResultFilePath);
            printZipLocation((File)analyzeResult);
        }
        else {
            analyzeResult = siteTest.analyze(analysisPorts);
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
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }

        ArrayList<ExportableSummary> transactionSummaries = analyzeResult.getTransactionSummaries();

        ExportableSummary firstTransactionSummary = transactionSummaries.get(0);
        DocumentProperties firstTransactionProperties = firstTransactionSummary.getProperties();
        System.out.println("\"Home Page\" transaction network time: " + (firstTransactionProperties.getNetworkTime()/1000) + "s");

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

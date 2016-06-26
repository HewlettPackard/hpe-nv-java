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
 This sample shows how to run several tests concurrently with different flow definitions.
 When running NV tests in parallel, make sure that:
 * each test is configured to use MULTI_USER mode
 * the include/exclude IP ranges in the tests' flows do not overlap - this ensures data separation between the tests
 * your NV Test Manager license supports multiple flows running in parallel

 AdvMultipleTestsConcurrent.java steps:
 1. Create and initialize the TestManager object.
 2. Set the active adapter.
 3. Start the first NV test with "Flow1" - view the sample's code to see the flow's properties.
 4. Connect the first NV test to the transaction manager.
 5. Start the second NV test with "Flow2" - view the sample's code to see the flow's properties.
 6. Connect the second NV test to the transaction manager.
 7. Start the "Home Page" NV transaction in the first test.
 8. Start the "Home Page" NV transaction in the second test.
 9. Build the Selenium WebDriver.
 10. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
 11. Stop the "Home Page" NV transaction in the first test.
 12. Stop the "Home Page" NV transaction in the second test.
 13. Stop the first NV test.
 14. Stop the second NV test.
 15. Analyze the first NV test and get the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
 16. Print the NV transaction's network time or the location of the .zip file for the first test, if the --zip-result-file-path argument is specified.
 17. Analyze the second NV test and get the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
 18. Print the NV transaction's network time or the location of the .zip file for the second test, if the --zip-result-file-path argument is specified.
 19. Close and quit the Selenium WebDriver.
 */
public class AdvMultipleTestsConcurrent {

    static TestManager testManager;
    static Test flow1Test, flow2Test;
    static Transaction flow1TestTransaction, flow2TestTransaction;
    static String browser, siteUrl, xpath, proxySetting, activeAdapterIp, serverIp, username, password, firstZipResultFilePath, secondZipResultFilePath;
    static WebDriver driver;
    static boolean testRunning = false, flow1TransactionInProgress = false, flow2TransactionInProgress = false, ssl = false, debug = false;
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
            options.addOption( "f", "first-zip-result-file-path", true, "[optional] [optional] File path to store the first test analysis results as a .zip file");
            options.addOption( "s", "second-zip-result-file-path", true, "[optional] File path to store the second test analysis results as a .zip file");
            options.addOption( "c", "first-test-flow-tcp-port", true, "[optional] TCP port to define in the flow of the first test");
            options.addOption( "p", "second-test-flow-tcp-port", true, "[optional] TCP port to define in the flow of the second test");
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
                formatter.printHelp( "AdvMultipleTestsConcurrent.java", options );
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

            if (line.hasOption("firstTestFlowTcpPort")) {
                firstTestFlowTcpPort = Integer.parseInt(line.getOptionValue("firstTestFlowTcpPort"));
            }
            else {
                firstTestFlowTcpPort = 8080;
            }

            if (line.hasOption("secondTestFlowTcpPort")) {
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

            if (line.hasOption("debug")) {
                debug = Boolean.parseBoolean(line.getOptionValue("debug"));
            }

            String newLine = System.getProperty("line.separator");
            String testDescription = "***   This sample shows how to run several tests concurrently with different flow definitions.                              ***" + newLine +
                                     "***   When running NV tests in parallel, make sure that:                                                                    ***" + newLine +
                                     "***   * each test is configured to use multi-user mode                                                                      ***" + newLine +
                                     "***   * the include/exclude IP ranges in the tests' flows do not overlap - this ensures data separation between the tests   ***" + newLine +
                                     "***   * your NV Test Manager license supports multiple flows running in parallel                                            ***" + newLine +
                                     "***                                                                                                                         ***" + newLine +
                                     "***   You can view the actual steps of this sample in the AdvMultipleTestsConcurrent.java file.                             ***" + newLine;
            // print the sample's description
            System.out.println(testDescription);

            // start console spinner
            if (!debug) {
                spinner = new Thread(new Spinner());
                spinner.start();
            }

            // sample execution steps
            /*****    Part 1 - Initialize the TestManager object and set the active adapter                                     *****/
            printPartDescription("\b------    Part 1 - Initialize the TestManager object and set the active adapter");
            initTestManager();
            setActiveAdapter();
            printPartSeparator();
            /*****    Part 2 - Start the first NV test with the flow "Flow1"                                                    *****/
            printPartDescription("------    Part 2 - Start the first NV test with flow \"Flow1\"");
            startTest("Flow1");
            testRunning = true;
            connectToTransactionManager("Flow1");
            printPartSeparator();
            /*****    Part 3 - Start the second NV test with the flow "Flow2"                                                   *****/
            printPartDescription("------    Part 3 - Start the second NV test with flow \"Flow2\"");
            startTest("Flow2");
            connectToTransactionManager("Flow2");
            printPartSeparator();
            /*****    Part 4 - Run the "Home Page" transactions in both tests                                                   *****/
            printPartDescription("------    Part 4 - Run the \"Home Page\" transactions in both tests");
            startTransaction("Flow1");
            flow1TransactionInProgress = true;
            startTransaction("Flow2");
            flow2TransactionInProgress = true;
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            stopTransaction("Flow1");
            flow1TransactionInProgress = false;
            stopTransaction("Flow2");
            flow2TransactionInProgress = false;
            printPartSeparator();
            /*****    Part 5 - Stop both tests, analyze them and print the results                                              *****/
            printPartDescription("------    Part 5 - Stop both tests, analyze them and print the results");
            stopTest("Flow1");
            stopTest("Flow2");
            testRunning = false;
            analyzeTest("Flow1");
            analyzeTest("Flow2");
            driverCloseAndQuit();
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

        if (flow2TransactionInProgress) {
            stopTransaction("Flow2");
            flow2TransactionInProgress = false;
            if (flow1TransactionInProgress) {
                stopTransaction("Flow1");
                flow1TransactionInProgress = false;
                if (testRunning) {
                    testManager.stopAllTests();
                    testRunning = false;
                }
            }
        }

        if (!flow2TransactionInProgress && testRunning) {
            testManager.stopAllTests();
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

    // start an NV test according to the specified flow ID
    public static void startTest(String flowId) throws Exception {
        // create an NV test
        logDebugMessage("Creating the NV test object");
        String testName =  "AdvMultipleTestsConcurrent Sample Test - " + flowId;
        String networkScenario = "3G Good";
        if (flowId.equals("Flow1")) {
            flow1Test = new Test(testManager, testName, networkScenario);
            flow1Test.setTestMode(Test.Mode.CUSTOM);
        }
        else {
            flow2Test = new Test(testManager, testName, networkScenario);
            flow2Test.setTestMode(Test.Mode.CUSTOM);
        }

        Flow flow = new Flow(flowId, 80, 0, 2000, 512);
        flow.setSrcIp(activeAdapterIp);

        IPRange ipRange = new IPRange(null, null, (flowId.equals("Flow1") ? firstTestFlowTcpPort: secondTestFlowTcpPort), IPRange.Protocol.TCP.getId());
        flow.includeDestIPRange(ipRange);

        ipRange = new IPRange(activeAdapterIp, activeAdapterIp, 0, IPRange.Protocol.ALL.getId());
        flow.excludeDestIPRange(ipRange);

        // start the test
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the NV test with flow \"" + flowId + "\"");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        EmulationResponse emulationResponse;
        if (flowId.equals("Flow1")) {
            flow1Test.addFlow(flow);
            emulationResponse = flow1Test.start();
        }
        else {
            flow2Test.addFlow(flow);
            emulationResponse = flow2Test.start();
        }
        logDebugMessage("New test started. Test token: \"" + emulationResponse.getTestToken() + "\"");
    }

    // connect the NV test to the transaction manager
    public static void connectToTransactionManager(String flowId) throws Exception {
        ConnectResponse connectResult;
        if (flowId.equals("Flow1")) {
            logDebugMessage("Connecting the NV test \"" + flow1Test.getTestMetadata().getTestName() + "\" to the transaction manager");
            connectResult = flow1Test.connectToTransactionManager();
            logDebugMessage("Test \"" + flow1Test.getTestMetadata().getTestName() + "\" is now connected to the transaction manager with session ID: \"" + connectResult.getTransactionManagerSessionIdentifier() + "\"");
        }
        else {
            logDebugMessage("Connecting the NV test \"" + flow2Test.getTestMetadata().getTestName() + "\" to the transaction manager");
            connectResult = flow2Test.connectToTransactionManager();
            logDebugMessage("Test \"" + flow2Test.getTestMetadata().getTestName() + "\" is now connected to the transaction manager with session ID: \"" + connectResult.getTransactionManagerSessionIdentifier() + "\"");
        }
    }

    // start the "Home Page" NV transaction
    public static void startTransaction(String flowId) throws Exception {
        // create an NV transaction
        logDebugMessage("Creating the \"Home Page\" transaction");
        String transactionName = "Home Page";
        Transaction pageTransaction;
        Test test;
        if (flowId.equals("Flow1")) {
            test = flow1Test;
            flow1TestTransaction = new Transaction(transactionName);
            pageTransaction = flow1TestTransaction;
        }
        else {
            test = flow2Test;
            flow2TestTransaction = new Transaction(transactionName);
            pageTransaction = flow2TestTransaction;
        }

        // add the NV transaction to the NV test
        logDebugMessage("Adding the \"Home Page\" transaction to NV test configured with flow \"" + flowId + "\"");
        TransactionEntity transactionEntity = pageTransaction.addToTest(test);
        logDebugMessage("New transaction added with ID \"" + transactionEntity.getId() + "\"");

        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Starting the \"Home Page\" transaction in the NV test configured with flow \"" + flowId + "\"");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        TransactionResponse transactionResponse = pageTransaction.start();
        logDebugMessage("Started transaction named \"" + pageTransaction.getName() + "\" with ID \"" + transactionResponse.getTransactionIdentifier() + "\"");
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
    public static void stopTransaction(String flowId) throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Stopping \"Home Page\" transaction in the NV test configured with flow \"" + flowId + "\"");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        Transaction pageTransaction;
        if (flowId.equals("Flow1")) {
            pageTransaction = flow1TestTransaction;
        }
        else {
            pageTransaction = flow2TestTransaction;
        }
        TransactionResponse stopTransactionResult = pageTransaction.stop();
        logDebugMessage("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
    }

    // stop the NV test
    public static void stopTest(String flowId) throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Stopping the NV test configured with flow \"" + flowId + "\"");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        Test siteTest;
        if (flowId.equals("Flow1")) {
            siteTest = flow1Test;
        }
        else {
            siteTest = flow2Test;
        }
        EmulationStopResponse stopTestResult = siteTest.stop();
        logDebugMessage("Test with token \"" + siteTest.getTestToken() + "\" stopped. Path to .shunra file: " + stopTestResult.getAnalysisResourcesLocation().get(siteTest.getTestToken()));
    }

    // analyze the NV test
    public static void analyzeTest(String flowId) throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Analyzing the NV test configured with flow \"" + flowId + "\"");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }

        Test siteTest;
        String zipResultFilePath = null;
        if (flowId.equals("Flow1")) {
            siteTest = flow1Test;
            zipResultFilePath = firstZipResultFilePath;
        }
        else {
            siteTest = flow2Test;
            zipResultFilePath = secondZipResultFilePath;
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

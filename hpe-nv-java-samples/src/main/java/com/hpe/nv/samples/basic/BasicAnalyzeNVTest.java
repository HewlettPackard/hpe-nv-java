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

import com.hpe.nv.api.*;
import com.hpe.nv.samples.Spinner;
import com.hpe.nv.samples.SpinnerStatus;
import com.shunra.dto.emulation.EmulationResponse;
import com.shunra.dto.emulation.EmulationStopResponse;
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
import java.util.concurrent.TimeUnit;

 /*
    This sample demonstrates the use of the most basic NV methods.

    First, the sample creates a TestManager object and initializes it.
    The sample starts an NV test over an emulated "3G Busy" network.

    Next, the sample navigates to the home page in the HPE Network Virtualization website
    using the Selenium WebDriver.

    Finally, the sample stops the NV test, analyzes it, and prints the path of the analysis .zip file to the console.

    BasicAnalyzeNVTest.java steps:
    1. Create a TestManager object and initialize it.
    2. Start the NV test with the "3G Busy" network scenario.
    3. Build the Selenium WebDriver.
    4. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
    5. Close and quit the Selenium WebDriver.
    6. Stop and analyze the NV test and get the result as a .zip file.
    7. Print the path of the .zip file to the console.
 */

public class BasicAnalyzeNVTest {
    static TestManager testManager;
    static Test siteTest;
    static String browser, proxySetting, serverIp, username, password, zipResultFilePath;
    static WebDriver driver;
    static boolean ssl = false, debug = false, testRunning = false;
    static int serverPort;
    static String[] analysisPorts;
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
                 formatter.printHelp( "BasicAnalyzeNVTest.java", options );
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
            String testDescription = "***   This sample demonstrates the use of the most basic NV methods.                                                      ***" + newLine +
                                     "***                                                                                                                       ***" + newLine +
                                     "***   First, the sample creates a TestManager object and initializes it.                                                  ***" + newLine +
                                     "***   The sample starts an NV test over an emulated \"3G Busy\" network.                                                    ***" + newLine +
                                     "***                                                                                                                       ***" + newLine +
                                     "***   Next, the sample navigates to the home page in the HPE Network Virtualization website                               ***" + newLine +
                                     "***   using the Selenium WebDriver.                                                                                       ***" + newLine +
                                     "***                                                                                                                       ***" + newLine +
                                     "***   Finally, the sample stops the NV test, analyzes it, and prints the path of the analysis .zip file to the console.   ***" + newLine +
                                     "***                                                                                                                       ***" + newLine +
                                     "***   You can view the actual steps of this sample in the BasicAnalyzeNVTest.java file.                                   ***" + newLine;

            // print the sample's description
            System.out.println(testDescription);

            // start console spinner
            if (!debug) {
                spinner = new Thread(new Spinner());
                spinner.start();
            }


            // sample execution steps
            /*****    Part 1 - Create a TestManager object and initialize it                                            *****/
            printPartDescription("\b------    Part 1 - Create a TestManager object and initialize it");
            initTestManager();
            printPartSeparator();
            /*****    Part 2 - Start the NV test with the "3G Busy" network scenario                                    *****/
            printPartDescription("------    Part 2 - Start the NV test with the \"3G Busy\" network scenario");
            startTest();
            testRunning = true;
            printPartSeparator();
            /*****    Part 3 - Navigate to the HPE Network Virtualization website                                       *****/
            printPartDescription("------    Part 3 - Navigate to the HPE Network Virtualization website");
            buildSeleniumWebDriver();
            seleniumNavigateToPage();
            driverCloseAndQuit();
            printPartSeparator();
            /*****    Part 4 - Stop the NV test, analyze it and print the results to the console                        *****/
            printPartDescription("------    Part 4 - Stop the NV test, analyze it and print the results to the console");
            stopTestAndAnalyze();
            testRunning = false;
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

        if (testRunning) {
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

    // start an NV test with the "3G Busy" network scenario
    public static void startTest() throws Exception {
        // create an NV test
        logDebugMessage("Creating the NV test object");
        String testName =  "BasicAnalyzeNVTest Sample Test";
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
        System.out.println("Starting the NV test with the \"3G Busy\" network scenario");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        EmulationResponse emulationResponse = siteTest.start();
        logDebugMessage("New test started. Test token: \"" + emulationResponse.getTestToken() + "\"");
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

    // stop and analyze the NV test and analyze
    public static void stopTestAndAnalyze() throws Exception {
        if (!debug) {
            SpinnerStatus.getInstance().pauseThread();
            System.out.print("\b");
        }
        System.out.println("Stopping the NV test, analyzing it and getting the result as a .zip file");
        if (!debug) {
            SpinnerStatus.getInstance().resumeThread();
        }
        File analyzeResult = siteTest.stopAndAnalyze(analysisPorts, zipResultFilePath);
        printZipLocation(analyzeResult);
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

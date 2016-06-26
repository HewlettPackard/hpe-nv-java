# HPE Network Virtualization Java API
Applications are often not designed to handle slow networks. Network Virtualization (NV) helps you to understand how slow networks affect your application's performance and what you can do to optimize client and server performance in your application. 

Do your customers complain that your application works slowly? Are there timeout exceptions? Has your application ever failed when moved to production, even though the application worked perfectly when tested under lab conditions? Does your application suffer from non-reproducible issues?

The NV API lets you test your application behavior under various network conditions, analyze performance results, and view optimization recommendations for improving application performance.

Learn more about NV at http://nvhelp.saas.hpe.com/en/9.10.  

This Java library lets you call the NV API in your automatic tests (for example, your Selenium tests). You can use this API instead of using the NV Test Manager user interface. 

This Readme includes the following topics:
* Prerequisites
* Usage
* Get Started
* Classes:
  * TestManager Class
  * Test Class
  * Transaction Class
  * IPRange Class
  * Range Class
  * Flow Class
* Enums
* Samples:
  * Basic Step-by-Step Samples:
    * BasicNVTest.java
    * BasicAnalyzeNVTest.java
	* BasicComparisonWithoutNV.java
	* BasicAnalyzeNVTransactions.java
    * BasicAnalyze2Scenarios.java    
  * Advanced Samples:
    * AdvAllTestClassMethods.java
    * AdvRealtimeUpdate.java
    * AdvAllTestManagerClassMethods.java
    * AdvMultipleTestsSequential.java
    * AdvMultipleTestsConcurrent.java
* Debugging
* License

## Prerequisites
* Java 7 installed (http://java.oracle.com) and available in your $PATH  
* Apache maven 3.2.2 (or later) installed (http://maven.apache.org/) and available in your $PATH  
* NV Test Manager 9.10 or later  
* To try the samples, use a Chrome or Firefox browser.  
  __Note:__ If you are using Chrome, download ChromeDriver (https://sites.google.com/a/chromium.org/chromedriver/) and add it to your $PATH.
	 
## Usage
* To try the samples (located in https://github.com/HewlettPackard/hpe-nv-java):
  * _mvn clean install_ the __pom.xml__ to create the __hpe-nv-java-samples-\<version\>.jar__ under the target folder.  
  * Either run the batch files from the _\hpe-nv-java-samples\scripts_ folder, or run the samples directly from your favorite Java IDE.  
  * To read more about the samples, go to the "Samples" section below.  
* To create a custom test of your own that calls the NV Java API, create a Maven project and use the following Maven dependency in your project's pom.xml:
  
  ```
  <dependency>
    <groupId>com.hpe.nv</groupId>
	<artifactId>hpe-nv-java-api</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```

## Get Started
Try a few basic samples (under the _\hpe-nv-java-samples\src\main\java\com\hpe\nv\samples\basic_ folder). The "Samples" section below describes all of our samples. For example, start with BasicNVTest.java and BasicAnalyzeNVTest.java.

Here's a basic code example that shows how to start a test in NV, run a transaction, stop the transaction, stop the test, and finally analyze the test results. This example shows how to create a home page transaction, but you can create any type of transaction, such as a search transaction.    


```java
static TestManager testManager;
static Test siteTest;
static Transaction pageTransaction;

// Create and initialize the TestManager object
public static void initTestManager() throws Exception {
	// create a TestManager object
	testManager = new TestManager(serverIp, serverPort, username, password);
	testManager.setUseSSL(ssl);
	// call the init method
	testManager.init();
}

// start an NV test with the "3G Busy" network scenario
public static void startBusyTest() throws Exception {
	// create an NV test
	String testName =  "Sample Test";
	String networkScenario = "3G Busy";
	siteTest = new Test(testManager, testName, networkScenario);
	siteTest.setTestMode(Test.Mode.CUSTOM);

	// add a flow to the test
	Flow flow = new Flow("Flow1", 200, 0.5, 384, 128);
	siteTest.addFlow(flow);

	// start the test
	EmulationResponse emulationResponse = siteTest.start();
	System.out.println("New test started. Test token: \"" + emulationResponse.getTestToken() + "\"");
}

// connect the NV test to the transaction manager
public static void connectToTransactionManager() throws Exception {
	ConnectResponse connectResult = siteTest.connectToTransactionManager();
	System.out.println("Connected to the transaction manager with session ID: \"" + connectResult.getTransactionManagerSessionIdentifier() + "\"");
}

// start the "Home Page" NV transaction
public static void startTransaction() throws Exception {
	// create an NV transaction
	String transactionName = "Home Page";
	pageTransaction = new Transaction(transactionName);

	// add the NV transaction to the NV test
	TransactionEntity transactionEntity = pageTransaction.addToTest(siteTest);
	System.out.println("New transaction added with ID \"" + transactionEntity.getId() + "\"");

	// start the NV transaction	
	TransactionResponse startTransactionResult = pageTransaction.start();
	System.out.println("Started transaction named \"" + pageTransaction.getName() + "\" with ID \"" + startTransactionResult.getTransactionIdentifier() + "\"");
}

// navigate to the site's home page using the Selenium WebDriver
public static void seleniumNavigateToPage() {
	// insert your Selenium code to navigate to your site's home page
	...
}

// stop the "Home Page" NV transaction
public static void stopTransaction() throws Exception {
	TransactionResponse stopTransactionResult = pageTransaction.stop();
	System.out.println("Transaction with ID \"" + stopTransactionResult.getTransactionIdentifier() + "\" stopped");
}

// stop the NV test
public static void stopTest() throws Exception {
	EmulationStopResponse stopTestResult = siteTest.stop();
	System.out.println("Test with token \"" + siteTest.getTestToken() + "\" stopped. Path to .shunra file: " + stopTestResult.getAnalysisResourcesLocation().get(siteTest.getTestToken()));
}

// analyze the NV test and retrieve the result as an object
public static void analyzeTest() throws Exception {
	ExportableResult analyzeResult = (ExportableResult) siteTest.analyze(analysisPorts);
	printNetworkTime(analyzeResult);
}

// print the transaction's network time
public static void printNetworkTime(ExportableResult analyzeResult) {
	ArrayList<ExportableSummary> transactionSummaries = analyzeResult.getTransactionSummaries();	
	ExportableSummary firstTransactionSummary = transactionSummaries.get(0);
	DocumentProperties firstTransactionProperties = firstTransactionSummary.getProperties();
	System.out.println("\"Home Page\" transaction network time: " + (firstTransactionProperties.getNetworkTime()/1000) + "s");
}

public static void main(String[] args) {
	initTestManager();
	startBusyTest();
	connectToTransactionManager();
	startTransaction();
	seleniumNavigateToPage();
	stopTransaction();
	stopTest();
	analyzeTest();
	printNetworkTime();
}
```	 

## Classes
This section describes the library's classes - including their methods, exceptions, and so on.  
__Note:__ Methods not described in this section are for internal use only.  

### TestManager Class
This class represents a connection instance to a specific NV Test Manager.  
__Important note:__ If you are using a secure Test Manager installation, set the "useSSL" property to __true__ using the setUseSSL method.  
The class includes the following methods:
* __TestManager(String serverIP, int serverPort, String username, String password)__ - the constructor method
    Parameters:
    * serverIP - NV Test Manager IP  
    * serverPort - NV Test Manager port  
    * username - username for authentication  
    * password - password for authentication  

* __setUseSSL(boolean useSSL)__ - sets the useSSL Boolean value.  
    Parameters - useSSL: set to __true__ if you are using a secure Test Manager installation. Default: __false__  
	
	Returns - void
	
* __init()__ - initializes the _TestManager_ object with the pre-existing tests, both completed and running  	
	Returns - void
	
	Throws:  
    * NVExceptions.MissingPropertyException - if the existing tests have missing properties.  
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.  

* __stopAllTests()__ - stops all tests that are part of the emulation  
	Returns - an _EmulationStopResponse_ object which contains a list of Shunra files (one .shunra file per test), for example:  
	```java
	{
		"analysisResourcesLocation": {
			"073bac1a-48a9-4f70-9c22-df23ff21473db3673d23-f4d4-47c1-817a-b619f7d7b032":
			"C:\\tmp\\TrafficResources\\073bac1a-48a9-4f70-9c22-df23ff21473d\\b3673d23-f4d4-47c1-817ab619f7d7b032\\AnalysisResources.shunra"
		}
	}
	```
	
	Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error. 
	
* __stopTests(List\<Test\> tests)__ - stops the specified list of tests   
	Parameters:
	* tests - a list of tests to stop
	
	Returns - an _EmulationStopResponse_ object (the same object as in "stopAllTests" method)
	
	Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error. 
	
* __getTestTokens(boolean allUsers)__ - gets all running test tokens  
	Parameters:
	* allUsers - set to __true__ to return test tokens for all users, not just the user for API authentication. The user for API authentication must have administrator privileges to use this parameter.  
	
	Returns - a _MultiUserEmulationTokensResponse_ object that contains all the running test tokens, for example:
	```java
	{
		"tests": [{
			"userId": "admin",
			"emulationEngine": {
				"IL-Igor-LT.shunra.net": {
					"testTokens": ["fe7aff67-6eef-4c92-b357-80da7becf50937d7cb1d-4a18-42ae-8533-992e4f2945a7"],
					"emulationMode": "SINGLE_USER"
				}
			}
		}]
	}
	```
	
	Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error. 
	
* __startPacketListCapture()__ - starts capturing all packet lists in all running tests  
	Returns - void

    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error. 

* __stopPacketListCapture()__ - stops capturing all packet lists in all running tests  
	Returns - void

    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error. 

* __getPacketListInfo()__ - provides packet list information for all packet lists in all running tests  
	Returns - a _PacketListInfoResponse_ object, for example:
	```java
	{
		"packetListsInfo": [
			{
				"flowId": "FLOWS_4",
				"plId": "ID_PACKET_LIST_CLIENT_FLOWS_4",
				"captureStatus": "CAPTURING"
			}, 
			{
				"flowId": "FLOWS_5",
				"plId": "ID_PACKET_LIST_CLIENT_FLOWS_5",
				"captureStatus": "CAPTURING"
			}
		],
		"globalCaptureStatus" : "CAPTURING"
	}
	```

    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __setActiveAdapter(String ip, boolean reverseDirection)__ - sets the active adapter used for the emulation. An active adapter determines the NIC through which impairments are applied to the traffic and packets are captured. Only IPV4 is supported.  
	Parameters:
	* ip - the active adapter IP
	* reverseDirection - (Relevant when using "Default Flow") When the packet direction cannot be determined from the packet header,
	NV cannot determine if the packet originated in the client or the server.  
	Possible values:
	  * __false__. The packets are treated as if the NV Driver is installed on the client. All packets arrive from the server endpoint, and all packets exit to the server endpoint.
	  * __true__. The NV driver is considered as if installed on the server. All packets arrive from the client endpoint, and all packets exit to the client endpoint.	  
	
	Returns - void.  

    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
    * NVExceptions.UpdateConfigErrorException - if tests are currently running (because the active adapter cannot be updated during a run session).	

* __setActiveAdapter(String ip)__ - sets the active adapter used for the emulation with a reverseDirection property value of __false__ (see above for details on the "reverseDirection" property). An active adapter determines the NIC through which impairments are applied to the traffic and packets are captured. Only IPV4 is supported. 
	Parameters - the active adapter IP  
	
	Returns - void  	
	
    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
    * NVExceptions.UpdateConfigErrorException - if tests are currently running (because the active adapter cannot be updated during a run session).
	
* __getActiveAdapter()__ - gets the active adapter   
	Returns - the _ActiveAdapter_ object, for example:  
	```java
	{
		"ip": "192.168.0.101",
		"reverseDirection": false
	}
	```
    
	Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __setConfiguration(boolean isPacketListCaptureCyclic, int packetListMaxSizeMB, double packetListServerClientRatio)__ - sets the NV configuration  
	Parameters:
	* isPacketListCaptureCyclic - Default: __true__
	* packetListMaxSizeMB - the maximum size of the packet list (comprises all devices in a test)
	* packetListServerClientRatio - Default: 0 (all packet lists are allocated on the client side)
	
	Returns - void.  	
	
    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
    * NVExceptions.UpdateConfigErrorException - if tests are currently running, the configuration cannot be updated.

* __getConfiguration()__ - gets the NV configuration  
	Returns - a _ConfigurationSettings_ object, for example: 
	```java
	{
		"isPacketListCaptureCyclic": true,
		"packetListMaxSizeMB": 100,
		"minNumOfPacketListSpace": 3,
		"captureBytesPerPacket": 1500,
		"packetListServerClientRatio": 0
	}
	```
	
    Throws:   
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __analyzeShunraFile(String[] ports, String zipResultFilePath, String shunraFilePath)__ - analyzes the specified .shunra file and returns the analysis result as a .zip file  
	Parameters:
	* ports - array of ports to be analyzed
	* zipResultFilePath - the path of the .zip file that is created. Default: "analysis-result.zip"
	* shunraFilePath - the .shunra file path used for analysis

	Returns - a _File_ object associated with the created .zip file
	
	Throws:  
    * NVExceptions.MissingPropertyException - if the "shunraFilePath" parameter passed is null.	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.

* __analyzeShunraFile(String[] ports, String shunraFilePath)__ - analyzes the specified .shunra file and returns the analysis result object  
	Parameters:
	* ports - array of ports to be analyzed
	* shunraFilePath - the .shunra file path used for analysis

	Returns - an _ExportableResult_ object (an analysis result object)
	
	Throws:  
    * NVExceptions.MissingPropertyException - if the "shunraFilePath" parameter passed is null.	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
### Test Class
This class represents an NV test instance and includes the following methods:
* __Test(TestManager testManager, String testName, String networkScenario)__ - the constructor method
	Parameters:
	* testManager - a previously created _TestManager_ object that contains the NV server URL, NV server port, user credentials, and so on
	* testName - test name
	* networkScenario - network scenario name	

	Throws:  
    * NVExceptions.MissingPropertyException - if one of the properties passed is null.
	
* __setDescription(String description)__ - sets the test's description  
	Parameters - the test's description  
	
	Returns - void

* __setEmulationMode(String emulationMode)__ - sets the emulation mode
	Parameters - the emulation mode. Set to "MULTI_USER" to use MULTI_USER emulation and set to "SINGLE_USER" to use SINGLE_USER emulation
	
	Returns - void
	
* __setTestMode(Mode testMode)__ - sets the test mode  
	Parameters - a Test.Mode enum. Possible values: Test.Mode.NTX and Test.Mode.CUSTOM.  
	
	Returns - void
	
* __setNtxxFilePath(String ntxxFilePath)__ - sets the path to the .ntx/.ntxx file used in Test.Mode.NTX test mode    
	Parameters - the path to the .ntx/.ntxx file containing the client and server IP ranges  
	
	Returns - void
	
* __setOverrideIp(boolean overrideIp)__ - sets the overrideIp value  
	Parameters - overrideIp: if set to __true__, allows you to override the client IP defined in the .ntx/.ntxx file with the active adapter value. In this case, the .ntx/.ntxx file must be a single flow file and the mode must be SINGLE_USER. 
	
	Returns - void
	
* __setUseNVProxy(boolean useNVProxy)__ - sets the "useNVProxy" parameter value. Relevant for analyzing HTTPS traffic. Requires configuration.  
    In NV Test Manager \> Settings tab, configure the NV proxy settings. Then, set the browser's proxy setting to use the IP of your NV Test Manager and the port configured in the proxy settings. For more details, see: http://nvhelp.saas.hpe.com/en/9.10/help/Content/Setup/Setup_environment.htm.    
	Parameters - useNVProxy: Indicates whether to enable NV to capture and decrypt HTTPS traffic in  the test.  
	
	Returns - void
	
* __addFlow(flow)__ - adds a flow to the list of test flows. Used to add a flow to the test before the "start" method is called.  
	Parameters:  
	* flow - a _Flow_ object

	Returns - void
	
	Throws:  	
    * NVExceptions.NotSupportedException - the following are not supported:   
	  a. If the test is currently running  
	  b. More than one "Default Flow" per test  
	  c. "Default Flow" in MULTI_USER mode  
      d. Empty source and destination IPs when the list of flows is not empty (only one flow is allowed in that case)  
    * NVExceptions.MissingPropertyException - an empty source IP is accepted only if the destination IP is also empty. 	  
	
* __start()__ - starts the test  
	Returns - an _EmulationResponse_ object, for example: 
	```java
	{
		"testToken":"133a1a9e-2885-443f-9ea5-4de373d4a57a372572b2-0d25-4852-91f8-fb849056c89a"
	}
	```
		
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.IllegalActionException - if a single-mode test is running, no additional tests can start and if a concurrent-mode test is running, no single-mode tests can start.
	* NVExceptions.NotSupportedException - if the test mode set is not supported.
	
* __connectToTransactionManager(String plId, String clientId, String flowID, boolean overwriteExistingConnection)__ - connects to the transaction manager. You can optionally specify a specific endpoint or packet list to mark the test's transactions for analysis.  
	Parameters:
	* plId - the ID of the packet list from which to connect or null
	* clientId - the IP address from which to connect (must be a valid IPv4 address) or null. 
	* flowID - the ID of a specific flow or null
	* overwriteExistingConnection - a Boolean flag indicating whether to overwrite an existing connection. Default: __true__	
	To connect to all packet lists in the running test, call "connectToTransactionManager" with no parameters.

	Returns - a _ConnectResponse_ object, for example: 	
	```java	
	{
		"transactionManagerSessionIdentifier": "Aead518af - 3fa3 - 460c - 9be5 - fc3b6a7101cfB"
	}
	```
    
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __connectToTransactionManager()__ - connects to the transaction manager. Call this method to connect to all packet lists in the running test.  	
	Returns - a _ConnectResponse_ object, for example: 	
	```java	
	{
		"transactionManagerSessionIdentifier": "Aead518af - 3fa3 - 460c - 9be5 - fc3b6a7101cfB"
	}
	```
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __disconnectFromTransactionManager()__ - disconnects from the transaction manager  
	Returns - void

	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.IllegalActionException - if the test is currently not connected to the transaction manager.
	
* __stop()__ - stops the test  
	Returns - an _EmulationStopResponse_ object (the same object as in the TestManager class' "stopAllTests" method).

	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __analyze(String[] ports, String zipResultFilePath)__ - analyzes the test and gets the analysis result as a .zip file  
	Parameters: 
	* ports - array of ports whose traffic is analyzed
	* zipResultFilePath - the path of the .zip file that is created. Default: "analysis-result.zip"

	Returns - a _File_ object associated with the created .zip file

	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.MissingPropertyException - if the "ports" parameter passed is null.
	
* __analyze(String[] ports)__ - analyzes the test and returns the analysis result object. This method does not write the analysis result to a .zip file.  
	Parameters: 
	* ports - array of ports whose traffic is analyzed
	
	Returns - an _ExportableResult_ object (an analysis result object)
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.MissingPropertyException - if the "ports" parameter passed is null.
	
* __stopAndAnalyze(String[] ports, String zipResultFilePath)__ - Stops and analyzes the test and gets the analysis result as a .zip file.  
    Parameters: 
	* ports - array of ports whose traffic is analyzed
	* zipResultFilePath - the path of the .zip file that is created. Default: "analysis-result.zip"
	
	Returns - a _File_ object associated with the created .zip file
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.MissingPropertyException - if the "ports" parameter passed is null.	

* __stopAndAnalyze(String[] ports)__ - Stops the test, analyzes it, and returns the analysis result object. This method does not write the analysis result to a .zip file.  
    Parameters: 
	* ports - array of ports whose traffic is analyzed
	
	Returns - an _ExportableResult_ object (an analysis result object)
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.MissingPropertyException - if the "ports" parameter passed is null.	
		
* __realTimeUpdate(String networkScenario, String description, String ntxxFilePath)__ - updates the test in real-time using Test.RTUMode.NTX mode.   
	Parameters:
	* networkScenario - the replacement network scenario name
	* description - the replacement description
	* ntxxFilePath - a path to an .ntx/.ntxx file. You can use the complete .ntx/.ntxx file or only the parts of the file that include the shapes that require updates. 
	
	Returns - a _RealTimeUpdateResponse_ object that includes the updated test properties
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.IllegalActionException - if, for example, the test is not running or the network scenario passed is not unique.
	
* __realTimeUpdate(String networkScenario, String description, List\<Flow\> flows)__ - updates the test in real-time using Test.RTUMode.CUSTOM mode.    
	Parameters:
	* networkScenario - the replacement network scenario name
	* description - the replacement description
	* flows - the replacement _Flow_ array.  
	
	Returns - a _RealTimeUpdateResponse_ object that includes the updated test properties
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.IllegalActionException - if, for example, the test is not running or the network scenario passed is not unique.
	
* __getFlowStatistics(Long startTime, Long endTime, List\<String\> flows)__ - gets flow statistics  
	Parameters:
	* startTime - used to get statistics from a defined start point. Default: test start time
	* endTime - used to get statistics until the defined end point. Default: current time
	* flows - list of flow IDs. Specify a null list to get statistics for all flows.
	
	Returns - a _MultiFlowStatistics_ object, for example:  
	```java
	{
		"statistics": [{
			"timeStamp": 1361528862628,
			"flowStats": [{
				"id": "FLOWS_1",
				"clientDownStats": {
					"bps": 480,
					"total": 300,
					"bwUtil": 0
				},
				"clientUpStats": {
					"bps": 480,
					"total": 360,
					"bwUtil": 0
				},
				"serverDownStats": {
					"bps": 480,
					"total": 360,
					"bwUtil": 0
				},
				"serverUpStats": {
					"bps": 480,
					"total": 300,
					"bwUtil": 0
				},
				"cloudStats": {
					"avgLatency": 500,
					"packetLossCount": 0,
					"packetLossPercent": 0,
					"packetLossTotal": 0
				}
			}]
		}]
	}
	```
    
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __getFlowStatistics(List\<String\> flows)__ - gets flow statistics from test start till now (current time)  
	Parameters:
	* flows - list of flow IDs. Specify a null list to get statistics for all flows.
	
	Returns - a _MultiFlowStatistics_ object (the same object as in the above "getFlowStatistics" method)
	   
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __getLastStats()__ - gets the data retrieved in the last call to "getFlowStatistics"  
	Returns - the last _MultiFlowStatistics_ object retrieved
	
* __getLastStatsTimestamp()__ - gets the value of the response header "x-shunra-next" in the last call to "getFlowStatistics". This header provides the last timestamp for the latest statistics data. You can use this header for future calls to collect statistics data starting from this timestamp.  
	Returns - the last timestamp

* __startPacketListCapture(String packetListId)__ - starts capturing the specified packet list  
	Parameters:
	* packetListId - used to capture a specific packet list  

	Returns - void
	   
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __startPacketListCapture()__ - starts capturing all of the test's packet lists  
	Returns - void		
	   
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __stopPacketListCapture(String packetListId)__ - stops capturing the specified packet list.  
	Parameters:
	* packetListId - used to stop capturing a specific packet list

	Returns - void
   
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __stopPacketListCapture()__ - stops capturing all of the test's packet lists.    
	Returns - void
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __downloadPacketList(String packetListId, Boolean clear, String packetListFilePath)__ - downloads the specified packet list  
	Parameters:
	* packetListId - the packet list ID
	* clear - set to __true__ to clear the packet list after download. Default: __false__. Use the "clear" flag to save disk space and prevent overwriting packet lists.
	* packetListFilePath - the path of the packet list file that is created. Default: "packet-list-\<packetListId\>.pcap".

	Returns - a _File_ object associated with the downloaded packet list file  
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.MissingPropertyException - if the "packetListId" parameter passed is null.
	
* __getPacketListInfo(String packetListId)__ - gets packet list information from all of the test's packet lists or from a specific packet list.  
	Parameters:
	* packetListId (optional) - the packet list ID

	Returns - a _PacketListInfoResponse_ object (the same object as in the TestManager class' "getPacketListInfo" method)
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
		
* __getPacketListInfo()__ - gets packet list information from all of the test's packet lists.  

	Returns - a _PacketListInfoResponse_ object (the same object as in the TestManager class' "getPacketListInfo" method)
		
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __downloadShunra(String packetListId, String shunraFilePath)__ - downloads a .shunra file containing all of the test's packet lists or a specific packet list (in addition to other files).  
	Parameters:
	* packetListId - the packet list ID (used to get a .shunra file containing a specific packet list) or null to get all the packet lists
	* shunraFilePath - the path of the .shunra file that is created. Pass null to use the default value: "shunra-\<testName\>.shunra" or "shunra-\<packetListId\>" (if "packetListId" is specified)

	Returns - a _File_ object associated with the downloaded .shunra file.
		
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
### Transaction Class
This class represents a _Transaction_ instance and includes the following methods:
* __Transaction(String name)__ - the constructor method  
	Parameters:
	* name - the transaction name
	
* __setDescription(String description)__ - sets the transaction's description  
	Parameters:
	* description - a description for the transaction 
	
	Returns - void

* __addToTest(Test test)__ - adds the transaction to the test specified    
	Returns - a _TransactionEntity_ object, for example:	
	```java	
	{
	  "id": "9ee4f61e-553b-42f4-b616-f6e55da6c39b",
	  "name": "Search",
	  "description": "",
	  "orderNum": 0,
	  "averageUserTime": 0.0,
	  "averageNetworkTime": 0.0,
	  "runs": {
		
	  }
	}
	```
		
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
* __start()__ - starts the transaction  	
	Returns - a _TransactionResponse_ object, for example:		
	```java
	{
		"transactionIdentifier": "e16645fa-6f96-4707-b884-fe46b872e3a436434aa6-ae80-4ace-8009-8ee8c970e689",
		"transactionEntity": {
			"id": "e16645fa-6f96-4707-b884-fe46b872e3a4",
			"name": "transaction1",
			"description": "Login transaction",
			"averageUserTime": 0,
			"averageNetworkTime": 0,
			"orderNum": 0,
			"runs": {
				"36434aa6-ae80-4ace-8009-8ee8c970e689": {
					"id": "36434aa6-ae80-4ace-8009-8ee8c970e689",
					"startTime": 1382461475606,
					"endTime": 0,
					"userTime": 0,
					"networkTime": 0,
					"status": "Start",
					"averageBandwith": 0,
					"totalThroughputClient": 0,
					"totalThroughputServer": 0,
					"aggregateScore": 0,
					"numberOfErrors": 0,
					"applicationTurns": 0,
					"protocolOverhead": 0,
					"passed": true
				}
			}
		}
	}
	```
	
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	* NVExceptions.IllegalActionException - if the transaction was added to an NV test that is not connected to the transaction manager (no session exists).
	
* __stop()__ - stops the transaction  
	Returns - a _TransactionResponse_ object
		
	Throws:  	
    * IOException - if an input or output exception occurs.  
    * NVExceptions.ServerErrorException - if the NV Test Manager server returns an error.
	
### IPRange Class
This class represents an _IPRange_ instance and includes the following methods:
* __IPRange(String from, String to, int port, int protocol)__ - the constructor method  
	Parameters:
	* from - pass null to set the "from" address to the default value: 0.0.0.1
	* to - pass null to set to the "from" address. If "from" is also null, the "to" address is set to the default value: 255.255.255.255
	* port - Default: 0
	* protocol - IPRange.PROTOCOL enum values (see "Enums" section below). Default: IPRange.PROTOCOL.ALL
	
### Range Class
This class represents a _Range_ instance that consists of included and excluded _IPRange_ arrays. It includes the following methods:
* __Range()__ - the constructor method that creates a _Range_ object with empty "include" and "exclude" lists of IP ranges
	
### Flow Class
This class represents a _Flow_ instance and includes the following methods:
* __Flow(String flowId, double latency, double packetloss, double bandwidthIn, double bandwidthOut)__ - the constructor method  
	Parameters:
	* flowId - flow ID
	* latency - Default: 100
	* packetloss - Default: 0.1
	* bandwidthIn - Default: 0
	* bandwidthOut - Default: 0

	Throws:  	
    * NVExceptions.MissingPropertyException - if the "flowId" parameter is null.  
	
* __Flow(String flowId)__ - call this constructor method to use the default values of the other parameters  
	Parameters - flowId: the flow ID  
	
* __setCaptureClientPL(boolean captureClientPL)__ - sets the isCaptureClientPL Boolean value  
	Parameters - captureClientPL: if set to __false__ no packets lists are captured for the flow  
	
	Returns - void
	
* __setDefaultFlow(boolean defaultFlow)__ - sets the defaultFlow Boolean value
	Parameters - defaultFlow: set to __true__ to use "Default Flow". __Note:__ Not supported in MULTI_USER mode. A test can include only one "Default Flow".   
	
	Returns - void

* __setSrcIp(String srcIp)__ - sets the client IP (source IP). You can use either a source IP or a source IP range. If no values are provided for srcIp and srcIpRange, the source IP takes the IP of the active adapter.  
	Parameters - srcIp: the client IP (source IP)    
	
	Returns - void
	
* __setDestIp(String destIp)__ - sets the server IP (destination IP). You can use either a destination IP or a destination IP range. If no values are provided for destIp and destIpRange, the destination IP range is set to the entire network (0.0.0.1-255.255.255.255), excluding all source IPs in the emulation (to prevent ambiguity).
	Parameters - destIp: the server IP (destination IP)   
	
	Returns - void
	
* __setSrcIpRange(Range srcIpRange)__ - sets the client (source) range (an instance of _Range_ class). You can set the source IP range by passing this _Range_ object to this method or by calling the "includeSourceIPRange" and/or "excludeSourceIPRange" methods after flow creation. You can use either a source IP or a source IP range. If no values are provided for srcIp and srcIpRange, the source IP takes the IP of the active adapter.  
	Parameters - srcIpRange: the client (source) range  
	
	Returns - void
	
* __setDestIpRange(Range destIpRange)__ - sets the server (destination) range (an instance of _Range_ class). You can set the destination IP range by passing this _Range_ object to this method or by calling the "includeDestIPRange" and/or "excludeDestIPRange" methods after flow creation. You can use either a destination IP or a destination IP range. If no values are provided for destIp and destIpRange, the destination IP range is set to the entire network (0.0.0.1-255.255.255.255), excluding all source IPs in the emulation (to prevent ambiguity).
	Parameters - destIpRange: the server (destination) range  
	
	Returns - void
	
* __setShareBandwidth(boolean shareBandwidth)__ - sets the server shareBandwidth Boolean value.   
	Parameters - shareBandwidth: set to __false__ to let every Source-Destination IP pair that fits this flow definition use the defined bandwidth. This enables packets to be handled without delay. (When set to __true__, a queue manages the packets, which may result in delayed packet handling.) Default: __true__  
	
	Returns - void
	
* __includeSourceIPRange(IPRange ipRange)__ - adds the specified source IP range to the srcIpRange _Range_ object's "include" array  
	Parameters - ipRange: an _IPRange_ object
	
	Returns - void
				
	Throws:  	
    * NVExceptions.IllegalArgumentException - if the specified argument is not an instance of _IPRange_ class.  	
	
* __excludeSourceIPRange(IPRange ipRange)__ - removes the specified source IP range from the srcIpRange _Range_ object's "exclude" array   
	Parameters - ipRange: an _IPRange_ object
		
	Returns - void
				
	Throws:  	
    * NVExceptions.IllegalArgumentException - if the specified argument is not an instance of _IPRange_ class.  
    * NVExceptions.NotSupportedException - if the flow is defined as "Default Flow" and the exclude range protocol and port settings are set.	
	
* __includeDestIPRange(IPRange ipRange)__ - adds the specified destination IP range to the destIpRange _Range_ object's "include" array  
	Parameters - ipRange: an _IPRange_ object
	
	Returns - void
				
	Throws:  	
    * NVExceptions.IllegalArgumentException - if the specified argument is not an instance of _IPRange_ class.  	
	
* __excludeDestIPRange(IPRange ipRange)__ - removes the specified destination IP range from the destIpRange _Range_ object's "exclude" array  
	Parameters - ipRange: an _IPRange_ object
	
	Returns - void
				
	Throws:  	
    * NVExceptions.IllegalArgumentException - if the specified argument is not an instance of _IPRange_ class.  
    * NVExceptions.NotSupportedException - if the flow is defined as "Default Flow" and the exclude range protocol and port settings are set.	
	
## Enums
* Test mode enum - represents the test mode. Possible values: Test.Mode.NTX and Test.Mode.CUSTOM.  

* Real-time update mode enum - represents the real-time update mode. Possible values: Test.RTUMode.NTX and Test.RTUMode.CUSTOM.

* IP range protocol enum - represents the IPRange protocol. Each protocol has a corresponding ID.   
	```java	
	public enum Protocol {
        ALL(0),
        ICMP(1),
        TCP(6),
        EGP(8),
        UDP(17),
        DDP(37),
        ICMPV6(58),
        L2TP(115);
		...
	}
	```
	
## Samples
The _\hpe-nv-java-samples\src\main\java\com\hpe\nv\samples_ folder contains both basic and advanced samples that demonstrate the NV API and show common use cases. 
   
To help you get started, each sample has a corresponding batch file (under the _\hpe-nv-java-samples\scripts_ folder) with suggested commands. You can run these batch files with the pre-populated commands, or you can run the samples using your own run arguments.  
__Note:__ Minimum recommended Command Prompt (cmd) window size width: 156
  
Some of the samples let you optionally generate a .zip file containing the *NV Analytics* report. You do this by specifying a .zip file path argument.      
   
To view the list of arguments, run:  
_java -cp hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.\<sample-class-name\> --help_  
or  
_java -cp hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.\<sample-class-name\> --help_  
__Example:__ __java -cp hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicAnalyze2Scenarios --help__  

We suggest starting with the basic samples according to the order in the "Basic Step-by-Step Samples" section. These samples walk you step-by-step through the most basic NV methods that you can use in your automated tests. Each subsequent sample demonstrates an additional NV capability. When you finish the basic samples, you can move on to the advanced samples that demonstrate more complex NV methods.

__Important notes:__  
* Each sample receives an --ssl argument. Make sure to pass __true__ if you are using a secure Test Manager installation.   
* If the website you are testing contains HTTPS traffic, you must use the NV proxy and install an appropriate SSL certificate, as described in http://nvhelp.saas.hpe.com/en/9.10/help/Content/Setup/Setup_environment.htm.

### Basic Step-by-Step Samples
#### BasicNVTest.java
This sample demonstrates the use of the most basic NV methods.

First, the sample creates a _TestManager_ object and initializes it.  

The sample starts an NV test over an emulated “3G Busy” network. ("3G Busy" is one of NV's built-in network profiles. A network profile specifies the network traffic behavior, including latency, packet loss, and incoming/outgoing bandwidth. Network profiles are used to emulate traffic over real-world networks.)  

Next, the sample navigates to the home page in the HPE Network Virtualization website using the Selenium WebDriver.  

Finally, the sample stops the NV test.

##### BasicNVTest.java steps:
1. Create a _TestManager_ object and initialize it.
2. Start the NV test with the "3G Busy" network scenario.
3. Build the Selenium WebDriver.
4. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
5. Close and quit the Selenium WebDriver.
6. Stop the NV test.


#### BasicAnalyzeNVTest.java
This sample demonstrates the use of the most basic NV methods.

First, the sample creates a _TestManager_ object and initializes it.  

The sample starts an NV test over an emulated “3G Busy” network.  

Next, the sample navigates to the home page in the HPE Network Virtualization website using the Selenium WebDriver.  
 
Finally, the sample stops the NV test, analyzes it, and prints the path of the analysis .zip file to the console.

##### BasicAnalyzeNVTest.java steps:
1. Create a _TestManager_ object and initialize it.
2. Start the NV test with the "3G Busy" network scenario.
3. Build the Selenium WebDriver.
4. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
5. Close and quit the Selenium WebDriver.
6. Stop and analyze the NV test and get the result as a .zip file.
7. Print the path of the .zip file to the console.


#### BasicComparisonWithoutNV.java
This sample demonstrates how NV helps you test your application under various network conditions. 
    
This test starts by navigating to the home page in the HPE Network Virtualization website using the Selenium WebDriver. This initial step runs without NV emulation and provides a basis for comparison.
    
Next, the sample starts an NV test configured with a "3G Busy" network scenario. The same step runs as before - navigating to the home page in the HPE Network Virtualization website - but this time, it does so over an emulated "3G Busy" network as part of an NV transaction.  
      
When the sample finishes running, it prints a summary to the console. This summary displays a comparison of the time it took to navigate to the site both with and without NV's network emulation. The results show that the slow "3G Busy" network increases the time it takes to navigate to the site, as you would expect.

##### BasicComparisonWithoutNV.java steps:
1. Build the Selenium WebDriver.
2. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
3. Close and quit the Selenium WebDriver.
4. Create and initialize the _TestManager_ object.
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


#### BasicAnalyzeNVTransactions.java
This sample demonstrates how to run transactions as part of an NV test.

In this sample, the NV test starts with the "3G Busy" network scenario, running three transactions (see below).
After the sample stops and analyzes the NV test, it prints the analysis .zip file path to the console.

This sample runs three NV transactions:  
1. "Home Page" transaction: Navigates to the home page in the HPE Network Virtualization website  
2. "Get Started" transaction: Navigates to the Get Started Now page in the HPE Network Virtualization website  
3. "Overview" transaction: Navigates back to the home page in the HPE Network Virtualization website  
	
##### BasicAnalyzeNVTransactions.java steps:
1. Create a _TestManager_ object and initialize it.
2. Start the NV test with the "3G Busy" network scenario.
3. Connect the NV test to the transaction manager.
4. Start the "Home Page" NV transaction.
5. Build the Selenium WebDriver.
6. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
7. Stop the "Home Page" NV transaction.
8. Start the "Get Started" NV transaction.
9. Click the __Get Started Now__ button using the Selenium WebDriver.
10. Stop the "Get Started" NV transaction.
11. Start the "Overview" NV transaction.
12. Click the __Overview__ button using the Selenium WebDriver.
13. Stop the "Overview" NV transaction.
14. Close and quit the Selenium WebDriver.
15. Stop and analyze the NV test and get the result as a .zip file.
16. Print the path of the .zip file to the console.


#### BasicAnalyze2Scenarios.java
This sample demonstrates a comparison between two network scenarios - "WiFi" and "3G Busy". 
  
In this sample, the NV test starts with the "WiFi" network scenario, running three transactions (see below). Then, the sample updates the NV test's network scenario to "3G Busy" using the real-time update API and runs the same transactions again.  
  
After the sample analyzes the NV test and extracts the transaction times from the analysis results, it prints a summary to the console. The summary displays the comparative network times for each transaction in both network scenarios. 

This sample runs three identical NV transactions before and after the real-time update:  
1. "Home Page" transaction: Navigates to the home page in the HPE Network Virtualization website  
2. "Get Started" transaction: Navigates to the Get Started Now page in the HPE Network Virtualization website  
3. "Overview" transaction: Navigates back to the home page in the HPE Network Virtualization website  
  
##### BasicAnalyze2Scenarios.java steps:
1. Create and initialize the _TestManager_ object.
2. Set the active adapter.
3. Start the NV test with the "WiFi" network scenario.
4. Connect the NV test to the transaction manager.
5. Start the "Home Page" NV transaction.
6. Build the Selenium WebDriver.
7. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
8. Stop the "Home Page" NV transaction.
9. Start the "Get Started" NV transaction.
10. Click the __Get Started Now__ button using the Selenium WebDriver.
11. Stop the "Get Started" NV transaction.
12. Start the "Overview" NV transaction.
13. Click the __Overview__ button using the Selenium WebDriver.
14. Stop the "Overview" NV transaction.
15. Close and quit the Selenium WebDriver.
16. Update the NV test in real time - update the network scenario to "3G Busy".
17. Rerun the transactions (repeat steps 5-11).
18. Stop the NV test.
19. Analyze the NV test and extract the network times for the NV transactions.
20. Print the network time comparison summary to the console.


### Advanced Samples
#### AdvAllTestClassMethods.java
This sample demonstrates all of the _Test_ class APIs except for the real-time update API, which is demonstrated in AdvRealtimeUpdate.java. You can start the test in this sample using either the NTX or Custom modes.

##### AdvAllTestClassMethods.java steps:
1. Create and initialize the _TestManager_ object.
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


#### AdvRealtimeUpdate.java
This sample demonstrates the real-time update API. You can use this API to update the test during runtime. For example, you can update the network scenario to run several "mini tests" in a single test.  
  
This sample starts by running an NV test with a single transaction that uses the "3G Busy" network scenario. Then the sample updates the network scenario to "3G Good" and reruns the transaction. You can update the test in real time using either the NTX or Custom real-time update modes.

##### AdvRealtimeUpdate.java steps:
1. Create and initialize the _TestManager_ object.
2. Set the active adapter.
3. Start the NV test with the "3G Busy" network scenario.
4. Connect the NV test to the transaction manager.
5. Start the "Home Page" NV transaction.
6. Build the Selenium WebDriver.
7. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
8. Stop the "Home Page" NV transaction.
9. Close and quit the Selenium WebDriver.
10. Update the NV test in real time - update the network scenario to "3G Good".
11. Rerun the transaction (repeat steps 5-9).
12. Stop the NV test.
13. Analyze the NV test and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
14. Print the NV transactions' network times or the path of the .zip file, if the --zip-result-file-path argument is specified.


#### AdvAllTestManagerClassMethods.java
This sample demonstrates all of the _TestManager_ class APIs. These APIs let you:
* initialize the _TestManager_ object to pass logon credentials, the NV server IP, the port, and so on
* set/get the NV configuration and active adapter
* get the running tests tokens  
* start/stop packet list capture
* get packet list information
* stop a specified array of tests or all of the running tests
* analyze a .shunra file, which is a compressed file that includes an events file, metadata, and packet lists 

##### AdvAllTestManagerClassMethods.java steps:
1. Create and initialize the _TestManager_ object.
2. Set the active adapter.
3. Get the active adapter and print its properties to the console (displayed only if the --debug argument is set to true).
4. Set the NV configuration.
5. Get the NV configuration and print its properties to the console (displayed only if the --debug argument is set to true).
6. Start the first NV test with "Flow1" - _view the sample's code to see the flow's properties_.
7. Connect the first NV test to the transaction manager.
8. Start the second NV test with "Flow2" - _view the sample's code to see the flow's properties_.
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
20. Stop the first NV test using the "stopTests" _TestManager_ class API.
21. Stop all tests using the "stopAllTests" _TestManager_ class API.
22. Analyze the specified .shunra file and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
23. Print the network times of the transactions in the .shunra file, or the path of the .zip file, if the --zip-result-file-path argument is specified.
24. Close and quit the Selenium WebDriver.


#### AdvMultipleTestsSequential.java
This sample shows how to run several tests sequentially with different network scenarios.
  
##### AdvMultipleTestsSequential.java steps:
1. Create and initialize the _TestManager_ object.
2. Set the active adapter.
3. Start the first NV test with the "3G Busy" network scenario.
4. Connect the first NV test to the transaction manager.
5. Start the "Home Page" NV transaction in the first NV test.
6. Build the Selenium WebDriver.
7. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
8. Stop the "Home Page" NV transaction in the first NV test.
9. Close and quit the Selenium WebDriver.
10. Stop the first NV test.
11. Analyze the first NV test and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
12. Print the NV transaction's network time or the location of the .zip file for the first test, if the --zip-result-file-path argument is specified.
13. Start the second NV test with the "3G Good" network scenario.
14. Connect the second NV test to the transaction manager.
15. Run the same transaction in the second test (repeat steps 5-9).
16. Stop the second NV test.
17. Analyze the second NV test and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
18. Print the NV transaction's network time or the location of the .zip file for the second test, if the --zip-result-file-path argument is specified.


#### AdvMultipleTestsConcurrent.java
This sample shows how to run several tests concurrently with different flow definitions. When running NV tests in parallel, make sure that:
* each test is configured to use MULTI_USER mode
* the include/exclude IP ranges in the tests' flows do not overlap - this ensures data separation between the tests
* your NV Test Manager license supports multiple flows running in parallel
  
##### AdvMultipleTestsConcurrent.java steps:
1. Create and initialize the _TestManager_ object.
2. Set the active adapter.
3. Start the first NV test with "Flow1" - _view the sample's code to see the flow's properties_.
4. Connect the first NV test to the transaction manager.
5. Start the second NV test with "Flow2" - _view the sample's code to see the flow's properties_.
6. Connect the second NV test to the transaction manager.
7. Start the "Home Page" NV transaction in the first test.
8. Start the "Home Page" NV transaction in the second test.
9. Build the Selenium WebDriver.
10. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
11. Stop the "Home Page" NV transaction in the first test.
12. Stop the "Home Page" NV transaction in the second test.
13. Stop the first NV test.
14. Stop the second NV test.
15. Analyze the first NV test and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
16. Print the NV transaction's network time or the location of the .zip file for the first test, if the --zip-result-file-path argument is specified.
17. Analyze the second NV test and retrieve the result as an object or as a .zip file, if the --zip-result-file-path argument is specified.
18. Print the NV transaction's network time or the location of the .zip file for the second test, if the --zip-result-file-path argument is specified.
19. Close and quit the Selenium WebDriver.


## Debugging
During a run session, the HPE Network Virtualization API Java library writes various messages to the __nv.log__ file. This log file is stored in the current directory under the __log__ folder (for example, _\hpe-nv-java-samples\scripts\basic\log_).  
  
In addition, each sample receives a --debug argument. You can pass __true__ to view debug messages in the console or pass __false__ to hide the debug messages.  

## License
```
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
```
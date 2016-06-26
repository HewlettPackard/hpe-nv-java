@echo off

  goto license-end
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
  :license-end

  goto comment
     adv_all_testmanager_class_methods.bat description:
     This batch file contains commands that run the AdvAllTestManagerClassMethods.java sample with different arguments.
     * The first command runs the sample without displaying debug messages in the console and stores the analysis results in a .zip file.
     * The second command, which is commented out, runs the sample and displays debug messages in the console. It also gets the analysis result as an object.
     * The third command, which is commented out, displays the list of supported arguments for the AdvAllTestManagerClassMethods.java sample.

     AdvAllTestManagerClassMethods.java description:
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
  :comment

 REM Run all TestManager class methods and store the analysis results in a .zip file.
 java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestManagerClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --analysis-ports 8080,80 --shunra-file-path sample.shunra --zip-result-file-path shunra-analysis-result.zip

 REM Run all TestManager class methods with debug messages and get the analysis result as an object.
 REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestManagerClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --analysis-ports 8080,80 --shunra-file-path sample.shunra --debug true

 REM Run the sample with the --help flag to get the list of supported arguments
 REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestManagerClassMethods --help

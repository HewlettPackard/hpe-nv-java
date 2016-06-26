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
    adv_multiple_tests_concurrent.bat description:
    This batch file contains commands that run the AdvMultipleTestsConcurrent.java sample with different arguments.
      * The first command runs the sample with the analysis result .zip file arguments and does not display debug messages in the console.
      * The second command, which is commented out, runs the sample and gets the analysis results as objects. It displays debug messages in the console.
      * The third command, which is commented out, runs the sample with the analysis result .zip file arguments and with ports configured for the first and second test flows.
      * The forth command, which is commented out, runs the sample and gets the analysis results as objects and with ports configured for the first and second test flows.
      * The fifth command, which is commented out, displays the list of supported arguments for the AdvMultipleTestsConcurrent.java sample.

    AdvMultipleTestsConcurrent.java description:
    This sample shows how to run several tests concurrently with different flow definitions.
    When running NV tests in parallel, make sure that:
    * each test is configured to use MULTI_USER mode
    * the include/exclude IP ranges in the tests' flows do not overlap---this ensures data separation between the tests

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
  :comment

REM Run test with analysis result .zip file arguments for the first and second tests
java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsConcurrent --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --first-zip-result-file-path analysis-result1.zip --second-zip-result-file-path analysis-result2.zip

REM Run test with analysis results as objects and with debug messages
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsConcurrent --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --debug true

REM Run test with analysis result .zip file arguments for the first and second tests and with ports for the first and second test flows
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsConcurrent --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --first-zip-result-file-path analysis-result1.zip --second-zip-result-file-path analysis-result2.zip --first-test-flow-tcp-port 9090 --second-test-flow-tcp-port 80

REM Run test with analysis results as objects and with ports for the first and second test flows
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsConcurrent --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --first-test-flow-tcp-port 8080 --second-test-flow-tcp-port 80

REM Run the sample with the --help flag to get the list of supported arguments
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsConcurrent --help
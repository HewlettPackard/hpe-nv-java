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
    adv_multiple_tests_sequential.bat description:
    This batch file contains commands that run the AdvMultipleTestsSequential.java sample with different arguments.
      * The first command runs the sample with the analysis result .zip file arguments and does not display debug messages in the console.
      * The second command, which is commented out, runs the sample and gets the analysis results as objects. It displays debug messages in the console.
      * The third command, which is commented out, displays the list of supported arguments for the AdvMultipleTestsSequential.java sample.

    AdvMultipleTestsSequential.java description:
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
:comment

REM Run test with analysis result .zip file arguments for the first and second tests
java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsSequential --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --first-zip-result-file-path analysis-result1.zip --second-zip-result-file-path analysis-result2.zip

REM Run test with analysis results as objects and with debug messages
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsSequential --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --debug true

REM Run the sample with the --help flag to get the list of supported arguments
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvMultipleTestsSequential --help
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
    basic_analyze_nv_transactions.bat description:
    This batch file contains commands that run the BasicAnalyzeNVTransactions.java sample with different arguments.
      * The first command runs the sample with the analysis result .zip file argument and without displaying debug messages in the console.
      * The second command, which is commented out, runs the sample with the analysis result .zip file argument and displays debug messages in the console.
      * The third command, which is commented out, displays the list of supported arguments for the BasicAnalyzeNVTransactions.java sample.

    BasicAnalyzeNVTransactions.java description:
    This sample demonstrates how to run transactions as part of an NV test.

    In this sample, the NV test starts with the "3G Busy" network scenario, running three transactions (see below).
    After the sample stops and analyzes the NV test, it prints the analysis .zip file path to the console.

    This sample runs three NV transactions:
    1. "Home Page" transaction: Navigates to the home page in the HPE Network Virtualization website
    2. "Get Started" transaction: Navigates to the Get Started Now page in the HPE Network Virtualization website
    3. "Overview" transaction: Navigates back to the home page in the HPE Network Virtualization website

    BasicAnalyzeNVTransactions.java steps:
    1. Create a TestManager object and initialize it.
    2. Start the NV test with the "3G Busy" network scenario.
    3. Connect the NV test to the transaction manager.
    4. Start the "Home Page" NV transaction.
    5. Build the Selenium WebDriver.
    6. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
    7. Stop the "Home Page" NV transaction.
    8. Start the "Get Started" NV transaction.
    9. Click the "Get Started Now" button using the Selenium WebDriver.
    10. Stop the "Get Started" NV transaction.
    11. Start the "Overview" NV transaction.
    12. Click the "Overview" button using the Selenium WebDriver.
    13. Stop the "Overview" NV transaction.
    14. Close and quit the Selenium WebDriver.
    15. Stop and analyze the NV test and get the result as a .zip file.
    16. Print the path of the .zip file to the console.
:comment

REM Run the sample with the analysis result .zip file argument
java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicAnalyzeNVTransactions --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --zip-result-file-path analysis-report-basic.zip

REM Run the sample with the analysis result .zip file argument and debug messages
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicAnalyzeNVTransactions --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --zip-result-file-path analysis-report-basic.zip --debug true

REM Run the sample with the --help flag to get the list of supported arguments
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicAnalyzeNVTransactions --help

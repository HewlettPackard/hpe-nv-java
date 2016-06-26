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
    basic_comparison_without_nv.bat description:
    This batch file contains commands that run the BasicComparisonWithoutNV.java sample with different arguments.
    * The first command runs the sample without displaying debug messages in the console.
    * The second command, which is commented out, runs the sample and displays debug messages in the console.
    * The third command, which is commented out, displays the list of supported arguments for the BasicComparisonWithoutNV.java sample.

    BasicComparisonWithoutNV.java description:
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
:comment

REM Run the sample without debug messages
java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicComparisonWithoutNV --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort

REM Run the sample with debug messages
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicComparisonWithoutNV --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --debug true

REM Run the sample with the --help flag to get the list of supported arguments
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicComparisonWithoutNV --help
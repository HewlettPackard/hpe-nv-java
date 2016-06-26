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
    adv_realtime_update.bat description:
    This batch file contains commands that run the AdvRealtimeUpdate.java sample with different arguments.
    * The first command runs the NV test, updates it in real-time using the Custom mode and outputs the analysis results as a .zip file. It does not display debug messages in the console.
    * The second command, which is commented out, runs the NV test, updates it in real-time using the Custom mode and outputs the analysis results as a .zip file. It displays debug messages in the console.
    * The third command, which is commented out, runs the NV test, updates it in real-time using the Ntx mode and gets the analysis results as an object. It does not display debug messages in the console.
    * The forth command, which is commented out, displays the list of supported arguments for the AdvRealtimeUpdate.java sample.

    AdvRealtimeUpdate.java description:
    This sample demonstrates the real-time update API. You can use this API to update the test during runtime.
    For example, you can update the network scenario to run several "mini tests" in a single test.

    This sample starts by running an NV test with a single transaction that uses the "3G Busy" network scenario. Then the
    sample updates the network scenario to "3G Good" and reruns the transaction. You can update the test in real time
    using either the NTX or Custom real-time update modes.

    AdvRealtimeUpdate.java steps:
    1. Create and initialize the TestManager object.
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
:comment

REM Run and update test in real-time in Custom mode with analysis result .zip file argument
java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvRealtimeUpdate --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --zip-result-file-path analysis-result.zip

REM Run and update test in real-time in Custom mode with analysis result .zip file argument and debug messages
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvRealtimeUpdate --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --zip-result-file-path analysis-result.zip --debug true

REM Run and update test in real-time in Ntx mode and get the analysis results as an object
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvRealtimeUpdate --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --ntx-file-path ntx_for_rtu.ntx

REM Run the sample with the --help flag to get the list of supported arguments
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvRealtimeUpdate --help
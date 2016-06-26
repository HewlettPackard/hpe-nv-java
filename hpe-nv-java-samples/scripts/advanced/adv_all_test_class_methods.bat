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
   adv_all_test_class_methods.bat description:
   This batch file contains commands that run the AdvAllTestClassMethods.java sample with different arguments.
   * The first command runs an NV test in Custom mode and does not display debug messages in the console.
   * The second command, which is commented out, runs an NV test in Custom mode and displays debug messages in the console.
   * The third command, which is commented out, runs an NV test in Custom mode with the packet list ID argument
   * The forth command, which is commented out, runs an NV test in Custom mode with the analysis result .zip file argument
   * The fifth command, which is commented out, runs an NV test in Ntx mode and does not display debug messages in the console.
   * The sixth command, which is commented out, runs an NV test in Ntx mode with SSL, and displays debug messages in the console.
   * The seventh command, which is commented out, displays the list of supported arguments for the AdvAllTestClassMethods.java sample.

   AdvAllTestClassMethods.java description:
   This sample demonstrates all of the Test class APIs except for the real-time update API, which is demonstrated in AdvRealtimeUpdate.java.
   You can start the test in this sample using either the NTX or Custom modes.

   AdvAllTestClassMethods.java steps:
   1. Create and initialize the TestManager object.
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
  :comment

  REM Run test in Custom mode
  java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --mode custom --analysis-ports 8080,80

  REM Run test in Custom mode with debug messages
  REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --mode custom --analysis-ports 8080,80 --debug true

  REM Run test in Custom mode with the packet list ID argument
  REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --mode custom --packet-list-id ID_CLIENT_PL_Flow1 --analysis-ports 8080,80

  REM Run test in Custom mode with the analysis result .zip file argument
  REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --mode custom --zip-result-file-path analysis-result.zip

  REM Run test in Ntx mode
  REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --mode ntx --ntx-file-path ntx_test.ntx

  REM Run test in Ntx mode with SSL and debug messages
  REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --mode ntx --ntx-file-path ntx_test.ntx --ssl true --debug true

  REM Run the sample with the --help flag to get the list of supported arguments
  REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.advanced.AdvAllTestClassMethods --help
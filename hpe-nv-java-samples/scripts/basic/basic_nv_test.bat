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
    basic_nv_test.bat description:
    This batch file contains commands that run the BasicNVTest.java sample with different arguments.
      * The first command runs the sample without displaying debug messages in the console.
      * The second command, which is commented out, runs the sample and displays debug messages in the console.
      * The third command, which is commented out, displays the list of supported arguments for the BasicNVTest.java sample.

    BasicNVTest.java description:
    This sample demonstrates the use of the most basic NV methods.

    First, the sample creates a TestManager object and initializes it.
    The sample starts an NV test over an emulated "3G Busy" network.
    ("3G Busy" is one of NV's built-in network profiles. A network profile specifies the
    network traffic behavior, including latency, packet loss, and incoming/outgoing bandwidth.
    Network profiles are used to emulate traffic over real-world networks.)

    Next, the sample navigates to the home page in the HPE Network Virtualization website using the Selenium WebDriver.

    Finally, the sample stops the NV test.

    BasicNVTest.java steps:
    1. Create a TestManager object and initialize it.
    2. Start the NV test with the "3G Busy" network scenario.
    3. Build the Selenium WebDriver.
    4. Navigate to: http://www8.hp.com/us/en/software-solutions/network-virtualization/index.html
    5. Close and quit the Selenium WebDriver.
    6. Stop the NV test.
:comment

REM Run the sample without displaying debug messages in the console
java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicNVTest --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort

REM Run the sample and display debug messages in the console
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicNVTest --server-ip 0.0.0.0 --server-port 8182 --username MyNVUser --password MyNVPassword --proxy MyProxyHost:MyProxyPort --debug true

REM Run the sample with the --help flag to get the list of supported arguments
REM java -cp ../../target/hpe-nv-java-samples-1.0.0.jar com.hpe.nv.samples.basic.BasicNVTest --help

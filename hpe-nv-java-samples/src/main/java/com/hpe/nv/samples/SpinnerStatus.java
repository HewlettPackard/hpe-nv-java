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

package com.hpe.nv.samples;

public class SpinnerStatus {

    private volatile boolean running = true;

    private static SpinnerStatus instance = null;
    protected SpinnerStatus() {
    }
    public static SpinnerStatus getInstance() {
        if(instance == null) {
            instance = new SpinnerStatus();
        }
        return instance;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void pauseThread() throws InterruptedException
    {
        running = false;
    }

    public void resumeThread()
    {
        running = true;
    }
}

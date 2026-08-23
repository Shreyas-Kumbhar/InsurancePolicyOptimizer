@echo off
cd /d "d:\Projects\InsurancePolicyOptimzer"
set JAVA_HOME=C:\Users\Admin\.jdks\ms-17.0.20.1
set MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd
"%MVN%" spring-boot:run

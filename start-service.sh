#!/bin/bash
echo Starting Error Reporting Service with PID: $$;
echo Usage:
echo 1. At the password prompt, enter the password for the secret key ring
echo 2. Send the service to background with CTRL+Z
echo 3. Restart the stopped job in the background by typing 'bg'
echo 4. Exit from shell
echo 5. Verify service is still running by checking
echo ..   TEST: http://localhost:9191/error-reporting/public-key   or
echo ..   LIVE: https://multibit.org/error-reporting/public-key
echo ..
echo TIP: You can find this process again by typing 'ps -A | grep error-reporting'
echo TIP: If 'NoClassDefFoundError: ... BouncyCastleProvider' copy bcprov-jdk16-1.46.jar to project root
java -cp "bcprov-jdk16-1.46.jar;target/error-reporting-service-develop-SNAPSHOT.jar" org.multibit.hd.error_reporting.ErrorReportingService server config.yml
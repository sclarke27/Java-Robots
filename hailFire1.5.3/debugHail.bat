rm hailRobot.sig
rm hailRobot.bin

set CLASSPATH = %CLASSPATH%;./bin

lejoslink -verbose org.hailfire.BotMain -o botCore.bin
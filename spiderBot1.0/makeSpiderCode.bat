rm -r .\bin
mkdir .\bin
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/stamp/math/UnsignedIntMath.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/stamp/peripheral/servo/psc/psc.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/org/SpiderBot/io/SerialLCD.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/org/SpiderBot/sensors/Ping.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/org/SpiderBot/core/SpiderActions.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/org/SpiderBot/core/Servos.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderBot1.0\lib;H:\spiderbot backup\spiderBot1.0\bin" ./src/org/SpiderBot/SpiderMain.java +U


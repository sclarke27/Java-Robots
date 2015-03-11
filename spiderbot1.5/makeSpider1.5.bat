rm -r .\bin
mkdir .\bin
jikes -depend -d .\bin -classpath "H:\spiderbot backup\spiderbot1.5\lib;H:\spiderbot backup\spiderbot1.5\bin" ./src/org/SpiderBot/SpiderMain.java +U +OLDCSO


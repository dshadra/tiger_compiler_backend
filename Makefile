DIR=cs8803_bin

all: setup antlr compile manifest jar

setup:
	mkdir -p  cs8803_bin

antlr:
	antlr -listener IR.g4

compile:
	javac -cp /usr/local/lib/antlr-4.11.1-complete.jar *.java

manifest:
#	touch MANIFEST.MF
#    @echo "Manifest-Version: 1.0" >> MANIFEST.MF
# 	@echo "Class-Path: ." >> MANIFEST.MF
#    @echo "Main-Class: Main" >> MANIFEST.MF

jar:
	jar -f tigerc.jar -m MANIFEST.MF -c *.class
	cp tigerc.jar ./cs8803_bin

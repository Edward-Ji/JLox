PACKAGE = "com/interpreter/jlox"
MAIN = "Lox"
PACKAGE_TOOL = "com/interpreter/tool"
MAIN_TOOL = "GenerateAst"

all: build run

build:
	javac $(PACKAGE)/*.java

run:
	java $(PACKAGE)/$(MAIN)

codegen:
	javac $(PACKAGE_TOOL)/*.java
	java $(PACKAGE_TOOL)/$(MAIN_TOOL) $(PACKAGE)
	rm $(PACKAGE_TOOL)/*.class

clean:
	rm PACKAGE/*.class

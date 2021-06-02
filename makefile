PACKAGE = "com/interpreter/jlox"
MAIN = "Lox"
PACKAGE_TOOL = "com/interpreter/tool"
MAIN_TOOL = "GenerateAst"
TEST_SCRIPT = "parse_test1.lox"

all: build run

build:
	javac $(PACKAGE)/*.java

run:
	java $(PACKAGE)/$(MAIN)

runscript:
	java $(PACKAGE)/$(MAIN) $(TEST_SCRIPT)

codegen:
	javac $(PACKAGE_TOOL)/*.java
	java $(PACKAGE_TOOL)/$(MAIN_TOOL) $(PACKAGE)
	rm $(PACKAGE_TOOL)/*.class

clean:
	rm PACKAGE/*.class

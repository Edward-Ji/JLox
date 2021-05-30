PACKAGE = "com/interpreter/jlox"
MAIN = "Lox"
PACKAGE_TOOL = "com/interpreter/tool"
MAIN_TOOL = "GenerateAst"

all: build

build:
	javac $(PACKAGE)/*.java
	java $(PACKAGE)/$(MAIN)

run:
	java $(PACKAGE)/$(MAIN)

gen:
	javac $(PACKAGE_TOOL)/*.java
	java $(PACKAGE_TOOL)/$(MAIN_TOOL) $(PACKAGE)

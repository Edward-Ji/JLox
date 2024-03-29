#
# source: https://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html
#

#
# define compiler and compiler flag variables
#

JFLAGS = -g
JC = javac
JVM = java

JCLASSDIR = com/interpreter/jlox
JTOOLDIR = com/interpreter/tool

MAIN = $(JCLASSDIR)/Lox
SCRIPT = test.lox

#
# Clear any default targets for building .class files from .java files; we
# will provide our own target entry to do this in this makefile.
# make has a set of default targets for different suffixes (like .c.o)
# Currently, clearing the default for .java.class is not necessary since
# make does not have a definition for this target, but later versions of
# make may, so it doesn't hurt to make sure that we clear any default
# definitions for these
#

.SUFFIXES: .java .class


#
# Here is our target entry for creating .class files from .java files
# This is a target entry that uses the suffix rule syntax:
#	DSTS:
#		rule
#  'TS' is the suffix of the target file, 'DS' is the suffix of the dependency
#  file, and 'rule'  is the rule for building a target
# '$*' is a built-in macro that gets the basename of the current target
# Remember that there must be a < tab > before the command line ('rule')
#

.java.class:
	$(JC) $(JFLAGS) $*.java

#
# CLASSES is a macro consisting of 4 words (one for each java source file)
#

CLASSES = \
	$(JCLASSDIR)/AstPrinter.java \
	$(JCLASSDIR)/Environment.java \
	$(JCLASSDIR)/Expr.java \
	$(JCLASSDIR)/Interpreter.java \
	$(JCLASSDIR)/Lox.java \
	$(JCLASSDIR)/Parser.java \
	$(JCLASSDIR)/RuntimeError.java \
	$(JCLASSDIR)/Stmt.java \
	$(JCLASSDIR)/Token.java \
	$(JCLASSDIR)/TokenType.java

GENERATE = $(JTOOLDIR)/GenerateAst

#
# the default make target entry
#

default: classes


#
# This target entry uses Suffix Replacement within a macro:
# $(name:string1=string2)
# 	In the words in the macro named 'name' replace 'string1' with 'string2'
# Below we are replacing the suffix .java of all words in the macro CLASSES
# with the .class suffix
#

classes: $(CLASSES:.java=.class)

generate: $(GENERATE).java
	$(JC) $(JFLAGS) $(GENERATE).java
	$(JVM) $(GENERATE) $(JCLASSDIR)

#
# RM is a predefined macro in make (RM = rm -f)
#

run:
	$(JVM) $(MAIN)

runscript:
	$(JVM) $(MAIN) $(SCRIPT)

clean:
	$(RM) *.class

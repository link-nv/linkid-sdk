README for SafeOnline
=====================

=== 1. Overview

An overview of the SafeOnline product can be found at:
http://buildserver/twiki/bin/view/Products/SafeOnline


=== 2. Requirements

Java 1.6.0	Compilation
Maven 2.0.7	Building


=== 3. Build

You can build the entire project via:
	mvn clean install
If you run into an OutOfMemoryError while running Maven you can use the
following environment variable to increase the available memory:
export MAVEN_OPTS="-Xms256m -Xmx768m -XX:PermSize=256m -XX:MaxPermSize=256m"


=== 4. Documentation

Each sub-project contains documentation in the form of a Maven site.
Enter a project and do
	mvn site
to generate a project site. View via (Linux):
	firefox target/site/index.html
under Windows view via:
	start target\site\index.html


=== 5. Getting Started

There is an overall documentation project: safe-online-documentation.
Go there and do:
	mvn site
	firefox target/site/index.html
under Windows view the resulting site via:
	start target\site\index.html

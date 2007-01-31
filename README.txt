README for SafeOnline
=====================

=== 1. Overview

An overview of the SafeOnline product can be found at:
http://buildserver/twiki/bin/view/Products/SafeOnline


=== 2. Requirements

Java 1.5.0	Compilation
Maven 2.0.4	Building


=== 3. Build

You can build the entire project via:
	mvn clean install


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

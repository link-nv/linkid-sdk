README for SafeOnline Demo Ticket Client
========================================

=== 1. Overview

This project holds the source code for the WinCE client application of the
SafeOnline Demo Ticket. This client application only runs on Zineon 2 devices.


=== 2. Requirements

Microsoft .NET Compact Framework 2.0 SP1 - free C# compiler
Microsoft .NET 2.0 Framework SDK (free, includes .NET CF = Compact Framework)
NAnt 0.85 - build system

Optional:
SharpDevelop 2.1 - IDE (free open source)


=== 3. Build

Execute the following command:

    nant clean build


=== 4. Installation

Just copy demo-cinema-client.exe onto an SD or Flash memory card.


=== 5. Usage

A connection will be made to the demo cinema service on port 8080.
Make sure the firewall on the server allows those incoming connections.

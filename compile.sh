#!/bin/bash

scalac -d out -classpath ./out ./src/ServerThread.scala
scalac -d out -classpath ./out ./src/EchoServer.scala

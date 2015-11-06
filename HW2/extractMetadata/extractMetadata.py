#!/usr/bin/env python2.7
import os

imageTypes=[".jpg",".jpeg",".gif",".png",".JPG",".JPEG",".GIF",".PNG"]
fileOutput=open('allMetadata.txt', 'w')
os.popen("cd /Users/sx/Desktop/CSCI572/Assignments/Assignment2")   
dir="/Users/sx/Desktop/CSCI572/Assignments/Assignment2/THIS"
def search(dir):
    list = os.listdir(dir)  
    for line in list:
        filepath = os.path.join(dir,line)
        if os.path.isdir(filepath):
            search(filepath)
        elif os.path.splitext(filepath)[1]==".html" or os.path.splitext(filepath)[1] in imageTypes:   
            path=filepath+line + '\n' 
            command="java -classpath ctakes-config:tika-app-1.10.jar:apache-ctakes-3.2.2/desc:apache-ctakes-3.2.2/resources:apache-ctakes-3.2.2/lib/\* org.apache.tika.cli.TikaCLI --config=ctakes-config/tika-config.xml -m "+filepath 
            result=os.popen(command).read()
            start=result.find("Content-Length:")
            #end=result.find("Last-Modified:")
            result=result[start:]
            fileOutput.write(filepath+'\n')
            fileOutput.write(result+'\n'+'---------------------------------'+'\n')         
search(dir)



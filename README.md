## akka-streams-project
This project processes the streams of records, transform and saves output.

## Problem:
<pre>
There are two separate infinite sources of data. 
  
Data is arriving "simultaneously" into our system as two streams via two channels: Channel 1 and Channel 2.
    
The data could be of three types: R, G and B. 
  
Each data element should have two properties: channelNumber and uniqueID.
  
These three types of Data are arriving in a random sequence on the two channels.
  
Write a program which creates pairs of "same types" arriving on two channels in their "order of arrival".
  
    For example, if a sample sequence is as follows:
Channel 1: R1_1 R1_2 R1_3 B1_4 B1_8 G1_5
Channel 2: B2_6 B2_8 R2_9 G2_10 B2_7 R2_20
  
    Then the expected output is:
    (R1_1, R2_9) (B1_4, B2_6) (B1_8, B2_8) (G1_5, G2_10) (R1_2, R2_20) 
</pre>

## The Graph of the stream processing
![The Graph of the stream processing](https://raw.githubusercontent.com/ashishtomer/akka-streams-project/media-branch/akka-stream-flow.png)

### Steps to run application
Execute <code>sbt run</code><br/>
Hit enter anytime on console to shutdown application.<br/>

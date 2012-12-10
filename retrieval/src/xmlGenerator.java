/* 

Ignacio Perez
Assignment #3


 * 
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.util.Iterator;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList; 
/**
 * <p>
 * This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 * 
 * * @author Ignacio Perez, based on the work of Yusuke Yamamoto * @author
 * Yusuke Yamamoto - yusuke at mac.com
 * 
 */
public final class xmlGenerator {
	public static void main(String[] args) {
		try {

			final Hashtable <String,Document> dailyconversation = new Hashtable <String,Document>();
			final Hashtable <String,Document> dailytopic = new Hashtable <String,Document>();
			final Hashtable <String,String> topicsconversation = new Hashtable <String,String>();
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DOMImplementation domImpl = docFactory.newDocumentBuilder().getDOMImplementation();
			//DocumentBuilder docBuilder = docFactory.newDocumentBuilder().getDOMImplementation();

			//reading the categories per conversation
			BufferedReader reader = new BufferedReader(new FileReader("../data/ConversationAnalysisResults/part-r-00000"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] elements = line.split("\t");
				String conversationid = elements[0].trim();
				String topic = elements[1].trim();
				double prob = Double.parseDouble(elements[2].trim());
				
				String newtopics;

				if(!topicsconversation.containsKey(conversationid))
				{
					topicsconversation.put(conversationid, "");
				}
							
				newtopics  = (String) topicsconversation.get(conversationid);
				
				if(prob > 0.7)
				{
					newtopics=newtopics+","+topic;
				}

				topicsconversation.put(conversationid, newtopics);
			}

			
			
			
			// reading the conversations
			reader = new BufferedReader(new FileReader("../data/coreysConversations20120605.txt"));
			
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				String[] elements = line.split("\t");

				//extracting the reference date
				//System.out.println(elements[2].substring(0, 10).replaceAll("/", "-")); 

				String date = elements[2].substring(0, 10).replaceAll("/", "-");
				String fulldate = elements[2].replaceAll("/", "-");
				String conversationid = elements[1];
				String userName = elements[4];
				String TweetId = elements[5];
				String TweetText = elements[6];

				//System.out.println(elements[2].substring(0, 10).replaceAll("/", "-"));

				if(!dailyconversation.containsKey(date))
				{
					Document doc =  domImpl.createDocument(null, "datesummary", null);
					Element rootElement = doc.getDocumentElement();			
					rootElement.setAttribute("ID",date);
					//System.out.println("attribute: "+rootElement.getAttribute("ID"));
					dailyconversation.put(date,doc);
				}

				Document docToFulfill = dailyconversation.get(date);

				Element rootdate = docToFulfill.getDocumentElement();

				NodeList nl = rootdate.getChildNodes();

				Element conversation= null;

				for(int j=0;j<nl.getLength();j++)
				{
					Element el = (org.w3c.dom.Element) nl.item(j);
					if(el.getAttribute("ID").equals(conversationid))
					{
						conversation=el;
					}			
				}

				if (conversation == null)
				{
					conversation =  docToFulfill.createElement("conversation");
					conversation.setAttribute("ID", conversationid);

				}

				Element newTweet = docToFulfill.createElement("tweet");
				newTweet.setAttribute("ID", TweetId);

				Element textTweet = docToFulfill.createElement("text");
				textTweet.setTextContent(TweetText);
				newTweet.appendChild(textTweet);

				Element tweetDate = docToFulfill.createElement("tweetDate");
				tweetDate.setTextContent(fulldate);
				newTweet.appendChild(tweetDate);

				Element userTweet = docToFulfill.createElement("tweetUser");
				userTweet.setTextContent(userName);
				newTweet.appendChild(userTweet);
				
				
				//rootdate.removeChild(conversation);
				conversation.appendChild(newTweet);
				rootdate.appendChild(conversation);
				//rootdate.appendChild(conversation);
				dailyconversation.put(date,docToFulfill);
			}
			reader.close();

			reader = new BufferedReader(new FileReader("../data/_ConversationscoreyhyllestedExtraData0.txt"));
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				String[] elements = line.split("\t");

				String date = elements[0].substring(0, 10).replaceAll("/", "-");
				//String fulldate = elements[0].replaceAll("/", "-");
				String conversationid = elements[1];
				String type = elements[2];
				String text = elements[3];

				Document docToFulfill = dailyconversation.get(date);
				
				if(docToFulfill==null) continue;
				
				Element rootdate = docToFulfill.getDocumentElement();
				NodeList nl = rootdate.getChildNodes();
				Element conversation= null;

				for(int j=0;j<nl.getLength();j++)
				{
					Element el = (org.w3c.dom.Element) nl.item(j);
					if(el.getAttribute("ID").equals(conversationid))
					{
						conversation=el;
					}			
				}
				if (conversation != null)
				{
					nl = conversation.getChildNodes();
					Element entities = null;
					for(int j=0;j<nl.getLength();j++)
					{
						Element el = (org.w3c.dom.Element) nl.item(j);
						if(el.getNodeName().equals("entities"))
						{
							entities=el;
							break;
						}			
					}

					if(entities == null)
					{
						entities = docToFulfill.createElement("entities");
						String topics = null;
						//System.out.println(" retrieving _"+conversationid+"_");
						if(topicsconversation.containsKey(conversationid))
						{
							topics = topicsconversation.get(conversationid);							
						}
						
						Element topicToXML;
						if(topics==null)
						{
							//System.out.println("topic is null");
							topicToXML = docToFulfill.createElement("topic");
							topicToXML.setTextContent("other");	
							entities.appendChild(topicToXML);
						}
						else if(topics.length()==0)
						{
							//System.out.println("topic is empty!!!");
							topicToXML = docToFulfill.createElement("topic");
							topicToXML.setTextContent("other");	
							entities.appendChild(topicToXML);
						}
						else	
						{
							String [] listOfTopics = topics.split(",");
							for(int j=0;j<listOfTopics.length;j++)
							{
								if(listOfTopics[j].isEmpty()) continue;
								//System.out.println("topic tenia algo");
								topicToXML = docToFulfill.createElement("topic");
								topicToXML.setTextContent(listOfTopics[j]);	
								entities.appendChild(topicToXML);								
							}
						}
					}

					Element newEntity = docToFulfill.createElement(type);
					if(type.compareTo("ConvoScore")==0) text=text.substring(0,2);
					
					newEntity.setTextContent(text);
										
					if(type.compareTo("ConvoScore")==0) conversation.appendChild(newEntity);
					
					Element child =  null;
					nl = entities.getChildNodes();
					for(int j=0;j<nl.getLength();j++)
					{
						Element el = (org.w3c.dom.Element) nl.item(j);
						if(el.getTextContent().equals(text))
						{
							child=el;
							break;
						}			
					}
					if(child==null)
					{	
						entities.appendChild(newEntity);
					}
					conversation.appendChild(entities);
					dailyconversation.put(date,docToFulfill);
				}
			}
			reader.close();
			
			
			reader = new BufferedReader(new FileReader("../data/timelineAnalysisResults/part-r-00000"));
			while ((line = reader.readLine()) != null) {
				String[] elements = line.split("\t");
				String date = elements[4].substring(0, 10).replaceAll("/", "-");
				String topic = elements[1].trim();
				String keyid = date+topic;
				String text = elements[5].trim();
				String user = elements[6].trim();
				
				double prob = Double.parseDouble(elements[2].trim());
				
				if(prob>0.7)
				{
					if(!dailytopic.containsKey(keyid))
					{
						Document doc =  domImpl.createDocument(null, "tweets", null);
						Element rootElement = doc.getDocumentElement();			
						rootElement.setAttribute("ID",date);
						//System.out.println("attribute: "+rootElement.getAttribute("ID"));
						dailytopic.put(keyid,doc);
					}

					Document docToFulfill = dailytopic.get(keyid);
					Element rootdate = docToFulfill.getDocumentElement();
					Element tweet = docToFulfill.createElement("tweet");
					Element userel = docToFulfill.createElement("user");
					Element textel = docToFulfill.createElement("text");
					
					userel.setTextContent(user);
					textel.setTextContent(text);
					tweet.appendChild(userel);
					tweet.appendChild(textel);
					rootdate.appendChild(tweet);
					dailytopic.put(keyid,docToFulfill);
				}
			}			
			
			//System.out.println(topicsconversation.toString());
			reader.close();
			
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			
			Enumeration<String> e = dailyconversation.keys();
			while(e.hasMoreElements())
			{
				String date = e.nextElement();

				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult(sw);
				DOMSource source = new DOMSource(dailyconversation.get(date));
				trans.transform(source, result);
				String xmlString = sw.toString();

				BufferedWriter writer = new BufferedWriter(new FileWriter("../data/Day"+date+".xml"));
				//writer.write("<?xml-stylesheet type=\"text/xsl\" href=\"conversation.xsl\" ?>"+xmlString);
				writer.write("<?xml version='1.0'?>\n"+xmlString.trim());
				writer.close();

				try {
					String inXML = "../data/Day"+date+".xml";
			        String inXSL = "../data/conversation.xsl";
			        String outTXT = "../data/Day"+date+".html";
					transformtoHTML(inXML,inXSL,outTXT);
				} catch(TransformerConfigurationException e1) {
					System.err.println("Invalid factory configuration");
					System.err.println(e1);
				} catch(TransformerException e1) {
					System.err.println("Error during transformation");
					System.err.println(e1);
				}

			}
			
			
			e = dailytopic.keys();
			while(e.hasMoreElements())
			{
				String date = e.nextElement();

				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult(sw);
				DOMSource source = new DOMSource(dailytopic.get(date));
				trans.transform(source, result);
				String xmlString = sw.toString();
				
				System.out.println("wrinting xml");
				BufferedWriter writer = new BufferedWriter(new FileWriter("../data/tweetsperday"+date+".xml"));
				//writer.write("<?xml-stylesheet type=\"text/xsl\" href=\"conversation.xsl\" ?>"+xmlString);
				writer.write("<?xml version='1.0'?>\n"+xmlString.trim());
				writer.close();

				try {
					String inXML = "../data/tweetsperday"+date+".xml";
			        String inXSL = "../data/tweets.xsl";
			        String outTXT = "../data/tweetsperday"+date+".html";
					transformtoHTML(inXML,inXSL,outTXT);
				} catch(TransformerConfigurationException e1) {
					System.err.println("Invalid factory configuration");
					System.err.println(e1);
				} catch(TransformerException e1) {
					System.err.println("Error during transformation");
					System.err.println(e1);
				}

			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void transformtoHTML(String inXML,String inXSL,String outTXT)
			throws TransformerConfigurationException,
			TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		StreamSource xslStream = new StreamSource(inXSL);
		Transformer transformer = factory.newTransformer(xslStream);
		StreamSource in = new StreamSource(inXML);
		StreamResult out = new StreamResult(outTXT);
		transformer.transform(in,out);
		System.out.println("The generated XML file is:" + outTXT);
	}	




}



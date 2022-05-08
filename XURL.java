/*  
    Script for counting all valid external URLs on a webpage or an HTML file

    Compile command: 
    javac -cp guava-31.1-jre.jar XURL.java
    Run command (example):
    java -cp guava-31.1-jre.jar XURL.java https://arstechnica.com/ ./sample.html https://www.medium.com/

    LIMITATION: Requires Java to be installed
    NOTE: Dependencies are provided as jars and reflected in the compile command above

    LIMITATION DIFFERENT FROM PYTHON VERSION:
        The Java implementation of fetching raw HTML from a URL seems have certain social links missing. 
        Most like due to the social links being generated after the full page has loaded.
        This results, in some cases, for the Java program to report less external links than the python counterpart
        of this program(xurl.py).

    Requires additional libraries - possible conflict if runtime size/ memory capacity constraints exist
    Published also on my github page at github.com/rezwanhaleem
*/

package xurl;

import com.google.common.net.InternetDomainName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class XURL {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No URLs provided. Stopping script.");
            return;
        }

        ArrayList<Integer> count_list = new ArrayList<Integer>(args.length);

        for (String url : args) {
            String domain = "", html = "";

            System.out.print("Parsing HTML of " + url + "\r");

            if (url.endsWith(".html")) {
                // If url is a file then it extracts name of website, excluding the extension of
                // the file ( For use with finding only external links)
                StringBuilder contentBuilder = new StringBuilder();
                try {
                    BufferedReader in = new BufferedReader(new FileReader(url));
                    String str;
                    while ((str = in.readLine()) != null) {
                        contentBuilder.append(str);
                    }
                    in.close();
                } catch (Exception e) {
                    System.out.println("Error opening file " + url + ". File may not exist");
                    count_list.add(0);
                    continue;
                }

                html = contentBuilder.toString();

                // Removes extension
                domain = url.split("\\.html")[0];
                // Gets basename
                domain = domain.substring(domain.lastIndexOf('\\') + 1);
                domain = domain.substring(domain.lastIndexOf('/') + 1);

            } else {
                StringBuffer buffer = new StringBuffer();
                try {
                    URL url_content = new URL(url);
                    InputStream is = url_content.openStream();
                    int ptr = 0;
                    while ((ptr = is.read()) != -1) {
                        buffer.append((char) ptr);
                    }
                    is.close();

                    // Gets TLD
                    domain = url_content.getHost();
                    // Extracts only the top level domain using InternetDomainName Class from Guava
                    // libary
                    domain = InternetDomainName.from(domain).topDomainUnderRegistrySuffix().toString();
                } catch (Exception e) {
                    System.out.println("Error opening " + url + ". URL syntax maybe wrong");
                    count_list.add(0);
                    continue;
                }

                html = buffer.toString();
            }

            if (html.isEmpty()) {
                System.out.println(url + " Invalid HTML. Unable to parse");
                count_list.add(0);
                continue;
            }

            // Overwriting load statement for html parsing
            String space= new String(new char[url.length() + 16]).replace('\0', ' ');
            System.out.print(space + "\r");

            int count = 0;
            ArrayList<String> link_list = new ArrayList<String>();

            // Parses all links from <a href=""> tags without duplicates using regex
            String regex = "href\\s?=\\s?\"([^\"]+)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                String link = matcher.group(1);
                if (!link_list.contains(link))
                    link_list.add(link);
            }

            // Only includes links from a different domain
            Iterator<String> iter = link_list.iterator();
            while (iter.hasNext()) {
                String link = iter.next();
                if (!link.startsWith("http") || link.contains(domain) )
                    iter.remove();
            }

            // Only counts valid URLs while showing a progress bar (Since fetching each link takes time)
            if (!link_list.isEmpty()) {
                int n = link_list.size();
                for (int i = 0; i < n; i ++) {
                    String link = link_list.get(i);
                    
                    // For progress display. Not essential, only for aesthetic purposes ( Does not require external libary )
                    int prog = (int)(((double)(i+1)/(double)n)*100.0);
                    String bar = new String(new char[prog]).replace('\0', '\u2588') + 
                        new String(new char[100 - prog]).replace('\0', '.');
                    System.out.print("Progress on " + url + " : [" + bar + "] " + (i+1) + "/" + n + "\r");

                    try{ 
                        URL link_url = new URL(link);
                        HttpURLConnection http = (HttpURLConnection) link_url.openConnection();
                        if( http.getResponseCode() == 200 )
                            count++;
                    } catch ( Exception e ) {
                        System.out.println("Invalid URL: " + link);
                    }

                    space= new String(new char[url.length() + 135]).replace('\0', ' ');
                    System.out.print(space + "\r");
                }
            }

            count_list.add(count);
        }

        int n = count_list.size();
        for (int i = 0; i < n; i ++) {
            System.out.println(args[i] + " " + count_list.get(i));
        }
    }
}

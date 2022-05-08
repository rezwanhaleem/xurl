#!/usr/bin/python3.5
"""
    Script for counting all valid external URLs on a webpage or an HTML file

    Run command (example):
    python xurl.py https://arstechnica.com/ ./sample.html https://www.medium.com/

    LIMITATION: Requires Python 3 to be installed
    NOTE: All dependencies can easily be insalled via the commad "pip install -r requirements.txt"
    ^This automatically installs all the libraries need for this script listed below 
    LIMITATION (CHECK NOTE): Requires Requests module to be installed (pip install requests)
    LIMITATION (CHECK NOTE): Requires tldextract  module to be installed (pip install tldextract) - Required for proper TLD determination

    Requires additional libraries - possible conflict if runtime size/ memory capacity constraints exist
    Published also on my github page at github.com/rezwanhaleem
"""

import sys
import os
import requests
import re 
import tldextract

# For progress display. Not essential, only for aesthetic purposes ( Does not require external libary )
def progressbar(it, prefix="", size=60, out=sys.stdout): # Python3.3+
    count = len(it)
    def show(j):
        x = int(size*j/count)
        print("{}[{}{}] {}/{}".format(prefix, u"█"*x, "."*(size-x), j, count), 
                end='\r', file=out, flush=True)
    show(0)
    for i, item in enumerate(it):
        yield item
        show(i+1)
    print(int(size * 1.5) * " ",flush=True, file=out, end='\r')

# Main program
url_list = sys.argv[1:]

if not url_list:
    sys.exit("No URLs provided. Stopping script.")

count_list = []

for url in url_list:
    domain = ''
    html = ''

    print("Parsing HTML of " + url, end='\r')
    if os.path.exists(os.path.dirname(url)):
        # If url is a file then it extracts name of website, excluding the extension of the file ( For use with finding only external links)
        domain = os.path.splitext(os.path.basename(url))[0]
        try:
            html = open(url, "r").read()
        except:
            print("Error opening file " + url + ". File may not exist")
            count_list.append(0)
            continue
    else:
        # Extracts only the top level domain
        extract = tldextract.extract(url)
        domain = "{}.{}".format(extract.domain, extract.suffix)
        try:
            html = requests.get(url).text
        except:
            print("Error opening " + url + ". URL syntax maybe wrong")
            count_list.append(0)
            continue

    if html == '':
        print(url + " Invalid HTML. Unable to parse")
        count_list.append(0)
        continue

    count = 0
    # Parses all links from <a href=""> tags using regex
    link_list = re.findall("href\\s?=\\s?\"([^\"]+)\"",html)
    # Prunes duplicates
    link_list = list(set(link_list))

    # Only includes links from a different domain
    # Creating new list is more efficient vs calling remove function of list with Python lists
    external_links = []
    for link in link_list:
        link_extract = tldextract.extract(link)
        link_domain = "{}.{}".format(link_extract.domain, link_extract.suffix)

        if link.startswith("http") and (not domain in link_domain):
            external_links.append(link)

    # Only counts valid URLs while showing a progress bar (Since fetching each link takes time)
    if external_links:
        for link in progressbar(external_links, "Progress on " + url + " : ", 100):
            try:
                if requests.get(link).status_code == 200:
                    count += 1
            except:
                print("Invalid URL: " + link)

    count_list.append(count)

for i, count in enumerate(count_list):
    print(url_list[i] + " " + str(count))


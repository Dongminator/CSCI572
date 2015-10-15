import  nutchpy
# export PATH=/Users/Jian/miniconda/bin:$PATH
# /Users/Jian/nutchpy/seqreader-app/src/main/java/com/continuumio/seqreaderapp/RecordIterator.java
# public void remove(){
#     }
# export JAVA_HOME="$(/usr/libexec/java_home -v 1.7)"
#  crawl/segments/20151008202503
# export PATH=/Users/Jian/apache-maven-3.3.3/bin:$PATH
path = "/Users/Jian/nutch/runtimes/local/crawl/crawldb/current/part-00000/data"
data = nutchpy.sequence_reader.read(path)
outf = open("output.txt",'w')
mime = []
# detail = "Metadata: Content-Type=image/html _pst_=moved(12), lastModified=0:"
# imageMime = detail[detail.find("Content-Type=image/") + 19 : detail.find("_pst_=") - 1]
# print imageMime
for list_item in data:
    # print(list_item[0]) # Prints the url
    # print(list_item[1]) # Prints details abt the url
    detail = list_item[1].strip()
    if detail[detail.find("Metadata:"): ] != "Metadata:":
        metadata = detail[detail.find("Metadata:") + 9 : ]
        # print  metadata.find("(")
        httpCode = metadata[metadata.find("(") + 1: metadata.find(")")]
        httpResponse = metadata[metadata.find("_pst_="): metadata.find(")") + 1]
        status = detail[detail.find("Status"): detail.find(")", 1, 100) + 1]
        # if httpCode != "1" and list_item[0].find("backpage") == -1 and status.find("db_redir_temp") > 0:
        #     print("httpResponse   " + httpResponse + "  " + status + "  URL   "  + list_item[0]  + "\n")
        if detail.find("Content-Type=image/") > 0:
            imageMime = detail[detail.find("Content-Type=image/") + 19: detail.find("_pst_=") - 1]
            print imageMime
            if imageMime not in mime:
                mime.append(imageMime)
print str(mime)


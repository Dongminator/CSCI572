# a faster version of multi-downloadurl.py
# get rid of output information, mime type counts and thread locks

import threading
import urllib.request
import shutil
import time
import os

start = time.time()
suffix = ["jpg","JPG","png","PNG","bmp","BMP","gif","GIF","jpeg","JPEG"]
newpath = "images" 
if not os.path.exists(newpath): os.makedirs(newpath)
inf = open("image_urls",'r')

class myThread (threading.Thread):
    def __init__(self, threadID, name, infile):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.infile = infile
    def run(self):
        print ("Starting " + self.name)
        download_url(self.name, self.infile, self.threadID)
        print ("Exiting " + self.name)

def download_url(threadName, infile, threadID):
    prefix = str(chr(ord('a')+threadID))
    sufcount = [0,0,0,0,0,0,0,0,0,0]
    line = infile.readline()
    while len(line)>0:
        url = line
        try:
            response = urllib.request.urlopen(url)
        except urllib.error.HTTPError as e:
            line = infile.readline()
            continue
        except urllib.error.URLError as e:
            line = infile.readline()
            continue
        token = line.split('.')
        for idx,suf in enumerate(suffix):
            if suf+"\n" == token[-1]:
                sufcount[idx] += 1
                file_name = prefix+str(sufcount[idx])+"."+suf             
                with open("images/"+file_name, 'wb') as out_file:
                    shutil.copyfileobj(response, out_file)
                break
        line = infile.readline()

# Create and start new threads
threads = []
for i in range(0,10):
    thread = myThread(i,"Thread-%d"%(i),inf)
    thread.start()
    threads.append(thread)

# Wait for all threads to complete
for t in threads:
    t.join()
inf.close()
end = time.time()
print("time elapsed: %.2f seconds"%(end-start))
print ("Exiting Main Thread")











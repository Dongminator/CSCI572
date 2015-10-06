import threading
import urllib.request
import shutil
import time
import os
import hashlib

start = time.time()
BLOCKSIZE = 65536
mydict = dict()
prob_url = 0
repeat = 0
suffix = ["jpg","JPG","png","PNG","bmp","BMP","gif","GIF","jpeg","JPEG"]
sufcount = [0,0,0,0,0,0,0,0,0,0]
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
    tempfilename = "images/"+prefix+"temp"
    global prob_url
    global repeat
    line = infile.readline()
    while len(line)>0:
        # read and check url
        url = line
        try:
            response = urllib.request.urlopen(url)
        except urllib.error.HTTPError as e:
            print(threadName+": problem with url: "+ url + str(e.code))
            threadLock.acquire()
            prob_url += 1
            threadLock.release()
            line = infile.readline()
            continue
        except urllib.error.URLError as e:
            print(threadName+": problem with url: "+ url + str(e.reason))
            threadLock.acquire()
            prob_url += 1
            threadLock.release()
            line = infile.readline()
            continue
        with open(tempfilename,'wb') as outf:
            shutil.copyfileobj(response, outf)
        outf.close()
        # calculate SHA-1 hash value
        hasher = hashlib.sha1()
        with open(tempfilename, 'rb') as tempfile:
            buf = tempfile.read(BLOCKSIZE)
            while len(buf) > 0:
                hasher.update(buf)
                buf = tempfile.read(BLOCKSIZE)
        tempfile.close()
        hash_value = hasher.hexdigest()
        threadLock.acquire()
        idx = mydict.get(hash_value,-1)
        if not idx == -1:
            print(threadName+": repeated image url: "+ url)
            repeat += 1
            threadLock.release()
            line = infile.readline()
            continue
        mydict[hash_value] = 1
        threadLock.release()
        # rename the image file if not repeating
        token = line.split('.')
        for idx,suf in enumerate(suffix):
            if suf+"\n" == token[-1]:
                threadLock.acquire()
                sufcount[idx] += 1
                file_name = str(sufcount[idx])+"."+suf
                threadLock.release()
                os.rename(tempfilename,"images/"+file_name)
                break
        line = infile.readline()
    if os.path.exists(tempfilename): os.remove(tempfilename)
        

threadLock = threading.Lock()
threads = []

# Create and start new threads
for i in range(0,10):
    thread = myThread(i,"Thread-%d"%(i),inf)
    thread.start()
    threads.append(thread)

# Wait for all threads to complete
for t in threads:
    t.join()
inf.close()
for idx,suf in enumerate(suffix):
    print(suf+": "+str(sufcount[idx]))
print("problematic urls: "+str(prob_url))
print("repeated images: "+str(repeat))
end = time.time()
print("time elapsed: %.2f seconds"%(end-start))
print ("Exiting Main Thread")











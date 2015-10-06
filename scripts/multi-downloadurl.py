import threading
import urllib.request
import shutil
import time
import os

start = time.time()
prob_url = 0
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
        download_url(self.name, self.infile)
        print ("Exiting " + self.name)

def download_url(threadName, infile):
    global prob_url
    line = infile.readline()
    while len(line)>0:
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
        token = line.split('.')
        for idx,suf in enumerate(suffix):
            if suf+"\n" == token[-1]:
                threadLock.acquire()
                sufcount[idx] += 1
                file_name = str(sufcount[idx])+"."+suf
                threadLock.release()
                with open("images/"+file_name, 'wb') as out_file:
                    shutil.copyfileobj(response, out_file)
                break
        line = infile.readline()
        

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
end = time.time()
print("time elapsed: %.2f seconds"%(end-start))
print ("Exiting Main Thread")











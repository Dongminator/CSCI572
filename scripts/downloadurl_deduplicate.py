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
for line in inf:
    # read and check url
    url = line
    try:
        response = urllib.request.urlopen(url)
    except urllib.error.HTTPError as e:
        print("problem with url: "+ url + str(e.code))
        prob_url += 1
        continue
    except urllib.error.URLError as e:
        print("problem with url: "+ url + str(e.reason))
        prob_url += 1
        continue
    with open("images/temp",'wb') as outf:
        shutil.copyfileobj(response, outf)
    outf.close()
    # calculate SHA-1 hash value
    hasher = hashlib.sha1()
    with open("images/temp", 'rb') as tempfile:
        buf = tempfile.read(BLOCKSIZE)
        while len(buf) > 0:
            hasher.update(buf)
            buf = tempfile.read(BLOCKSIZE)
    tempfile.close()
    hash_value = hasher.hexdigest()
    idx = mydict.get(hash_value,-1)
    if not idx == -1:
        print("repeated image url: "+ url)
        repeat += 1
        continue
    mydict[hash_value] = 1
    # rename the image file if not repeating
    token = line.split('.')
    for idx,suf in enumerate(suffix):
        if suf+"\n" == token[-1]:
            sufcount[idx] += 1
            file_name = str(sufcount[idx])+"."+suf
            os.rename("images/temp","images/"+file_name)
            break
inf.close()
if os.path.exists("images/temp"): os.remove("images/temp")
for idx,suf in enumerate(suffix):
    print(suf+": "+str(sufcount[idx]))
print("problematic urls: "+str(prob_url))
print("repeated images: "+str(repeat))
end = time.time()
print("time elapsed: %.2f seconds"%(end-start))
